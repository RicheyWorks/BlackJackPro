package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.achievement.AchievementService;
import com.richeyworks.blackjack.engine.BasicStrategy;
import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.engine.SessionStats;
import com.richeyworks.blackjack.media.MusicService;
import com.richeyworks.blackjack.media.SoundFx;
import com.richeyworks.blackjack.persist.SaveManager;
import com.richeyworks.blackjack.plugin.PluginRegistry;
import com.richeyworks.blackjack.plugin.SideBetManager;
import com.richeyworks.blackjack.plugin.TableTheme;
import com.richeyworks.blackjack.plugins.builtin.HiLoCounterAi;
import com.richeyworks.blackjack.settings.GameSettings;
import com.richeyworks.blackjack.steam.SteamBridge;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

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

    private int previousBankroll;
    private int previousWins;
    private int processedHands;   // rounds already post-processed (round-complete detection)
    private int winStreak;        // consecutive winning rounds (streak achievement)

    private final JLabel statusBar = new JLabel(" ");
    private final JLabel bankLabel = new JLabel();
    private final JLabel betLabel  = new JLabel();
    private final JLabel shoeLabel = new JLabel();

    private JButton bDeal, bHit, bStand, bDouble, bSplit, bSurrender, bHint;
    private JButton bIns, bNoIns;
    private JButton[] chipBtns;
    private JButton bSide;
    private final JLabel sideLabel = new JLabel();
    private final SideBetManager sideBets;
    private String sideMsg = "";
    private final HiLoCounterAi counter;
    private int lastShoeRemaining;
    private boolean showCount = true;
    private final JLabel countLabel = new JLabel();

    private static final int[] CHIP_VALUES = {1, 5, 25, 100, 500};

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
        HiLoCounterAi hilo = null;
        for (var ai : plugins.aiStrategies()) if (ai instanceof HiLoCounterAi h) { hilo = h; break; }
        this.counter = hilo;
        this.lastShoeRemaining = engine.shoe().remaining();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        save.load(engine);
        previousBankroll = engine.bankroll();
        previousWins     = engine.stats().wins;
        processedHands   = engine.stats().hands;
        achievements.onUnlock(a -> {
            sfx.achievement();
            AchievementToast.show(this, a);
        });

        table = new TablePanel(engine, theme);
        add(table, BorderLayout.CENTER);
        add(buildControlBar(), BorderLayout.SOUTH);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBar.setForeground(new Color(0xF8E9A1));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(0x0E2E1A));
        add(statusBar, BorderLayout.NORTH);
        setJMenuBar(buildMenuBar());

        setSize(1200, 820);
        setLocationRelativeTo(null);
        updateUi("Place your bet to begin.");

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                save.save(engine);
                settings.save();
                achievements.save();
                music.stop();
                sfx.shutdown();
                plugins.shutdown();
                SteamBridge.shutdown();
            }
        });
    }

    /* ----------------------------------------------------------------------- */
    /* Layout                                                                  */
    /* ----------------------------------------------------------------------- */

    private JPanel buildControlBar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(0x0D2A18));
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
        JButton clear = pirateButton("Clear");
        clear.addActionListener(e -> clearBet());
        c.gridx = CHIP_VALUES.length;
        p.add(clear, c);

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
        bHint       = pirateButton("Hint");
        bIns        = pirateButton("Insure");
        bNoIns      = pirateButton("Decline");

        bDeal.addActionListener(e -> dealRound());
        bHit.addActionListener(e -> safe(engine::hit, null));
        bStand.addActionListener(e -> safe(engine::stand, null));
        bDouble.addActionListener(e -> safe(engine::doubleDown, "Cannot double."));
        bSplit.addActionListener(e -> safe(engine::split, "Cannot split."));
        bSurrender.addActionListener(e -> safe(engine::surrender, null));
        bHint.addActionListener(e -> showHint());
        bIns.addActionListener(e -> takeInsurance(true));
        bNoIns.addActionListener(e -> takeInsurance(false));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        actions.add(bDeal); actions.add(bHit); actions.add(bStand);
        actions.add(bDouble); actions.add(bSplit); actions.add(bSurrender);
        actions.add(bIns); actions.add(bNoIns); actions.add(bHint);

        c.gridx = 0; c.gridy = 1; c.gridwidth = CHIP_VALUES.length + 1;
        c.anchor = GridBagConstraints.CENTER;
        p.add(actions, c);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        info.setOpaque(false);
        Font hud = bankLabel.getFont().deriveFont(Font.BOLD, 16f);
        bankLabel.setForeground(new Color(0xF8E9A1));
        betLabel.setForeground(new Color(0xF8E9A1));
        shoeLabel.setForeground(new Color(0xBDBDBD));
        bankLabel.setFont(hud); betLabel.setFont(hud);
        sideLabel.setForeground(new Color(0xC9A227));
        countLabel.setForeground(new Color(0x9FE0B0));
        info.add(bankLabel); info.add(betLabel);
        if (sideBets.available()) info.add(sideLabel);
        info.add(shoeLabel);
        if (counter != null) info.add(countLabel);
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
        quit.addActionListener(e -> { save.save(engine); System.exit(0); });
        game.add(newGame); game.add(stats); game.addSeparator(); game.add(rules);
        game.addSeparator(); game.add(quit);

        JMenu opts = new JMenu("Options");
        JMenuItem prefs = new JMenuItem("Preferences…");
        prefs.addActionListener(e -> new SettingsDialog(this, settings, engine, music, sfx).setVisible(true));
        JCheckBoxMenuItem h17 = new JCheckBoxMenuItem("Dealer hits soft 17");
        h17.setState(engine.rules().dealerHitsSoft17);
        h17.addActionListener(e -> engine.rules().dealerHitsSoft17 = h17.getState());
        JCheckBoxMenuItem countToggle = new JCheckBoxMenuItem("Show Hi-Lo count");
        countToggle.setState(showCount);
        countToggle.setEnabled(counter != null);
        countToggle.addActionListener(e -> { showCount = countToggle.getState(); updateUi(statusBar.getText()); });
        JMenuItem mute = new JMenuItem(music.isMuted() ? "Unmute music" : "Mute music");
        mute.addActionListener(e -> {
            music.toggleMute();
            mute.setText(music.isMuted() ? "Unmute music" : "Mute music");
        });
        JMenuItem nextTrack = new JMenuItem("Next track");
        nextTrack.addActionListener(e -> music.next());
        opts.add(prefs); opts.addSeparator(); opts.add(h17); opts.add(countToggle);
        opts.addSeparator(); opts.add(mute); opts.add(nextTrack);

        JMenu themeMenu = new JMenu("Theme");
        addThemeItem(themeMenu, theme); // Classic always present
        for (TableTheme t : plugins.themes()) addThemeItem(themeMenu, t);

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
            sb.append(a.unlocked() ? "★ " : "☆ ");
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
        item.addActionListener(e -> {
            this.theme = t;
            table.setTheme(t);
        });
        menu.add(item);
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
        b.setBackground(new Color(0x8B5E2B));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x5C3A0F), 1, true),
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
        engine.clearBet();
        int refund = sideBets.clear();
        if (refund > 0) engine.setBankroll(engine.bankroll() + refund);
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
            engine.deal();
            resolveSideBet();
            sfx.cardSnap();
            postAction();
        } catch (RuntimeException ex) {
            flash("Place a bet first.");
        }
    }

    private void resolveSideBet() {
        if (!sideBets.available() || sideBets.pending() == 0) return;
        int stake  = sideBets.pending();
        int payout = sideBets.resolve(engine.hands().get(0).cards(), engine.dealer().first());
        engine.stats().totalWagered  += stake;
        engine.stats().totalReturned += payout;
        engine.setBankroll(engine.bankroll() + payout);
        if (payout > 0) { sfx.winSting(); sideMsg = "21+3 " + sideBets.lastOutcome() + ": +$" + (payout - stake); }
        else            { sideMsg = "21+3: no win (-$" + stake + ")"; }
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
        if (engine.phase() == Phase.BETTING && engine.stats().hands > processedHands) {
            processedHands = engine.stats().hands;
            onRoundComplete();
        }
        updateUi(describe());
    }

    private void onRoundComplete() {
        SessionStats s = engine.stats();
        int wonThisRound = s.wins - previousWins;
        boolean playerWon  = wonThisRound > 0;
        boolean playerLost = !playerWon && !anyHandPushed();

        // Outcome SFX
        boolean naturalBJ = engine.hands().stream().anyMatch(h -> h.isBlackjack());
        if (naturalBJ)      sfx.blackjackFanfare();
        else if (playerWon) sfx.winSting();
        else if (anyHandPushed()) sfx.pushBeep();
        else if (playerLost)      sfx.loseSting();

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
        achievements.setProgress("bankroll_5k",  Math.min(5000,  engine.bankroll()));
        achievements.setProgress("bankroll_10k", Math.min(10000, engine.bankroll()));
        // Heart of Stone: 5 winning rounds in a row (a push keeps the streak alive).
        if (playerWon)       winStreak++;
        else if (playerLost) winStreak = 0;
        achievements.setProgress("survived_bust_streak", winStreak);

        // Feed the Hi-Lo counter every card revealed this round. After a reshuffle
        // the shoe's card count jumps back up, so reset before counting the fresh deal.
        if (counter != null) {
            if (engine.shoe().remaining() > lastShoeRemaining) counter.resetCount();
            for (var hh : engine.hands()) for (Card cc : hh.cards()) counter.observe(cc);
            for (Card cc : engine.dealer().cards()) counter.observe(cc);
            lastShoeRemaining = engine.shoe().remaining();
        }
        previousWins     = s.wins;
        previousBankroll = engine.bankroll();
    }

    private boolean anyHandPushed() {
        int dv = engine.dealer().value();
        for (var h : engine.hands()) {
            if (h.surrendered() || h.isBust()) continue;
            int pv = h.value();
            if (engine.dealer().isBlackjack() && h.isBlackjack()) return true;
            if (!engine.dealer().isBlackjack() && !h.isBlackjack() && pv == dv && dv <= 21) return true;
        }
        return false;
    }

    private String describe() {
        switch (engine.phase()) {
            case BETTING:   return engine.bankroll() == 0 ? "Out of chips!" : "Place your bet.";
            case INSURANCE: return "Dealer shows Ace — insurance?";
            case PLAYER:    return "Hand " + (engine.activeIndex() + 1) + " of "
                    + engine.hands().size() + " — your move.";
            case DEALER:    return "Dealer playing…";
            case SETTLE:    return roundSummary();
            default:        return "";
        }
    }

    private String roundSummary() {
        StringBuilder sb = new StringBuilder();
        List<com.richeyworks.blackjack.engine.Hand> hs = engine.hands();
        int dv = engine.dealer().value();
        for (int i = 0; i < hs.size(); i++) {
            var h = hs.get(i);
            String tag = hs.size() > 1 ? ("Hand " + (i + 1) + ": ") : "";
            if (h.surrendered())        sb.append(tag).append("Surrendered  ");
            else if (h.isBust())        sb.append(tag).append("Bust  ");
            else if (h.isBlackjack())   sb.append(tag).append("Blackjack!  ");
            else if (engine.dealer().isBlackjack()) sb.append(tag).append("Dealer BJ  ");
            else if (dv > 21 || h.value() > dv) sb.append(tag).append("Win  ");
            else if (h.value() == dv)   sb.append(tag).append("Push  ");
            else                        sb.append(tag).append("Loss  ");
        }
        return sb.toString().trim();
    }

    private void showHint() {
        if (engine.phase() != Phase.PLAYER) return;
        var action = BasicStrategy.recommend(engine.active(), engine.dealer().first());
        String advice = switch (action) {
            case H -> "Basic strategy: HIT";
            case S -> "Basic strategy: STAND";
            case D -> engine.canDouble() ? "Basic strategy: DOUBLE (else hit)" : "Basic strategy: HIT";
            case P -> engine.canSplit()  ? "Basic strategy: SPLIT" : "Basic strategy: HIT";
            case R -> engine.canSurrender() ? "Basic strategy: SURRENDER (else hit)" : "Basic strategy: HIT";
        };
        JOptionPane.showMessageDialog(this, advice, "Hint", JOptionPane.INFORMATION_MESSAGE);
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
        String text =
                "BlackJack Pro — house rules\n\n"
              + "  * " + engine.rules().decks + "-deck shoe, "
                       + (int)(engine.rules().penetration * 100) + "% penetration\n"
              + "  * Blackjack pays " + engine.rules().blackjackPayoutNum + ":"
                                      + engine.rules().blackjackPayoutDen + "\n"
              + "  * Dealer " + (engine.rules().dealerHitsSoft17 ? "hits" : "stands") + " on soft 17\n"
              + "  * Double on any first two cards\n"
              + "  * Split up to " + (engine.rules().maxSplits + 1) + " hands\n"
              + "  * Late surrender on first two cards\n"
              + "  * Insurance offered on dealer Ace; pays 2:1";
        JOptionPane.showMessageDialog(this, text, "Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSession() {
        int yes = JOptionPane.showConfirmDialog(this,
                "Reset bankroll to $1000 and clear stats?",
                "New Session", JOptionPane.YES_NO_OPTION);
        if (yes != JOptionPane.YES_OPTION) return;
        engine.setBankroll(1000);
        engine.stats().reset();
        processedHands = 0;
        winStreak      = 0;
        sideBets.clear();
        sideMsg = "";
        engine.shoe().reshuffle();
        engine.clearBet();
        if (counter != null) counter.resetCount();
        lastShoeRemaining = engine.shoe().remaining();
        updateUi("New session. Place your bet.");
    }

    private void updateUi(String msg) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setText(msg);
            bankLabel.setText("Bankroll: $" + engine.bankroll());
            int displayed = engine.phase() == Phase.BETTING
                    ? engine.pendingBet()
                    : engine.hands().isEmpty() ? 0 : engine.hands().get(0).bet();
            betLabel.setText("Bet: $" + displayed);
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
            if (counter != null) {
                if (showCount) {
                    int dr = Math.max(1, engine.shoe().remaining() / 52);
                    countLabel.setText(String.format("Count: %+d (TC %+.1f)", counter.runningCount(), counter.trueCount(dr)));
                } else countLabel.setText("");
            }
            for (JButton b : chipBtns) b.setEnabled(betting);
            bDeal.setEnabled(engine.canDeal());
            bHit.setEnabled(engine.canHit());
            bStand.setEnabled(engine.canStand());
            bDouble.setEnabled(engine.canDouble());
            bSplit.setEnabled(engine.canSplit());
            bSurrender.setEnabled(engine.canSurrender());
            bIns.setEnabled(insure);
            bNoIns.setEnabled(insure);
            bHint.setEnabled(playing);
            table.repaint();

            if (betting && engine.bankroll() == 0 && engine.pendingBet() == 0
                    && sideBets.pending() == 0) {
                int yes = JOptionPane.showConfirmDialog(this,
                        "You're out of chips! Reload $1000?", "Bust",
                        JOptionPane.YES_NO_OPTION);
                if (yes == JOptionPane.YES_OPTION) engine.setBankroll(1000);
            }
        });
    }

    private void flash(String msg) {
        Color was = statusBar.getBackground();
        statusBar.setText(msg);
        statusBar.setBackground(new Color(0x6E3030));
        new Timer(900, e -> {
            statusBar.setBackground(was);
            ((Timer) e.getSource()).stop();
        }).start();
    }

    /* ----------------------------------------------------------------------- */
    /* Bootstrap                                                               */
    /* ----------------------------------------------------------------------- */

    public static void launch() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        Path resourceRoot = Paths.get("resources");

        GameSettings settings = new GameSettings(resourceRoot.resolve("settings.properties"));
        MusicService music    = new MusicService(resourceRoot.resolve("music"));
        SoundFx      sfx      = new SoundFx();
        sfx.setMuted(!settings.sfxEnabled);
        sfx.setVolume(settings.sfxVolume);
        music.setVolume(settings.musicVolume);
        if (!settings.musicEnabled) music.toggleMute();

        SaveManager  save  = new SaveManager(resourceRoot.resolve("save.txt"));
        AchievementService achievements = new AchievementService(resourceRoot.resolve("achievements.txt"));
        PluginRegistry plugins = new PluginRegistry();
        plugins.loadAll(Paths.get("plugins"));

        // Optional Steam integration — only activates if the SDK is on the classpath
        // and steam_appid.txt is present. Replace 0 with your real app id once assigned.
        SteamBridge.init(0);
        SteamBridge.wire(achievements);

        Engine engine = new Engine(1000, new Random());
        engine.rules().dealerHitsSoft17 = settings.dealerHitsSoft17;
        engine.rules().lateSurrender    = settings.lateSurrender;
        engine.rules().offerInsurance   = settings.offerInsurance;

        TableTheme theme = new ClassicTheme();
        for (TableTheme t : plugins.themes()) {
            if (t.displayName().equalsIgnoreCase(settings.themeId)) { theme = t; break; }
        }

        if (music.hasTracks()) music.play();

        final TableTheme initialTheme = theme;
        SwingUtilities.invokeLater(() -> {
            new BlackJackProApp(engine, music, sfx, save, plugins, settings,
                    achievements, initialTheme).setVisible(true);
        });
    }
}
