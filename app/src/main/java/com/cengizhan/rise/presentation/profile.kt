package com.cengizhan.rise.presentation

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cengizhan.rise.core.CoachNotificationSettings
import com.cengizhan.rise.core.RiseThemeColors
import com.cengizhan.rise.core.RiseThemeSettings
import java.util.Locale

private data class PreferenceOption(
    val title: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

@Composable
fun ProfileScreen(
    onAddWidgetClick: () -> Unit,
    selectedThemeMode: String = RiseThemeSettings.THEME_DARK_PREMIUM,
    onThemeModeChange: (String) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(RiseThemeSettings.PREFS_NAME, Context.MODE_PRIVATE)
    }
    val colors = RiseThemeSettings.colorsFor(selectedThemeMode)
    var generalNotificationsEnabled by remember {
        mutableStateOf(
            prefs.getBoolean(RiseThemeSettings.KEY_GENERAL_NOTIFICATIONS, true)
        )
    }
    var generalNotificationStartTime by remember {
        mutableStateOf(
            prefs.getString(RiseThemeSettings.KEY_GENERAL_NOTIFICATION_START_TIME, "09:00") ?: "09:00"
        )
    }
    var generalNotificationEndTime by remember {
        mutableStateOf(
            prefs.getString(RiseThemeSettings.KEY_GENERAL_NOTIFICATION_END_TIME, "22:00") ?: "22:00"
        )
    }
    var generalDailyReminder by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_GENERAL_DAILY_REMINDER, true))
    }
    var generalNoActivityReminder by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_GENERAL_NO_ACTIVITY_REMINDER, true))
    }
    var generalThreeDayComeback by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_GENERAL_THREE_DAY_COMEBACK, true))
    }
    var generalFiveDayComeback by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_GENERAL_FIVE_DAY_COMEBACK, true))
    }
    var smartCoachNotificationsEnabled by remember {
        mutableStateOf(
            prefs.getBoolean(RiseThemeSettings.KEY_SMART_COACH_NOTIFICATIONS, true)
        )
    }
    var smartCoachNotificationStartTime by remember {
        mutableStateOf(
            prefs.getString(RiseThemeSettings.KEY_SMART_COACH_NOTIFICATION_START_TIME, "10:00") ?: "10:00"
        )
    }
    var smartCoachNotificationEndTime by remember {
        mutableStateOf(
            prefs.getString(RiseThemeSettings.KEY_SMART_COACH_NOTIFICATION_END_TIME, "21:00") ?: "21:00"
        )
    }
    var coachHabitAdded by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_COACH_HABIT_ADDED, true))
    }
    var coachHabitCompleted by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_COACH_HABIT_COMPLETED, true))
    }
    var coachDailyCompletion by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_COACH_DAILY_COMPLETION, true))
    }
    var coachDailyMotivation by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_COACH_DAILY_MOTIVATION, true))
    }
    var coachPastSuccess by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_COACH_PAST_SUCCESS, true))
    }
    var coachMotivationLetter by remember {
        mutableStateOf(prefs.getBoolean(CoachNotificationSettings.KEY_COACH_MOTIVATION_LETTER, true))
    }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            containerColor = colors.card,
            titleContentColor = colors.primaryText,
            textContentColor = colors.secondaryText,
            title = {
                Text(text = "Coming Soon")
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = { dialogMessage = null }) {
                    Text(
                        text = "OK",
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
            text = "Profile",
            color = colors.primaryText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Settings for your discipline system.",
            color = colors.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        NotificationTimeRangeCard(
            title = "General Notification Settings",
            description = "Choose when Rise can send daily reminders and app notifications.",
            enabled = generalNotificationsEnabled,
            startTime = generalNotificationStartTime,
            endTime = generalNotificationEndTime,
            options = listOf(
                PreferenceOption("Daily reminder", generalDailyReminder) { enabled ->
                    generalDailyReminder = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_GENERAL_DAILY_REMINDER, enabled).apply()
                },
                PreferenceOption("No activity today reminder", generalNoActivityReminder) { enabled ->
                    generalNoActivityReminder = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_GENERAL_NO_ACTIVITY_REMINDER, enabled).apply()
                },
                PreferenceOption("3 day comeback reminder", generalThreeDayComeback) { enabled ->
                    generalThreeDayComeback = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_GENERAL_THREE_DAY_COMEBACK, enabled).apply()
                },
                PreferenceOption("5 day comeback reminder", generalFiveDayComeback) { enabled ->
                    generalFiveDayComeback = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_GENERAL_FIVE_DAY_COMEBACK, enabled).apply()
                }
            ),
            colors = colors,
            onCheckedChange = { enabled ->
                generalNotificationsEnabled = enabled
                prefs.edit()
                    .putBoolean(RiseThemeSettings.KEY_GENERAL_NOTIFICATIONS, enabled)
                    .apply()
            },
            onStartTimeChange = { time ->
                generalNotificationStartTime = time
                prefs.edit()
                    .putString(RiseThemeSettings.KEY_GENERAL_NOTIFICATION_START_TIME, time)
                    .apply()
            },
            onEndTimeChange = { time ->
                generalNotificationEndTime = time
                prefs.edit()
                    .putString(RiseThemeSettings.KEY_GENERAL_NOTIFICATION_END_TIME, time)
                    .apply()
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        NotificationTimeRangeCard(
            title = "Smart Coach Notifications",
            description = "Choose when Smart Coach can send motivational discipline reminders.",
            enabled = smartCoachNotificationsEnabled,
            startTime = smartCoachNotificationStartTime,
            endTime = smartCoachNotificationEndTime,
            options = listOf(
                PreferenceOption("Habit added message", coachHabitAdded) { enabled ->
                    coachHabitAdded = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_COACH_HABIT_ADDED, enabled).apply()
                },
                PreferenceOption("Habit completed message", coachHabitCompleted) { enabled ->
                    coachHabitCompleted = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_COACH_HABIT_COMPLETED, enabled).apply()
                },
                PreferenceOption("Daily completion congratulations", coachDailyCompletion) { enabled ->
                    coachDailyCompletion = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_COACH_DAILY_COMPLETION, enabled).apply()
                },
                PreferenceOption("Daily motivation message", coachDailyMotivation) { enabled ->
                    coachDailyMotivation = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_COACH_DAILY_MOTIVATION, enabled).apply()
                },
                PreferenceOption("Past success based motivation", coachPastSuccess) { enabled ->
                    coachPastSuccess = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_COACH_PAST_SUCCESS, enabled).apply()
                },
                PreferenceOption("Motivation letter after inactivity", coachMotivationLetter) { enabled ->
                    coachMotivationLetter = enabled
                    prefs.edit().putBoolean(CoachNotificationSettings.KEY_COACH_MOTIVATION_LETTER, enabled).apply()
                }
            ),
            colors = colors,
            onCheckedChange = { enabled ->
                smartCoachNotificationsEnabled = enabled
                prefs.edit()
                    .putBoolean(RiseThemeSettings.KEY_SMART_COACH_NOTIFICATIONS, enabled)
                    .apply()
            },
            onStartTimeChange = { time ->
                smartCoachNotificationStartTime = time
                prefs.edit()
                    .putString(RiseThemeSettings.KEY_SMART_COACH_NOTIFICATION_START_TIME, time)
                    .apply()
            },
            onEndTimeChange = { time ->
                smartCoachNotificationEndTime = time
                prefs.edit()
                    .putString(RiseThemeSettings.KEY_SMART_COACH_NOTIFICATION_END_TIME, time)
                    .apply()
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ActionCard(
            title = "Support Rise",
            description = "Would you like to support projects like Rise and help us keep improving?",
            buttonText = "Donate",
            colors = colors,
            onClick = {
                dialogMessage = "Donation support will be added later."
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ActionCard(
            title = "Rise Premium",
            description = "Remove ads and use Rise without interruptions.",
            buttonText = "Upgrade for $1",
            priceText = "$1",
            colors = colors,
            onClick = {
                dialogMessage = "Premium purchase will be added later."
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ThemeModeCard(
            selectedThemeMode = selectedThemeMode,
            colors = colors,
            onThemeModeChange = { themeMode ->
                prefs.edit()
                    .putString(RiseThemeSettings.KEY_SELECTED_THEME_MODE, themeMode)
                    .apply()
                onThemeModeChange(themeMode)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ActionCard(
            title = "Home Screen Widget",
            description = "Track today's discipline progress without opening the app.",
            buttonText = "Add Home Screen Widget",
            colors = colors,
            onClick = onAddWidgetClick
        )
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    colors: RiseThemeColors,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsCard(colors = colors) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                CardTitle(title = title, colors = colors)
                CardDescription(description = description, colors = colors)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primaryButtonText,
                    checkedTrackColor = colors.primaryButton,
                    uncheckedThumbColor = colors.secondaryText,
                    uncheckedTrackColor = colors.cardAlt,
                    uncheckedBorderColor = colors.outline
                )
            )
        }
    }
}

@Composable
private fun NotificationTimeRangeCard(
    title: String,
    description: String,
    enabled: Boolean,
    startTime: String,
    endTime: String,
    options: List<PreferenceOption>,
    colors: RiseThemeColors,
    onCheckedChange: (Boolean) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    val context = LocalContext.current

    SettingsCard(colors = colors) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                CardTitle(title = title, colors = colors)
                CardDescription(description = description, colors = colors)
            }

            Switch(
                checked = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primaryButtonText,
                    checkedTrackColor = colors.primaryButton,
                    uncheckedThumbColor = colors.secondaryText,
                    uncheckedTrackColor = colors.cardAlt,
                    uncheckedBorderColor = colors.outline
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TimeRangeButton(
                label = "Start time",
                value = startTime,
                enabled = enabled,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = {
                    showTimePicker(
                        context = context,
                        currentTime = startTime,
                        onTimeSelected = onStartTimeChange
                    )
                }
            )

            TimeRangeButton(
                label = "End time",
                value = endTime,
                enabled = enabled,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = {
                    showTimePicker(
                        context = context,
                        currentTime = endTime,
                        onTimeSelected = onEndTimeChange
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { option ->
            PreferenceSwitchRow(
                title = option.title,
                checked = option.checked,
                enabled = enabled,
                colors = colors,
                onCheckedChange = option.onCheckedChange
            )
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    colors: RiseThemeColors,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(
                color = colors.cardAlt,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (enabled) colors.primaryText else colors.secondaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primaryButtonText,
                checkedTrackColor = colors.primaryButton,
                uncheckedThumbColor = colors.secondaryText,
                uncheckedTrackColor = colors.indicator,
                uncheckedBorderColor = colors.outline,
                disabledCheckedThumbColor = colors.secondaryText,
                disabledCheckedTrackColor = colors.indicator,
                disabledUncheckedThumbColor = colors.secondaryText,
                disabledUncheckedTrackColor = colors.indicator
            )
        )
    }
}

@Composable
private fun TimeRangeButton(
    label: String,
    value: String,
    enabled: Boolean,
    colors: RiseThemeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = if (enabled) colors.cardAlt else colors.indicator,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = colors.secondaryText,
            fontSize = 12.sp
        )

        Text(
            text = value,
            color = colors.primaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    buttonText: String,
    colors: RiseThemeColors,
    priceText: String? = null,
    onClick: () -> Unit
) {
    SettingsCard(colors = colors) {
        CardTitle(title = title, colors = colors)
        CardDescription(description = description, colors = colors)

        priceText?.let {
            Text(
                text = it,
                color = colors.primaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        PrimaryProfileButton(
            text = buttonText,
            colors = colors,
            onClick = onClick
        )
    }
}

@Composable
private fun ThemeModeCard(
    selectedThemeMode: String,
    colors: RiseThemeColors,
    onThemeModeChange: (String) -> Unit
) {
    SettingsCard(colors = colors) {
        CardTitle(title = "Theme Mode", colors = colors)
        CardDescription(description = "Choose how Rise looks.", colors = colors)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeOption(
                title = "Dark Premium",
                selected = selectedThemeMode == RiseThemeSettings.THEME_DARK_PREMIUM,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = {
                    onThemeModeChange(RiseThemeSettings.THEME_DARK_PREMIUM)
                }
            )

            ThemeOption(
                title = "Light Mode",
                selected = selectedThemeMode == RiseThemeSettings.THEME_LIGHT,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = {
                    onThemeModeChange(RiseThemeSettings.THEME_LIGHT)
                }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    colors: RiseThemeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) colors.primaryButton else colors.cardAlt
    val textColor = if (selected) colors.primaryButtonText else colors.primaryText
    val borderColor = if (selected) colors.primaryButton else colors.outline

    Column(
        modifier = modifier
            .height(52.dp)
            .background(
                color = background,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsCard(
    colors: RiseThemeColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun CardTitle(
    title: String,
    colors: RiseThemeColors
) {
    Text(
        text = title,
        color = colors.primaryText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CardDescription(
    description: String,
    colors: RiseThemeColors
) {
    Text(
        text = description,
        color = colors.secondaryText,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun PrimaryProfileButton(
    text: String,
    colors: RiseThemeColors,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primaryButton
        )
    ) {
        Text(
            text = text,
            color = colors.primaryButtonText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun showTimePicker(
    context: Context,
    currentTime: String,
    onTimeSelected: (String) -> Unit
) {
    val parts = currentTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(
                String.format(
                    Locale.US,
                    "%02d:%02d",
                    selectedHour,
                    selectedMinute
                )
            )
        },
        hour,
        minute,
        true
    ).show()
}
