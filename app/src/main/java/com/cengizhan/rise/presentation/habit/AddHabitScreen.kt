package com.cengizhan.rise.presentation.habit

import android.app.TimePickerDialog
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.core.LocalRiseColors
import java.util.Locale

@Composable
fun AddHabitScreen(
    onSaveClick: (String, String, String?) -> Unit,
    onSaveCustomClick: (String, String?, String?) -> Unit = { _, _, _ -> }
) {
    var showCustomHabitScreen by remember { mutableStateOf(false) }

    if (showCustomHabitScreen) {
        CreateYourOwnHabitScreen(
            onBackClick = {
                showCustomHabitScreen = false
            },
            onSaveCustomClick = onSaveCustomClick
        )
        return
    }

    SuggestedHabitScreen(
        onSaveClick = onSaveClick,
        onCreateYourOwnClick = {
            showCustomHabitScreen = true
        }
    )
}

@Composable
private fun SuggestedHabitScreen(
    onSaveClick: (String, String, String?) -> Unit,
    onCreateYourOwnClick: () -> Unit
) {
    val colors = LocalRiseColors.current
    val context = LocalContext.current
    var selectedHabit by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<String?>(null) }

    val suggestedHabits = listOf(
        "Avoid Unhealthy Sugar",
        "Sleep 8 Hours",
        "No Smoke",
        "No Alcohol",
        "10,000 Steps"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Create Habit",
            color = colors.primaryText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Choose a suggested habit or create your own.",
            color = colors.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 6.dp)
        )

        ReminderCard(
            reminderTime = reminderTime,
            onReminderTimeChange = { reminderTime = it },
            modifier = Modifier.padding(top = 28.dp)
        )

        Text(
            text = "Suggested Habits",
            color = colors.primaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestedHabits) { habit ->
                SuggestedHabitItem(
                    title = habit,
                    selected = selectedHabit == habit,
                    onClick = {
                        selectedHabit = habit
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateYourOwnClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cardAlt
            )
        ) {
            Text(
                text = "Create Your Own Habit",
                color = colors.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                onSaveClick(selectedHabit, "Suggested", reminderTime)
            },
            enabled = selectedHabit.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryButton,
                disabledContainerColor = colors.outline
            )
        ) {
            Text(
                text = "Save Habit",
                color = if (selectedHabit.isNotBlank()) colors.primaryButtonText else colors.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CreateYourOwnHabitScreen(
    onBackClick: () -> Unit,
    onSaveCustomClick: (String, String?, String?) -> Unit
) {
    val colors = LocalRiseColors.current
    var habitName by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    val milestoneMessages = remember {
        mutableStateListOf<String>().apply {
            repeat(milestoneLabels.size) { add("") }
        }
    }

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
                text = "Create Your Own Habit",
                color = colors.primaryText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Build a custom long-term discipline program.",
                color = colors.secondaryText,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Habit name") },
                placeholder = { Text("Example: Deep Focus") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                singleLine = true,
                colors = riseTextFieldColors()
            )

            ReminderCard(
                reminderTime = reminderTime,
                onReminderTimeChange = { reminderTime = it },
                modifier = Modifier.padding(top = 18.dp)
            )

            Text(
                text = "Milestone Messages",
                color = colors.primaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )

            milestoneLabels.forEachIndexed { index, label ->
                OutlinedTextField(
                    value = milestoneMessages[index],
                    onValueChange = { milestoneMessages[index] = it },
                    label = { Text(label) },
                    placeholder = { Text("Write a message for $label") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    minLines = 2,
                    colors = riseTextFieldColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                onSaveCustomClick(
                    habitName.trim(),
                    reminderTime,
                    encodeMilestoneMessages(milestoneMessages)
                )
            },
            enabled = habitName.trim().isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryButton,
                disabledContainerColor = colors.outline
            )
        ) {
            Text(
                text = "Save Custom Habit",
                color = if (habitName.trim().isNotEmpty()) colors.primaryButtonText else colors.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminderTime: String?,
    onReminderTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalRiseColors.current
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Daily Reminder",
                    color = colors.primaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = reminderTime ?: "No reminder set",
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onReminderTimeChange(
                                String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    hourOfDay,
                                    minute
                                )
                            )
                        },
                        9,
                        0,
                        true
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryButton
                )
            ) {
                Text(
                    text = if (reminderTime == null) "Set" else "Change",
                    color = colors.primaryButtonText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SuggestedHabitItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalRiseColors.current

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.primaryButton else colors.card
        )
    ) {
        Text(
            text = title,
            color = if (selected) colors.primaryButtonText else colors.primaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun riseTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = LocalRiseColors.current.primaryText,
        unfocusedTextColor = LocalRiseColors.current.primaryText,
        focusedLabelColor = LocalRiseColors.current.primaryText,
        unfocusedLabelColor = LocalRiseColors.current.secondaryText,
        focusedPlaceholderColor = LocalRiseColors.current.secondaryText,
        unfocusedPlaceholderColor = LocalRiseColors.current.mutedText,
        focusedBorderColor = LocalRiseColors.current.primaryText,
        unfocusedBorderColor = LocalRiseColors.current.outline,
        cursorColor = LocalRiseColors.current.primaryText
    )

private fun encodeMilestoneMessages(messages: List<String>): String {
    return messages.joinToString(MILESTONE_SEPARATOR) { Uri.encode(it) }
}

val milestoneLabels = listOf(
    "Day 1",
    "Day 2",
    "Day 3",
    "Day 4",
    "Day 5",
    "Day 6",
    "Day 7",
    "Week 2",
    "Week 3",
    "Week 4",
    "Month 2",
    "Month 3",
    "Month 6",
    "Month 9",
    "Year 1",
    "Year 2",
    "Year 3",
    "Year 4",
    "Year 5",
    "Year 10",
    "Year 15+"
)

const val MILESTONE_SEPARATOR = "|~|"
