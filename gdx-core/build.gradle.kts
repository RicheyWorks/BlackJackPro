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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val gdxVersion = "1.12.1"

dependencies {
    api(project(":core"))
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
}
