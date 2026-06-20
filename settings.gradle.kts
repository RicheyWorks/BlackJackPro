rootProject.name = "blackjack-pro"

include(":core")
include(":swing")
include(":gdx-core")
include(":gdx-desktop")

// Android is optional: included when an SDK is configured locally.
// Desktop-only builds (Swing, libGDX desktop) work without it.
val androidSdkConfigured = file("local.properties").exists()
    || !System.getenv("ANDROID_HOME").isNullOrBlank()
if (androidSdkConfigured) {
    include(":android")
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application",
                "com.android.library" -> {
                    useModule("com.android.tools.build:gradle:${requested.version}")
                }
            }
        }
    }
}
