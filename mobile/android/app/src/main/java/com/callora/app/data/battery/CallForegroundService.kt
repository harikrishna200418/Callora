package com.callora.app.data.battery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.callora.app.domain.usecase.BatteryMonitorUseCase
import com.callora.app.domain.usecase.CallTerminationController
import com.callora.app.data.repository.UserSettingsRepository
import com.callora.app.data.local.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallForegroundService : Service() {

    @Inject
    lateinit var batteryMonitorUseCase: BatteryMonitorUseCase

    @Inject
    lateinit var callTerminationController: CallTerminationController

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var tokenManager: TokenManager

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                
                if (level != -1 && scale != -1) {
                    val batteryPct = (level * 100) / scale
                    
                    val settings = userSettingsRepository.getSettingsOrDefault()
                    
                    batteryMonitorUseCase.onBatteryChanged(
                        level = batteryPct,
                        warningThreshold = settings.warningBatteryThreshold,
                        criticalThreshold = settings.criticalBatteryThreshold
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        CoroutineScope(Dispatchers.Main).launch {
            callTerminationController.terminationEvents.collect {
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        // Reset the state machine for the new call
        batteryMonitorUseCase.reset()
        
        // Start listening to battery changes
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        // Start reacting to state changes
        callTerminationController.startMonitoring()
        
        // Fetch latest settings from backend
        CoroutineScope(Dispatchers.IO).launch {
            tokenManager.getUserId()?.let { userId ->
                userSettingsRepository.fetchSettings(userId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        callTerminationController.stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CallServiceChannel",
                "Active Call",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "CallServiceChannel")
            .setContentTitle("Callora")
            .setContentText("Active call in progress")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .build()
    }
}
