import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.application)
}

kotlin {
    androidTarget {
        compilations.all {
            compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.play.services.location)
            implementation(libs.androidx.navigation.compose)
            // Room + SQLCipher (Android only)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)
            implementation(libs.sqlcipher)
            implementation(libs.androidx.sqlite.ktx)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            // Lifecycle ViewModel (works on desktop too)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            // SQLite for desktop (SQLDelight or room-desktop)
            implementation(libs.sqlite.jdbc)
        }
    }
}

android {
    namespace = "com.gloam"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.gloam"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "com.gloam.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Gloam"
            packageVersion = "2.0.0"
            description = "A minimal, solar-timed journaling app with CBT prompts and mood tracking"
            copyright = "© 2025-2026 Gloam Contributors. MIT License."
            vendor = "Gloam"

            macOS {
                iconFile.set(project.file("desktopApp/icons/gloam.icns"))
                bundleID = "com.gloam.desktop"
            }
            windows {
                iconFile.set(project.file("desktopApp/icons/gloam.ico"))
                menuGroup = "Gloam"
            }
            linux {
                iconFile.set(project.file("desktopApp/icons/gloam.png"))
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("desktopApp/proguard-rules.pro"))
        }
    }
}
