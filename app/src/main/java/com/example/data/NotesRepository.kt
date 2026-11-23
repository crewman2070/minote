package com.example.data

import kotlinx.coroutines.flow.Flow

class NotesRepository(private val dao: ScriptNoteDao) {

    val allNotes: Flow<List<ScriptNoteEntity>> = dao.observeAllNotes()

    suspend fun getById(id: Long): ScriptNoteEntity? = dao.getNoteById(id)

    suspend fun createEmptyNote(kind: NoteKind = NoteKind.NOTE): ScriptNoteEntity {
        val now = System.currentTimeMillis()
        val entity = ScriptNoteEntity(
            title = "",
            content = "",
            kind = kind.name,
            pinned = false,
            wordCount = 0,
            createdAt = now,
            updatedAt = now
        )
        val generatedId = dao.insertNote(entity)
        return entity.copy(id = generatedId)
    }

    suspend fun saveNote(
        id: Long,
        title: String,
        content: String,
        kind: NoteKind,
        pinned: Boolean,
        createdAt: Long
    ): ScriptNoteEntity {
        val now = System.currentTimeMillis()
        val updated = ScriptNoteEntity(
            id = id,
            title = title,
            content = content,
            kind = kind.name,
            pinned = pinned,
            wordCount = ScriptCueHelper.countWords(title, content),
            createdAt = createdAt,
            updatedAt = now
        )
        dao.updateNote(updated)
        return updated
    }

    suspend fun togglePin(note: ScriptNoteEntity) {
        dao.updatePinned(
            id = note.id,
            pinned = !note.pinned,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun duplicateNote(note: ScriptNoteEntity): ScriptNoteEntity {
        val now = System.currentTimeMillis()
        val copyTitle = if (note.title.isBlank()) {
            "Untitled Copy"
        } else {
            "${note.title.trim()} (Copy)"
        }
        val copy = ScriptNoteEntity(
            title = copyTitle,
            content = note.content,
            kind = note.kind,
            pinned = false,
            wordCount = ScriptCueHelper.countWords(copyTitle, note.content),
            createdAt = now,
            updatedAt = now
        )
        val newId = dao.insertNote(copy)
        return copy.copy(id = newId)
    }

    suspend fun deleteNoteById(id: Long) {
        dao.deleteNoteById(id)
    }
}
