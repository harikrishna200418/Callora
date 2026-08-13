buildscript {
    ext {
        compose_ui_version = '1.6.8'
        hilt_version = '2.51.1'
        room_version = '2.6.1'
        retrofit_version = '2.11.0'
    }
}

plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}
