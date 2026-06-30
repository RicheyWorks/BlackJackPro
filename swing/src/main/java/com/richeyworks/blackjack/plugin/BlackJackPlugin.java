package com.richeyworks.blackjack.plugin;

import java.util.List;

/**
 * Top-level plugin contract. Implementations are discovered via Java's
 * {@link java.util.ServiceLoader} (META-INF/services/com.richeyworks.blackjack.plugin.BlackJackPlugin)
 * or loaded from JARs dropped into the user's {@code plugins/} directory.
 *
 * A plugin can contribute any combination of themes, AI strategies, and side
 * bets. Most plugins will contribute just one.
 */
public interface BlackJackPlugin {

    /** Stable identifier — must be unique across installed plugins. */
    PluginManifest manifest();

    /** Themes shipped by this plugin. Empty list is fine. */
    default List<TableTheme> themes() { return List.of(); }

    /** AI personalities shipped by this plugin. Empty list is fine. */
    default List<AiPlugin> aiStrategies() { return List.of(); }

    /** Side bets shipped by this plugin. Empty list is fine. */
    default List<SideBet> sideBets() { return List.of(); }

    /** Lifecycle hook — called once after the plugin is registered. */
    default void onLoad() {}

    /** Lifecycle hook — called once before unload (game exit or reload). */
    default void onUnload() {}
}
