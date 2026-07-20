/*
 * android — libGDX Android launcher.
 *
 * Build the APK:    gradlew :android:assembleDebug
 *                   APK lands in android/build/outputs/apk/debug/
 *
 * Signed release:   gradlew :android:assembleRelease
 *                   (after configuring signingConfig below with a real keystore)
 */
/*
 * AGP and Gradle are version-locked. AGP 8.5 requires Gradle 8.7+, and the whole
 * AGP 8.x line pairs with Gradle 8.x — AGP 9.0 is the first release that accepts
 * Gradle 9 (and needs 9.1.0+). So the wrapper cannot move to Gradle 9 while this
 * stays on 8.5.2; the two have to be upgraded together.
 *
 * This is easy to miss because CI never builds the APK — it runs
 * :gdx-desktop:classes as the compile check for the mobile port. A Gradle 9
 * wrapper bump would go green in CI and break assembleDebug only on a developer
 * machine. Matrix: https://developer.android.com/build/releases/about-agp
 */
plugins {
    id("com.android.application") version "8.5.2"
}

val gdxVersion = "1.12.1"
val natives = configurations.create("natives")

android {
    namespace = "com.richeyworks.blackjack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.richeyworks.blackjack"
        // API 26 (Android 8.0), not 21: :core persists saves, settings, and
        // achievements through java.nio.file, which only exists natively from
        // 26. Below that it has to be emulated via desugar_jdk_libs_nio, and
        // Google documents that some nio APIs cannot be emulated and throw
        // UnsupportedOperationException -- AtomicFiles depends on exactly those
        // (Files.move with ATOMIC_MOVE, createTempFile), so the crash-safe
        // writes the security audit added would fail on precisely the old,
        // low-memory devices most likely to kill the app mid-write.
        minSdk        = 26
        targetSdk     = 34
        versionCode   = 1
        versionName   = "0.3.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Required for Java 17 -> dex desugaring of records, switch expressions, etc.
        isCoreLibraryDesugaringEnabled = true
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // To enable a signed release, drop a keystore at the path below and
            // set the environment variables before running `assembleRelease`:
            //   KEYSTORE_PATH      e.g. C:\Users\you\android-release.keystore
            //   KEYSTORE_PASS, KEY_ALIAS, KEY_PASS
            //
            // signingConfig = signingConfigs.getByName("release")
        }
    }

    // signingConfigs {
    //     create("release") {
    //         storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release.keystore")
    //         storePassword = System.getenv("KEYSTORE_PASS")
    //         keyAlias      = System.getenv("KEY_ALIAS")
    //         keyPassword   = System.getenv("KEY_PASS")
    //     }
    // }

    packaging {
        resources.excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(project(":gdx-core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}

// Copy native .so files into the right ABI folders before packaging
tasks.register<Copy>("copyAndroidNatives") {
    val libsDir = file("libs")
    libsDir.mkdirs()
    listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64").forEach { abi ->
        from(configurations["natives"]) {
            include("**/*$abi*.so")
            into("$abi")
        }
    }
    into(libsDir)
}

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("JniLibFolders") }
    .configureEach { dependsOn("copyAndroidNatives") }
