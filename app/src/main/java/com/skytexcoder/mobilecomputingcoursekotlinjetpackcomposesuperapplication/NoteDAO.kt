package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDAO {
    @Upsert
    suspend fun upsertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id IN (:NoteIDs)")
    suspend fun deleteNotesByIDs(NoteIDs: List<Long>)

    @Query("SELECT * FROM notes where id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
}