package com.richeyworks.blackjack.plugin;

import com.richeyworks.blackjack.engine.Card;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A visual theme that paints the felt, cards, and chips. Implementations should
 * be stateless and safe to call from the Swing EDT.
 *
 * The renderer hands the theme pixel rectangles to draw into; the theme owns
 * fonts, colors, and decorative details.
 */
public interface TableTheme {

    /** Display name shown in the theme picker. */
    String displayName();

    /** Background gradient top / bottom. */
    Color feltTop();
    Color feltBottom();

    /** Accent (table arc, decorative text). */
    default Color accent() { return new Color(0xC9A227); }

    /** Active-hand glow color (low alpha). */
    default Color highlight() { return new Color(255, 230, 100, 80); }

    /** Card width / height in pixels. */
    default int cardWidth()  { return 96; }
    default int cardHeight() { return 134; }

    /** Paint a face-up card at the given position. */
    void paintCardFace(Graphics2D g, int x, int y, Card card);

    /** Paint a face-down card at the given position. */
    void paintCardBack(Graphics2D g, int x, int y);

    /** Paint a chip with the given face value at center (cx, cy). */
    void paintChip(Graphics2D g, int cx, int cy, int radius, int value);
}
