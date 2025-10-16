package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.app.Application
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
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
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderLine: Boolean = false,
)

class NotesEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NotesRepository

    private val _userInterfaceState = MutableStateFlow(NotesEditorUserInterfaceState())
    val userInterfaceState = _userInterfaceState.asStateFlow()

    private var currentNoteID: Long? = null

    init {
        val NotesDAO = SuperApplicationDatabase.getDatabase(application).NoteDAO()
        repository = NotesRepository(NotesDAO)
    }

    fun loadNoteByID(NoteID: Long) {
        if (NoteID == currentNoteID) return
        viewModelScope.launch {
            val existing = if (NoteID <= 0) repository.getNoteByID(NoteID) else repository.getNoteByID(NoteID)
            if (existing != null && NoteID != -1L) {
                currentNoteID = NoteID
                val richTextData = try {
                    Json.decodeFromString<RichTextData>(existing.content)
                } catch (exception: Exception) {
                    RichTextData(existing.content, emptyList())
                }
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
            } else if (existing != null && NoteID == -1L) {
                currentNoteID = existing.id
                val richTextData = try {
                    Json.decodeFromString<RichTextData>(existing.content)
                } catch (exception: Exception) {
                    RichTextData(existing.content, emptyList())
                }
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
            } else if (NoteID > 0) {
                currentNoteID = null
                _userInterfaceState.value = NotesEditorUserInterfaceState(isThisNoteANewNote = true)
            } else {
                currentNoteID = null
                _userInterfaceState.value = NotesEditorUserInterfaceState(isThisNoteANewNote = true)
            }
        }
    }

    fun updateNoteTitle(newTitle: String) {
        _userInterfaceState.update { it.copy(noteTitle = newTitle) }
    }

    fun updateContent(newContent: TextFieldValue) {
        val selection = newContent.selection
        if (selection.collapsed) {
            val styles = newContent.annotatedString.spanStyles
                .filter { it.start <= selection.start && it.end >= selection.end }
            _userInterfaceState.update {
                it.copy(
                    noteContent = newContent,
                    isBold = styles.any { style -> style.item.fontWeight == FontWeight.Bold },
                    isItalic = styles.any { style -> style.item.fontStyle == FontStyle.Italic },
                    isUnderLine = styles.any { style -> style.item.textDecoration == TextDecoration.Underline }
                )
            }
        } else {
            _userInterfaceState.update {
                it.copy(noteContent = newContent)
            }
        }
    }

    private fun applyStyle(
        isStyleApplied: Boolean,
        styleToAdd: SpanStyle,
        styleToRemove: SpanStyle,
    ) {
        val content = _userInterfaceState.value.noteContent
        val selection = content.selection
        if (selection.collapsed) return

        val builder = AnnotatedString.Builder(content.annotatedString)

        if (isStyleApplied) {
            builder.addStyle(styleToRemove, selection.start, selection.end)
        } else {
            builder.addStyle(styleToAdd, selection.start, selection.end)
        }
        updateContent(content.copy(annotatedString = builder.toAnnotatedString()))
    }

    fun toggleBold() {
        val isBold = _userInterfaceState.value.isBold
        applyStyle(
            isStyleApplied = isBold,
            styleToAdd = SpanStyle(fontWeight = FontWeight.Bold),
            styleToRemove = SpanStyle(fontWeight = FontWeight.Normal)
        )
    }

    fun toggleItalic() {
        val isItalic = _userInterfaceState.value.isItalic
        applyStyle(
            isStyleApplied = isItalic,
            styleToAdd = SpanStyle(fontStyle = FontStyle.Italic),
            styleToRemove = SpanStyle(fontStyle = FontStyle.Normal),
        )
    }

    fun toggleUnderline() {
        val isUnderLine = _userInterfaceState.value.isUnderLine
        applyStyle(
            isStyleApplied = isUnderLine,
            styleToAdd = SpanStyle(textDecoration = TextDecoration.Underline),
            styleToRemove = SpanStyle(textDecoration = TextDecoration.None)
        )
    }

    fun saveNote() {
        viewModelScope.launch {
            saveNoteANDWait()
        }
    }

    suspend fun saveNoteANDWait() {
        val state = _userInterfaceState.value
        val contentToSave = state.noteContent.annotatedString
        if (contentToSave.text.isBlank() && state.noteTitle.isBlank()) return

        val contentJSON = Json.encodeToString(contentToSave.toRichTextData())
        val now = System.currentTimeMillis()

        val idToSave = currentNoteID?.takeIf { it > 0 } ?: 0L
        val createdAt = if (idToSave == 0L) {
            now
        } else {
            repository.getNoteByID(currentNoteID!!)?.createdAt ?: now
        }

        val note = Note(
            id = idToSave,
            title = state.noteTitle.ifBlank { "Currently Untitled Note" },
            content = contentJSON,
            createdAt = createdAt,
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