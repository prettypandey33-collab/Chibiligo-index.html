package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class ChibiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChibiRepository

    init {
        val database = ChibiDatabase.getDatabase(application)
        repository = ChibiRepository(database.chibiDao())

        // Ensure database is populated with initial vocabulary and default stats
        viewModelScope.launch {
            repository.ensurePopulated()
        }
    }

    // Navigation and Tab States
    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: Quiz, 2: Achievements, 3: Admin
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(index: Int) {
        _currentTab.value = index
        if (index != 1) {
            // Reset active quiz sessions if we navigate away
            _quizSession.value = null
        }
    }

    // Language list filter selection
    private val _selectedLanguage = MutableStateFlow<String?>("Korean")
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    fun selectLanguage(lang: String?) {
        _selectedLanguage.value = lang
    }

    // Database Flows
    val allVocabulary: StateFlow<List<Vocabulary>> = repository.allVocabulary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Mascot interactive quotes (Hindi + English)
    private val mascotKoreanQuotes = listOf(
        "Anyoung! Learn Korean with me! It's so fun! 🌸 - आन्योंग! मेरे साथ कोरियाई सीखें!",
        "Cheer up! You are doing amazing! 💖 - हिम्मत मत हारो! आप कमाल कर रहे हैं!",
        "Did you know? Hangeul was created in 1443! 📜 - क्या आप जानते हैं? हंगुल 1443 में बना था!",
        "Let's practice the vocabulary today! Fighting! ✊ - चलिए आज शब्दावली का अभ्यास करते हैं! फाइटिंग!"
    )
    private val mascotJapaneseQuotes = listOf(
        "Konnichiwa! Let's learn Kanji and hiragana! 🎋 - कोन्निचिवा! चलिए कांजी और हीरागाना सीखते हैं!",
        "Ganbare! Don't give up! 👑 - गनबारे! हार मत मानो!",
        "Japanese grammar is so neat! Let's conquer it! 🇯🇵 - जापानी व्याकरण बहुत साफ है! इसे जीतते हैं!",
        "You earned some XP! Chibi is proud! 🐰 - आपने कुछ XP अर्जित किया! चिबी को गर्व है!"
    )
    private val mascotChineseQuotes = listOf(
        "Nǐ hǎo! Chinese tones are musical! 🎵 - नि हाओ! चीनी टोन संगीत की तरह हैं!",
        "Jiāyóu! Keep going! 🐼 - जियायौ! बढ़ते रहो!",
        "Chinese letters are artistic drawings! 🎨 - चीनी अक्षर कलात्मक चित्र हैं!",
        "Repeat after me: Wǒ ài nǐ! 💖 - मेरे बाद दोहराएं: वो आई नि!"
    )
    private val mascotThaiQuotes = listOf(
        "Sawatdee! Thai is the land of smiles! 🇹🇭 - सवात्दी! थाई मुस्कान का देश है!",
        "Su su! You can do it! 🐘 - सु सु! आप यह कर सकते हैं!",
        "Thai letters look like beautiful curls! 🌀 - थाई अक्षर सुंदर कर्ल की तरह दिखते हैं!",
        "Did you learn 'Khob khun' today? 🙌 - क्या आपने आज 'खोप खुन' सीखा?"
    )

    private val _currentMascotQuote = MutableStateFlow("Anyoung! Select a language below and let's learn! 🌸 - नमस्ते! नीचे एक भाषा चुनें और सीखना शुरू करें!")
    val currentMascotQuote: StateFlow<String> = _currentMascotQuote.asStateFlow()

    fun triggerNewMascotQuote(language: String) {
        val quotes = when (language) {
            "Korean" -> mascotKoreanQuotes
            "Japanese" -> mascotJapaneseQuotes
            "Chinese" -> mascotChineseQuotes
            "Thai" -> mascotThaiQuotes
            else -> listOf("Keep learning every day to keep your streak! 🔥 - अपनी लय बनाए रखने के लिए हर दिन सीखते रहें!")
        }
        val randomIndex = Random.nextInt(quotes.size)
        _currentMascotQuote.value = quotes[randomIndex]
    }

    // Active Quiz Session State
    data class QuizQuestion(
        val vocabulary: Vocabulary,
        val options: List<String>,
        val correctAnswer: String,
        val originalScript: String,
        val phonetic: String
    )

    data class QuizSession(
        val language: String,
        val questions: List<QuizQuestion>,
        val currentIndex: Int,
        val selectedAnswer: String?,
        val isAnswerChecked: Boolean,
        val isCorrect: Boolean?,
        val score: Int,
        val totalQuestions: Int,
        val quizCompleted: Boolean,
        val earnedXp: Int
    )

    private val _quizSession = MutableStateFlow<QuizSession?>(null)
    val quizSession: StateFlow<QuizSession?> = _quizSession.asStateFlow()

    fun startQuizSession(language: String) {
        viewModelScope.launch {
            val words = allVocabulary.value.filter { it.language == language }
            if (words.size < 2) {
                // Not enough words to form a quiz
                return@launch
            }

            val numQuestions = words.size.coerceAtMost(5)
            val quizWords = words.shuffled().take(numQuestions)

            // Generate questions
            val questions = quizWords.map { targetWord ->
                val stats = userStats.value
                val isHindiPreferred = stats?.preferredLanguage == "Hindi"

                val correctAnswer = if (isHindiPreferred) {
                    targetWord.translationHin
                } else if (stats?.preferredLanguage == "Both") {
                    "${targetWord.translationEng} / ${targetWord.translationHin}"
                } else {
                    targetWord.translationEng
                }

                // Gather choices
                val wrongWords = words.filter { it.id != targetWord.id }
                val wrongChoices = wrongWords.shuffled().take(3).map {
                    if (isHindiPreferred) {
                        it.translationHin
                    } else if (stats?.preferredLanguage == "Both") {
                        "${it.translationEng} / ${it.translationHin}"
                    } else {
                        it.translationEng
                    }
                }.toMutableList()

                val options = (wrongChoices + correctAnswer).distinct().shuffled()

                QuizQuestion(
                    vocabulary = targetWord,
                    options = options,
                    correctAnswer = correctAnswer,
                    originalScript = targetWord.word,
                    phonetic = targetWord.pronunciation
                )
            }

            _quizSession.value = QuizSession(
                language = language,
                questions = questions,
                currentIndex = 0,
                selectedAnswer = null,
                isAnswerChecked = false,
                isCorrect = null,
                score = 0,
                totalQuestions = questions.size,
                quizCompleted = false,
                earnedXp = 0
            )
            // Navigate to Quiz Tab
            _currentTab.value = 1
        }
    }

    fun selectQuizAnswer(answer: String) {
        val session = _quizSession.value ?: return
        if (session.isAnswerChecked) return
        _quizSession.value = session.copy(selectedAnswer = answer)
    }

    fun checkQuizAnswer() {
        val session = _quizSession.value ?: return
        val currentQuestion = session.questions[session.currentIndex]
        val selected = session.selectedAnswer ?: return

        val isCorrect = (selected == currentQuestion.correctAnswer)
        val newScore = if (isCorrect) session.score + 1 else session.score

        _quizSession.value = session.copy(
            isAnswerChecked = true,
            isCorrect = isCorrect,
            score = newScore
        )

        // Award dynamic XP per correct answer!
        viewModelScope.launch {
            if (isCorrect) {
                repository.addXp(15, session.language)
            }
        }
    }

    fun nextQuizQuestion() {
        val session = _quizSession.value ?: return
        val nextIndex = session.currentIndex + 1

        if (nextIndex >= session.totalQuestions) {
            // Quiz completed!
            val totalWonXp = session.score * 15
            _quizSession.value = session.copy(
                quizCompleted = true,
                earnedXp = totalWonXp
            )
        } else {
            _quizSession.value = session.copy(
                currentIndex = nextIndex,
                selectedAnswer = null,
                isAnswerChecked = false,
                isCorrect = null
            )
        }
    }

    // Streak and Admin Operations
    fun incrementStreakSimulated() {
        viewModelScope.launch {
            repository.advanceStreakSimulated()
        }
    }

    fun changePreferredLanguage(pref: String) {
        viewModelScope.launch {
            repository.updatePreferredLanguage(pref)
        }
    }

    fun addNewVocabularyWord(language: String, word: String, pronunciation: String, eng: String, hin: String, category: String) {
        viewModelScope.launch {
            val newVocab = Vocabulary(
                language = language,
                word = word,
                pronunciation = pronunciation,
                translationEng = eng,
                translationHin = hin,
                category = category
            )
            repository.addCustomWord(newVocab)
        }
    }

    fun deleteVocabularyWord(id: Int) {
        viewModelScope.launch {
            repository.deleteWord(id)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetSimulatedStats()
        }
    }
}
