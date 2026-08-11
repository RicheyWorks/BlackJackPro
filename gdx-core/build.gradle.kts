/*
 * gdx-core — libGDX cross-platform game code. Consumed by :gdx-desktop
 * (LWJGL3) and :android. Depends on :core for the rules engine.
 */
plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// See the note in core/build.gradle.kts: --release actually compiles against the
// Java 17 class library, where source/target only stamp the bytecode version and
// would let a Java 18+ call through to fail on an Android device at runtime.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

val gdxVersion = "1.14.2"

dependencies {
    api(project(":core"))
    api("com.badlogicgames.gdx:gdx:$gdxVersion")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
