package com.cengizhan.rise.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.core.LocalRiseColors
import com.cengizhan.rise.data.local.entity.CoachMessageEntity
import com.cengizhan.rise.data.local.entity.HabitEntity

@Composable
fun CoachScreen(
    habits: List<HabitEntity>,
    feedMessages: List<CoachMessageEntity> = emptyList()
) {
    val colors = LocalRiseColors.current
    val isPremiumUser = true

    if (!isPremiumUser) {
        LockedCoachScreen()
        return
    }

    val completedToday = habits.count { it.isCompletedToday }
    val completionPercent =
        if (habits.isEmpty()) 0 else ((completedToday.toFloat() / habits.size) * 100).toInt()
    val totalStreak = habits.sumOf { it.streakCount }
    val bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
    val coachMessage = when {
        habits.isEmpty() ->
            "Start with one habit. A simple system beats a perfect plan."
        completionPercent == 0 ->
            "Your day is still open. Complete one habit now and claim momentum."
        completionPercent in 1..49 ->
            "You have started. Now make the next small win obvious and finish one more."
        completionPercent in 50..99 ->
            "Strong progress today. Push a little further and protect the streak."
        else ->
            "Perfect day. You showed up for every habit and reinforced your identity."
    }

    val streakInsight = when {
        bestStreak > 0 ->
            "Your best streak is $bestStreak days. Keep building from that proof."
        totalStreak > 0 ->
            "Your current total streak is $totalStreak days across your habits."
        else ->
            "Your first streak begins with the next completed habit."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Smart Coach",
            color = colors.primaryText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your discipline assistant.",
            color = colors.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CoachCard(
            title = "Coach Message",
            body = coachMessage,
            large = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        CoachCard(
            title = "Streak Insight",
            body = streakInsight
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Today's Coach Feed",
            color = colors.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (feedMessages.isEmpty()) {
            CoachCard(
                title = "No coach messages yet",
                body = "Complete habits and keep building. Smart Coach will add updates here during the day."
            )
        } else {
            feedMessages.forEach { message ->
                CoachFeedCard(message = message)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CoachFeedCard(
    message: CoachMessageEntity
) {
    val colors = LocalRiseColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = message.title,
                color = colors.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message.body,
                color = colors.secondaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = message.type.replace("_", " ").uppercase(),
                color = colors.mutedText,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun CoachCard(
    title: String,
    body: String,
    large: Boolean = false
) {
    val colors = LocalRiseColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                color = colors.secondaryText,
                fontSize = 14.sp
            )

            Text(
                text = body,
                color = colors.primaryText,
                fontSize = if (large) 21.sp else 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (large) 29.sp else 24.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun LockedCoachScreen() {
    val colors = LocalRiseColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.card
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "Unlock Smart Coach",
                    color = colors.primaryText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Get personalized discipline guidance.",
                    color = colors.secondaryText,
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
                        containerColor = colors.primaryButton
                    )
                ) {
                    Text(
                        text = "Upgrade to Premium",
                        color = colors.primaryButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

