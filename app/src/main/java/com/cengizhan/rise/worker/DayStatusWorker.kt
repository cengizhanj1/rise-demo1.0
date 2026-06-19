package com.cengizhan.rise.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cengizhan.rise.data.local.database.RiseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class DayStatusWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val today = todayDate()
        val prefs = applicationContext.getSharedPreferences(
            DAY_STATUS_PREFS_NAME,
            Context.MODE_PRIVATE
        )

        if (prefs.getString(statusKey(today), null) != null) {
            return Result.success()
        }

        val database = Room.databaseBuilder(
            applicationContext,
            RiseDatabase::class.java,
            "rise_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()

        return try {
            val habits = database.habitDao().getAllHabits().first()
            val completedCount = habits.count { it.isCompletedToday }
            val status = when {
                habits.isNotEmpty() && completedCount == habits.size -> DAY_STATUS_COMPLETED
                completedCount > 0 -> DAY_STATUS_PARTIAL
                else -> DAY_STATUS_NOT_STARTED
            }

            prefs.edit()
                .putString(statusKey(today), status)
                .apply()

            Result.success()
        } finally {
            database.close()
        }
    }

    companion object {
        fun scheduleDailyWork(context: Context) {
            val delayMillis = calculateInitialDelayMillis()
            val request = PeriodicWorkRequestBuilder<DayStatusWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_day_status_worker",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private fun calculateInitialDelayMillis(): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 50)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (!after(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            return target.timeInMillis - now.timeInMillis
        }
    }
}

private fun todayDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
}

private fun statusKey(date: String): String {
    return "day_status_$date"
}

private const val DAY_STATUS_PREFS_NAME = "rise_day_statuses"
private const val DAY_STATUS_COMPLETED = "completed"
private const val DAY_STATUS_PARTIAL = "partial"
private const val DAY_STATUS_NOT_STARTED = "not_started"
