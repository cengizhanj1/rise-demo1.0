package com.cengizhan.rise.notification

import android.annotation.SuppressLint
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cengizhan.rise.R
import java.util.Calendar

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("RiseReminder", "Reminder receiver triggered: ${HabitReminderReceiver::class.java.name}")
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("RiseReminder", "Notification permission not granted")
            return
        }

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to stay disciplined")
            .setContentText("Don't break your streak today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
        Log.d("RiseReminder", "Reminder notification shown with id=$notificationId")

        if (intent.getBooleanExtra(EXTRA_IS_TEST_REMINDER, false)) {
            Log.d("RiseReminder", "Test reminder fired; skipping daily reschedule")
        } else {
            scheduleNextDailyReminder(context, intent)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("RiseReminder", "Notification channel is ready")
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNextDailyReminder(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(EXTRA_REMINDER_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_REMINDER_MINUTE, -1)
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)

        if (hour !in 0..23 || minute !in 0..59 || requestCode == -1) {
            Log.d("RiseReminder", "Skipping next reminder schedule because extras are missing")
            return
        }

        val nextAlarmTime = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nextIntent = Intent(context, HabitReminderReceiver::class.java)
            .putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            .putExtra(EXTRA_REMINDER_HOUR, hour)
            .putExtra(EXTRA_REMINDER_MINUTE, minute)
            .putExtra(EXTRA_REQUEST_CODE, requestCode)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExact =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime.timeInMillis,
                pendingIntent
            )
        }

        Log.d(
            "RiseReminder",
            "Next daily reminder scheduled at ${nextAlarmTime.timeInMillis}, exact=$canScheduleExact"
        )
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_REMINDER_HOUR = "reminder_hour"
        const val EXTRA_REMINDER_MINUTE = "reminder_minute"
        const val EXTRA_REQUEST_CODE = "request_code"
        const val EXTRA_IS_TEST_REMINDER = "is_test_reminder"
    }
}
