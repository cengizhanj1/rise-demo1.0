package com.cengizhan.rise.core

object CoachNotificationSettings {
    const val KEY_GENERAL_DAILY_REMINDER = "generalDailyReminderEnabled"
    const val KEY_GENERAL_NO_ACTIVITY_REMINDER = "generalNoActivityReminderEnabled"
    const val KEY_GENERAL_THREE_DAY_COMEBACK = "generalThreeDayComebackEnabled"
    const val KEY_GENERAL_FIVE_DAY_COMEBACK = "generalFiveDayComebackEnabled"

    const val KEY_COACH_HABIT_ADDED = "coachHabitAddedEnabled"
    const val KEY_COACH_HABIT_COMPLETED = "coachHabitCompletedEnabled"
    const val KEY_COACH_DAILY_COMPLETION = "coachDailyCompletionEnabled"
    const val KEY_COACH_DAILY_MOTIVATION = "coachDailyMotivationEnabled"
    const val KEY_COACH_PAST_SUCCESS = "coachPastSuccessEnabled"
    const val KEY_COACH_MOTIVATION_LETTER = "coachMotivationLetterEnabled"

    const val TYPE_HABIT_ADDED = "habit_added"
    const val TYPE_HABIT_COMPLETED = "habit_completed"
    const val TYPE_DAILY_COMPLETED = "daily_completed"
    const val TYPE_DAILY_MOTIVATION = "daily_motivation"
    const val TYPE_PAST_SUCCESS = "past_success"
    const val TYPE_COMEBACK_3_DAY = "comeback_3_day"
    const val TYPE_COMEBACK_5_DAY = "comeback_5_day"
    const val TYPE_MOTIVATION_LETTER = "motivation_letter"

    const val KEY_LAST_NO_ACTIVITY_DATE = "lastNoActivityReminderDate"
    const val KEY_LAST_THREE_DAY_COMEBACK_DATE = "lastThreeDayComebackDate"
    const val KEY_LAST_FIVE_DAY_COMEBACK_DATE = "lastFiveDayComebackDate"
}
