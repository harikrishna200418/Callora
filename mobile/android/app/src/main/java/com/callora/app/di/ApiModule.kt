package com.callora.app.di

import com.callora.app.data.remote.api.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserSettingsApi(retrofit: Retrofit): com.callora.app.data.remote.api.UserSettingsApi {
        return retrofit.create(com.callora.app.data.remote.api.UserSettingsApi::class.java)
    }
}
