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

    /*
     * Compile every module as UTF-8, explicitly.
     *
     * javac falls back to the JVM's default charset when -encoding is absent.
     * The sources carry literal non-ASCII characters in player-facing strings
     * (em dashes, middle dots, stars, arrows), so under a non-UTF-8 default the
     * mangled bytes are baked into the class files as string constants and the
     * game ships visibly broken text. Verified: the same source compiles to
     * "Hand 1 — your move · $5" under UTF-8 and to mojibake under a
     * windows-1252 default.
     *
     * JDK 18+ defaults to UTF-8 (JEP 400) so this usually works by luck today,
     * but it is one -Dfile.encoding away from breaking, and the failure is
     * silent at build time — it only shows up on screen, on someone else's
     * machine.
     */
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        // Test JVMs need it too, or an assertion on a non-ASCII string can fail
        // only when the report is written.
        systemProperty("file.encoding", "UTF-8")
    }
}
