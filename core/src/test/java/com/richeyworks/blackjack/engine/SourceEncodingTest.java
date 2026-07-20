package com.richeyworks.blackjack.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the build's source encoding.
 *
 * <p>{@code javac} decodes sources with the JVM's default charset unless
 * {@code -encoding} is set. The sources carry literal non-ASCII characters in
 * player-facing text, so under a non-UTF-8 default the mangled bytes get baked
 * into the class files as string constants; the build succeeds and the game
 * ships visibly broken text. JDK 18+ defaults to UTF-8, so this passes by luck
 * unless the root build sets the encoding explicitly, which it now does.
 *
 * <p>These assertions compare compiled constants against {@code \\uXXXX}
 * escapes. Escapes are processed by the compiler from pure ASCII and cannot be
 * corrupted by a charset, so any mismatch means the literal on the other side
 * was decoded wrongly. That turns a silent cosmetic failure into a red test.
 */
class SourceEncodingTest {

    @Test void theBuildCompilesSourcesAsUtf8() {
        // The load-bearing assertion. BuildEncoding.CANARY is a literal em dash
        // in main source; the escape below is pure ASCII and cannot be
        // corrupted. If javac decoded that file with the wrong charset the
        // literal becomes several characters and this fails.
        assertEquals("\u2014", BuildEncoding.CANARY,
                "source was not compiled as UTF-8 - set options.encoding in the build");
        assertEquals(1, BuildEncoding.CANARY.length(),
                "em dash decoded to " + BuildEncoding.CANARY.length() + " chars");
    }

    @Test void everyDisplayedGlyphSurvivesCompilation() {
        assertEquals("\u2014\u00b7\u2605\u2606\u25b8\u2026\u2660\u2665\u2666\u2663",
                BuildEncoding.CANARY_SET,
                "at least one displayed character was mangled at compile time");
        assertEquals(10, BuildEncoding.CANARY_SET.length(),
                "expected 10 characters, got " + BuildEncoding.CANARY_SET.length()
                    + " - a wrong charset expands each one");
    }

    @Test void cardSuitGlyphsAreIntact() {
        // Suit already uses escapes internally; this pins the values so a
        // refactor to literals would be caught rather than shipped.
        assertEquals("\u2660", Suit.SPADES.glyph(),   "spade");
        assertEquals("\u2665", Suit.HEARTS.glyph(),   "heart");
        assertEquals("\u2666", Suit.DIAMONDS.glyph(), "diamond");
        assertEquals("\u2663", Suit.CLUBS.glyph(),    "club");
    }

    @Test void suitGlyphsAreDistinct() {
        // The failure mode worth catching: a bad charset can collapse several
        // multi-byte characters onto the same replacement, and four identical
        // suits would make the table unreadable rather than merely ugly.
        String[] glyphs = {
            Suit.SPADES.glyph(), Suit.HEARTS.glyph(),
            Suit.DIAMONDS.glyph(), Suit.CLUBS.glyph()
        };
        for (int i = 0; i < glyphs.length; i++) {
            for (int j = i + 1; j < glyphs.length; j++) {
                assertNotEquals(glyphs[i], glyphs[j], "suit glyphs " + i + " and " + j + " collided");
            }
        }
    }

    @Test void suitGlyphsAreSingleCharacters() {
        // A UTF-8 glyph decoded as windows-1252 becomes three characters. Length
        // is the cheapest way to detect that the decode went wrong.
        for (Suit s : Suit.values()) {
            assertEquals(1, s.glyph().length(),
                    s + " glyph is " + s.glyph().length() + " chars - decoded with the wrong charset?");
        }
    }

    @Test void suitGlyphsAreInThePlayingCardBlock() {
        for (Suit s : Suit.values()) {
            int cp = s.glyph().codePointAt(0);
            assertTrue(cp >= 0x2660 && cp <= 0x2667,
                    s + " glyph U+" + Integer.toHexString(cp).toUpperCase()
                       + " is outside the card-suit range");
        }
    }

    @Test void rankLabelsAreAscii() {
        // Ranks are drawn in the card corners at small sizes; keeping them ASCII
        // means no font or charset can affect the most-read text in the game.
        for (Rank r : Rank.values()) {
            for (char c : r.label().toCharArray()) {
                assertTrue(c < 128,
                        "rank label \"" + r.label() + "\" contains non-ASCII U+"
                            + Integer.toHexString(c).toUpperCase());
            }
        }
    }

    @Test void outcomeLabelsAreAscii() {
        // These are the round results the player reads after every hand. They
        // have no reason to need non-ASCII, and staying ASCII makes them immune.
        for (Outcome o : Outcome.values()) {
            assertNotNull(o.label());
            assertFalse(o.label().isBlank(), o + " has no label");
            for (char c : o.label().toCharArray()) {
                assertTrue(c < 128,
                        "outcome label \"" + o.label() + "\" contains non-ASCII U+"
                            + Integer.toHexString(c).toUpperCase());
            }
        }
    }

    @Test void aRoundTripThroughUtf8PreservesGlyphs() {
        // Belt and braces: encoding and decoding as UTF-8 must be lossless. If
        // the platform default leaked into either direction this would fail.
        for (Suit s : Suit.values()) {
            byte[] bytes = s.glyph().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            String back = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            assertEquals(s.glyph(), back, s + " did not survive a UTF-8 round trip");
            assertEquals(3, bytes.length, s + " should be 3 bytes in UTF-8");
        }
    }
}
