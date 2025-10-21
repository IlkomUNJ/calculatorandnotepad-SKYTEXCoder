package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun getFormattedCreationDateOfNote(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(createdAt))
    }

    fun getFormattedModifiedDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(updatedAt))
    }
}
