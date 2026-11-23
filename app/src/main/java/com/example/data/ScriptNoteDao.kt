package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptNoteDao {
    @Query("SELECT * FROM script_notes ORDER BY pinned DESC, updatedAt DESC")
    fun observeAllNotes(): Flow<List<ScriptNoteEntity>>

    @Query("SELECT * FROM script_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): ScriptNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ScriptNoteEntity): Long

    @Update
    suspend fun updateNote(note: ScriptNoteEntity)

    @Query("UPDATE script_notes SET pinned = :pinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinned(id: Long, pinned: Boolean, updatedAt: Long)

    @Query("DELETE FROM script_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
