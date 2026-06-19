package com.cengizhan.rise.presentation

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.core.LocalRiseColors
import com.cengizhan.rise.data.local.entity.HabitEntity
import com.cengizhan.rise.presentation.habit.MILESTONE_SEPARATOR
import com.cengizhan.rise.presentation.habit.milestoneLabels

@Composable
fun ProgressScreen(
    habits: List<HabitEntity>
) {
    var selectedHabit by remember { mutableStateOf<HabitEntity?>(null) }

    selectedHabit?.let { habit ->
        HabitDetailScreen(
            habit = habit,
            onBackClick = {
                selectedHabit = null
            }
        )
        return
    }

    val colors = LocalRiseColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Progress",
            color = colors.primaryText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Track your active habits and long-term progress.",
            color = colors.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (habits.isEmpty()) {
            EmptyProgressCard()
        } else {
            habits.forEach { habit ->
                HabitProgressCard(
                    habit = habit,
                    onClick = {
                        selectedHabit = habit
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HabitProgressCard(
    habit: HabitEntity,
    onClick: () -> Unit
) {
    val colors = LocalRiseColors.current
    val elapsedDays = elapsedDays(habit)
    val progress = (elapsedDays.toFloat() / 5475f).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (habit.isCompletedToday)
                    colors.completedHabitCard
                else
                    colors.card
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = habit.title,
                color = colors.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = runningCounterText(habit),
                color = colors.secondaryText,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                color = colors.primaryButton,
                trackColor = colors.indicator
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressMiniStat(
                    title = "Current",
                    value = "${habit.streakCount} days",
                    modifier = Modifier.weight(1f)
                )

                ProgressMiniStat(
                    title = "Best",
                    value = "${habit.bestStreak} days",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HabitDetailScreen(
    habit: HabitEntity,
    onBackClick: () -> Unit
) {
    val colors = LocalRiseColors.current
    val elapsedDays = elapsedDays(habit)
    val milestoneMessages = decodeMilestoneMessages(habit.milestoneMessages)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cardAlt
            )
        ) {
            Text(
                text = "Back",
                color = colors.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = habit.title,
                color = colors.primaryText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = runningCounterText(habit),
                color = colors.secondaryText,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressMiniStat(
                    title = "Current Streak",
                    value = "${habit.streakCount} days",
                    modifier = Modifier.weight(1f)
                )

                ProgressMiniStat(
                    title = "Best Streak",
                    value = "${habit.bestStreak} days",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Milestones",
                color = colors.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            milestoneLabels.forEachIndexed { index, label ->
                val requiredDays = milestoneRequiredDays[index]
                MilestoneItem(
                    label = label,
                    message = milestoneMessages.getOrNull(index).orEmpty(),
                    reached = elapsedDays >= requiredDays,
                    requiredDays = requiredDays
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    label: String,
    message: String,
    reached: Boolean,
    requiredDays: Int
) {
    val colors = LocalRiseColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reached) colors.completedHabitCard else colors.card
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                color = colors.primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    if (reached)
                        "Reached"
                    else
                        "Unlocks at $requiredDays days",
                color = colors.secondaryText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    color = colors.primaryText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressMiniStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalRiseColors.current

    Column(
        modifier = modifier
            .background(
                color = colors.cardAlt,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = colors.secondaryText,
            fontSize = 12.sp
        )

        Text(
            text = value,
            color = colors.primaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun EmptyProgressCard() {
    val colors = LocalRiseColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Text(
            text = "No habits yet. Create a habit to start tracking long-term progress.",
            color = colors.secondaryText,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(18.dp)
        )
    }
}

private fun elapsedDays(habit: HabitEntity): Int {
    val start = habit.startDateMillis ?: System.currentTimeMillis()
    val elapsedMillis = (System.currentTimeMillis() - start).coerceAtLeast(0L)
    return (elapsedMillis / DAY_MILLIS).toInt()
}

private fun runningCounterText(habit: HabitEntity): String {
    val days = elapsedDays(habit)
    return when {
        days <= 0 -> "Started today"
        days == 1 -> "Running for 1 day"
        else -> "Running for $days days"
    }
}

private fun decodeMilestoneMessages(encoded: String?): List<String> {
    if (encoded.isNullOrBlank()) return emptyList()

    return encoded
        .split(MILESTONE_SEPARATOR)
        .map { Uri.decode(it) }
}

private val milestoneRequiredDays = listOf(
    1,
    2,
    3,
    4,
    5,
    6,
    7,
    14,
    21,
    28,
    60,
    90,
    180,
    270,
    365,
    730,
    1095,
    1460,
    1825,
    3650,
    5475
)

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
