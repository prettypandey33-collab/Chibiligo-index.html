package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Vocabulary::class, UserStats::class], version = 1, exportSchema = false)
abstract class ChibiDatabase : RoomDatabase() {
    abstract fun chibiDao(): ChibiDao

    companion object {
        @Volatile
        private var INSTANCE: ChibiDatabase? = null

        fun getDatabase(context: Context): ChibiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChibiDatabase::class.java,
                    "chibi_lingo_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
