package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class Vocabulary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val language: String, // "Korean", "Japanese", "Chinese", "Thai"
    val word: String, // Char, e.g. 안녕하세요 / こんにちは
    val pronunciation: String, // Annyeonghaseyo / Konnichiwa
    val translationEng: String, // Hello
    val translationHin: String, // नमस्ते
    val category: String, // "Greetings", "Numbers", "Food", "Colors", "Feelings"
    val difficulty: String = "Basic"
)
