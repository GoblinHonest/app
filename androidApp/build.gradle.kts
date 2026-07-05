plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))
                // Media3 ExoPlayer for video playback
                implementation("androidx.media3:media3-exoplayer:1.5.1")
                implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
                implementation("androidx.media3:media3-exoplayer-dash:1.5.1")
                implementation("androidx.media3:media3-ui:1.5.1")
                implementation("androidx.activity:activity-compose:1.9.3")
            }
        }
    }
}

android {
    namespace = "com.stark.miuix"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.cinehub.app"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
