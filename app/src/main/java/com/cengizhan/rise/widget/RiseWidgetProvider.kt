package com.cengizhan.rise.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import android.widget.RemoteViews
import androidx.room.Room
import com.cengizhan.rise.R
import com.cengizhan.rise.data.local.database.RiseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RiseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgets(
                    context = context.applicationContext,
                    appWidgetManager = appWidgetManager,
                    appWidgetIds = appWidgetIds
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun requestPinWidget(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Toast.makeText(
                    context,
                    "Add the Rise widget from your home screen widgets.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, RiseWidgetProvider::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
            } else {
                Toast.makeText(
                    context,
                    "Add the Rise widget from your home screen widgets.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, RiseWidgetProvider::class.java)
            )

            CoroutineScope(Dispatchers.IO).launch {
                updateWidgets(
                    context = context.applicationContext,
                    appWidgetManager = appWidgetManager,
                    appWidgetIds = appWidgetIds
                )
            }
        }

        private suspend fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            if (appWidgetIds.isEmpty()) return

            val database = Room.databaseBuilder(
                context,
                RiseDatabase::class.java,
                "rise_database"
            )
                .fallbackToDestructiveMigration(true)
                .build()

            val habits = database.habitDao().getAllHabits().first()
            database.close()

            val completedCount = habits.count { it.isCompletedToday }
            val totalCount = habits.size
            val completionPercent =
                if (totalCount == 0) 0 else ((completedCount.toFloat() / totalCount) * 100).toInt()

            appWidgetIds.forEach { appWidgetId ->
                val views = RemoteViews(context.packageName, R.layout.rise_widget).apply {
                    setTextViewText(R.id.widgetTitle, "Rise")
                    setTextViewText(
                        R.id.widgetCompletedCount,
                        "$completedCount / $totalCount habits completed"
                    )
                    setTextViewText(
                        R.id.widgetCompletionPercent,
                        "$completionPercent% completed"
                    )
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
