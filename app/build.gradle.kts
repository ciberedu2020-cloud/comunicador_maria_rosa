import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// The bot token lives in telegram.properties (never committed). It is baked
// into the debug/release build: acceptable for a family app on one device,
// but it means the APK itself must not be shared.
val telegramProps = Properties().apply {
    val file = rootProject.file("telegram.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "cat.mariarosa.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "cat.mariarosa.app"
        // 29 = Android 10: the first version that records voice as Ogg/Opus,
        // the format Telegram accepts as a voice message.
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "0.1"
        buildConfigField("String", "TELEGRAM_TOKEN", "\"${telegramProps.getProperty("token", "")}\"")
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

    buildFeatures {
        compose = true
        // So the version can be shown on screen: without a cable it is the
        // only way to tell which build is on the device.
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
