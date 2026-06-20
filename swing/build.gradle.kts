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
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

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
            srcDirs("src/main/resources", "${rootDir}/resources")
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
