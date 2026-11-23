package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteKind {
    NOTE,
    SCRIPT;

    companion object {
        fun fromStored(value: String): NoteKind =
            entries.firstOrNull { it.name == value } ?: NOTE
    }
}

@Entity(tableName = "script_notes")
data class ScriptNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val kind: String = NoteKind.NOTE.name,
    val pinned: Boolean = false,
    val wordCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val noteKind: NoteKind
        get() = NoteKind.fromStored(kind)
}
