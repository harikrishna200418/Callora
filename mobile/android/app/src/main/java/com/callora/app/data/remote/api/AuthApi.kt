package com.callora.app.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String, val email: String)
data class AuthResponse(val accessToken: String, val refreshToken: String, val userId: String, val username: String)

data class OtpRequest(val phoneNumber: String)
data class OtpVerifyRequest(val phoneNumber: String, val code: String)
data class OtpResponse(val success: Boolean, val message: String)

interface AuthApi {
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/otp/request")
    suspend fun requestOtp(@Body request: OtpRequest): Response<OtpResponse>

    @POST("api/auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<AuthResponse>
}
