plugins {
    id("org.beryx.runtime") version "1.13.1"
}

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
