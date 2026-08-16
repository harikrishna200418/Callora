package com.callora.app.data.repository

import com.callora.app.data.local.TokenManager
import com.callora.app.data.remote.api.UserSettingsApi
import com.callora.app.data.remote.api.UserSettingsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepository @Inject constructor(
    private val api: UserSettingsApi,
    private val tokenManager: TokenManager
) {
    private val _settings = MutableStateFlow<UserSettingsDto?>(null)
    val settings: StateFlow<UserSettingsDto?> = _settings.asStateFlow()

    suspend fun fetchSettings(userId: String): Result<UserSettingsDto> {
        return try {
            val res = api.getUserSettings(userId)
            _settings.value = res
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Returns cached settings, falling back to safe defaults
    fun getSettingsOrDefault(): UserSettingsDto {
        return _settings.value ?: UserSettingsDto(
            userId = "",
            batteryProtectionEnabled = true,
            automaticCallEndEnabled = true,
            criticalBatteryThreshold = 7, // fallback
            warningBatteryThreshold = 10, // fallback
            readReceiptsEnabled = true,
            lastSeenVisibility = "EVERYONE",
            profileVisibility = "EVERYONE"
        )
    }
}
