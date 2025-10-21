package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesListUserInterfaceState(
    val notes: List<Note> = emptyList(),
    val selectedNoteIDs: Set<Long> = emptySet(),
    val isCurrentlyInSelectionMode: Boolean = false,
)

class NotesListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NotesRepository
    private val _selectedNoteIDs = MutableStateFlow<Set<Long>>(emptySet())

    val currentUserInterfaceState: StateFlow<NotesListUserInterfaceState>

    init {
        val NotesDAO = SuperApplicationDatabase.getDatabase(application).NoteDAO()
        repository = NotesRepository(NotesDAO)
        currentUserInterfaceState = combine(
            flow = repository.AllNotes,
            flow2 = _selectedNoteIDs
        ) {
            notes, selectedIDs ->
            NotesListUserInterfaceState(
                notes = notes,
                selectedNoteIDs = selectedIDs,
                isCurrentlyInSelectionMode = selectedIDs.isNotEmpty(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesListUserInterfaceState(),
        )
    }

    fun toggleNoteSelection(noteID: Long) {
        val currentSelection = _selectedNoteIDs.value.toMutableSet()
        if (currentSelection.contains(noteID)) {
            currentSelection.remove(noteID)
        } else {
            currentSelection.add(noteID)
        }
        _selectedNoteIDs.value = currentSelection
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            repository.deleteNotesByIDs(_selectedNoteIDs.value.toList())
            clearSelection()
        }
    }

    fun clearSelection() {
        _selectedNoteIDs.value = emptySet()
    }
}