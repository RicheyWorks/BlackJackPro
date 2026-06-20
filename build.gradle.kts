/*
 * BlackJack Pro — root build file
 *
 * Modules:
 *   :core         pure rules, no UI deps (Card, Hand, Shoe, Engine,
 *                 BasicStrategy, Achievement, GameSettings, SaveManager)
 *   :swing        original desktop Swing build (themes, plugins, music, SFX,
 *                 SteamBridge). Uses :core.
 *   :gdx-core     libGDX cross-platform game logic. Uses :core. Used by
 *                 :gdx-desktop and :android.
 *   :gdx-desktop  libGDX LWJGL3 desktop launcher. Verifies the libGDX UI
 *                 before pushing to Android.
 *   :android      libGDX Android launcher. Produces an APK.
 *
 * Common targets:
 *   gradlew :swing:run                    Swing desktop (the existing build)
 *   gradlew :gdx-desktop:run              libGDX desktop preview
 *   gradlew :android:assembleDebug        Android APK to android/build/outputs/
 *   gradlew :swing:jpackage               Native installers (Phase 5)
 *   gradlew test                          Run every test suite
 *
 * One-time setup:
 *   gradle wrapper --gradle-version 8.10
 *   Android: set ANDROID_HOME or sdk.dir in local.properties (Android Studio
 *            installs the SDK automatically).
 */

plugins {
    // Root declares no plugins; subprojects apply their own.
}

allprojects {
    group = "com.richeyworks"
    version = "0.3.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
    }
}
