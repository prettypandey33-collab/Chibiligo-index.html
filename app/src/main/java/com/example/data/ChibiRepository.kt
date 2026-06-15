package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChibiRepository(private val chibiDao: ChibiDao) {

    val allVocabulary: Flow<List<Vocabulary>> = chibiDao.getAllVocabulary()
    val userStats: Flow<UserStats?> = chibiDao.getUserStatsFlow()

    fun getVocabularyByLanguage(lang: String): Flow<List<Vocabulary>> {
        return chibiDao.getVocabularyByLanguage(lang)
    }

    suspend fun ensurePopulated() {
        val count = chibiDao.getVocabularyCount()
        if (count == 0) {
            val defaultList = listOf(
                // ---- KOREAN 🇰🇷 ----
                Vocabulary(language = "Korean", word = "안녕하세요", pronunciation = "Annyeonghaseyo", translationEng = "Hello", translationHin = "नमस्ते", category = "Greetings"),
                Vocabulary(language = "Korean", word = "감사합니다", pronunciation = "Gamsahabnida", translationEng = "Thank you", translationHin = "धन्यवाद", category = "Greetings"),
                Vocabulary(language = "Korean", word = "네", pronunciation = "Ne", translationEng = "Yes", translationHin = "हाँ", category = "Common"),
                Vocabulary(language = "Korean", word = "아니요", pronunciation = "Aniyo", translationEng = "No", translationHin = "नहीं", category = "Common"),
                Vocabulary(language = "Korean", word = "사랑해요", pronunciation = "Saranghaeyo", translationEng = "I love you", translationHin = "मैं तुमसे प्यार करता हूँ", category = "Feelings"),
                Vocabulary(language = "Korean", word = "물", pronunciation = "Mul", translationEng = "Water", translationHin = "पानी", category = "Food & Drink"),
                Vocabulary(language = "Korean", word = "밥", pronunciation = "Bap", translationEng = "Rice / Food", translationHin = "चावल / भोजन", category = "Food & Drink"),
                Vocabulary(language = "Korean", word = "친구", pronunciation = "Chingu", translationEng = "Friend", translationHin = "दोस्त", category = "Relationships"),

                // ---- JAPANESE 🇯🇵 ----
                Vocabulary(language = "Japanese", word = "こんにちは", pronunciation = "Konnichiwa", translationEng = "Hello", translationHin = "नमस्ते", category = "Greetings"),
                Vocabulary(language = "Japanese", word = "ありがとう", pronunciation = "Arigatou", translationEng = "Thank you", translationHin = "धन्यवाद", category = "Greetings"),
                Vocabulary(language = "Japanese", word = "はい", pronunciation = "Hai", translationEng = "Yes", translationHin = "हाँ", category = "Common"),
                Vocabulary(language = "Japanese", word = "いいえ", pronunciation = "Iie", translationEng = "No", translationHin = "नहीं", category = "Common"),
                Vocabulary(language = "Japanese", word = "愛してる", pronunciation = "Aishiteru", translationEng = "I love you", translationHin = "मैं तुमसे प्यार करता हूँ", category = "Feelings"),
                Vocabulary(language = "Japanese", word = "水", pronunciation = "Mizu", translationEng = "Water", translationHin = "पानी", category = "Food & Drink"),
                Vocabulary(language = "Japanese", word = "寿司", pronunciation = "Sushi", translationEng = "Sushi", translationHin = "सुशी", category = "Food & Drink"),
                Vocabulary(language = "Japanese", word = "友達", pronunciation = "Tomodachi", translationEng = "Friend", translationHin = "दोस्त", category = "Relationships"),

                // ---- CHINESE 🇨🇳 ----
                Vocabulary(language = "Chinese", word = "你好", pronunciation = "Nǐ hǎo", translationEng = "Hello", translationHin = "नमस्ते", category = "Greetings"),
                Vocabulary(language = "Chinese", word = "谢谢", pronunciation = "Xièxiè", translationEng = "Thank you", translationHin = "धन्यवाद", category = "Greetings"),
                Vocabulary(language = "Chinese", word = "是", pronunciation = "Shì", translationEng = "Yes", translationHin = "हाँ", category = "Common"),
                Vocabulary(language = "Chinese", word = "不", pronunciation = "Bù", translationEng = "No", translationHin = "नहीं", category = "Common"),
                Vocabulary(language = "Chinese", word = "我爱你", pronunciation = "Wǒ ài nǐ", translationEng = "I love you", translationHin = "मैं तुमसे प्यार करता हूँ", category = "Feelings"),
                Vocabulary(language = "Chinese", word = "水", pronunciation = "Shuǐ", translationEng = "Water", translationHin = "पानी", category = "Food & Drink"),
                Vocabulary(language = "Chinese", word = "米饭", pronunciation = "Mǐfàn", translationEng = "Rice", translationHin = "चावल", category = "Food & Drink"),
                Vocabulary(language = "Chinese", word = "朋友", pronunciation = "Péngyǒu", translationEng = "Friend", translationHin = "दोस्त", category = "Relationships"),

                // ---- THAI 🇹🇭 ----
                Vocabulary(language = "Thai", word = "สวัสดี", pronunciation = "Sawatdee", translationEng = "Hello", translationHin = "नमस्ते", category = "Greetings"),
                Vocabulary(language = "Thai", word = "ขอบคุณ", pronunciation = "Khob khun", translationEng = "Thank you", translationHin = "धन्यवाद", category = "Greetings"),
                Vocabulary(language = "Thai", word = "ใช่", pronunciation = "Chai", translationEng = "Yes", translationHin = "हाँ", category = "Common"),
                Vocabulary(language = "Thai", word = "ไม่", pronunciation = "Mai", translationEng = "No", translationHin = "नहीं", category = "Common"),
                Vocabulary(language = "Thai", word = "ฉันรักคุณ", pronunciation = "Chan rak khun", translationEng = "I love you", translationHin = "मैं तुमसे प्यार करता हूँ", category = "Feelings"),
                Vocabulary(language = "Thai", word = "น้ำ", pronunciation = "Nam", translationEng = "Water", translationHin = "पानी", category = "Food & Drink"),
                Vocabulary(language = "Thai", word = "ข้าว", pronunciation = "Khao", translationEng = "Rice", translationHin = "चावल", category = "Food & Drink"),
                Vocabulary(language = "Thai", word = "เพื่อน", pronunciation = "Phuean", translationEng = "Friend", translationHin = "दोस्त", category = "Relationships")
            )
            chibiDao.insertVocabulary(defaultList)
        }

        var currentStats = chibiDao.getUserStatsDirect()
        if (currentStats == null) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val initialStats = UserStats(
                id = 1,
                name = "Chibi Learner",
                totalXp = 50, // Give them 50 starter XP!
                currentLevel = 1,
                dailyStreak = 1,
                lastActiveDateStr = todayStr,
                unlockedBadgesListStr = "🎓 Starter"
            )
            chibiDao.insertOrUpdateUserStats(initialStats)
        }
    }

    suspend fun addXp(amount: Int, contributingLang: String) {
        val stats = chibiDao.getUserStatsDirect() ?: return
        val newXp = stats.totalXp + amount
        // Level up formula: each level takes Level * 100 XP
        // e.g., Level 1 takes 100 XP to get to Level 2 (total 100)
        // Level 2 takes 200 XP to get to Level 3 (total 300)
        // Let's use simple XP rules: Level = (newXp / 100) + 1
        val newLevel = (newXp / 100) + 1
        val levelGained = newLevel > stats.currentLevel

        // Update contributing language's progress
        var kProg = stats.kProgress
        var jProg = stats.jProgress
        var cProg = stats.cProgress
        var tProg = stats.tProgress

        when (contributingLang) {
            "Korean" -> kProg = (kProg + 12).coerceAtMost(100)
            "Japanese" -> jProg = (jProg + 12).coerceAtMost(100)
            "Chinese" -> cProg = (cProg + 12).coerceAtMost(100)
            "Thai" -> tProg = (tProg + 12).coerceAtMost(100)
        }

        // Check if any badges should be awarded
        val badgeSet = stats.unlockedBadgesListStr.split(", ").map { it.trim() }.toMutableSet()
        if (badgeSet.isEmpty() || stats.unlockedBadgesListStr.isEmpty()) {
            badgeSet.add("🎓 Starter")
        }

        if (newXp >= 150 && !badgeSet.contains("🏆 Quiz Champion")) {
            badgeSet.add("🏆 Quiz Champion")
        }

        // Check for Polyglot: if they have any progress in 3 languages
        var languagesWithProgress = 0
        if (kProg > 0) languagesWithProgress++
        if (jProg > 0) languagesWithProgress++
        if (cProg > 0) languagesWithProgress++
        if (tProg > 0) languagesWithProgress++

        if (languagesWithProgress >= 3 && !badgeSet.contains("🌍 Polyglot")) {
            badgeSet.add("🌍 Polyglot")
        }

        if (stats.dailyStreak >= 3 && !badgeSet.contains("🔥 Streak Master")) {
            badgeSet.add("🔥 Streak Master")
        }

        val updatedStats = stats.copy(
            totalXp = newXp,
            currentLevel = newLevel,
            kProgress = kProg,
            jProgress = jProg,
            cProgress = cProg,
            tProgress = tProg,
            unlockedBadgesListStr = badgeSet.joinToString(", ")
        )
        chibiDao.insertOrUpdateUserStats(updatedStats)
    }

    suspend fun advanceStreakSimulated() {
        val stats = chibiDao.getUserStatsDirect() ?: return
        val currentStreak = stats.dailyStreak
        val newStreak = currentStreak + 1

        val badgeSet = stats.unlockedBadgesListStr.split(", ").map { it.trim() }.toMutableSet()
        if (newStreak >= 3) {
            badgeSet.add("🔥 Streak Master")
        }

        val updatedStats = stats.copy(
            dailyStreak = newStreak,
            unlockedBadgesListStr = badgeSet.joinToString(", ")
        )
        chibiDao.insertOrUpdateUserStats(updatedStats)
    }

    suspend fun resetSimulatedStats() {
        val stats = chibiDao.getUserStatsDirect() ?: return
        val resetStats = UserStats(
            id = 1,
            name = stats.name,
            totalXp = 50,
            currentLevel = 1,
            dailyStreak = 1,
            lastActiveDateStr = stats.lastActiveDateStr,
            kProgress = 0,
            jProgress = 0,
            cProgress = 0,
            tProgress = 0,
            preferredLanguage = stats.preferredLanguage,
            unlockedBadgesListStr = "🎓 Starter"
        )
        chibiDao.insertOrUpdateUserStats(resetStats)
    }

    suspend fun updatePreferredLanguage(pref: String) {
        val stats = chibiDao.getUserStatsDirect() ?: return
        chibiDao.insertOrUpdateUserStats(stats.copy(preferredLanguage = pref))
    }

    suspend fun addCustomWord(word: Vocabulary) {
        chibiDao.insertSingleWord(word)
    }

    suspend fun deleteWord(id: Int) {
        chibiDao.deleteWordById(id)
    }
}
