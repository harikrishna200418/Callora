package com.callora.app.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Body

data class UserSettingsDto(
    val userId: String,
    val batteryProtectionEnabled: Boolean,
    val automaticCallEndEnabled: Boolean,
    val criticalBatteryThreshold: Int,
    val warningBatteryThreshold: Int,
    val readReceiptsEnabled: Boolean,
    val lastSeenVisibility: String,
    val profileVisibility: String
)

interface UserSettingsApi {
    
    @GET("api/users/{id}/settings")
    suspend fun getUserSettings(@Path("id") userId: String): UserSettingsDto

    @PUT("api/users/{id}/settings")
    suspend fun updateSettings(
        @Path("id") userId: String,
        @Body settings: UserSettingsDto
    ): UserSettingsDto
}
