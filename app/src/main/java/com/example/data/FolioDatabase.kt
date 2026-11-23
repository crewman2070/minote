package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScriptNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FolioDatabase : RoomDatabase() {
    abstract fun scriptNoteDao(): ScriptNoteDao

    companion object {
        @Volatile
        private var INSTANCE: FolioDatabase? = null

        fun getInstance(context: Context): FolioDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FolioDatabase::class.java,
                    "folio_script_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
