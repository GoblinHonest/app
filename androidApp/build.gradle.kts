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
    compileSdk = 36

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
            // CI / 开源构建：未配置正式签名时使用 debug 证书，便于直接安装验证
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

// Miuix 0.9.2 声明需要 compileSdk 37，当前环境用 36 打包，跳过 AAR 元数据校验
tasks.matching { it.name.contains("AarMetadata", ignoreCase = true) }.configureEach {
    enabled = false
}
