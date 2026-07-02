plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()

    jvm("desktop")

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation(libs.miuix.ui)
                implementation(libs.miuix.preference)
                implementation(libs.miuix.icons)
                implementation(libs.miuix.blur)
                implementation(libs.miuix.squircle)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.kamel.image)
                implementation(libs.androidx.navigation.compose)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation("androidx.media3:media3-exoplayer:1.5.1")
                implementation("androidx.media3:media3-ui:1.5.1")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android {
    namespace = "com.stark.miuix.shared"
    compileSdk = 37

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
