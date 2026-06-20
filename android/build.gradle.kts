/*
 * android — libGDX Android launcher.
 *
 * Build the APK:    gradlew :android:assembleDebug
 *                   APK lands in android/build/outputs/apk/debug/
 *
 * Signed release:   gradlew :android:assembleRelease
 *                   (after configuring signingConfig below with a real keystore)
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
        minSdk        = 21
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
