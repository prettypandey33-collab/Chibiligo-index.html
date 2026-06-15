package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val name: String = "Cute chibi learner",
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val dailyStreak: Int = 1,
    val lastActiveDateStr: String = "", // "YYYY-MM-DD" style
    val kProgress: Int = 0, // Korean progress %
    val jProgress: Int = 0, // Japanese progress %
    val cProgress: Int = 0, // Chinese progress %
    val tProgress: Int = 0, // Thai progress %
    val preferredLanguage: String = "Both", // "English", "Hindi", "Both"
    val unlockedBadgesListStr: String = "🎓" // Store badges as emoji strings or keys
)
