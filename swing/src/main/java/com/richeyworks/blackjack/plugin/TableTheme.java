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
 *
 * <p><b>Graphics state:</b> paint methods receive a scratch {@link Graphics2D}
 * that the renderer disposes afterwards, so a theme may freely change font,
 * color, stroke, clip, and transform without restoring them. Nothing a theme
 * does to that object can affect the rest of the table.
 */
public interface TableTheme {

    /**
     * Stable identifier used to persist the user's choice, e.g. {@code "classic"}.
     * Unlike {@link #displayName()} this must not change between releases and is
     * never shown to the player — renaming a theme in the UI would otherwise
     * silently reset everyone's preference.
     *
     * <p>Defaults to the display name lowercased so existing themes keep working,
     * but implementations should override it with a deliberate constant.
     */
    default String id() { return displayName().toLowerCase(java.util.Locale.ROOT); }

    /** Display name shown in the theme picker. */
    String displayName();

    /** Background gradient top / bottom. */
    Color feltTop();
    Color feltBottom();

    /**
     * Decorate the felt itself, drawn above the gradient and beneath
     * everything else — cards, chips, captions, and the table arc all paint
     * over it, so it can afford to cover the whole surface as long as it
     * stays quiet. Default is no decoration, which is what a plain casino
     * felt looks like; the built-in palette themes draw their card-back
     * motif here at large scale and low alpha so a theme reads as one
     * pattern language from the felt to the cards.
     *
     * <p>Receives a scratch {@link Graphics2D} like every other paint method.
     */
    default void paintFeltDecor(Graphics2D g, int width, int height) {}

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
