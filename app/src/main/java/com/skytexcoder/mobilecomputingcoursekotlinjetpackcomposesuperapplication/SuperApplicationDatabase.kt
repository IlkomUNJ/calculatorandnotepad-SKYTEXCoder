package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class SuperApplicationDatabase : RoomDatabase() {
    abstract fun NoteDAO(): NoteDAO

    companion object {
        @Volatile
        private var INSTANCE: SuperApplicationDatabase? = null

        fun getDatabase(context: Context): SuperApplicationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SuperApplicationDatabase::class.java,
                    "notes_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}