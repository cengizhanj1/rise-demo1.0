package com.cengizhan.rise.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cengizhan.rise.core.LocalRiseColors
import com.cengizhan.rise.core.RiseThemeSettings
import com.cengizhan.rise.presentation.CoachScreen
import com.cengizhan.rise.presentation.ProgressScreen
import com.cengizhan.rise.presentation.ProfileScreen
import com.cengizhan.rise.presentation.habit.AddHabitScreen
import com.cengizhan.rise.widget.RiseWidgetProvider

private data class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val widgetPromptPrefs = remember {
        context.getSharedPreferences("rise_widget_prompt", android.content.Context.MODE_PRIVATE)
    }
    val themeColors = LocalRiseColors.current
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Coach", Icons.Default.Psychology),
        BottomNavItem("Progress", Icons.Default.BarChart),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showAddHabitScreen by remember { mutableStateOf(false) }
    var showWidgetPrompt by remember {
        mutableStateOf(!widgetPromptPrefs.getBoolean("has_shown_widget_prompt", false))
    }

    val habits by homeViewModel.habits.collectAsState()
    val coachMessages by homeViewModel.coachMessages.collectAsState()

    if (showWidgetPrompt) {
        AlertDialog(
            onDismissRequest = {
                widgetPromptPrefs.edit()
                    .putBoolean("has_shown_widget_prompt", true)
                    .apply()
                showWidgetPrompt = false
            },
            containerColor = themeColors.card,
            titleContentColor = themeColors.primaryText,
            textContentColor = themeColors.secondaryText,
            title = {
                Text(text = "Add Rise widget to your home screen?")
            },
            text = {
                Text(text = "Track today's discipline progress without opening the app.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        widgetPromptPrefs.edit()
                            .putBoolean("has_shown_widget_prompt", true)
                            .apply()
                        showWidgetPrompt = false
                        RiseWidgetProvider.requestPinWidget(context)
                    }
                ) {
                    Text(
                        text = "Add Widget",
                        color = themeColors.primaryText
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        widgetPromptPrefs.edit()
                            .putBoolean("has_shown_widget_prompt", true)
                            .apply()
                        showWidgetPrompt = false
                    }
                ) {
                    Text(
                        text = "Not now",
                        color = themeColors.secondaryText
                    )
                }
            }
        )
    }

    if (showAddHabitScreen) {
        AddHabitScreen(
            onSaveClick = { title, category, reminderTime ->
                homeViewModel.addHabit(
                    title = title,
                    category = category,
                    reminderTime = reminderTime
                )
                showAddHabitScreen = false
            },
            onSaveCustomClick = { title, reminderTime, milestoneMessages ->
                homeViewModel.addCustomHabit(
                    title = title,
                    reminderTime = reminderTime,
                    milestoneMessages = milestoneMessages
                )
                showAddHabitScreen = false
            }
        )
        return
    }

    Scaffold(
        containerColor = themeColors.background,
        bottomBar = {
            NavigationBar(
                containerColor = themeColors.card
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(text = item.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = themeColors.primaryText,
                            selectedTextColor = themeColors.primaryText,
                            unselectedIconColor = themeColors.mutedText,
                            unselectedTextColor = themeColors.mutedText,
                            indicatorColor = themeColors.navIndicator
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(innerPadding)
        ) {
            when (selectedIndex) {
                0 -> HomeScreen(
                    habits = habits,
                    onAddHabitClick = {
                        showAddHabitScreen = true
                    },
                    onHabitCheckedChange = { habit ->
                        homeViewModel.toggleHabit(habit)
                    },
                    onDeleteHabit = { habitId ->
                        homeViewModel.deleteHabit(habitId)
                    }
                )

                1 -> CoachScreen(
                    habits = habits,
                    feedMessages = coachMessages
                )
                2 -> ProgressScreen(
                    habits = habits
                )
                3 -> ProfileScreen(
                    onAddWidgetClick = {
                        RiseWidgetProvider.requestPinWidget(context)
                    },
                    selectedThemeMode = RiseThemeSettings.selectedThemeMode,
                    onThemeModeChange = { themeMode ->
                        RiseThemeSettings.saveThemeMode(context, themeMode)
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderTab(title: String) {
    val colors = LocalRiseColors.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = colors.primaryText
        )
    }
}
