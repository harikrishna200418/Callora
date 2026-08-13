package com.callora.app.domain.usecase

import com.callora.app.domain.model.BatteryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryMonitorUseCase @Inject constructor() {

    private val _batteryState = MutableStateFlow(BatteryState.NORMAL)
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val _currentBatteryLevel = MutableStateFlow(100)
    val currentBatteryLevel: StateFlow<Int> = _currentBatteryLevel.asStateFlow()

    private var isTerminated = false

    fun onBatteryChanged(
        level: Int,
        warningThreshold: Int = 10,
        criticalThreshold: Int = 7
    ) {
        // Once critical/terminated, the state machine cannot be reversed for the duration of this call
        if (isTerminated) return

        _currentBatteryLevel.value = level

        when {
            level <= criticalThreshold -> {
                _batteryState.value = BatteryState.CRITICAL
                isTerminated = true
            }
            level <= warningThreshold -> {
                _batteryState.value = BatteryState.LOW_BATTERY_WARNING
            }
            else -> {
                _batteryState.value = BatteryState.NORMAL
            }
        }
    }

    fun reset() {
        isTerminated = false
        _batteryState.value = BatteryState.NORMAL
    }
}
