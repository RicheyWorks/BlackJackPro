/*
 * gdx-desktop — libGDX LWJGL3 launcher. Lets us run the libGDX game on the
 * desktop for fast iteration before pushing to Android.
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

val gdxVersion = "1.14.2"

dependencies {
    implementation(project(":gdx-core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    runtimeOnly  ("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

application {
    mainClass.set("com.richeyworks.blackjack.gdx.desktop.DesktopLauncher")
}
