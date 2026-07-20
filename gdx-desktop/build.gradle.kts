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

val gdxVersion = "1.12.1"

dependencies {
    implementation(project(":gdx-core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    runtimeOnly  ("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

application {
    mainClass.set("com.richeyworks.blackjack.gdx.desktop.DesktopLauncher")
    // Same reason as :swing — the table draws non-ASCII text, and the preview
    // is meant to show what the mobile build will look like, so it must not
    // render differently just because the host locale differs.
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}
