// Same arrangement as the Comunicador project: no Kotlin plugin here, since
// the Android plugin brings its own Kotlin support.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
