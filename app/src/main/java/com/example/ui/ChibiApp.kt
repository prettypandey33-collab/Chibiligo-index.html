package com.example.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserStats
import com.example.data.Vocabulary
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChibiApp(viewModel: ChibiViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val allVocab by viewModel.allVocabulary.collectAsStateWithLifecycle()
    val selectedLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val activeQuiz by viewModel.quizSession.collectAsStateWithLifecycle()
    val mascotQuote by viewModel.currentMascotQuote.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var isDarkTheme by remember { mutableStateOf(false) }

    // TextToSpeech Integration
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.setLanguage(Locale.ENGLISH)
            }
        }
        ttsInstance
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.shutdown()
        }
    }

    fun speakWord(word: String, lang: String) {
        val locale = when (lang) {
            "Korean" -> Locale.KOREAN
            "Japanese" -> Locale.JAPANESE
            "Chinese" -> Locale.CHINESE
            "Thai" -> Locale("th")
            else -> Locale.ENGLISH
        }
        tts?.language = locale
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "ChibiLingoSpeech")
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (selectedLang) {
                                        "Korean" -> "🐰"
                                        "Japanese" -> "🦊"
                                        "Chinese" -> "🐼"
                                        "Thai" -> "🐘"
                                        else -> "🌸"
                                    },
                                    fontSize = 22.sp
                                )
                            }
                            Text(
                                text = "ChibiLingo",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // Quick switch for Translation Mode
                        IconButton(
                            onClick = {
                                val currentPref = userStats?.preferredLanguage ?: "Both"
                                val nextPref = when (currentPref) {
                                    "Both" -> "English"
                                    "English" -> "Hindi"
                                    else -> "Both"
                                }
                                viewModel.changePreferredLanguage(nextPref)
                            },
                            modifier = Modifier.testTag("translation_switch")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Switch language",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Quick switch for Dark Mode
                        IconButton(
                            onClick = { isDarkTheme = !isDarkTheme },
                            modifier = Modifier.testTag("theme_switch")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ColorsGradient().first
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = {
                            if (activeQuiz == null && selectedLang != null) {
                                viewModel.startQuizSession(selectedLang!!)
                            } else {
                                viewModel.selectTab(1)
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.School, contentDescription = "Quiz Study") },
                        label = { Text("Study Quiz", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_quiz")
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Badges") },
                        label = { Text("My Stats", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_achievements")
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Admin") },
                        label = { Text("Editor", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_admin")
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                if (isDarkTheme) Color(0xFF131324) else Color(0xFFFFF1F4)
                            )
                        )
                    )
            ) {
                when (currentTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        vocab = allVocab,
                        userStats = userStats,
                        selectedLang = selectedLang,
                        mascotQuote = mascotQuote,
                        speak = { word, lang -> speakWord(word, lang) }
                    )
                    1 -> StudyQuizScreen(
                        viewModel = viewModel,
                        activeQuiz = activeQuiz,
                        userStats = userStats
                    )
                    2 -> MyAchievementsScreen(
                        viewModel = viewModel,
                        userStats = userStats
                    )
                    3 -> AdminLessonsScreen(
                        viewModel = viewModel,
                        vocab = allVocab
                    )
                }
            }
        }
    }
}

// Return colors list for background gradient header decoration based on theme values
@Composable
fun ColorsGradient(): Pair<Color, Color> {
    return if (isSystemInDarkTheme()) {
        Pair(Color(0xFF2E1B4E), Color(0xFF1B1B3A))
    } else {
        Pair(ChibiLavenderLight, ChibiPinkLight)
    }
}

// --- SCREEN 1: HOME ---
@Composable
fun HomeScreen(
    viewModel: ChibiViewModel,
    vocab: List<Vocabulary>,
    userStats: UserStats?,
    selectedLang: String?,
    mascotQuote: String,
    speak: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Daily Streak Banner!
        item {
            Card(
                onClick = { viewModel.incrementStreakSimulated() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF423517) else Color(0xFFFFF8E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        Brush.horizontalGradient(listOf(ChibiGold, ChibiGoldDark)),
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("streak_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 32.sp,
                        modifier = Modifier.scale(1.1f)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Streak: ${userStats?.dailyStreak ?: 1} Days!",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (isSystemInDarkTheme()) ChibiGold else ChibiTextDark
                        )
                        Text(
                            text = "Tap to extend streak for the simulated next day! ⚡",
                            fontSize = 11.sp,
                            color = if (isSystemInDarkTheme()) Color.White.copy(0.7f) else ChibiTextLight
                        )
                    }
                }
            }
        }

        // Mascot Speech Bubble!
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Speech bubble
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary.copy(0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                        .testTag("mascot_speech")
                ) {
                    Text(
                        text = mascotQuote,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Chibi Mascot representation and interactive action
                val animScale by animateFloatAsState(targetValue = 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clickable {
                            if (selectedLang != null) {
                                viewModel.triggerNewMascotQuote(selectedLang)
                            }
                        }
                        .scale(animScale)
                        .padding(4.dp)
                ) {
                    Text(
                        text = when (selectedLang) {
                            "Korean" -> "🐰"
                            "Japanese" -> "🦊"
                            "Chinese" -> "🐼"
                            "Thai" -> "🐘"
                            else -> "🌸"
                        },
                        fontSize = 44.sp
                    )
                    Column {
                        Text(
                            text = when (selectedLang) {
                                "Korean" -> "Mascot Hani"
                                "Japanese" -> "Mascot Sakura"
                                "Chinese" -> "Mascot Bao"
                                "Thai" -> "Mascot Mali"
                                else -> "Teacher Chibi-chan"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tap me to talk! 🗣️",
                            fontSize = 10.sp,
                            color = ChibiTextLight
                        )
                    }
                }
            }
        }

        // Language Selectors (Grid representing individual progress)
        item {
            Text(
                text = "Choose Your Language",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            val languages = listOf(
                Triple("Korean", "🇰🇷", userStats?.kProgress ?: 0),
                Triple("Japanese", "🇯🇵", userStats?.jProgress ?: 0),
                Triple("Chinese", "🇨🇳", userStats?.cProgress ?: 0),
                Triple("Thai", "🇹🇭", userStats?.tProgress ?: 0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { (lang, flag, progress) ->
                    val isSelected = selectedLang == lang
                    Card(
                        onClick = {
                            viewModel.selectLanguage(lang)
                            viewModel.triggerNewMascotQuote(lang)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("lang_card_$lang")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(text = flag, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lang,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Simple Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = progress.toFloat() / 100f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$progress%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Active Lessons and word definitions list
        if (selectedLang != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖 $selectedLang Lessons",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // Button to launch vocabulary quiz directly!
                    Button(
                        onClick = { viewModel.startQuizSession(selectedLang) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("start_quiz_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            val matchingVocab = vocab.filter { it.language == selectedLang }
            if (matchingVocab.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(text = "📭", fontSize = 48.sp)
                        Text(
                            text = "No words found in database for $selectedLang! Go to 'Editor' tab to add custom vocabulary.",
                            color = ChibiTextLight,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                // Group by category
                val grouped = matchingVocab.groupBy { it.category }
                grouped.forEach { (cat, words) ->
                    item {
                        Text(
                            text = cat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }

                    items(words) { itemVocab ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(0.12f),
                                    RoundedCornerShape(16.dp)
                                )
                                .testTag("vocab_card_${itemVocab.word}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = itemVocab.word,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(0.1f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = itemVocab.pronunciation,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Display translations according to settings
                                    val mode = userStats?.preferredLanguage ?: "Both"
                                    if (mode == "English" || mode == "Both") {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🇬🇧", fontSize = 12.sp)
                                            Text(
                                                text = itemVocab.translationEng,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                    if (mode == "Hindi" || mode == "Both") {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text("🇮🇳", fontSize = 12.sp)
                                            Text(
                                                text = itemVocab.translationHin,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSystemInDarkTheme()) Color(0xFFCEBEE2) else Color(0xFF6B5885)
                                            )
                                        }
                                    }
                                }

                                // Interactive Text To Speech speaker button
                                IconButton(
                                    onClick = { speak(itemVocab.word, selectedLang) },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            MaterialTheme.colorScheme.secondary.copy(0.15f),
                                            CircleShape
                                        )
                                        .testTag("speaker_${itemVocab.word}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Speak pronunciation",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "🐰", fontSize = 54.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Please tap any language card above to start exploring cute words, translations, phonetic pronunciations, and take interactive games!",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChibiTextLight
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: MAIN DYNAMIC STUDY QUIZ ---
@Composable
fun StudyQuizScreen(
    viewModel: ChibiViewModel,
    activeQuiz: ChibiViewModel.QuizSession?,
    userStats: UserStats?
) {
    if (activeQuiz == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎓", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Study Room Closed",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To unlock interactive quizzes, first tap 'Home' below, click one of the Japanese/Korean/Chinese/Thai cards, and press the 'Start Quiz' button! 🎯",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ChibiTextLight
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { viewModel.selectTab(0) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Go to Home", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (activeQuiz.quizCompleted) {
                // Completed congrats screen!
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "👑", fontSize = 84.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Completed: ${activeQuiz.language} Quiz!",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your Score",
                                fontWeight = FontWeight.Bold,
                                color = ChibiTextLight,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${activeQuiz.score} / ${activeQuiz.totalQuestions}",
                                fontWeight = FontWeight.Black,
                                fontSize = 42.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("XP Reward", fontSize = 11.sp, color = ChibiTextLight)
                                    Text("+${activeQuiz.earnedXp} XP", fontWeight = FontWeight.Black, color = ChibiSuccess, fontSize = 18.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Bonus Level", fontSize = 11.sp, color = ChibiTextLight)
                                    Text("Lvl ${userStats?.currentLevel ?: 1}", fontWeight = FontWeight.Black, color = ChibiLavenderDark, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { viewModel.selectTab(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                            .testTag("quiz_finish_back_btn")
                    ) {
                        Text("Finish Study session", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            } else {
                val currentQuestion = activeQuiz.questions[activeQuiz.currentIndex]
                val progress = (activeQuiz.currentIndex.toFloat() / activeQuiz.totalQuestions.toFloat())

                // Question Header (Progress indicator)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activeQuiz.language} Study session",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Question ${activeQuiz.currentIndex + 1}/${activeQuiz.totalQuestions}",
                            fontWeight = FontWeight.Bold,
                            color = ChibiTextLight,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // Chibi Question Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .padding(vertical = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Translate this word:",
                            color = ChibiTextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQuestion.originalScript,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Phonetic: ${currentQuestion.phonetic}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Options list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentQuestion.options.forEach { option ->
                        val isSelected = activeQuiz.selectedAnswer == option
                        val isChecked = activeQuiz.isAnswerChecked
                        val isOptionCorrect = option == currentQuestion.correctAnswer

                        val borderStroke = when {
                            isChecked && isOptionCorrect -> BorderStroke(2.dp, ChibiSuccess)
                            isChecked && isSelected -> BorderStroke(2.dp, ChibiDanger)
                            isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.15f))
                        }

                        val containerColor = when {
                            isChecked && isOptionCorrect -> ChibiSuccess.copy(0.12f)
                            isChecked && isSelected -> ChibiDanger.copy(0.12f)
                            isSelected -> MaterialTheme.colorScheme.primary.copy(0.08f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val textColor = when {
                            isChecked && isOptionCorrect -> ChibiSuccess
                            isChecked && isSelected -> ChibiDanger
                            else -> MaterialTheme.colorScheme.onBackground
                        }

                        Card(
                            onClick = { viewModel.selectQuizAnswer(option) },
                            shape = RoundedCornerShape(16.dp),
                            border = borderStroke,
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("quiz_option_$option")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isChecked && isOptionCorrect) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Correct", tint = ChibiSuccess)
                                } else if (isChecked && isSelected) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Wrong", tint = ChibiDanger)
                                } else if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(0.2f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Chibi Feedback bubble during checked status
                if (activeQuiz.isAnswerChecked) {
                    val wasCorrect = activeQuiz.isCorrect == true
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(
                                color = if (wasCorrect) ChibiSuccess.copy(0.15f) else ChibiDanger.copy(0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (wasCorrect) ChibiSuccess else ChibiDanger,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = if (wasCorrect) "🎉" else "💡", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = if (wasCorrect) "Yatta! Absolute Genius! (+15 XP)" else "Ganbare! Close, try again next time!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = if (wasCorrect) ChibiSuccess else ChibiDanger
                                )
                                Text(
                                    text = "Correct answer: ${currentQuestion.correctAnswer}",
                                    fontSize = 10.sp,
                                    color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
                                )
                            }
                        }
                    }
                }

                // Check Button Action Area
                Button(
                    onClick = {
                        if (!activeQuiz.isAnswerChecked) {
                            viewModel.checkQuizAnswer()
                        } else {
                            viewModel.nextQuizQuestion()
                        }
                    },
                    enabled = activeQuiz.selectedAnswer != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeQuiz.isAnswerChecked) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(48.dp)
                        .padding(top = 4.dp)
                        .testTag("quiz_action_btn")
                ) {
                    Text(
                        text = if (!activeQuiz.isAnswerChecked) "Check Answer" else "Next Word",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// --- SCREEN 3: ACHIEVEMENTS & GLOBAL STATS ---
@Composable
fun MyAchievementsScreen(
    viewModel: ChibiViewModel,
    userStats: UserStats?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Dynamic level stats header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(0.2f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "👑", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userStats?.name ?: "Chibi Learner",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Global Asian Language level: ${userStats?.currentLevel ?: 1}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress to Next Level
                    val totalXp = userStats?.totalXp ?: 50
                    val currentLvl = userStats?.currentLevel ?: 1
                    val baseLvlXp = (currentLvl - 1) * 100
                    val relativeXp = totalXp - baseLvlXp
                    val progressFraction = (relativeXp.toFloat() / 100f).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Level Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChibiTextLight)
                        Text(text = "$relativeXp / 100 XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            ChibiLavender
                                        )
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Score", fontSize = 11.sp, color = ChibiTextLight)
                            Text("$totalXp XP", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Preferred Mode", fontSize = 11.sp, color = ChibiTextLight)
                            Text(userStats?.preferredLanguage ?: "Both", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ChibiLavenderDark)
                        }
                    }
                }
            }
        }

        // BADGE GALLERY
        item {
            Text(
                text = "Adorable Badge Collection",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            val databaseBadges = userStats?.unlockedBadgesListStr?.split(", ")?.map { it.trim() } ?: listOf("🎓 Starter")

            val badgesPreset = listOf(
                BadgeItem(
                    title = "Starter Club",
                    emoji = "🎓",
                    description = "Commence your cute Asian languages learning path!",
                    isUnlocked = databaseBadges.any { it.contains("Starter") }
                ),
                BadgeItem(
                    title = "Quiz Master",
                    emoji = "🏆",
                    description = "Score 150+ overall points in gamified study sets!",
                    isUnlocked = databaseBadges.any { it.contains("Quiz Champion") }
                ),
                BadgeItem(
                    title = "Polyglot Child",
                    emoji = "🌍",
                    description = "Acquire progress increments in 3 or more Asian languages!",
                    isUnlocked = databaseBadges.any { it.contains("Polyglot") }
                ),
                BadgeItem(
                    title = "Streak Champion",
                    emoji = "🔥",
                    description = "Reach a daily study streak of 3+ days!",
                    isUnlocked = databaseBadges.any { it.contains("Streak Master") }
                )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                badgesPreset.forEach { badge ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (badge.isUnlocked) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surface.copy(0.4f)
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (badge.isUnlocked) 2.dp else 1.dp,
                                color = if (badge.isUnlocked) ChibiGold else MaterialTheme.colorScheme.primary.copy(0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("badge_card_${badge.title}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (badge.isUnlocked) ChibiGold.copy(0.18f) else Color.LightGray.copy(0.2f)
                                    )
                                    .border(
                                        if (badge.isUnlocked) 2.dp else 1.dp,
                                        if (badge.isUnlocked) ChibiGold else Color.Gray,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badge.emoji,
                                    fontSize = 28.sp,
                                    modifier = Modifier.scale(if (badge.isUnlocked) 1f else 0.85f)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onBackground else ChibiTextLight
                                )
                                Text(
                                    text = badge.description,
                                    fontSize = 11.sp,
                                    color = ChibiTextLight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (badge.isUnlocked) "Earned! 🎉" else "Locked 🔒",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (badge.isUnlocked) ChibiGoldDark else ChibiTextLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Progress Stats Reset Button configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reset Progress Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Clears all points, badges, levels, and streaks to start fresh.",
                        fontSize = 10.sp,
                        color = ChibiTextLight,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.resetProgress() },
                        colors = ButtonDefaults.buttonColors(containerColor = ChibiDanger),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Reset Stats", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Badge data structure
data class BadgeItem(
    val title: String,
    val emoji: String,
    val description: String,
    val isUnlocked: Boolean
)

// --- SCREEN 4: ADMIN LESSONS WRITING/DELETING ---
@Composable
fun AdminLessonsScreen(
    viewModel: ChibiViewModel,
    vocab: List<Vocabulary>
) {
    var language by remember { mutableStateOf("Korean") }
    var word by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var engTranslation by remember { mutableStateOf("") }
    var hinTranslation by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Greetings") }

    val languages = listOf("Korean", "Japanese", "Chinese", "Thai")
    val categories = listOf("Greetings", "Common", "Food & Drink", "Feelings", "Relationships")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(0.2f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "➕ Add Custom Lesson Word",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Language Selector
                    Column {
                        Text("Choose Target Language", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChibiTextLight)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            languages.forEach { l ->
                                val active = language == l
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(0.08f))
                                        .clickable { language = l }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = l,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }

                    // Original text field
                    OutlinedTextField(
                        value = word,
                        onValueChange = { word = it },
                        label = { Text("Original Script (e.g. 안녕하세요)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_input_word"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.3f)
                        )
                    )

                    // Phonetic Pronunciation
                    OutlinedTextField(
                        value = pronunciation,
                        onValueChange = { pronunciation = it },
                        label = { Text("Transliteration / Pronunciation (e.g. Annyeonghaseyo)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_input_pron"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.3f)
                        )
                    )

                    // English Translation
                    OutlinedTextField(
                        value = engTranslation,
                        onValueChange = { engTranslation = it },
                        label = { Text("English Translation (e.g. Hello)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_input_eng"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.3f)
                        )
                    )

                    // Hindi Translation
                    OutlinedTextField(
                        value = hinTranslation,
                        onValueChange = { hinTranslation = it },
                        label = { Text("Hindi Translation (e.g. नमस्ते)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_input_hin"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.3f)
                        )
                    )

                    // Category Selector Segmented Box
                    Column {
                        Text("Vocabulary Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChibiTextLight)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(top = 4.dp)
                        ) {
                            items(categories) { cat ->
                                val active = category == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary.copy(0.08f))
                                        .clickable { category = cat }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }

                    // Save Word CTA
                    Button(
                        onClick = {
                            if (word.isNotBlank() && pronunciation.isNotBlank() && engTranslation.isNotBlank() && hinTranslation.isNotBlank()) {
                                viewModel.addNewVocabularyWord(
                                    language, word, pronunciation, engTranslation, hinTranslation, category
                                )
                                // Clear Form inputs
                                word = ""
                                pronunciation = ""
                                engTranslation = ""
                                hinTranslation = ""
                            }
                        },
                        enabled = word.isNotBlank() && pronunciation.isNotBlank() && engTranslation.isNotBlank() && hinTranslation.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_word_btn")
                    ) {
                        Text("Add to Database", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Manage Active Vocabulary (${vocab.size} total)",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(vocab) { entry ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(0.1f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when (entry.language) {
                                    "Korean" -> "🇰🇷"
                                    "Japanese" -> "🇯🇵"
                                    "Chinese" -> "🇨🇳"
                                    "Thai" -> "🇹🇭"
                                    else -> "🌸"
                                },
                                fontSize = 14.sp
                            )
                            Text(
                                text = entry.word,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Phonetic: ${entry.pronunciation} | Category: ${entry.category}",
                            fontSize = 11.sp,
                            color = ChibiTextLight
                        )
                        Text(
                            text = "EN: ${entry.translationEng} | HI: ${entry.translationHin}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deleteVocabularyWord(entry.id) },
                        modifier = Modifier.testTag("admin_delete_btn_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete word",
                            tint = ChibiDanger
                        )
                    }
                }
            }
        }
    }
}
