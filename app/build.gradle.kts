import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)  version "2.2.21"
    id("com.google.gms.google-services")
}

val localProperties: Properties by lazy {
    val properties = Properties()

    // Use layout.projectDirectory to get the project root directory as a Path
    val localPropertiesFile = layout.projectDirectory.file("local.properties").asFile

    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use {
            properties.load(it)
        }
    }
    properties
}

android {
    namespace = "com.gujju.thoughts"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gujju.thoughts"
        minSdk = 24
        targetSdk = 36
        versionCode = 23
        versionName = "1.22"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "ADMOB_APP_ID",
            "\"${localProperties.getProperty("ADMOB_APP_ID")}\"")
        buildConfigField("String", "ADMOB_BANNER_ID",
            "\"${localProperties.getProperty("ADMOB_BANNER_ID")}\"")
        buildConfigField("String", "ADMOB_FULL_ID",
            "\"${localProperties.getProperty("ADMOB_FULL_ID")}\"")
        buildConfigField("String", "ADMOB_OPENAD_ID",
            "\"${localProperties.getProperty("ADMOB_OPENAD_ID")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation (libs.jetbrains.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.play.services.ads)
    implementation (libs.androidx.lifecycle.process)
    implementation(libs.user.messaging.platform)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation (libs.androidx.compose.ui.tooling)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.navigation.compose)
}