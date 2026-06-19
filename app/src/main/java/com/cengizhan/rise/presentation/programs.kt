package com.cengizhan.rise.presentation

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.core.LocalRiseColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

private data class ChallengeProgram(
    val id: String,
    val title: String,
    val durationDays: Int,
    val difficulty: String,
    val description: String,
    val dailyTasks: List<String>
) {
    val duration: String
        get() = "$durationDays Days"
}

@Composable
fun ProgramsScreen() {
    val isPremiumUser = true

    if (!isPremiumUser) {
        LockedProgramsScreen()
        return
    }

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("rise_challenge_tracking", android.content.Context.MODE_PRIVATE)
    }
    var selectedProgram by remember { mutableStateOf<ChallengeProgram?>(null) }
    var activeChallengeId by remember {
        mutableStateOf(prefs.getString(KEY_ACTIVE_CHALLENGE_ID, null))
    }
    val programs = remember { challengePrograms() }
    val activeProgram = programs.firstOrNull { it.id == activeChallengeId }

    if (activeProgram != null) {
        ActiveChallengeScreen(
            program = activeProgram,
            prefs = prefs,
            onQuitChallenge = {
                clearActiveChallenge(prefs)
                activeChallengeId = null
            },
            onArchiveCompletedChallenge = {
                clearActiveChallenge(prefs)
                activeChallengeId = null
            }
        )
        return
    }

    selectedProgram?.let { program ->
        ChallengeDetailScreen(
            program = program,
            onStartChallenge = {
                startChallenge(prefs, program)
                activeChallengeId = program.id
                selectedProgram = null
            },
            onBackClick = {
                selectedProgram = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalRiseColors.current.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Challenge Programs",
            color = LocalRiseColors.current.primaryText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Follow guided challenges to build discipline.",
            color = LocalRiseColors.current.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(programs) { program ->
                ChallengeCard(
                    program = program,
                    onStartClick = {
                        selectedProgram = program
                    }
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    program: ChallengeProgram,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalRiseColors.current.card
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = program.title,
                color = LocalRiseColors.current.primaryText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 25.sp
            )

            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgramMetaPill(text = program.duration)
                ProgramMetaPill(text = program.difficulty)
            }

            Text(
                text = program.description,
                color = LocalRiseColors.current.secondaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 12.dp)
            )

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalRiseColors.current.primaryButton
                )
            ) {
                Text(
                    text = "Start Challenge",
                    color = LocalRiseColors.current.primaryButtonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChallengeDetailScreen(
    program: ChallengeProgram,
    onStartChallenge: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalRiseColors.current.background)
            .padding(20.dp)
    ) {
        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalRiseColors.current.indicator
            )
        ) {
            Text(
                text = "Back",
                color = LocalRiseColors.current.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = program.title,
            color = LocalRiseColors.current.primaryText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )

        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProgramMetaPill(text = program.duration)
            ProgramMetaPill(text = program.difficulty)
        }

        Text(
            text = program.description,
            color = LocalRiseColors.current.secondaryText,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            onClick = onStartChallenge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalRiseColors.current.primaryButton
            )
        ) {
            Text(
                text = "Start Challenge",
                color = LocalRiseColors.current.primaryButtonText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Daily Tasks",
            color = LocalRiseColors.current.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(program.dailyTasks) { task ->
                TaskCard(task = task)
            }
        }
    }
}

@Composable
private fun ActiveChallengeScreen(
    program: ChallengeProgram,
    prefs: SharedPreferences,
    onQuitChallenge: () -> Unit,
    onArchiveCompletedChallenge: () -> Unit
) {
    val startDate = prefs.getString(KEY_ACTIVE_CHALLENGE_START_DATE, getTodayDate()) ?: getTodayDate()
    val currentDay = calculateCurrentDay(startDate)
    val isCompleted = currentDay > program.durationDays

    if (isCompleted) {
        ChallengeCompletedScreen(
            program = program,
            onArchiveCompletedChallenge = onArchiveCompletedChallenge
        )
        return
    }

    var refreshCounter by remember { mutableIntStateOf(0) }
    var showEndDialog by remember { mutableStateOf(false) }
    refreshCounter
    val todayTasks = program.tasksForDay(currentDay)
    val completedCount = todayTasks.indices.count { index ->
        prefs.getBoolean(taskKey(program.id, currentDay, index), false)
    }
    val progress = if (todayTasks.isEmpty()) 0f else completedCount.toFloat() / todayTasks.size
    val progressPercent = (progress * 100).toInt()

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = {
                showEndDialog = false
            },
            containerColor = LocalRiseColors.current.card,
            titleContentColor = LocalRiseColors.current.primaryText,
            textContentColor = LocalRiseColors.current.secondaryText,
            title = {
                Text(text = "Quit Challenge?")
            },
            text = {
                Text(text = "This will stop tracking this challenge. Your current challenge progress will be removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndDialog = false
                        onQuitChallenge()
                    }
                ) {
                    Text(
                        text = "Quit",
                        color = Color(0xFFFCA5A5)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDialog = false
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = LocalRiseColors.current.primaryText
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalRiseColors.current.background)
            .padding(20.dp)
    ) {
        Text(
            text = program.title,
            color = LocalRiseColors.current.primaryText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )

        Text(
            text = "Day $currentDay / ${program.durationDays}",
            color = LocalRiseColors.current.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = LocalRiseColors.current.card
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "$progressPercent% complete today",
                    color = LocalRiseColors.current.primaryText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    color = LocalRiseColors.current.primaryText,
                    trackColor = LocalRiseColors.current.indicator
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (completedCount == todayTasks.size && todayTasks.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF102018)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Day completed",
                        color = LocalRiseColors.current.primaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Come back tomorrow for the next tasks.",
                        color = Color(0xFF86EFAC),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = "Today's Tasks",
            color = LocalRiseColors.current.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(todayTasks) { index, task ->
                val key = taskKey(program.id, currentDay, index)
                val checked = prefs.getBoolean(key, false)

                ActiveTaskCard(
                    task = task,
                    checked = checked,
                    onClick = {
                        prefs.edit()
                            .putBoolean(key, !checked)
                            .apply()
                        refreshCounter++
                    }
                )
            }
        }

        Button(
            onClick = {
                showEndDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalRiseColors.current.indicator
            )
        ) {
            Text(
                text = "Quit Challenge",
                color = Color(0xFFFCA5A5),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActiveTaskCard(
    task: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (checked)
                    Color(0xFF102018)
                else
                    LocalRiseColors.current.card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    onClick()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = LocalRiseColors.current.primaryButton,
                    uncheckedColor = LocalRiseColors.current.mutedText,
                    checkmarkColor = LocalRiseColors.current.primaryButtonText
                )
            )

            Text(
                text = task,
                color = LocalRiseColors.current.primaryText,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ChallengeCompletedScreen(
    program: ChallengeProgram,
    onArchiveCompletedChallenge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalRiseColors.current.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = LocalRiseColors.current.card
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "Challenge Completed",
                    color = LocalRiseColors.current.primaryText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "You finished ${program.title}. Lock in the identity and choose your next challenge.",
                    color = LocalRiseColors.current.secondaryText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Button(
                    onClick = onArchiveCompletedChallenge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalRiseColors.current.primaryButton
                    )
                ) {
                    Text(
                        text = "Return to Programs",
                        color = LocalRiseColors.current.primaryButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramMetaPill(
    text: String
) {
    Text(
        text = text,
        color = LocalRiseColors.current.primaryText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(
                color = LocalRiseColors.current.indicator,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun TaskCard(
    task: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalRiseColors.current.card
        )
    ) {
        Text(
            text = task,
            color = LocalRiseColors.current.primaryText,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun LockedProgramsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalRiseColors.current.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = LocalRiseColors.current.card
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "Unlock Challenge Programs",
                    color = LocalRiseColors.current.primaryText,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )

                Text(
                    text = "Follow guided self-improvement challenges.",
                    color = LocalRiseColors.current.secondaryText,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalRiseColors.current.primaryButton
                    )
                ) {
                    Text(
                        text = "Upgrade to Premium",
                        color = LocalRiseColors.current.primaryButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun challengePrograms(): List<ChallengeProgram> {
    return listOf(
        ChallengeProgram(
            id = "dopamine_detox_7",
            title = "7-Day Dopamine Detox Challenge",
            durationDays = 7,
            difficulty = "Medium",
            description = "Reduce cheap dopamine and regain focus.",
            dailyTasks = listOf(
                "Avoid short-form videos.",
                "No junk food or sugary snacks.",
                "Do one focused work block.",
                "Take a 20-minute walk without headphones.",
                "Write a short reflection before sleep."
            )
        ),
        ChallengeProgram(
            id = "monk_mode_30",
            title = "30-Day Monk Mode Challenge",
            durationDays = 30,
            difficulty = "Hard",
            description = "Build deep discipline with strict daily rules.",
            dailyTasks = listOf(
                "Wake up at the same time.",
                "Train or move your body.",
                "Complete two deep work blocks.",
                "Avoid social media until evening.",
                "Review your day and plan tomorrow."
            )
        ),
        ChallengeProgram(
            id = "no_sugar_14",
            title = "14-Day No Sugar Challenge",
            durationDays = 14,
            difficulty = "Medium",
            description = "Reset your cravings and improve self-control.",
            dailyTasks = listOf(
                "Avoid added sugar.",
                "Drink enough water.",
                "Eat a high-protein meal.",
                "Prepare one clean snack.",
                "Track cravings without acting on them."
            )
        ),
        ChallengeProgram(
            id = "deep_focus_7",
            title = "7-Day Deep Focus Challenge",
            durationDays = 7,
            difficulty = "Easy",
            description = "Train your mind for distraction-free work.",
            dailyTasks = listOf(
                "Choose one priority task.",
                "Put your phone away for one focus block.",
                "Work for 45 minutes without switching tabs.",
                "Take a short break outside the screen.",
                "Write what helped you focus."
            )
        ),
        ChallengeProgram(
            id = "study_discipline_21",
            title = "21-Day Study Discipline Challenge",
            durationDays = 21,
            difficulty = "Medium",
            description = "Build a consistent study routine.",
            dailyTasks = listOf(
                "Review your study goal.",
                "Study for at least 60 minutes.",
                "Summarize what you learned.",
                "Practice active recall.",
                "Plan tomorrow's study session."
            )
        )
    )
}

private fun startChallenge(
    prefs: SharedPreferences,
    program: ChallengeProgram
) {
    prefs.edit()
        .putString(KEY_ACTIVE_CHALLENGE_ID, program.id)
        .putString(KEY_ACTIVE_CHALLENGE_START_DATE, getTodayDate())
        .apply()
}

private fun clearActiveChallenge(prefs: SharedPreferences) {
    val activeChallengeId = prefs.getString(KEY_ACTIVE_CHALLENGE_ID, null)
    val editor = prefs.edit()
        .remove(KEY_ACTIVE_CHALLENGE_ID)
        .remove(KEY_ACTIVE_CHALLENGE_START_DATE)

    if (activeChallengeId != null) {
        prefs.all.keys
            .filter { key -> key.startsWith("task_${activeChallengeId}_") }
            .forEach { key -> editor.remove(key) }
    }

    editor.apply()
}

private fun ChallengeProgram.tasksForDay(dayNumber: Int): List<String> {
    if (dailyTasks.isEmpty()) return emptyList()

    val rotation = (dayNumber - 1).coerceAtLeast(0) % dailyTasks.size
    return dailyTasks.drop(rotation) + dailyTasks.take(rotation)
}

private fun calculateCurrentDay(startDate: String): Int {
    val formatter = dateFormat()
    val start = formatter.parse(startDate) ?: return 1
    val todayText = formatter.format(Calendar.getInstance().time)
    val today = formatter.parse(todayText) ?: return 1
    val dayMillis = 24L * 60L * 60L * 1000L
    val daysBetween = max(0, ((today.time - start.time) / dayMillis).toInt())
    return daysBetween + 1
}

private fun taskKey(
    challengeId: String,
    dayNumber: Int,
    taskIndex: Int
): String {
    return "task_${challengeId}_${dayNumber}_$taskIndex"
}

private fun getTodayDate(): String {
    return dateFormat().format(Calendar.getInstance().time)
}

private fun dateFormat(): SimpleDateFormat {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US)
}

private const val KEY_ACTIVE_CHALLENGE_ID = "activeChallengeId"
private const val KEY_ACTIVE_CHALLENGE_START_DATE = "activeChallengeStartDate"

