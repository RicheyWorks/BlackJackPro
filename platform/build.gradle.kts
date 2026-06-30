/*
 * platform — server-side, compliance-first online platform (skeleton).
 *
 * Depends on :core for the authoritative game engine (GameRoundService reuses it).
 * This is a server target, so it uses the full Java 21 language level.
 *
 * See docs/architecture/ADR-0001-real-money-crypto-platform.md.
 * NOTE: interface stubs + a reference ComplianceGate only — no money-moving
 * implementations. Do not operate without licensing, MSB registration, and counsel.
 */
plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    api(project(":core"))

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
