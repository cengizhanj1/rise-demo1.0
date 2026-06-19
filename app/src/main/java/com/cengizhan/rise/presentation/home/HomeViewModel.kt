package com.cengizhan.rise.presentation.home

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.cengizhan.rise.core.CoachNotificationSettings
import com.cengizhan.rise.core.RiseThemeSettings
import com.cengizhan.rise.data.local.database.RiseDatabase
import com.cengizhan.rise.data.local.entity.CoachMessageEntity
import com.cengizhan.rise.data.local.entity.HabitEntity
import com.cengizhan.rise.data.repository.HabitRepositoryImpl
import com.cengizhan.rise.notification.HabitReminderReceiver
import com.cengizhan.rise.presentation.habit.MILESTONE_SEPARATOR
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application,
        RiseDatabase::class.java,
        "rise_database"
    )
        .fallbackToDestructiveMigration(true)
        .build()

    private val repository = HabitRepositoryImpl(
        database.habitDao()
    )
    private val coachMessageDao = database.coachMessageDao()
    private val settingsPrefs = application.getSharedPreferences(
        RiseThemeSettings.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _habits =
        MutableStateFlow<List<HabitEntity>>(emptyList())

    val habits: StateFlow<List<HabitEntity>> =
        _habits.asStateFlow()

    private val _coachMessages =
        MutableStateFlow<List<CoachMessageEntity>>(emptyList())

    val coachMessages: StateFlow<List<CoachMessageEntity>> =
        _coachMessages.asStateFlow()

    init {
        cleanupOldCoachMessages()

        coachMessageDao.observeMessagesSince(System.currentTimeMillis() - COACH_FEED_RETENTION_MILLIS)
            .onEach { messages ->
                _coachMessages.value = messages
            }
            .launchIn(viewModelScope)

        repository.getAllHabits()
            .onEach { habits ->
                _habits.value = habits
                resetHabitsForNewDay(habits)
                generateDailyCoachMessages(habits)
            }
            .launchIn(viewModelScope)
    }

    fun addHabit(
        title: String,
        category: String,
        reminderTime: String? = null
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    title = title,
                    category = category,
                    reminderTime = reminderTime,
                    startDateMillis = System.currentTimeMillis(),
                    milestoneMessages = builtInMilestoneMessagesFor(title)
                )
            )

            addCoachMessageIfAllowed(
                title = "New habit added",
                body = "You added $title. Small systems create big results.",
                type = CoachNotificationSettings.TYPE_HABIT_ADDED,
                preferenceKey = CoachNotificationSettings.KEY_COACH_HABIT_ADDED,
                oncePerDay = false
            )

            if (reminderTime != null) {
                val requestCode = createReminderRequestCode()
                scheduleDailyReminder(
                    reminderTime = reminderTime,
                    requestCode = requestCode
                )
                scheduleTestReminder(requestCode + TEST_REMINDER_OFFSET)
            }
        }
    }

    fun addCustomHabit(
        title: String,
        reminderTime: String? = null,
        milestoneMessages: String? = null
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    title = title,
                    category = "Custom",
                    reminderTime = reminderTime,
                    startDateMillis = System.currentTimeMillis(),
                    isCustomProgram = true,
                    milestoneMessages = milestoneMessages
                )
            )

            addCoachMessageIfAllowed(
                title = "New habit added",
                body = "You added $title. Small systems create big results.",
                type = CoachNotificationSettings.TYPE_HABIT_ADDED,
                preferenceKey = CoachNotificationSettings.KEY_COACH_HABIT_ADDED,
                oncePerDay = false
            )

            if (reminderTime != null) {
                val requestCode = createReminderRequestCode()
                scheduleDailyReminder(
                    reminderTime = reminderTime,
                    requestCode = requestCode
                )
                scheduleTestReminder(requestCode + TEST_REMINDER_OFFSET)
            }
        }
    }

    fun toggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val today = getTodayDate()

            if (habit.isCompletedToday) {
                repository.updateHabit(
                    habit.copy(
                        isCompletedToday = false,
                        streakCount = (habit.streakCount - 1).coerceAtLeast(0),
                        lastCompletedDate = null
                    )
                )
            } else {
                val newStreakCount = habit.streakCount + 1

                repository.updateHabit(
                    habit.copy(
                        isCompletedToday = true,
                        streakCount = newStreakCount,
                        bestStreak = maxOf(habit.bestStreak, newStreakCount),
                        lastCompletedDate = today
                    )
                )

                addCoachMessageIfAllowed(
                    title = "Habit completed",
                    body = "You completed ${habit.title}. Momentum is building.",
                    type = CoachNotificationSettings.TYPE_HABIT_COMPLETED,
                    preferenceKey = CoachNotificationSettings.KEY_COACH_HABIT_COMPLETED,
                    oncePerDay = false
                )

                val habitsAfterToggle = _habits.value.map { item ->
                    if (item.id == habit.id) {
                        item.copy(isCompletedToday = true)
                    } else {
                        item
                    }
                }
                if (habitsAfterToggle.isNotEmpty() && habitsAfterToggle.all { it.isCompletedToday }) {
                    addCoachMessageIfAllowed(
                        title = "Perfect day",
                        body = "Congratulations! You completed today's goals.",
                        type = CoachNotificationSettings.TYPE_DAILY_COMPLETED,
                        preferenceKey = CoachNotificationSettings.KEY_COACH_DAILY_COMPLETION,
                        oncePerDay = true
                    )
                }
            }
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    private fun resetHabitsForNewDay(habits: List<HabitEntity>) {
        val today = getTodayDate()
        val yesterday = getYesterdayDate()

        habits.forEach { habit ->
            val lastCompletedDate = habit.lastCompletedDate
            val missedMoreThanOneDay =
                lastCompletedDate != null && lastCompletedDate != yesterday

            if (lastCompletedDate != null &&
                lastCompletedDate != today &&
                (habit.isCompletedToday || missedMoreThanOneDay)
            ) {
                viewModelScope.launch {
                    repository.updateHabit(
                        habit.copy(
                            isCompletedToday = false,
                            streakCount =
                                if (lastCompletedDate == yesterday)
                                    habit.streakCount
                                else
                                    0,
                            lastCompletedDate =
                                if (lastCompletedDate == yesterday)
                                    lastCompletedDate
                                else
                                    null
                        )
                    )
                }
            }
        }
    }

    private fun cleanupOldCoachMessages() {
        viewModelScope.launch {
            coachMessageDao.deleteOlderThan(System.currentTimeMillis() - COACH_FEED_RETENTION_MILLIS)
        }
    }

    private fun generateDailyCoachMessages(habits: List<HabitEntity>) {
        if (habits.isEmpty()) return

        viewModelScope.launch {
            addCoachMessageIfAllowed(
                title = "Daily Motivation",
                body = "Discipline is built one small action at a time.",
                type = CoachNotificationSettings.TYPE_DAILY_MOTIVATION,
                preferenceKey = CoachNotificationSettings.KEY_COACH_DAILY_MOTIVATION,
                oncePerDay = true
            )

            val proofHabit = habits
                .filter { it.bestStreak > 0 || it.streakCount > 0 }
                .maxByOrNull { maxOf(it.bestStreak, it.streakCount) }
            if (proofHabit != null) {
                addCoachMessageIfAllowed(
                    title = "You have proof",
                    body = "You completed ${proofHabit.title} before. You can do it again today.",
                    type = CoachNotificationSettings.TYPE_PAST_SUCCESS,
                    preferenceKey = CoachNotificationSettings.KEY_COACH_PAST_SUCCESS,
                    oncePerDay = true
                )
            }

            generateGeneralReminderMessages(habits)
        }
    }

    private suspend fun generateGeneralReminderMessages(habits: List<HabitEntity>) {
        if (!settingsPrefs.getBoolean(RiseThemeSettings.KEY_GENERAL_NOTIFICATIONS, true)) return
        if (!isNowInAllowedRange(
                settingsPrefs.getString(
                    RiseThemeSettings.KEY_GENERAL_NOTIFICATION_START_TIME,
                    "09:00"
                ) ?: "09:00",
                settingsPrefs.getString(
                    RiseThemeSettings.KEY_GENERAL_NOTIFICATION_END_TIME,
                    "22:00"
                ) ?: "22:00"
            )
        ) return

        val today = getTodayDate()
        val noHabitsCompletedToday = habits.none { it.isCompletedToday }
        if (noHabitsCompletedToday &&
            settingsPrefs.getBoolean(CoachNotificationSettings.KEY_GENERAL_NO_ACTIVITY_REMINDER, true) &&
            settingsPrefs.getString(CoachNotificationSettings.KEY_LAST_NO_ACTIVITY_DATE, null) != today
        ) {
            coachMessageDao.insertMessage(
                CoachMessageEntity(
                    title = "Start with one action",
                    body = "No habits are completed yet today. One small win can restart your rhythm.",
                    type = "general_no_activity",
                    createdAtMillis = System.currentTimeMillis()
                )
            )
            settingsPrefs.edit()
                .putString(CoachNotificationSettings.KEY_LAST_NO_ACTIVITY_DATE, today)
                .apply()
        }

        val inactiveDays = calculateInactiveDays(habits)
        if (inactiveDays >= 3 &&
            settingsPrefs.getBoolean(CoachNotificationSettings.KEY_GENERAL_THREE_DAY_COMEBACK, true) &&
            settingsPrefs.getString(CoachNotificationSettings.KEY_LAST_THREE_DAY_COMEBACK_DATE, null) != today
        ) {
            coachMessageDao.insertMessage(
                CoachMessageEntity(
                    title = "Comeback Reminder",
                    body = "Three quiet days do not define you. Restart with one habit today.",
                    type = CoachNotificationSettings.TYPE_COMEBACK_3_DAY,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
            settingsPrefs.edit()
                .putString(CoachNotificationSettings.KEY_LAST_THREE_DAY_COMEBACK_DATE, today)
                .apply()
        }

        if (inactiveDays >= 5 &&
            settingsPrefs.getBoolean(CoachNotificationSettings.KEY_GENERAL_FIVE_DAY_COMEBACK, true) &&
            settingsPrefs.getString(CoachNotificationSettings.KEY_LAST_FIVE_DAY_COMEBACK_DATE, null) != today
        ) {
            coachMessageDao.insertMessage(
                CoachMessageEntity(
                    title = "Strong Comeback Reminder",
                    body = "Five inactive days is a signal, not a sentence. Rebuild the system today.",
                    type = CoachNotificationSettings.TYPE_COMEBACK_5_DAY,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
            settingsPrefs.edit()
                .putString(CoachNotificationSettings.KEY_LAST_FIVE_DAY_COMEBACK_DATE, today)
                .apply()
        }
    }

    private suspend fun addCoachMessageIfAllowed(
        title: String,
        body: String,
        type: String,
        preferenceKey: String,
        oncePerDay: Boolean
    ) {
        if (!settingsPrefs.getBoolean(RiseThemeSettings.KEY_SMART_COACH_NOTIFICATIONS, true)) return
        if (!settingsPrefs.getBoolean(preferenceKey, true)) return
        if (!isNowInAllowedRange(
                settingsPrefs.getString(
                    RiseThemeSettings.KEY_SMART_COACH_NOTIFICATION_START_TIME,
                    "10:00"
                ) ?: "10:00",
                settingsPrefs.getString(
                    RiseThemeSettings.KEY_SMART_COACH_NOTIFICATION_END_TIME,
                    "21:00"
                ) ?: "21:00"
            )
        ) return

        val startOfDay = startOfTodayMillis()
        val now = System.currentTimeMillis()
        if (oncePerDay && coachMessageDao.countMessagesForTypeBetween(type, startOfDay, now) > 0) {
            return
        }

        coachMessageDao.insertMessage(
            CoachMessageEntity(
                title = title,
                body = body,
                type = type,
                createdAtMillis = now
            )
        )
    }

    private fun calculateInactiveDays(habits: List<HabitEntity>): Int {
        val lastCompletedDates = habits.mapNotNull { it.lastCompletedDate }
        if (lastCompletedDates.isEmpty()) return 0

        val formatter = dateFormat()
        val latest = lastCompletedDates.maxOrNull() ?: return 0
        val latestDate = formatter.parse(latest) ?: return 0
        val todayDate = formatter.parse(getTodayDate()) ?: return 0
        val dayMillis = 24L * 60L * 60L * 1000L
        return ((todayDate.time - latestDate.time) / dayMillis).toInt().coerceAtLeast(0)
    }

    private fun isNowInAllowedRange(startTime: String, endTime: String): Boolean {
        val now = Calendar.getInstance()
        val start = parseTimeToday(startTime)
        val end = parseTimeToday(endTime)

        return if (start.timeInMillis <= end.timeInMillis) {
            now.timeInMillis in start.timeInMillis..end.timeInMillis
        } else {
            now.timeInMillis >= start.timeInMillis || now.timeInMillis <= end.timeInMillis
        }
    }

    private fun parseTimeToday(time: String): Calendar {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun builtInMilestoneMessagesFor(title: String): String? {
        val messages = when (title) {
            "Avoid Unhealthy Sugar" -> avoidUnhealthySugarMilestones
            "Sleep 8 Hours" -> sleepEightHoursMilestones
            "10,000 Steps" -> tenThousandStepsMilestones
            "No Smoke" -> noSmokeMilestones
            "No Alcohol" -> noAlcoholMilestones
            else -> return null
        }

        return messages.joinToString(MILESTONE_SEPARATOR) { message ->
            Uri.encode(message)
        }
    }

    private fun getTodayDate(): String {
        return dateFormat().format(Calendar.getInstance().time)
    }

    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat().format(calendar.time)
    }

    private fun dateFormat(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleDailyReminder(
        reminderTime: String,
        requestCode: Int
    ) {
        val parts = reminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return

        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d(
            "RiseReminder",
            "Creating PendingIntent receiver=${HabitReminderReceiver::class.java.name}, requestCode=$requestCode"
        )

        val intent = Intent(
            getApplication(),
            HabitReminderReceiver::class.java
        )
            .putExtra(HabitReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            .putExtra(HabitReminderReceiver.EXTRA_REMINDER_HOUR, hour)
            .putExtra(HabitReminderReceiver.EXTRA_REMINDER_MINUTE, minute)
            .putExtra(HabitReminderReceiver.EXTRA_REQUEST_CODE, requestCode)

        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val canScheduleExact =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        Log.d(
            "RiseReminder",
            "Scheduling reminder for $reminderTime at ${alarmTime.timeInMillis}, now=${now.timeInMillis}, future=${alarmTime.timeInMillis > now.timeInMillis}, exact=$canScheduleExact"
        )

        scheduleAlarm(
            alarmManager = alarmManager,
            triggerAtMillis = alarmTime.timeInMillis,
            pendingIntent = pendingIntent,
            canScheduleExact = canScheduleExact
        )
    }

    private fun scheduleTestReminder(requestCode: Int) {
        val triggerAtMillis = System.currentTimeMillis() + TEST_REMINDER_DELAY_MILLIS
        val intent = Intent(
            getApplication(),
            HabitReminderReceiver::class.java
        )
            .putExtra(HabitReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            .putExtra(HabitReminderReceiver.EXTRA_IS_TEST_REMINDER, true)

        Log.d(
            "RiseReminder",
            "Creating TEST PendingIntent receiver=${HabitReminderReceiver::class.java.name}, requestCode=$requestCode"
        )

        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExact =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        Log.d(
            "RiseReminder",
            "Scheduling TEST reminder at $triggerAtMillis, now=${System.currentTimeMillis()}, exact=$canScheduleExact"
        )

        scheduleAlarm(
            alarmManager = alarmManager,
            triggerAtMillis = triggerAtMillis,
            pendingIntent = pendingIntent,
            canScheduleExact = canScheduleExact
        )
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
        canScheduleExact: Boolean
    ) {
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            Log.d("RiseReminder", "Exact alarms not allowed, using setAndAllowWhileIdle fallback")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun createReminderRequestCode(): Int {
        return abs(System.currentTimeMillis().hashCode())
    }

    companion object {
        private const val COACH_FEED_RETENTION_MILLIS = 24L * 60L * 60L * 1000L
        private const val TEST_REMINDER_DELAY_MILLIS = 10_000L
        private const val TEST_REMINDER_OFFSET = 50_000

        private val avoidUnhealthySugarMilestones = listOf(
            "🍬 You chose to avoid unhealthy sugar. Today you are reducing the blood sugar spikes and crashes that added sugar often causes.",
            "💧 Avoiding sugary drinks reduces unnecessary calorie intake and lowers acid exposure for your teeth.",
            "🎯 You completed your first day. Sugar cravings may appear as your brain begins adjusting its reward system.",
            "⚡ You may experience energy fluctuations. This transition period is common during the first few days.",
            "🧠 Sugar cravings can become stronger around this stage. Many people give up during this period.",
            "🍽️ You may begin noticing hunger and fullness signals more clearly.",
            "😁 Constant cravings for sweets may start becoming less frequent.",
            "🦷 Reducing sugary foods and drinks helps lower the risk of tooth decay.",
            "🏆 You completed your first week. You are no longer relying on motivation alone—you are building a behavior pattern.",
            "👅 Your taste preferences may begin changing. Foods that once seemed normal may now taste overly sweet.",
            "📈 Daily energy fluctuations may become less noticeable.",
            "🔥 You have limited added sugar for an entire month. This is a powerful step for weight management and metabolic health.",
            "⚖️ Many people report improved appetite control by this stage.",
            "🧬 Positive effects on blood sugar regulation and insulin sensitivity may begin becoming more noticeable.",
            "❤️ You have maintained this habit for a long time. Lower added sugar intake supports long-term heart health.",
            "🌱 This is no longer a temporary diet. It has become part of your lifestyle.",
            "🩺 Long-term reduction of added sugar may contribute to lowering risks associated with obesity, type 2 diabetes, and cardiovascular disease.",
            "🧠 Your brain and body are becoming increasingly adapted to sustainable energy rather than quick sugar rewards.",
            "💪 This habit is no longer something you actively force. It is becoming part of your identity.",
            "🛡️ Ten years of consistency has transformed not only your nutrition but also your decision-making patterns. Healthy choices are becoming automatic.",
            "👑 Small daily choices have compounded over many years. This is no longer a habit—it is part of who you are."
        )

        private val sleepEightHoursMilestones = listOf(
            "😴 You committed to getting enough sleep tonight. Quality sleep is an investment in tomorrow's energy.",
            "🌙 As bedtime approaches, reducing screen time and relaxing may help support better sleep quality.",
            "🏆 You completed your first night. Adequate sleep is one of the most important foundations of focus, energy, and daily performance.",
            "⚡ You are beginning to build a more consistent sleep schedule. Many people start feeling more refreshed in the morning after a few days.",
            "🧠 Preventing sleep debt accumulation may help support concentration and mental performance.",
            "📚 Adequate sleep plays an important role in learning and memory processes.",
            "💪 A large portion of physical recovery occurs during sleep. Keep going.",
            "❤️ Consistent sleep is an important habit for heart health, hormonal balance, and overall well-being.",
            "🏅 You have spent a full week prioritizing sleep. Many people notice improvements in energy levels during this stage.",
            "🌱 Two weeks of consistency completed. Daytime fatigue may begin decreasing as your sleep schedule stabilizes.",
            "📈 Twenty-one days completed. Healthy sleep is becoming part of your daily routine.",
            "✨ One month completed. Consistent sleep may positively influence productivity and daily performance.",
            "🧬 Adequate sleep may provide long-term support for immune function, metabolism, and cognitive performance.",
            "🚀 Three months completed. Many people begin realizing how much sleep quality affects overall quality of life.",
            "💎 Six months completed. Consistent sleep is becoming more than a goal—it is becoming a lifestyle.",
            "🏆 One full year of prioritizing sleep health. This is a powerful investment in both physical and mental well-being.",
            "🛡️ Two years completed. This habit continues building a strong foundation for long-term health and quality of life.",
            "🌱 Years of consistency have transformed sleep from a task into a natural daily behavior.",
            "🧠 Five years of prioritizing sleep. Rest is no longer something you try to do—it is part of who you are.",
            "🌟 Ten years completed. Long-term success is often built from small daily habits repeated consistently.",
            "👑 More than fifteen years of investing in sleep health. Thousands of nights of recovery, restoration, and renewal have compounded into one of the strongest health habits you can build."
        )

        private val tenThousandStepsMilestones = listOf(
            "🚶 You completed your first 10,000 steps. Daily movement supports heart health, circulation, and overall energy levels. If you are also maintaining a calorie deficit, you have taken the first step toward weight loss.",
            "🔥 You have stayed consistent for two days. Walking 10,000 steps can increase daily energy expenditure significantly. Combined with controlled nutrition, small changes may already be beginning.",
            "⚡ Your body is starting to adapt to a more active routine. Regular walking reduces sedentary time and supports overall fitness.",
            "📈 Four days completed. If you continue maintaining a calorie deficit, your chances of seeing visible progress by the end of the week increase.",
            "💪 Many people quit during the first week. You are continuing to build consistency. Movement is becoming a natural part of your day.",
            "🚶 You are close to completing your first full week. Regular walking supports leg strength, endurance, and overall mobility.",
            "🏅 One full week completed. If you are maintaining a calorie deficit, some people may lose around 0.5–1 kg during this period. Results vary between individuals.",
            "🔥 Two weeks completed. Many people maintaining a calorie deficit may lose around 1–2 kg by this point. More importantly, you are building a powerful routine.",
            "📊 Twenty-one days completed. Daily movement is beginning to become a habit. Combined with a calorie deficit, some people may see around 2–3 kg of weight loss.",
            "🌟 One month completed. Regular walking and controlled nutrition may lead to noticeable improvements in energy, endurance, and body composition.",
            "🚀 Two months completed. Consistency is creating momentum. Many people maintaining a calorie deficit may experience significant progress by this stage.",
            "💎 Three months completed. Regular walking and healthy nutrition can create visible physical changes and improved fitness.",
            "🏆 Six months completed. This is no longer a short-term challenge. You are building a long-term lifestyle change.",
            "🌱 Nine months completed. Regular movement is deeply integrated into your lifestyle. Many people notice significant improvements in endurance, mobility, and overall fitness by this stage.",
            "🌿 One year completed. You have invested heavily in your heart health, fitness, mobility, and long-term well-being.",
            "🛡️ Two years completed. Sustainable lifestyle habits are far more valuable than temporary diets. You are proving consistency works.",
            "🚀 Three years completed. Daily walking has become a long-term investment in your health. Consistency over years creates results that short-term motivation never can.",
            "💎 Four years completed. You are proving that sustainable habits outperform extreme plans. Movement is now part of your identity.",
            "🧠 Five years completed. Walking is no longer a task. It has become a natural part of your identity and daily routine.",
            "⭐ Ten years completed. This is no longer an exercise plan—it is simply how you live.",
            "👑 More than twenty years completed. Walking is now a permanent part of your lifestyle, health, and identity. The greatest transformations are often built from simple daily actions repeated for decades."
        )

        private val noSmokeMilestones = listOf(
            "🚭 You decided to stop smoking. Every smoke-free hour is a step toward a healthier future.",
            "🩸 Carbon monoxide levels in your blood begin to decrease while oxygen levels start improving.",
            "💨 Carbon monoxide levels continue returning toward normal levels.",
            "❤️ Blood pressure and heart rate may begin returning toward healthier ranges.",
            "🧹 Nicotine is largely eliminated from the body.",
            "⚡ Many people begin noticing small improvements in energy levels.",
            "🛡️ Antioxidant activity may start improving as your body recovers from smoking exposure.",
            "🧬 Your lungs and kidneys continue working to remove smoking-related toxins.",
            "🌱 The tiny cilia in your airways begin recovering and improving their cleaning function.",
            "😮‍💨 Breathing may start feeling easier during daily activities.",
            "📈 Lung function may begin showing measurable improvements.",
            "🏆 Many former smokers report improved endurance and easier breathing after sustained consistency.",
            "💨 Coughing and mucus production may begin decreasing.",
            "🌬️ Breathing difficulties may continue improving as recovery progresses.",
            "❤️ Lung function continues improving while stress on the respiratory system decreases.",
            "🛡️ Your immune system may benefit from continued recovery and reduced smoking exposure.",
            "💓 Risk of heart disease may be significantly lower compared to continued smoking.",
            "📉 Risk of several smoking-related diseases continues decreasing over time.",
            "🏅 Major cardiovascular risks may be substantially reduced compared to active smokers.",
            "🌟 Long-term recovery continues. The risk of many smoking-related illnesses may be much lower than when smoking regularly.",
            "👑 After many smoke-free years, your health profile may become similar to someone who never smoked in several important areas."
        )

        private val noAlcoholMilestones = listOf(
            "🍷 You decided to stop drinking alcohol. Your body immediately begins focusing on recovery instead of processing alcohol.",
            "💧 Alcohol is leaving your system. Some people may experience headaches, fatigue, or temporary discomfort during this adjustment period.",
            "⚡ Withdrawal-related symptoms such as irritability or restlessness may still occur. Your body is working to restore balance.",
            "🧠 Your brain is beginning to adapt to functioning without regular alcohol exposure.",
            "❤️ Blood pressure and heart rate may start moving toward healthier ranges.",
            "💪 Your body is spending less energy recovering from alcohol and more energy supporting normal functions.",
            "🏆 One week completed. Many people notice improved sleep quality, reduced bloating, and clearer thinking.",
            "🌱 Your digestive system may begin functioning more efficiently while your liver continues its recovery process.",
            "⚡ Many people report feeling more productive, energetic, and mentally clear at this stage.",
            "📈 Blood pressure, kidney function, and overall recovery may continue improving.",
            "✨ Alcohol-related calories are no longer part of your routine. Some people notice improvements in body composition and energy levels.",
            "🛡️ Your immune system may benefit from continued alcohol-free living.",
            "💎 Six months completed. Confidence, self-control, and emotional stability often improve with long-term consistency.",
            "🚀 Healthy habits become easier to maintain. Many people report fewer digestive issues and improved overall wellness.",
            "🌟 One year alcohol-free. This is often viewed as a major life milestone and a significant investment in long-term health.",
            "🏅 Two years completed. Alcohol-free living is becoming part of your identity rather than a daily challenge.",
            "🌱 Three years completed. Healthy choices are becoming automatic. Many people report greater clarity, stability, and confidence compared to when they started.",
            "❤️ Five years completed. Long-term alcohol-free living helps reduce exposure to many chronic health risks associated with alcohol.",
            "🧬 Ten years completed. You have protected your body from thousands of alcohol exposures. Long-term health is built through consistent choices repeated for years.",
            "🛡️ Fifteen years completed. Alcohol-free living is widely recognized as a lifestyle that supports liver health, cardiovascular health, and overall well-being.",
            "👑 More than twenty years alcohol-free. Your brain, liver, and body have benefited from decades of consistent healthy choices."
        )
    }
}
