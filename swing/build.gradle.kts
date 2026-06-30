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

    testImplementation(platform("org.junit:junit-bom:6.1.1"))
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

// Native installers only when explicitly requested (avoids Beryx on normal builds).
if (project.hasProperty("jpackage")) {
    apply(from = "jpackage.gradle.kts")
}
