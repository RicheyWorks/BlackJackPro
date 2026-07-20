package com.richeyworks.blackjack.engine;

/**
 * A canary for the build's source encoding.
 *
 * <p>{@code javac} decodes source files using the JVM's default charset unless
 * {@code -encoding} is passed. The UI carries literal non-ASCII characters in
 * player-facing text — em dashes, middle dots, arrows — so under a non-UTF-8
 * default those bytes are decoded wrongly and the mangled result is baked into
 * the class files as string constants. The build still succeeds. The damage
 * only shows up on screen, usually on a machine other than the one that built
 * it.
 *
 * <p>{@link #CANARY} is deliberately written as a <em>literal</em> character
 * rather than a {@code \\uXXXX} escape, so it shares the fate of every other
 * literal in the codebase. {@code SourceEncodingTest} compares it against the
 * equivalent escape — escapes are pure ASCII and cannot be corrupted — so a
 * mismatch means the compiler decoded this file with the wrong charset. That
 * converts a silent cosmetic failure into a failing test.
 *
 * <p>This class exists only to be compiled. Nothing reads it at runtime.
 */
public final class BuildEncoding {

    private BuildEncoding() {}

    /**
     * U+2014 EM DASH, written literally on purpose. The character the UI uses
     * most often, and the one most likely to be mangled.
     */
    public static final String CANARY = "—";

    /**
     * A wider sample, each written literally: em dash, middle dot, black star,
     * white star, black right-pointing pointer, horizontal ellipsis, and the
     * four card suits. Between them these cover every non-ASCII character the
     * game actually displays.
     */
    public static final String CANARY_SET = "—·★☆▸…♠♥♦♣";
}
