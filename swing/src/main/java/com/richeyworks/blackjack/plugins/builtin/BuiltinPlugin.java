package com.richeyworks.blackjack.plugins.builtin;

import com.richeyworks.blackjack.plugin.AiPlugin;
import com.richeyworks.blackjack.plugin.BlackJackPlugin;
import com.richeyworks.blackjack.plugin.PluginManifest;
import com.richeyworks.blackjack.plugin.SideBet;
import com.richeyworks.blackjack.plugin.TableTheme;

import java.util.List;

/**
 * The default plugin that ships in the box. Bundles the Neon theme, the
 * Hi-Lo Counter AI, and the 21+3 side bet. Registered via
 * {@code META-INF/services/com.richeyworks.blackjack.plugin.BlackJackPlugin}
 * so {@link com.richeyworks.blackjack.plugin.PluginRegistry} finds it
 * automatically.
 */
public final class BuiltinPlugin implements BlackJackPlugin {

    private static final PluginManifest MANIFEST = new PluginManifest(
            "com.richeyworks.blackjack.builtin",
            "Built-in Pack",
            "0.2.0",
            "RicheyWorks",
            "Ships a bonus theme, a counting AI, and the 21+3 side bet."
    );

    @Override public PluginManifest manifest() { return MANIFEST; }

    @Override public List<TableTheme> themes() { return List.of(new NeonTheme()); }
    @Override public List<AiPlugin>   aiStrategies() { return List.of(new HiLoCounterAi()); }
    @Override public List<SideBet>    sideBets()     { return List.of(new TwentyOnePlusThree()); }
}
