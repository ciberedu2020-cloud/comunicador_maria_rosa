import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// The bot token lives in telegram.properties (never committed). It is baked
// into the DEBUG build only, for development: that APK must not be shared.
// The release build carries no token: each phone gets its own bot from the guide.
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
        versionName = "1.0"
    }

    // The signing key and its password live in keystore.properties (never
    // committed). Without that file the release bundle is built unsigned.
    val keystoreProps = Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "TELEGRAM_TOKEN", "\"${telegramProps.getProperty("token", "")}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "TELEGRAM_TOKEN", "\"\"")
            if (keystoreProps.isNotEmpty()) signingConfig = signingConfigs.getByName("release")
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
