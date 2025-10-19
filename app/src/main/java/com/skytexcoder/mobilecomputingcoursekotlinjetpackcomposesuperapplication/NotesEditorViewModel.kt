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

data class NotesEditorUserInterfaceState(
    val noteTitle: String = "",
    val noteContent: TextFieldValue = TextFieldValue(""),
    val isThisNoteANewNote: Boolean = true,
    val isTextBoldToggleCurrentlyEnabled: Boolean = false,
    val isTextItalicToggleCurrentlyEnabled: Boolean = false,
    val isTextUnderLineToggleCurrentlyEnabled: Boolean = false,
    val currentTextFontSize: Int = 16,
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
        const val MIN_FONT_SIZE = 8
        const val MAX_FONT_SIZE = 40
        private const val DEFAULTLY_APPLIED_FONT_SIZE = 16
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
        val oldContent = _userInterfaceState.value.noteContent
        val textChanged = oldContent.text != newContent.text
        val selectionCollapsed = newContent.selection.collapsed
        var nextValue = newContent
        if (textChanged && selectionCollapsed) {
            nextValue = applyActiveTypingTextStyles(oldContent, newContent)
        }
        _userInterfaceState.update {
            it.copy(noteContent = nextValue)
        }
    }

    private fun applyActiveTypingTextStyles(oldContent: TextFieldValue, newContent: TextFieldValue): TextFieldValue {
        val typedTextLength = newContent.text.length - oldContent.text.length
        if (typedTextLength <= 0) return newContent // Not a simple typing action

        val insertPosition = newContent.selection.start - typedTextLength
        val state = _userInterfaceState.value
        val activeStyle = SpanStyle(
            fontWeight = if (state.isTextBoldToggleCurrentlyEnabled) FontWeight.Bold else null,
            fontStyle = if (state.isTextItalicToggleCurrentlyEnabled) FontStyle.Italic else null,
            textDecoration = if (state.isTextUnderLineToggleCurrentlyEnabled) TextDecoration.Underline else null,
            fontSize = state.currentTextFontSize.sp
        )

        val builder = AnnotatedString.Builder(newContent.annotatedString)
        builder.addStyle(activeStyle, insertPosition, insertPosition + typedTextLength)
        return newContent.copy(annotatedString = builder.toAnnotatedString())
    }

    // REFACTOR: Generic function to apply/remove styles to a selection.
    private fun toggleSelectionStyle(
        addStyle: SpanStyle,
        removeStyle: SpanStyle,
        styleCheck: (SpanStyle) -> Boolean
    ) {
        val content = _userInterfaceState.value.noteContent
        val selection = content.selection
        if (selection.collapsed) return

        val builder = AnnotatedString.Builder(content.annotatedString)
        val currentStyles = content.annotatedString.spanStyles.filter {
            it.start < selection.end && it.end > selection.start
        }

        // If the style is present everywhere in the selection, remove it. Otherwise, add it.
        val shouldAdd = currentStyles.none { styleCheck(it.item) }

        if (shouldAdd) {
            builder.addStyle(addStyle, selection.start, selection.end)
        } else {
            builder.addStyle(removeStyle, selection.start, selection.end)
        }
        _userInterfaceState.update {
            it.copy(
                noteContent = content.copy(annotatedString = builder.toAnnotatedString())
            )
        }
    }

    // REFACTOR: Toggles now have two modes: cursor mode (update state) and selection mode (apply style).
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

    // REFACTOR: New function to handle font size changes.
    fun changeFontSize(delta: Int) {
        val selection = _userInterfaceState.value.noteContent.selection
        val currentSize = _userInterfaceState.value.currentTextFontSize
        val newSize = (currentSize + delta).coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)

        if (selection.collapsed) {
            _userInterfaceState.update { it.copy(currentTextFontSize = newSize) }
        } else {
            val builder = AnnotatedString.Builder(_userInterfaceState.value.noteContent.annotatedString)
            builder.addStyle(SpanStyle(fontSize = newSize.sp), selection.start, selection.end)
            _userInterfaceState.update {
                it.copy(
                    noteContent = _userInterfaceState.value.noteContent.copy(annotatedString = builder.toAnnotatedString())
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