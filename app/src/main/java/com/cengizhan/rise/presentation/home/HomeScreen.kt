package com.cengizhan.rise.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.data.local.entity.HabitEntity
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.cengizhan.rise.core.LocalRiseColors
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    habits: List<HabitEntity>,
    onAddHabitClick: () -> Unit,
    onHabitCheckedChange: (HabitEntity) -> Unit,
    onDeleteHabit: (Int) -> Unit
) {
    val colors = LocalRiseColors.current
    val context = LocalContext.current
    val dayStatusPrefs = remember {
        context.getSharedPreferences(DAY_STATUS_PREFS_NAME, android.content.Context.MODE_PRIVATE)
    }
    val todayDate = remember { formatDate(Calendar.getInstance()) }
    var calendarRefreshKey by remember { mutableIntStateOf(0) }
    var habitPendingDelete by remember { mutableStateOf<HabitEntity?>(null) }

    val completedCount = habits.count { it.isCompletedToday }
    val totalStreakCount = habits.sumOf { it.streakCount }
    val bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
    val completionPercent =
        if (habits.isEmpty()) 0 else ((completedCount.toFloat() / habits.size) * 100).toInt()
    val progress =
        if (habits.isEmpty()) 0f else completedCount.toFloat() / habits.size.toFloat()

    LaunchedEffect(habits.map { habit -> habit.id to habit.isCompletedToday }) {
        if (habits.isEmpty()) return@LaunchedEffect

        val previousStatus = dayStatusPrefs.getString(statusKey(todayDate), null)
        val todayStatus = when {
            completedCount == habits.size -> DAY_STATUS_COMPLETED
            completedCount > 0 -> DAY_STATUS_PARTIAL
            else -> DAY_STATUS_NOT_STARTED
        }

        dayStatusPrefs.edit()
            .putString(statusKey(todayDate), todayStatus)
            .apply()
        calendarRefreshKey++

        val messageKey = completionMessageKey(todayDate)
        val alreadyShown = dayStatusPrefs.getBoolean(messageKey, false)

        if (todayStatus == DAY_STATUS_COMPLETED &&
            previousStatus != DAY_STATUS_COMPLETED &&
            !alreadyShown
        ) {
            Toast.makeText(
                context,
                "Congratulations! You completed today’s goals.",
                Toast.LENGTH_SHORT
            ).show()

            dayStatusPrefs.edit()
                .putBoolean(messageKey, true)
                .apply()
        }
    }

    habitPendingDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = {
                habitPendingDelete = null
            },
            containerColor = colors.card,
            titleContentColor = colors.primaryText,
            textContentColor = colors.secondaryText,
            title = {
                Text(text = "Delete Habit?")
            },
            text = {
                Text(text = "This habit and its streak data will be permanently removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteHabit(habit.id)
                        habitPendingDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFFCA5A5)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        habitPendingDelete = null
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.primaryText
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Today",
            color = colors.primaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Build your discipline system.",
            color = colors.secondaryText,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.card
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Daily Completion",
                    color = colors.secondaryText,
                    fontSize = 14.sp
                )

                Text(
                    text = "$completionPercent%",
                    color = colors.primaryText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    color = colors.primaryButton,
                    trackColor = colors.indicator
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        YearlyHabitCalendar(
            externalRefreshKey = calendarRefreshKey
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallStatCard(
                title = "Completed",
                value = "$completedCount/${habits.size}",
                modifier = Modifier.weight(1f)
            )

            SmallStatCard(
                title = "Streak",
                value = "$totalStreakCount days",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallStatCard(
                title = "Best",
                value = "$bestStreak days",
                modifier = Modifier.weight(1f)
            )

            SmallStatCard(
                title = "Habits",
                value = habits.size.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Today’s Habits",
            color = colors.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (habits.isEmpty()) {
            Text(
                text = "No habits yet. Create your first discipline habit.",
                color = colors.secondaryText,
                fontSize = 14.sp
            )
        } else {
            habits.forEach { habit ->
                HabitItem(
                    habit = habit,
                    onClick = {
                        onHabitCheckedChange(habit)
                    },
                    onDeleteClick = {
                        habitPendingDelete = habit
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddHabitClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryButton
            )
        ) {
            Text(
                text = "Add Habit",
                color = colors.primaryButtonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HabitItem(
    habit: HabitEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = LocalRiseColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (habit.isCompletedToday)
                    colors.completedHabitCard
                else
                    colors.card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    color = colors.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration =
                        if (habit.isCompletedToday)
                            TextDecoration.LineThrough
                        else
                            TextDecoration.None
                )

                Text(
                    text = habit.category,
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Streak: ${habit.streakCount} days",
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete habit",
                        tint = Color(0xFFFCA5A5),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                        if (habit.isCompletedToday)
                            colors.primaryButton
                        else
                            colors.indicator
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (habit.isCompletedToday) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = colors.primaryButtonText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyHabitCalendar(
    externalRefreshKey: Int
) {
    val colors = LocalRiseColors.current
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(DAY_STATUS_PREFS_NAME, android.content.Context.MODE_PRIVATE)
    }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val today = formatDate(Calendar.getInstance())
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var refreshCounter by remember { mutableIntStateOf(0) }
    val statuses = remember(refreshCounter, externalRefreshKey, currentYear) {
        loadYearStatuses(prefs, currentYear)
    }

    selectedDate?.let { date ->
        AlertDialog(
            onDismissRequest = {
                selectedDate = null
            },
            containerColor = colors.card,
            titleContentColor = colors.primaryText,
            textContentColor = colors.secondaryText,
            title = {
                Text(text = date)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DayStatusDialogOption(
                        text = "Completed",
                        color = Color(0xFF22C55E),
                        onClick = {
                            prefs.edit()
                                .putString(statusKey(date), DAY_STATUS_COMPLETED)
                                .putBoolean(manualStatusKey(date), true)
                                .apply()
                            refreshCounter++
                            selectedDate = null
                        }
                    )

                    DayStatusDialogOption(
                        text = "Partially completed",
                        color = Color(0xFFEAB308),
                        onClick = {
                            prefs.edit()
                                .putString(statusKey(date), DAY_STATUS_PARTIAL)
                                .putBoolean(manualStatusKey(date), true)
                                .apply()
                            refreshCounter++
                            selectedDate = null
                        }
                    )

                    DayStatusDialogOption(
                        text = "Not started",
                        color = Color(0xFFEF4444),
                        onClick = {
                            prefs.edit()
                                .putString(statusKey(date), DAY_STATUS_NOT_STARTED)
                                .putBoolean(manualStatusKey(date), true)
                                .apply()
                            refreshCounter++
                            selectedDate = null
                        }
                    )
                }
            },
            confirmButton = { }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
        ) {
            Text(
                text = currentYear.toString(),
                color = colors.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap a day to mark your discipline status.",
                color = colors.secondaryText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            repeat(12) { monthIndex ->
                HeatmapMonthRow(
                    year = currentYear,
                    monthIndex = monthIndex,
                    today = today,
                    statuses = statuses,
                    onDateClick = { date ->
                        if (date <= today) {
                            selectedDate = date
                        }
                    }
                )

                if (monthIndex != 11) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun HeatmapMonthRow(
    year: Int,
    monthIndex: Int,
    today: String,
    statuses: Map<String, String>,
    onDateClick: (String) -> Unit
) {
    val colors = LocalRiseColors.current
    val monthName = DateFormatSymbols(Locale.US).shortMonths[monthIndex]
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dates = (1..daysInMonth).map { day ->
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, day)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = monthName,
            color = colors.primaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            dates.chunked(16).forEach { rowDates ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    rowDates.forEach { date ->
                        val dateText = formatDate(date)
                        val isFuture = dateText > today

                        DaySquare(
                            status = if (isFuture) null else statuses[dateText],
                            enabled = !isFuture,
                            onClick = {
                                onDateClick(dateText)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySquare(
    status: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalRiseColors.current
    val color = if (!enabled) {
        colors.cardAlt
    } else {
        when (status) {
            DAY_STATUS_COMPLETED -> Color(0xFF22C55E)
            DAY_STATUS_PARTIAL -> Color(0xFFEAB308)
            DAY_STATUS_NOT_STARTED -> Color(0xFFEF4444)
            else -> colors.indicator
        }
    }

    Box(
        modifier = modifier
            .size(width = 18.dp, height = 16.dp)
            .then(
                if (enabled)
                    Modifier.clickable { onClick() }
                else
                    Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 17.dp, height = 14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        ) {
            if (!enabled) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(width = 5.dp, height = 2.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(colors.mutedText)
                    )

                    Spacer(
                        modifier = Modifier
                            .size(width = 6.dp, height = 4.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(colors.mutedText)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayStatusDialogOption(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    val colors = LocalRiseColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardAlt)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = text,
            color = colors.primaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun SmallStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalRiseColors.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = colors.secondaryText,
                fontSize = 13.sp
            )

            Text(
                text = value,
                color = colors.primaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun formatDate(calendar: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
}

private fun statusKey(date: String): String {
    return "day_status_$date"
}

private fun manualStatusKey(date: String): String {
    return "day_status_manual_$date"
}

private fun completionMessageKey(date: String): String {
    return "completion_message_$date"
}

private fun loadYearStatuses(
    prefs: android.content.SharedPreferences,
    year: Int
): Map<String, String> {
    val prefix = "$year-"
    return prefs.all
        .filterKeys { key -> key.startsWith("day_status_$prefix") }
        .mapKeys { entry -> entry.key.removePrefix("day_status_") }
        .mapValues { entry -> entry.value.toString() }
}

private const val DAY_STATUS_PREFS_NAME = "rise_day_statuses"
private const val DAY_STATUS_COMPLETED = "completed"
private const val DAY_STATUS_PARTIAL = "partial"
private const val DAY_STATUS_NOT_STARTED = "not_started"
