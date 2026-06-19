package com.cengizhan.rise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.cengizhan.rise.core.LocalRiseColors
import com.cengizhan.rise.core.RiseThemeSettings
import androidx.core.content.ContextCompat
import com.cengizhan.rise.core.navigation.RiseNavigation
import com.cengizhan.rise.ui.theme.RiseTheme
import com.cengizhan.rise.widget.RiseWidgetProvider
import com.cengizhan.rise.worker.DayStatusWorker

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        RiseWidgetProvider.updateAllWidgets(this)
        DayStatusWorker.scheduleDailyWork(this)
        RiseThemeSettings.selectedThemeMode = RiseThemeSettings.loadThemeMode(this)

        setContent {
            val themeMode = RiseThemeSettings.selectedThemeMode
            val colors = RiseThemeSettings.colorsFor(themeMode)

            CompositionLocalProvider(LocalRiseColors provides colors) {
                RiseTheme(
                    darkTheme = themeMode != RiseThemeSettings.THEME_LIGHT
                ) {
                    RiseNavigation()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val isGranted =
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            notificationPermissionLauncher.launch(permission)
        }
    }
}
