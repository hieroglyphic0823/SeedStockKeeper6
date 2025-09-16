import java.util.Properties

// ファイルの先頭、androidブロックの外で読み込む
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val geminiKey: String = localProperties.getProperty("GEMINI_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.googleServices)
}

android {
    namespace = "com.example.seedstockkeeper6"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.seedstockkeeper6"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        buildConfig = true
        compose = true
        mlModelBinding = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Firebase（BoM で統一）
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth.ktx)

    // Google サインイン（Credential Manager + Google ID）
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.base)
    implementation(libs.play.services.tasks)

    // 画像/AI/その他
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.generativeai)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lottie.compose)

    // ML Kit / TFLite
    implementation("com.google.mlkit:object-detection:17.0.2") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite-support")
        exclude(group = "org.tensorflow", module = "tensorflow-lite-support-api")
    }
    implementation(libs.tflite)
    implementation(libs.tflite.api)
    implementation(libs.tflite.task.vision)
    implementation(libs.tensorflowLiteMetadata)

    // desugar
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.accompanist.systemuicontroller)
    
    // Test dependencies
    testImplementation(libs.androidx.test.ext.junit)
}
