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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
