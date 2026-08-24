package com.callora.app.domain.usecase

import android.content.Context
import android.util.Log
import com.callora.app.domain.model.BatteryState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class CallTerminationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryMonitorUseCase: BatteryMonitorUseCase
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var monitorJob: Job? = null

    private val _terminationEvents = MutableSharedFlow<Unit>()
    val terminationEvents = _terminationEvents.asSharedFlow()

    private val _warningEvents = MutableSharedFlow<Unit>()
    val warningEvents = _warningEvents.asSharedFlow()

    fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            batteryMonitorUseCase.batteryState.collect { state ->
                when (state) {
                    BatteryState.CRITICAL -> terminateCall()
                    BatteryState.LOW_BATTERY_WARNING -> showWarning()
                    BatteryState.NORMAL -> { /* Do nothing */ }
                }
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
    }

    private fun showWarning() {
        Log.w("CallTerminationCtrl", "Low Battery Warning: Displaying banner to user.")
        scope.launch {
            _warningEvents.emit(Unit)
        }
    }

    private fun terminateCall() {
        Log.e("CallTerminationCtrl", "CRITICAL BATTERY REACHED: Automatically terminating call.")
        scope.launch {
            _terminationEvents.emit(Unit)
        }
    }
}
