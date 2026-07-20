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

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
