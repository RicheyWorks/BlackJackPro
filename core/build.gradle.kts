/*
 * core — pure rules + persistence. No AWT, no Swing, no libGDX. This is the
 * one module every UI front-end consumes.
 *
 * Java 17 source level because Android can't yet handle Java 21 bytecode
 * cleanly (records, switch expressions, pattern instanceof are all available
 * in 17 and that's what mobile devices can run via desugaring).
 */
plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

// release = 17 rather than sourceCompatibility/targetCompatibility = 17.
//
// They are not equivalent. source/target only set the language level and
// bytecode version; they still compile against the JDK 21 class library, so a
// Java 18+ method call compiles happily into a class file stamped "Java 17" and
// then throws NoSuchMethodError on a device. Verified: Character.isEmoji (Java
// 21) builds cleanly under -source 17 -target 17 and is rejected by --release
// 17. Since this module is the one Android actually runs, that check has to be
// real. --release also replaces the toolchain + sourceCompatibility pairing,
// which Gradle discourages and which reads as if it does more than it does.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
