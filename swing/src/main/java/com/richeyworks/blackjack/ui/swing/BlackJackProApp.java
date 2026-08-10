package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.achievement.AchievementService;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Outcome;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.engine.SessionStats;
import com.richeyworks.blackjack.media.MusicService;
import com.richeyworks.blackjack.media.SoundFx;
import com.richeyworks.blackjack.persist.AppPaths;
import com.richeyworks.blackjack.persist.SaveManager;
import com.richeyworks.blackjack.plugin.PluginRegistry;
import com.richeyworks.blackjack.sidebet.SideBetManager;
import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.strategy.HiLoCounter;
import com.richeyworks.blackjack.strategy.TableObservation;
import com.richeyworks.blackjack.settings.GameSettings;
import com.richeyworks.blackjack.steam.SteamBridge;
import com.richeyworks.blackjack.table.TableChatter;
import com.richeyworks.blackjack.table.TableEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.security.SecureRandom;

/**
 * The main game window. Owns the {@link Engine} and renders the {@link TablePanel}
 * plus a control bar. Wires every user interaction back through engine methods
 * (no game state mutated in this class directly).
 */
public final class BlackJackProApp extends JFrame {

    private final Engine             engine;
    private final TablePanel         table;
    private final MusicService       music;
    private final SoundFx            sfx;
    private final SaveManager        save;
    private final PluginRegistry     plugins;
    private final GameSettings       settings;
    private final AchievementService achievements;
    private       TableTheme         theme;

    private int processedHands;   // rounds already post-processed (round-complete detection)
    private int winStreak;        // consecutive winning rounds (streak achievement)

    private final AtomicBoolean shutdownDone = new AtomicBoolean();
    private Thread shutdownHook;

    /** Result of the last settled round, shown until the next deal. */
    private String lastResult = "";
    /** True while the out-of-chips prompt is up, to stop it stacking. */
    private boolean reloadPrompted;
    /**
     * Sticky "No" on the reload dialog. Without this, every refresh while
     * broke re-opens the modal (reloadPrompted only covered re-entry during
     * the dialog itself), so declining once was not enough.
     */
    private boolean reloadDeclined;
    /** Single reusable timer behind {@link #flash(String)}. */
    private Timer flashTimer;


    /**
     * The characters at the table. They talk; they never play a hand. Not
     * final: the crewed themes (Pirate Cove, Dusty Saloon, Nebula) seat a
     * different cast, so changing theme can change who is sitting here —
     * see {@link com.richeyworks.blackjack.table.Casts}.
     */
    private TableChatter chatter;
    /** Repaints while a bubble is fading, since nothing else drives a frame. */
    private Timer chatterTimer;
    /** Consecutive losing rounds, the mirror of winStreak. */
    private int lossStreak;

    private final JLabel statusBar = new JLabel(" ");
    private final JLabel bankLabel = new JLabel();
    private final JLabel betLabel  = new JLabel();
    private final JLabel shoeLabel = new JLabel();

    private JButton bDeal, bHit, bStand, bDouble, bSplit, bSurrender;
    private JButton bIns, bNoIns;
    private JButton[] chipBtns;
    private JButton bSide, bClear;
    private JCheckBoxMenuItem soft17Item;
    private final JLabel sideLabel = new JLabel();
    private final SideBetManager sideBets;
    private String sideMsg = "";
    private final TableObservation observation;
    private boolean showCount = true;
    private final JLabel countLabel = new JLabel();

    private static final int[] CHIP_VALUES = {1, 5, 25, 100, 500};

    // Table palette — hex literals collected here for consistency and reuse.
    private static final Color HUD_GOLD    = new Color(0xF8E9A1); // status text + bankroll/bet HUD
    private static final Color STATUS_BG   = new Color(0x0E2E1A); // status-bar background
    private static final Color BAR_BG      = new Color(0x0D2A18); // control-bar background
    private static final Color SHOE_GREY   = new Color(0xBDBDBD); // shoe-count label
    private static final Color SIDE_GOLD   = new Color(0xC9A227); // 21+3 side-bet label
    private static final Color COUNT_GREEN = new Color(0x9FE0B0); // Hi-Lo count label
    private static final Color BTN_FACE    = new Color(0x8B5E2B); // pirate-button face
    private static final Color BTN_BORDER  = new Color(0x5C3A0F); // pirate-button border
    private static final Color FLASH_BG    = new Color(0x6E3030); // transient error flash

    public BlackJackProApp(Engine engine,
                           MusicService music,
                           SoundFx sfx,
                           SaveManager save,
                           PluginRegistry plugins,
                           GameSettings settings,
                           AchievementService achievements,
                           TableTheme theme) {
        super("BlackJack Pro");
        this.engine       = engine;
        this.music        = music;
        this.sfx          = sfx;
        this.save         = save;
        this.plugins      = plugins;
        this.settings     = settings;
        this.achievements = achievements;
        this.theme        = theme;
        this.sideBets     = new SideBetManager(
                plugins.sideBets().isEmpty() ? null : plugins.sideBets().get(0));
        // Owned directly rather than fished out of the plugin list: counting is
        // core logic now, so the display works whether or not a plugin ships an
        // AI that happens to wrap it. TableObservation keys on card identity so
        // a split cannot double-count the dealer's up-card.
        this.observation = new TableObservation(new HiLoCounter());
        // Seat the cast that matches the saved theme, so a player who left on
        // Pirate Cove comes back to the same crew rather than the regulars.
        this.chatter = new TableChatter(
                com.richeyworks.blackjack.table.Casts.forPalette(theme.id()),
                new java.util.Random());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        save.load(engine);
        processedHands   = engine.stats().hands;
        // Heart of Stone tracks a win streak. Progress is persisted, but the
        // session counter used to start at 0 on every launch — setProgress(1)
        // after the first win then no-op'd until the in-session streak beat the
        // saved progress. Seed from the achievement so a 3/5 resume keeps working.
        var streak = achievements.get("survived_bust_streak");
        if (streak != null && !streak.unlocked()) winStreak = streak.progress();
        achievements.onUnlock(a -> {
            sfx.achievement();
            AchievementToast.show(this, a);
        });

        table = new TablePanel(engine, theme);
        table.setChatter(chatter);
        // Bubbles fade over a few seconds and the game is otherwise event-driven,
        // so without a repaint tick they would freeze mid-fade until the next
        // click. Cheap: it only runs while something is actually on screen.
        chatterTimer = new Timer(80, e -> {
            if (!chatter.visibleAt(System.currentTimeMillis()).isEmpty()) table.repaint();
            else ((Timer) e.getSource()).stop();
        });
        add(table, BorderLayout.CENTER);
        add(buildControlBar(), BorderLayout.SOUTH);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBar.setForeground(HUD_GOLD);
        statusBar.setOpaque(true);
        statusBar.setBackground(STATUS_BG);
        add(statusBar, BorderLayout.NORTH);
        setJMenuBar(buildMenuBar());

        setSize(1200, 820);
        setLocationRelativeTo(null);
        updateUi("Place your bet to begin.");
        // Greet after the window is up, so the bubble isn't drawn into a frame
        // that hasn't been shown yet.
        SwingUtilities.invokeLater(() -> say(TableEvent.SESSION_START));

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { shutdownAll(); }
        });
        // Safety net for every other way the process can end -- Game ▸ Quit,
        // a SIGTERM, the platform's "quit all applications". EXIT_ON_CLOSE and
        // System.exit() both run shutdown hooks; shutdownAll() is idempotent, so
        // the normal path still does the work on the EDT and this is a no-op.
        shutdownHook = new Thread(this::shutdownAll, "blackjack-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Persist everything and release resources. Idempotent, and safe to call
     * off the EDT.
     *
     * <p>Chips the player has pushed onto the felt but not yet played live in
     * {@code pendingBet} (and in the side-bet manager), neither of which is
     * persisted — the save file records the bankroll only. Returning them
     * before saving is what stops a quit-while-betting from destroying them.
     */
    private void shutdownAll() {
        if (!shutdownDone.compareAndSet(false, true)) return;
        try {
            returnStakedChips(engine, sideBets);
            save.save(engine);
            settings.save();
            achievements.save();
        } finally {
            music.shutdown();
            sfx.shutdown();
            plugins.shutdown();
            SteamBridge.shutdown();
        }
    }

    /**
     * Move any chips sitting on the felt back into the bankroll.
     *
     * <p>Only the bankroll is persisted, so anything still held as a pending
     * main bet or a pending side bet is destroyed by a save. Call this before
     * saving on any path that ends the session.
     *
     * @return the total returned, for tests and logging
     */
    static int returnStakedChips(Engine engine, SideBetManager sideBets) {
        int pending = engine.pendingBet();
        engine.clearBet();                 // phase-guarded; no-op mid-round
        int returned = engine.pendingBet() == 0 ? pending : 0;

        int refund = sideBets.clear();
        if (refund > 0) {
            engine.setBankroll(saturatedBankroll(engine.bankroll(), refund));
            returned += refund;
        }
        return returned;
    }


    /** Exit cleanly, without waiting for the shutdown hook to do the saving. */
    private void quit() {
        shutdownAll();
        try { Runtime.getRuntime().removeShutdownHook(shutdownHook); }
        catch (IllegalStateException alreadyShuttingDown) { /* fine */ }
        System.exit(0);
    }

    /* ----------------------------------------------------------------------- */
    /* Layout                                                                  */
    /* ----------------------------------------------------------------------- */

    private JPanel buildControlBar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BAR_BG);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.gridy  = 0;

        chipBtns = new JButton[CHIP_VALUES.length];
        for (int i = 0; i < CHIP_VALUES.length; i++) {
            final int v = CHIP_VALUES[i];
            JButton b = chipButton(v);
            b.addActionListener(e -> placeBet(v));
            c.gridx = i;
            p.add(b, c);
            chipBtns[i] = b;
        }
        bClear = pirateButton("Clear");
        bClear.addActionListener(e -> clearBet());
        c.gridx = CHIP_VALUES.length;
        p.add(bClear, c);

        if (sideBets.available()) {
            bSide = pirateButton("21+3 +$5");
            bSide.addActionListener(e -> placeSideBet(5));
            c.gridx = CHIP_VALUES.length + 1;
            p.add(bSide, c);
        }

        bDeal       = pirateButton("Deal");
        bHit        = pirateButton("Hit");
        bStand      = pirateButton("Stand");
        bDouble     = pirateButton("Double");
        bSplit      = pirateButton("Split");
        bSurrender  = pirateButton("Surrender");
        bIns        = pirateButton("Insure");
        bNoIns      = pirateButton("Decline");

        bDeal.addActionListener(e -> dealRound());
        bHit.addActionListener(e -> safe(engine::hit, null));
        bStand.addActionListener(e -> safe(engine::stand, null));
        bDouble.addActionListener(e -> safe(() -> { engine.doubleDown(); say(TableEvent.PLAYER_DOUBLE); },
                "Cannot double."));
        bSplit.addActionListener(e -> safe(() -> { engine.split(); say(TableEvent.PLAYER_SPLIT); },
                "Cannot split."));
        bSurrender.addActionListener(e -> safe(engine::surrender, null));
        bIns.addActionListener(e -> takeInsurance(true));
        bNoIns.addActionListener(e -> takeInsurance(false));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        actions.add(bDeal); actions.add(bHit); actions.add(bStand);
        actions.add(bDouble); actions.add(bSplit); actions.add(bSurrender);
        actions.add(bIns); actions.add(bNoIns);

        c.gridx = 0; c.gridy = 1; c.gridwidth = CHIP_VALUES.length + 1;
        c.anchor = GridBagConstraints.CENTER;
        p.add(actions, c);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        info.setOpaque(false);
        Font hud = bankLabel.getFont().deriveFont(Font.BOLD, 16f);
        bankLabel.setForeground(HUD_GOLD);
        betLabel.setForeground(HUD_GOLD);
        shoeLabel.setForeground(SHOE_GREY);
        bankLabel.setFont(hud); betLabel.setFont(hud);
        sideLabel.setForeground(SIDE_GOLD);
        countLabel.setForeground(COUNT_GREEN);
        info.add(bankLabel); info.add(betLabel);
        if (sideBets.available()) info.add(sideLabel);
        info.add(shoeLabel);
        info.add(countLabel);
        c.gridy = 2;
        p.add(info, c);

        return p;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu game = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Session (reset bankroll)");
        newGame.addActionListener(e -> resetSession());
        JMenuItem stats = new JMenuItem("Stats");
        stats.addActionListener(e -> showStats());
        JMenuItem rules = new JMenuItem("Rules");
        rules.addActionListener(e -> showRules());
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(e -> quit());
        game.add(newGame); game.add(stats); game.addSeparator(); game.add(rules);
        game.addSeparator(); game.add(quit);

        JMenu opts = new JMenu("Options");
        JMenuItem prefs = new JMenuItem("Preferences…");
        // The dialog and the checkbox below edit the same setting, so the dialog
        // hands back control after saving and we re-read the value rather than
        // letting the two controls drift apart.
        prefs.addActionListener(e -> new SettingsDialog(
                this, settings, engine, music, sfx, this::syncRuleControls).setVisible(true));

        soft17Item = new JCheckBoxMenuItem("Dealer hits soft 17");
        soft17Item.setState(engine.rules().dealerHitsSoft17);
        soft17Item.addActionListener(e -> setDealerHitsSoft17(soft17Item.getState()));

        JCheckBoxMenuItem countToggle = new JCheckBoxMenuItem("Show Hi-Lo count");
        countToggle.setState(showCount);
        countToggle.addActionListener(e -> { showCount = countToggle.getState(); updateUi(statusBar.getText()); });
        JMenuItem mute = new JMenuItem(music.isMuted() ? "Unmute music" : "Mute music");
        mute.addActionListener(e -> {
            music.toggleMute();
            settings.musicEnabled = !music.isMuted();
            settings.save();
            mute.setText(music.isMuted() ? "Unmute music" : "Mute music");
        });

        JMenuItem nextTrack = new JMenuItem("Next track");
        nextTrack.addActionListener(e -> music.next());
        opts.add(prefs); opts.addSeparator(); opts.add(soft17Item); opts.add(countToggle);
        opts.addSeparator(); opts.add(mute); opts.add(nextTrack);

        JMenu themeMenu = new JMenu("Theme");
        JMenuItem gallery = new JMenuItem("Theme Gallery…");
        gallery.addActionListener(e ->
                new ThemeGalleryDialog(this, allThemes(), theme.id(), this::applyTheme)
                        .setVisible(true));
        themeMenu.add(gallery);
        themeMenu.addSeparator();
        // Every theme seats a matching cast now, so there is no "crewed"
        // subset worth separating — the gallery's badges name each crew.
        for (TableTheme t : allThemes()) addThemeItem(themeMenu, t);

        JMenu extras = new JMenu("Extras");
        JMenuItem achievementsItem = new JMenuItem("Achievements…");
        achievementsItem.addActionListener(e -> showAchievements());
        JMenuItem pluginsItem = new JMenuItem("Plugins…");
        pluginsItem.addActionListener(e -> new PluginManagerDialog(this, plugins).setVisible(true));
        extras.add(achievementsItem); extras.add(pluginsItem);

        bar.add(game); bar.add(opts); bar.add(themeMenu); bar.add(extras);
        return bar;
    }

    private void showAchievements() {
        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif;width:360px;'>");
        for (var a : achievements.all()) {
            sb.append("<p>");
            // Escaped, not literal: these two glyphs are the only thing telling
            // locked from unlocked. If a bad charset mangled both they would
            // corrupt to the same bytes and the list would become unreadable —
            // wrong, not merely ugly, unlike the decorative dashes elsewhere.
            sb.append(a.unlocked() ? "\u2605 " : "\u2606 ");   // filled / hollow star
            sb.append("<b>").append(a.name()).append("</b><br/>");
            sb.append("<small>").append(a.description());
            sb.append("  (")
              .append(a.progress()).append("/").append(a.goal())
              .append(")</small></p>");
        }
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Achievements",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void addThemeItem(JMenu menu, TableTheme t) {
        JMenuItem item = new JMenuItem(t.displayName());
        item.addActionListener(e -> applyTheme(t));
        menu.add(item);
    }

    /**
     * Every theme available right now, deduplicated by id: Classic, the
     * built-in pack, external plugins, and — belt and braces — whatever is
     * currently applied, so an external theme still shows while its JAR is
     * loaded even if the plugin list changed underneath it.
     */
    private java.util.List<TableTheme> allThemes() {
        java.util.LinkedHashMap<String, TableTheme> byId = new java.util.LinkedHashMap<>();
        TableTheme classic = new ClassicTheme();
        byId.put(classic.id(), classic);
        for (TableTheme t : plugins.themes()) byId.putIfAbsent(t.id(), t);
        byId.putIfAbsent(theme.id(), theme);
        return java.util.List.copyOf(byId.values());
    }

    /** Apply {@code t} everywhere a theme matters: felt, settings, and cast. */
    private void applyTheme(TableTheme t) {
        this.theme = t;
        table.setTheme(t);
        // Persist by stable id, not display name: launch() looks the theme
        // back up by id, and renaming a theme in the UI should not silently
        // reset the player's choice.
        settings.themeId = t.id();
        settings.save();
        // A crewed theme seats its own characters. Only swap when the cast
        // actually changes, so hopping between two plain-decor themes
        // doesn't reset the regulars' conversation mid-flow.
        java.util.List<com.richeyworks.blackjack.table.Persona> cast =
                com.richeyworks.blackjack.table.Casts.forPalette(t.id());
        if (!cast.get(0).id().equals(chatter.personas().get(0).id())) {
            chatter = new TableChatter(cast, new java.util.Random());
            table.setChatter(chatter);
        }
        repaint();
    }

    /**
     * Apply a soft-17 rule change from either control, persist it, and put both
     * back in agreement. Only takes effect between rounds — changing how the
     * dealer draws while a hand is live would move the odds under the player
     * after they've already committed chips.
     */
    private void setDealerHitsSoft17(boolean hits) {
        if (engine.phase() != Phase.BETTING) {
            flash("House rules can only change between rounds.");
            syncRuleControls();
            return;
        }
        settings.dealerHitsSoft17        = hits;
        engine.rules().dealerHitsSoft17  = hits;
        settings.save();
        syncRuleControls();
        table.repaint();       // the felt prints the soft-17 rule
    }

    /** Re-read rule state into the menu controls that mirror it. */
    private void syncRuleControls() {
        if (soft17Item != null) soft17Item.setState(engine.rules().dealerHitsSoft17);
        table.repaint();
    }

    /* ----------------------------------------------------------------------- */
    /* Buttons                                                                 */
    /* ----------------------------------------------------------------------- */

    private JButton chipButton(int value) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                theme.paintChip(g2, getWidth() / 2, getHeight() / 2,
                        Math.min(getWidth(), getHeight()) / 2 - 2, value);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(54, 54));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setToolTipText("Bet $" + value);
        return b;
    }

    private JButton pirateButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BTN_FACE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BTN_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        return b;
    }

    /* ----------------------------------------------------------------------- */
    /* Engine wiring                                                           */
    /* ----------------------------------------------------------------------- */

    private void placeBet(int v) {
        if (engine.phase() != Phase.BETTING) return;
        if (!engine.canBet(v)) { flash("Not enough chips."); return; }
        engine.addBet(v);
        sfx.chipClick();
        updateUi("Bet: $" + engine.pendingBet() + ". Deal when ready.");
    }

    private void clearBet() {
        // Button is disabled outside BETTING, but guard anyway: mid-round this
        // moves no money (Engine.clearBet is phase-guarded and the side bet is
        // already resolved) yet would still wipe the result the player is reading.
        if (engine.phase() != Phase.BETTING) return;
        engine.clearBet();
        int refund = sideBets.clear();
        if (refund > 0) engine.setBankroll(saturatedBankroll(engine.bankroll(), refund));
        sideMsg = "";
        updateUi("Bet cleared.");

    }

    private void placeSideBet(int v) {
        if (engine.phase() != Phase.BETTING) return;
        int added = sideBets.add(v, engine.bankroll());
        if (added == 0) { flash("Not enough chips for the side bet."); return; }
        engine.setBankroll(engine.bankroll() - added);
        sideMsg = "";
        sfx.chipClick();
        updateUi("21+3 side bet: $" + sideBets.pending());
    }

    /** Deal a fresh round, then resolve any 21+3 side bet on the opening cards. */
    private void dealRound() {
        try {
            lastResult    = "";       // previous round's result leaves the status bar
            engine.deal();
            resolveSideBet();
            sfx.cardSnap();
            postAction();
            if (engine.phase() == Phase.INSURANCE) say(TableEvent.INSURANCE_OFFERED);
            else if (engine.phase() == Phase.PLAYER && dealerShowsWeakCard())
                say(TableEvent.DEALER_WEAK_CARD);
        } catch (RuntimeException ex) {
            flash("Place a bet first.");
        }
    }

    private void resolveSideBet() {
        if (!sideBets.available() || sideBets.pending() == 0) return;
        int stake  = sideBets.pending();
        int payout = sideBets.resolve(engine.hands().get(0).cards(), engine.dealer().first());
        long tw = (long) engine.stats().totalWagered + stake;
        engine.stats().totalWagered = tw >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) tw;
        long tr = (long) engine.stats().totalReturned + Math.max(0, payout);
        engine.stats().totalReturned = tr >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) tr;
        engine.setBankroll(saturatedBankroll(engine.bankroll(), Math.max(0, payout)));
        if (payout > 0) { sfx.winSting(); sideMsg = "21+3 " + sideBets.lastOutcome() + ": +$" + (payout - stake); }
        else            { sideMsg = "21+3: no win (-$" + stake + ")"; }
    }

    private static int saturatedBankroll(int bankroll, int delta) {
        long s = (long) bankroll + delta;
        if (s >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (s <= 0) return 0;
        return (int) s;
    }



    private void takeInsurance(boolean accept) {
        try {
            engine.takeInsurance(accept);
            postAction();   // may complete the round when the dealer has blackjack
        } catch (RuntimeException ex) {
            flash(ex.getMessage());
        }
    }

    private void safe(Runnable action, String fallback) {
        try {
            action.run();
            sfx.cardSnap();
            postAction();
        } catch (RuntimeException ex) {
            flash(fallback != null ? fallback : ex.getMessage());
        }
    }

    /**
     * Run after any engine action. Fires {@link #onRoundComplete()} exactly once
     * per round, keyed on the engine's hand counter so it also catches rounds
     * that resolve during the deal (dealt naturals, dealer blackjack) or out of
     * the insurance prompt — cases the old phase-transition check silently missed.
     */
    private void postAction() {
        observeNewCards();
        if (engine.phase() == Phase.BETTING && engine.stats().hands > processedHands) {
            processedHands = engine.stats().hands;
            onRoundComplete();
        }
        updateUi(describe());
    }

    private void onRoundComplete() {
        SessionStats s = engine.stats();
        List<Outcome> outcomes = engine.lastOutcomes();

        // The engine records an outcome per hand as it pays it out, so these
        // agree with the money by construction. Re-deriving them here from hand
        // values (as this used to) was a second implementation of the settlement
        // rules that could disagree with what the player was actually paid.
        boolean playerWon  = outcomes.stream().anyMatch(Outcome::isWin);
        boolean anyPush    = outcomes.contains(Outcome.PUSH);
        boolean naturalBJ  = outcomes.contains(Outcome.BLACKJACK);
        // A pure push keeps the streak; any loss (even beside a push on a split)
        // breaks it. Using !won && !push left LOSS+PUSH as a "push" and kept
        // Heart of Stone alive after a net-losing split.
        boolean anyLoss    = outcomes.stream().anyMatch(o ->
                o == Outcome.LOSS || o == Outcome.BUST || o == Outcome.SURRENDER);
        boolean playerLost = anyLoss && !playerWon;


        lastResult = summarize(outcomes, engine.lastNet());

        // Outcome SFX
        if (naturalBJ)      sfx.blackjackFanfare();
        else if (playerWon) sfx.winSting();
        else if (anyPush)   sfx.pushBeep();
        else if (playerLost) sfx.loseSting();

        // Achievement progress
        achievements.increment("first_hand");
        if (playerWon)      achievements.increment("first_win");
        if (playerWon)      achievements.increment("ten_wins");
        if (playerWon)      achievements.increment("fifty_wins");
        if (naturalBJ)      achievements.increment("first_blackjack");
        if (s.splits > 0)   achievements.setProgress("first_split", 1);
        if (s.doubles > 0)  achievements.setProgress("first_double", 1);
        if (s.surrenders > 0) achievements.setProgress("first_surrender", 1);
        if (playerWon && engine.dealer().isBust()) achievements.increment("survived_bust");
        // High-water marks: never erase a bankroll peak the player already hit.
        achievements.liftProgress("bankroll_5k",  Math.min(5000,  engine.bankroll()));
        achievements.liftProgress("bankroll_10k", Math.min(10000, engine.bankroll()));
        // Heart of Stone: 5 winning rounds in a row (a push keeps the streak).
        // setProgress may lower — a loss must zero the saved progress, not just
        // the session counter.
        if (playerWon)       winStreak++;
        else if (playerLost) winStreak = 0;
        achievements.setProgress("survived_bust_streak", winStreak);

        if (playerWon)       lossStreak = 0;
        else if (playerLost) lossStreak++;

        say(mostNotable(outcomes, playerWon, playerLost));
        // Crash mid-session must not rewind bankroll/stats (GDX already persists).
        save.save(engine);
        achievements.save();
    }


    /**
     * Pick the single most remark-worthy thing about a finished round.
     *
     * <p>One event, not several: the chatter enforces a gap between remarks, so
     * offering it five things would just mean four get silently dropped in
     * whatever order the code happened to check them. Choosing deliberately
     * means the table comments on the blackjack rather than the push that
     * happened on the other split hand.
     */
    private TableEvent mostNotable(List<Outcome> outcomes, boolean playerWon, boolean playerLost) {
        if (outcomes.contains(Outcome.BLACKJACK))     return TableEvent.PLAYER_BLACKJACK;

        // Rarities first: these are the things a table actually turns to look at,
        // and they'd never be heard if the ordinary win/loss outranked them.
        if (playerWon && engine.hands().stream().anyMatch(h -> h.size() >= 5))
            return TableEvent.FIVE_CARD_HAND;
        if (playerWon && engine.hands().stream().anyMatch(h -> h.doubled()))
            return TableEvent.DOUBLE_WIN;
        // Threshold against the pre-settlement stack. Post-payout bankroll
        // includes the win, so a $100 win from $400 (25%) missed BIG_WIN when
        // the bar became max(100, 500/4)=125.
        int stackBefore = engine.bankroll() - engine.lastNet();
        if (engine.lastNet() >= Math.max(100, Math.max(0, stackBefore) / 4))
            return TableEvent.BIG_WIN;
        if (playerWon && engine.hands().stream()
                .anyMatch(h -> h.value() == 21 && h.size() >= 3))
            return TableEvent.TWENTY_ONE;

        if (engine.dealer().isBust() && playerWon)    return TableEvent.DEALER_BUST;
        if (engine.dealer().isBlackjack())            return TableEvent.DEALER_BLACKJACK;

        // Losing by exactly one point stings more than losing badly, so it gets
        // its own reaction rather than disappearing into PLAYER_LOSS.
        if (playerLost && !outcomes.contains(Outcome.BUST) && isCloseCall())
            return TableEvent.CLOSE_CALL;

        if (outcomes.contains(Outcome.BUST))          return TableEvent.PLAYER_BUST;
        if (outcomes.contains(Outcome.SURRENDER))     return TableEvent.PLAYER_SURRENDER;

        // Streaks outrank a plain win or loss — they're the more interesting
        // thing to notice, and they're what a real table would comment on.
        if (winStreak  >= 3) return TableEvent.HOT_STREAK;
        if (lossStreak >= 3) return TableEvent.COLD_STREAK;

        if (engine.bankroll() > 0 && engine.bankroll() <= 100) return TableEvent.LOW_CHIPS;
        if (engine.stats().hands >= 60 && engine.stats().hands % 25 == 0)
            return TableEvent.LONG_SESSION;
        if (engine.bankroll() >= 2000) return TableEvent.RUNNING_WELL;

        if (playerWon)  return TableEvent.PLAYER_WIN;
        if (playerLost) return TableEvent.PLAYER_LOSS;
        return TableEvent.PUSH;
    }

    /** True when a surviving hand lost to the dealer by exactly one point. */
    private boolean isCloseCall() {
        int dv = engine.dealer().value();
        if (dv > 21) return false;
        return engine.hands().stream()
                .anyMatch(h -> !h.isBust() && !h.surrendered() && dv - h.value() == 1);
    }

    /**
     * Dealer showing 4, 5, or 6 — the up-cards that bust them most often, and
     * the thing a real table always remarks on.
     */
    private boolean dealerShowsWeakCard() {
        if (engine.dealer().isEmpty()) return false;
        int up = engine.dealer().first().rank().value();
        return up >= 4 && up <= 6;
    }

    /** Offer an event to the table and repaint if anybody took it up. */
    private void say(TableEvent event) {
        if (chatter.react(event, System.currentTimeMillis()).isEmpty()) return;
        table.repaint();
        if (chatterTimer != null && !chatterTimer.isRunning()) chatterTimer.start();
    }

    /**
     * Feed the Hi-Lo counter every card that has appeared since the last check.
     *
     * <p>Called after each action rather than only at round end, so the count
     * reflects the hand in progress — which is the only time a counter is any
     * use. Identity-tracked via {@link TableObservation} so a split that
     * reorders the table cannot re-observe the dealer's up-card.
     */
    private void observeNewCards() {
        observation.sync(engine);
        if (observation.reshuffled()) say(TableEvent.SHUFFLE);
    }

    private String describe() {
        switch (engine.phase()) {
            case BETTING:
                if (engine.bankroll() <= 0 && engine.pendingBet() == 0) return "Out of chips!";
                // The round has already settled by the time the UI sees BETTING
                // again, so the result of the hand just played is shown here --
                // there is no observable SETTLE phase to hang it off.
                return lastResult.isEmpty() ? "Place your bet." : lastResult + "   ·   Place your bet.";
            case INSURANCE: return "Dealer shows Ace — insurance?";
            case PLAYER:    return "Hand " + (engine.activeIndex() + 1) + " of "
                    + engine.hands().size() + " — your move.";
            case DEALER:    return "Dealer playing…";
            default:        return "";
        }
    }

    /**
     * Render the engine's per-hand outcomes plus the round's net as a status
     * line, e.g. {@code "Hand 1: Win   Hand 2: Bust   -$10"}.
     */
    private String summarize(List<Outcome> outcomes, int net) {
        if (outcomes.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean multi = outcomes.size() > 1;
        for (int i = 0; i < outcomes.size(); i++) {
            if (multi) sb.append("Hand ").append(i + 1).append(": ");
            sb.append(outcomes.get(i).label()).append("   ");
        }
        if (engine.dealer().isBlackjack()) sb.append("(dealer blackjack)   ");
        if (net > 0)      sb.append("+$").append(net);
        // Plain ASCII hyphen, not U+2212 MINUS SIGN: this prefixes a money
        // amount, so a charset problem here would garble the one character that
        // says the player lost. Not worth the typography.
        else if (net < 0) sb.append("-$").append(-net);
        else              sb.append("even");
        return sb.toString().trim();
    }

    private void showStats() {
        SessionStats s = engine.stats();
        String text = "Hands played: " + s.hands + "\n"
                + "Wins:           " + s.wins + "\n"
                + "Losses:         " + s.losses + "\n"
                + "Pushes:         " + s.pushes + "\n"
                + "Blackjacks:     " + s.blackjacks + "\n"
                + "Busts:          " + s.busts + "\n"
                + "Doubles:        " + s.doubles + "\n"
                + "Splits:         " + s.splits + "\n"
                + "Surrenders:     " + s.surrenders + "\n"
                + String.format("Win rate:       %.1f%%%n", s.winRate() * 100)
                + "Peak bankroll:  $" + s.peakBankroll + "\n"
                + "Total wagered:  $" + s.totalWagered + "\n"
                + "Total returned: $" + s.totalReturned + "\n"
                + "Net:            $" + s.net();
        JOptionPane.showMessageDialog(this, text, "Session stats", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRules() {
        var r = engine.rules();
        String text =
                "BlackJack Pro — house rules\n\n"
              + "  * " + r.decks + "-deck shoe, "
                       + (int)(r.penetration * 100) + "% penetration\n"
              + "  * Blackjack pays " + r.blackjackPayoutNum + ":"
                                      + r.blackjackPayoutDen + "\n"
              + "  * Dealer " + (r.dealerHitsSoft17 ? "hits" : "stands") + " on soft 17\n"
              + "  * Double on any first two cards\n"
              + "  * Split up to " + (r.maxSplits + 1) + " hands\n"
              + (r.lateSurrender
                    ? "  * Late surrender on first two cards\n"
                    : "  * Late surrender off\n")
              + (r.offerInsurance
                    ? "  * Insurance offered on dealer Ace; pays 2:1\n"
                    : "  * Insurance not offered\n")
              + "  * Odd half-dollars round in your favour";
        JOptionPane.showMessageDialog(this, text, "Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSession() {
        int yes = JOptionPane.showConfirmDialog(this,
                "Reset bankroll to $1000 and clear stats?",
                "New Session", JOptionPane.YES_NO_OPTION);
        if (yes != JOptionPane.YES_OPTION) return;
        // abandonRound() clears any mid-hand cards and returns to BETTING.
        // clearBet() alone is a no-op once cards are out, which left
        // phase=PLAYER, Deal disabled, and a free $1000 next to a live hand.
        engine.abandonRound();
        engine.stats().reset();
        engine.setBankroll(1000);
        processedHands = 0;
        winStreak      = 0;
        lossStreak     = 0;
        lastResult     = "";
        reloadDeclined = false;
        chatter.clear();
        sideBets.clear();
        sideMsg = "";
        engine.shoe().reshuffle();
        observation.reset();
        // Durable — a kill after New Session must not resurrect the old stack.
        save.save(engine);
        achievements.setProgress("survived_bust_streak", 0);
        achievements.save();
        updateUi("New session. Place your bet.");
    }


    private void updateUi(String msg) {
        SwingUtilities.invokeLater(() -> {
            refresh(msg);
            maybeOfferReload();
        });
    }

    /** Repaint every control from current engine state. Must run on the EDT. */
    private void refresh(String msg) {
        statusBar.setText(msg);
        bankLabel.setText("Bankroll: $" + engine.bankroll());
        betLabel.setText("Bet: $" + displayedBet());
        shoeLabel.setText("Shoe: " + engine.shoe().remaining() + " cards");

        boolean betting = engine.phase() == Phase.BETTING;
        boolean playing = engine.phase() == Phase.PLAYER;
        boolean insure  = engine.phase() == Phase.INSURANCE;
        if (sideBets.available()) {
            if (betting && sideBets.pending() > 0) sideLabel.setText("21+3 bet: $" + sideBets.pending());
            else if (!sideMsg.isEmpty())           sideLabel.setText(sideMsg);
            else                                   sideLabel.setText("21+3 ready");
            if (bSide != null) bSide.setEnabled(betting);
        }
        if (showCount) {
                double dr = HiLoCounter.decksRemaining(engine.shoe().remaining());
                HiLoCounter c = observation.counter();
                countLabel.setText(String.format("Count: %+d (TC %+.1f)", c.runningCount(), c.trueCount(dr)));
            } else countLabel.setText("");
        for (JButton b : chipBtns) b.setEnabled(betting);
        bClear.setEnabled(betting && (engine.pendingBet() > 0 || sideBets.pending() > 0));
        bDeal.setEnabled(engine.canDeal());
        bHit.setEnabled(engine.canHit());
        bStand.setEnabled(engine.canStand());
        bDouble.setEnabled(engine.canDouble());
        bSplit.setEnabled(engine.canSplit());
        bSurrender.setEnabled(engine.canSurrender());
        bIns.setEnabled(engine.canInsure());
        bNoIns.setEnabled(insure);
        table.repaint();
    }

    /**
     * Offer a reload once the player is broke. Guarded against re-entry: the
     * modal dialog pumps the event queue, so any {@code updateUi} already
     * queued would run inside it and stack a second identical prompt.
     */
    private void maybeOfferReload() {
        if (reloadPrompted || reloadDeclined) return;
        if (engine.phase() != Phase.BETTING) return;
        if (engine.bankroll() > 0 || engine.pendingBet() != 0 || sideBets.pending() != 0) {
            // Chips came back (reload elsewhere / win) — allow a future prompt.
            reloadDeclined = false;
            return;
        }

        reloadPrompted = true;
        try {
            int yes = JOptionPane.showConfirmDialog(this,
                    "You're out of chips! Reload $1000?", "Bust",
                    JOptionPane.YES_NO_OPTION);
            if (yes == JOptionPane.YES_OPTION) {
                engine.setBankroll(1000);
                reloadDeclined = false;
                refresh("Reloaded $1000. Place your bet.");   // HUD still read $0
            } else {
                // Remember No until the player has chips again.
                reloadDeclined = true;
            }
        } finally {
            reloadPrompted = false;
        }
    }


    /**
     * Briefly tint the status bar to flag a rejected action.
     *
     * <p>Restores the {@link #STATUS_BG} constant rather than whatever colour
     * happened to be showing: capturing the current background meant a second
     * flash within the timer window captured the flash colour as "normal" and
     * left the bar red for the rest of the session. One reusable timer, so
     * repeated flashes extend the tint instead of racing each other.
     */
    private void flash(String msg) {
        statusBar.setText(msg);
        statusBar.setBackground(FLASH_BG);
        if (flashTimer == null) {
            flashTimer = new Timer(900, e -> statusBar.setBackground(STATUS_BG));
            flashTimer.setRepeats(false);
        }
        flashTimer.restart();
    }

    /**
     * Chips the player currently has at risk. During betting that is the
     * pending stake; once cards are out it is the sum across every split hand
     * (the old HUD only showed hand 0, so a double-split $25 table still read
     * "Bet: $25" while $100 sat on the felt).
     */
    private int displayedBet() {
        if (engine.phase() == Phase.BETTING) return engine.pendingBet();
        int total = 0;
        for (var h : engine.hands()) total += h.bet();
        return total;
    }

    /* ----------------------------------------------------------------------- */
    /* Bootstrap                                                               */
    /* ----------------------------------------------------------------------- */

    /**
     * Locate the bundled {@code resources/} directory.
     *
     * <p>Resolving it against the working directory works when the game is run
     * from a checkout, but a {@code jpackage} installer launches with a working
     * directory that is not the install directory — so the packaged build found
     * no music and silently played none. Prefer the directory the running code
     * was loaded from (and its parent, since the jar sits in {@code app/}), and
     * fall back to the working directory for development runs.
     */
    private static Path assetRoot() {
        List<Path> candidates = new java.util.ArrayList<>();
        try {
            Path self = Paths.get(BlackJackProApp.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path dir = Files.isDirectory(self) ? self : self.getParent();
            for (int up = 0; dir != null && up < 3; up++, dir = dir.getParent()) {
                candidates.add(dir.resolve("resources"));
            }
        } catch (Exception ignored) { /* unusual class loader; fall through */ }
        candidates.add(Paths.get("resources"));

        for (Path p : candidates) {
            if (Files.isDirectory(p)) return p;
        }
        return candidates.get(candidates.size() - 1);
    }

    public static void launch() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        // Bundled, read-only assets (music, art) ship next to the app.
        Path assetRoot = assetRoot();
        // Writable state (settings, save, achievements) and trusted plugins live
        // in a fixed per-user data directory — never the process working dir.
        Path dataDir = AppPaths.dataDir();

        GameSettings settings = new GameSettings(dataDir.resolve("settings.properties"));
        MusicService music    = new MusicService(assetRoot.resolve("music"));
        SoundFx      sfx      = new SoundFx();
        sfx.setMuted(!settings.sfxEnabled);
        sfx.setVolume(settings.sfxVolume);
        music.setVolume(settings.musicVolume);
        if (!settings.musicEnabled) music.toggleMute();

        SaveManager  save  = new SaveManager(dataDir.resolve("save.txt"));
        AchievementService achievements = new AchievementService(dataDir.resolve("achievements.txt"));
        PluginRegistry plugins = new PluginRegistry();
        plugins.loadAll(dataDir.resolve("plugins"));

        // Optional Steam integration — only activates if the SDK is on the classpath
        // and steam_appid.txt is present. Replace 0 with your real app id once assigned.
        SteamBridge.init(0);
        SteamBridge.wire(achievements);

        Engine engine = new Engine(1000, new SecureRandom());
        engine.rules().dealerHitsSoft17 = settings.dealerHitsSoft17;
        engine.rules().lateSurrender    = settings.lateSurrender;
        engine.rules().offerInsurance   = settings.offerInsurance;

        // Match on the stable id, not the display name -- "classic" never
        // equalled "Classic Felt", so the saved preference was always ignored.
        TableTheme theme = new ClassicTheme();
        for (TableTheme t : plugins.themes()) {
            if (t.id().equalsIgnoreCase(settings.themeId)) { theme = t; break; }
        }

        if (music.hasTracks()) music.play();

        final TableTheme initialTheme = theme;
        SwingUtilities.invokeLater(() -> {
            new BlackJackProApp(engine, music, sfx, save, plugins, settings,
                    achievements, initialTheme).setVisible(true);
        });
    }
}
