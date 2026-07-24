/*
 * swing — the original desktop Swing build, now sitting alongside the libGDX
 * mobile target. Owns the Swing UI, plugin API (Graphics2D-based themes), and
 * platform-specific media (javax.sound) and Steam bridge.
 *
 * Run:        gradlew :swing:run
 * Installer:  gradlew :swing:jpackage -Pjpackage
 */
plugins {
    java
    application
    // Badass Runtime: jlink runtime images + jpackage native installers.
    // Applied unconditionally because a plugins{} block inside a script
    // applied via apply(from = ...) is silently unsupported by Gradle -- the
    // old conditional apply meant `:swing:jpackage` had never actually
    // worked. The plugin only adds tasks; normal builds are unaffected.
    id("org.beryx.runtime") version "1.13.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":core"))
    // NOTE: jackson-databind was removed — it was on the classpath but never
    // imported by any source file. Re-add via Gradle (current patched release)
    // only if a feature actually needs JSON, and never enable polymorphic
    // default typing on untrusted input.

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.richeyworks.blackjack.Main")
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.java2d.uiScale.enabled=true"
    )
}

sourceSets {
    main {
        resources {
            // src/main/resources is already a default resource root; only add the
            // shared repo-level resources/ (deck, lang, css) alongside it. Adding
            // src/main/resources again here would scan it twice and make every
            // entry (e.g. META-INF/services) a duplicate. Excludes keep runtime
            // junk (logs/music/saves) out of the jar.
            srcDir("${rootDir}/resources")
            exclude("logs/**", "music/**", "saves/**", "save.txt")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title"   to "BlackJack Pro (Swing)",
            "Implementation-Version" to version,
            "Implementation-Vendor"  to "RicheyWorks",
            "Main-Class"             to application.mainClass.get()
        )
    }
}

/*
 * Native installers: `gradlew :swing:jpackage` builds MSI on Windows, DMG on
 * macOS, DEB elsewhere (Windows additionally needs the WiX Toolset on PATH).
 * Configured here rather than in an applied script -- see the plugins block.
 */
runtime {
    options.set(listOf(
        "--strip-debug", "--compress", "2",
        "--no-header-files", "--no-man-pages"
    ))
    modules.set(listOf(
        "java.desktop", "java.logging", "java.management",
        "java.naming", "java.sql", "java.xml", "jdk.unsupported"
    ))
    jpackage {
        imageName     = "BlackJackPro"
        installerName = "BlackJackPro"
        appVersion    = (version as String).removeSuffix("-SNAPSHOT")

        // The installed launcher gets its own JVM, so it does not inherit the
        // args :swing:run uses. Without the encoding flag the packaged build can
        // render the table text differently from the one that was tested.
        jvmArgs = listOf(
            "-Dfile.encoding=UTF-8",
            "-Dsun.java2d.uiScale.enabled=true"
        )

        val os = org.gradle.internal.os.OperatingSystem.current()
        if (os.isWindows) {
            installerType = "msi"
            installerOptions = listOf(
                "--win-dir-chooser", "--win-menu", "--win-shortcut",
                "--vendor", "RicheyWorks",
                "--description", "BlackJack Pro — casino-grade single-player blackjack"
            )
        } else if (os.isMacOsX) {
            installerType = "dmg"
            installerOptions = listOf("--vendor", "RicheyWorks", "--description", "BlackJack Pro")
        } else {
            installerType = "deb"
            installerOptions = listOf("--linux-shortcut", "--vendor", "RicheyWorks",
                "--description", "BlackJack Pro")
        }
    }
}
