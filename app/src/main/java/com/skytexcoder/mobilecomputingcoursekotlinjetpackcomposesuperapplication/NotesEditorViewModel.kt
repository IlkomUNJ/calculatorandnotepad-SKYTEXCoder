package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.app.Application
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.min

data class NotesEditorUserInterfaceState(
    val noteTitle: String = "",
    val noteContent: TextFieldValue = TextFieldValue(AnnotatedString("")),
    val isThisNoteANewNote: Boolean = true,
    val isTextBoldToggleCurrentlyEnabled: Boolean = false,
    val isTextItalicToggleCurrentlyEnabled: Boolean = false,
    val isTextUnderLineToggleCurrentlyEnabled: Boolean = false,
    val currentTextFontSize: Int = 16,
)

private data class TextEditDiff(
    val start: Int,
    val deletedCount: Int,
    val insertedCount: Int,
)

class NotesEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NotesRepository

    private val _userInterfaceState = MutableStateFlow(NotesEditorUserInterfaceState())
    val userInterfaceState = _userInterfaceState.asStateFlow()

    private var currentNoteID: Long? = null
    private var originalTitle: String = ""
    private var originalContent: String = ""
    private var isNewNote: Boolean = true

    companion object {
        const val MINIMUM_TEXT_FONT_SIZE_IN_PIXELS = 8
        const val MAXIMUM_TEXT_FONT_SIZE_IN_PIXELS = 40
        private const val DEFAULT_APPLIED_TEXT_FONT_SIZE_IN_PIXELS = 16
    }

    init {
        val NotesDAO = SuperApplicationDatabase.getDatabase(application).NoteDAO()
        repository = NotesRepository(NotesDAO)
    }

    fun loadNoteByID(NoteID: Long) {
        if (NoteID == currentNoteID) return
        viewModelScope.launch {
            val existing = repository.getNoteByID(NoteID)
            if (existing != null) {
                currentNoteID = NoteID
                val richTextData = try {
                    Json.decodeFromString<RichTextData>(existing.content)
                } catch (exception: Exception) {
                    RichTextData(existing.content, emptyList())
                }
                originalTitle = existing.title
                originalContent = existing.content
                isNewNote = false
                _userInterfaceState.update {
                    it.copy(
                        noteTitle = existing.title,
                        noteContent = TextFieldValue(
                            annotatedString = richTextData.toAnnotatedString(),
                            selection = TextRange(richTextData.text.length)
                        ),
                        isThisNoteANewNote = false
                    )
                }
            } else {
                currentNoteID = null
                originalTitle = ""
                originalContent = ""
                isNewNote = true
                _userInterfaceState.value = NotesEditorUserInterfaceState(isThisNoteANewNote = true)
            }
        }
    }

    fun updateNoteTitle(newTitle: String) {
        _userInterfaceState.update { it.copy(noteTitle = newTitle) }
    }

    fun updateContent(newContent: TextFieldValue) {
        val old = _userInterfaceState.value.noteContent

        if (old.text == newContent.text) {
            _userInterfaceState.update {
                it.copy(
                    noteContent = old.copy(
                        selection = newContent.selection,
                        composition = newContent.composition
                    )
                )
            }
            return
        }

        val diff = computeDiff(old.text, newContent.text)
        val active = currentActiveStyle()
        val shouldApplyActive =
            diff.insertedCount > 0 && (
                    _userInterfaceState.value.isTextBoldToggleCurrentlyEnabled ||
                            _userInterfaceState.value.isTextItalicToggleCurrentlyEnabled ||
                            _userInterfaceState.value.isTextUnderLineToggleCurrentlyEnabled ||
                            _userInterfaceState.value.currentTextFontSize != DEFAULT_APPLIED_TEXT_FONT_SIZE_IN_PIXELS
                    )

        val newAnnotated = rebuildAnnotatedAfterEdit(
            oldAnnotated = old.annotatedString,
            newPlainText = newContent.text,
            diff = diff,
            applyActive = shouldApplyActive,
            activeStyle = active
        )

        _userInterfaceState.update {
            it.copy(
                noteContent = TextFieldValue(
                    annotatedString = newAnnotated,
                    selection = newContent.selection,
                    composition = newContent.composition
                )
            )
        }
    }

    private fun currentActiveStyle(): SpanStyle {
        val state = _userInterfaceState.value
        return SpanStyle(
            fontWeight = if (state.isTextBoldToggleCurrentlyEnabled) FontWeight.Bold else null,
            fontStyle = if (state.isTextItalicToggleCurrentlyEnabled) FontStyle.Italic else null,
            textDecoration = if (state.isTextUnderLineToggleCurrentlyEnabled) TextDecoration.Underline else null,
            fontSize = state.currentTextFontSize.sp
        )
    }

    private fun computeDiff(oldText: String, newText: String): TextEditDiff {
        val prefix = commonPrefix(oldText, newText)
        val oldSuffixStart = oldText.length - commonSuffix(
            oldText.substring(prefix),
            newText.substring(prefix)
        )
        val newSuffixStart = newText.length - commonSuffix(
            oldText.substring(prefix),
            newText.substring(prefix)
        )
        val deleted = oldSuffixStart - prefix
        val inserted = newSuffixStart - prefix
        return TextEditDiff(prefix, deleted, inserted)
    }

    private fun commonPrefix(a: String, b: String): Int {
        val n = min(a.length, b.length)
        var i = 0
        while (i < n && a[i] == b[i]) i++
        return i
    }

    private fun commonSuffix(a: String, b: String): Int {
        val na = a.length
        val nb = b.length
        var i = 0
        while (i < na && i < nb && a[na - 1 - i] == b[nb - 1 - i]) i++
        return i
    }

    private fun rebuildAnnotatedAfterEdit(
        oldAnnotated: AnnotatedString,
        newPlainText: String,
        diff: TextEditDiff,
        applyActive: Boolean,
        activeStyle: SpanStyle
    ): AnnotatedString {
        val start = diff.start
        val oldRemovedEnd = diff.start + diff.deletedCount
        val insertedEnd = diff.start + diff.insertedCount
        val delta = diff.insertedCount - diff.deletedCount

        val builder = AnnotatedString.Builder()
        builder.append(newPlainText)

        for (range in oldAnnotated.spanStyles) {
            val s = range.start
            val e = range.end
            val item = range.item

            when {
                e <= start -> builder.safeAdd(item, s, e)

                s >= oldRemovedEnd -> builder.safeAdd(item, s + delta, e + delta)

                else -> {
                    if (s < start) builder.safeAdd(item, s, start)
                    if (e > oldRemovedEnd) {
                        val rightLen = e - oldRemovedEnd
                        builder.safeAdd(item, insertedEnd, insertedEnd + rightLen)
                    }
                    val typedInsideThisSpan =
                        diff.insertedCount > 0 && s <= start && oldRemovedEnd <= e
                    if (typedInsideThisSpan) {
                        builder.safeAdd(item, start, insertedEnd)
                    }
                }
            }
        }

        if (applyActive && diff.insertedCount > 0) {
            builder.safeAdd(activeStyle, start, insertedEnd)
        }

        return builder.toAnnotatedString()
    }

    private fun AnnotatedString.Builder.safeAdd(style: SpanStyle, start: Int, end: Int) {
        if (start < end && start >= 0 && end <= this.length) {
            addStyle(style, start, end)
        }
    }

    private fun toggleSelectionStyle(
        addStyle: SpanStyle,
        removeStyle: SpanStyle,
        styleCheck: (SpanStyle) -> Boolean
    ) {
        val content = _userInterfaceState.value.noteContent
        val selection = content.selection
        if (selection.collapsed) return

        val builder = AnnotatedString.Builder()
        builder.append(content.text)

        content.annotatedString.spanStyles.forEach { r ->
            builder.addStyle(r.item, r.start, r.end)
        }

        val currentStyles = content.annotatedString.spanStyles.filter {
            it.start < selection.end && it.end > selection.start
        }
        val shouldAdd = currentStyles.none { styleCheck(it.item) }

        if (shouldAdd) {
            builder.addStyle(addStyle, selection.start, selection.end)
        } else {
            builder.addStyle(removeStyle, selection.start, selection.end)
        }

        _userInterfaceState.update {
            it.copy(
                noteContent = content.copy(
                    annotatedString = builder.toAnnotatedString()
                )
            )
        }
    }

    fun toggleBold() {
        val selection = _userInterfaceState.value.noteContent.selection
        if (selection.collapsed) {
            _userInterfaceState.update { it.copy(isTextBoldToggleCurrentlyEnabled = !it.isTextBoldToggleCurrentlyEnabled) }
        } else {
            toggleSelectionStyle(
                addStyle = SpanStyle(fontWeight = FontWeight.Bold),
                removeStyle = SpanStyle(fontWeight = FontWeight.Normal),
                styleCheck = { it.fontWeight == FontWeight.Bold }
            )
        }
    }

    fun toggleItalic() {
        val selection = _userInterfaceState.value.noteContent.selection
        if (selection.collapsed) {
            _userInterfaceState.update { it.copy(isTextItalicToggleCurrentlyEnabled = !it.isTextItalicToggleCurrentlyEnabled) }
        } else {
            toggleSelectionStyle(
                addStyle = SpanStyle(fontStyle = FontStyle.Italic),
                removeStyle = SpanStyle(fontStyle = FontStyle.Normal),
                styleCheck = { it.fontStyle == FontStyle.Italic }
            )
        }
    }

    fun toggleUnderline() {
        val selection = _userInterfaceState.value.noteContent.selection
        if (selection.collapsed) {
            _userInterfaceState.update { it.copy(isTextUnderLineToggleCurrentlyEnabled = !it.isTextUnderLineToggleCurrentlyEnabled) }
        } else {
            toggleSelectionStyle(
                addStyle = SpanStyle(textDecoration = TextDecoration.Underline),
                removeStyle = SpanStyle(textDecoration = TextDecoration.None),
                styleCheck = { it.textDecoration == TextDecoration.Underline }
            )
        }
    }

    fun changeFontSize(delta: Int) {
        val selection = _userInterfaceState.value.noteContent.selection
        val currentSize = _userInterfaceState.value.currentTextFontSize
        val newSize = (currentSize + delta).coerceIn(MINIMUM_TEXT_FONT_SIZE_IN_PIXELS, MAXIMUM_TEXT_FONT_SIZE_IN_PIXELS)

        if (selection.collapsed) {
            _userInterfaceState.update { it.copy(currentTextFontSize = newSize) }
        } else {
            val content = _userInterfaceState.value.noteContent
            val builder = AnnotatedString.Builder()
            builder.append(content.text)
            content.annotatedString.spanStyles.forEach { r ->
                builder.addStyle(r.item, r.start, r.end)
            }
            builder.addStyle(SpanStyle(fontSize = newSize.sp), selection.start, selection.end)
            _userInterfaceState.update {
                it.copy(
                    noteContent = content.copy(
                        annotatedString = builder.toAnnotatedString()
                    ),
                    currentTextFontSize = newSize
                )
            }
        }
    }

    suspend fun saveNoteANDWait() {
        val state = _userInterfaceState.value
        val contentToSave = state.noteContent.annotatedString
        if (contentToSave.text.isBlank() && state.noteTitle.isBlank()) return

        val contentJSON = Json.encodeToString(contentToSave.toRichTextData())
        val now = System.currentTimeMillis()

        if (!isNewNote && originalTitle == state.noteTitle && originalContent == contentJSON) {
            return
        }

        val note = Note(
            id = currentNoteID ?: 0,
            title = state.noteTitle.ifBlank { "Currently Untitled Note" },
            content = contentJSON,
            createdAt = if (isNewNote) now else repository.getNoteByID(currentNoteID!!)?.createdAt ?: now,
            updatedAt = now,
        )

        withContext(Dispatchers.IO) {
            repository.upsertNote(note)
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            currentNoteID?.let { id ->
                repository.getNoteByID(id)?.let {
                    repository.deleteNote(it)
                }
            }
        }
    }
}