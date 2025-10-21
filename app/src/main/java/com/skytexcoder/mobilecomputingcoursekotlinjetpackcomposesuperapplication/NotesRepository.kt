package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import kotlinx.coroutines.flow.Flow

class NotesRepository(private val NoteDAO: NoteDAO) {
    val AllNotes: Flow<List<Note>> = NoteDAO.getAllNotes()

    suspend fun getNoteByID(id: Long): Note? {
        return NoteDAO.getNoteById(id)
    }

    suspend fun upsertNote(note: Note) {
        NoteDAO.upsertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        NoteDAO.deleteNote(note)
    }

    suspend fun deleteNotesByIDs(NoteIDs: List<Long>) {
        NoteDAO.deleteNotesByIDs(NoteIDs)
    }
}