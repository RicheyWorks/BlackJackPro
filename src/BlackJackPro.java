import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * BlackJackPro - a self-contained Swing blackjack table.
 *
 * Built to replace / complement the original JavaFX BlackJackGUI which relied on
 * a missing deck file, missing CSS, missing images, missing voice clips, and a
 * missing resource bundle. This file has zero external assets: cards and chips
 * are drawn with Graphics2D, music plays from resources/music (any WAV; MP3 is
 * skipped gracefully on JDKs without JMF). The whole game compiles with stock
 * javac and runs with java.
 *
 * Casino rules implemented:
 *   - 6-deck shoe, ~75% penetration before reshuffle
 *   - 3:2 natural blackjack payout
 *   - Push on tie (stake returned)
 *   - Double-down on any first two cards
 *   - Split up to 4 hands; split aces receive one card only
 *   - Late surrender (half-bet refund) before any other action
 *   - Insurance offered when dealer shows an Ace (pays 2:1)
 *   - Dealer stands on soft 17 by default; toggle in Options for H17
 *   - Basic-strategy hint button (S17 chart)
 *
 * Entry point:  java -cp bin BlackJackPro
 *
 * Author: rewritten/refactored from the audited BlackJackGUI.
 */
public final class BlackJackPro extends JFrame {

    /* ------------------------------------------------------------------ */
    /* Domain model                                                       */
    /* ------------------------------------------------------------------ */

    public enum Suit {
        SPADES("♠", Color.BLACK), HEARTS("♥", new Color(0xC0392B)),
        DIAMONDS("♦", new Color(0xC0392B)), CLUBS("♣", Color.BLACK);
        final String glyph; final Color color;
        Suit(String g, Color c) { glyph = g; color = c; }
    }

    public enum Rank {
        TWO("2",2), THREE("3",3), FOUR("4",4), FIVE("5",5), SIX("6",6),
        SEVEN("7",7), EIGHT("8",8), NINE("9",9), TEN("10",10),
        JACK("J",10), QUEEN("Q",10), KING("K",10), ACE("A",11);
        final String label; final int value;
        Rank(String l, int v) { label = l; value = v; }
    }

    /** Immutable playing card. */
    public static final class GameCard {
        final Rank rank; final Suit suit;
        GameCard(Rank r, Suit s) { rank = r; suit = s; }
        @Override public String toString() { return rank.label + suit.glyph; }
    }

    /** Multi-deck shoe. Deals from the top, reshuffles past the cut. */
    public static final class Shoe {
        private final List<GameCard> cards = new ArrayList<>();
        private final int decks;
        private final Random rng;
        private int cutIndex;

        public Shoe(int decks, Random rng) {
            this.decks = decks;
            this.rng   = rng;
            reshuffle();
        }

        public void reshuffle() {
            cards.clear();
            for (int d = 0; d < decks; d++) {
                for (Suit s : Suit.values())
                    for (Rank r : Rank.values())
                        cards.add(new GameCard(r, s));
            }
            Collections.shuffle(cards, rng);
            // Cut card ~25% from the back -> roughly 75% penetration
            cutIndex = (int) (cards.size() * 0.25);
        }

        public boolean needsShuffle() { return cards.size() <= cutIndex; }

        public GameCard deal() {
            if (cards.isEmpty()) reshuffle();
            return cards.remove(cards.size() - 1);
        }

        public int remaining() { return cards.size(); }
    }

    /** A single hand of cards with bet/state tracking. */
    public static final class Hand {
        public final List<GameCard> cards = new ArrayList<>();
        public int    bet;
        public boolean doubled;
        public boolean surrendered;
        public boolean fromSplit;
        public boolean splitAce;       // split aces get one card and freeze
        public boolean stood;

        public void clear() {
            cards.clear();
            bet = 0;
            doubled = surrendered = fromSplit = splitAce = stood = false;
        }

        /** Highest legal value <= 21, or the smallest bust value if all bust. */
        public int value() {
            int total = 0, aces = 0;
            for (GameCard c : cards) {
                total += c.rank.value;
                if (c.rank == Rank.ACE) aces++;
            }
            while (total > 21 && aces > 0) { total -= 10; aces--; }
            return total;
        }

        /** Hard total ignoring ace flexibility (used for "soft" detection). */
        public int hardValue() {
            int total = 0;
            for (GameCard c : cards) total += c.rank.value;
            int aces = 0;
            for (GameCard c : cards) if (c.rank == Rank.ACE) aces++;
            while (total > 21 && aces > 0) { total -= 10; aces--; }
            return total;
        }

        public boolean isSoft() {
            int total = 0, aces = 0;
            for (GameCard c : cards) {
                total += c.rank.value;
                if (c.rank == Rank.ACE) aces++;
            }
            if (aces == 0) return false;
            // soft if at least one ace can still be counted as 11 with total<=21
            return total <= 21;
        }

        public boolean isBlackjack() {
            return cards.size() == 2 && value() == 21 && !fromSplit;
        }

        public boolean isBust() { return value() > 21; }
        public boolean isPair() {
            return cards.size() == 2 && cards.get(0).rank == cards.get(1).rank;
        }
    }

    /* ------------------------------------------------------------------ */
    /* Engine                                                             */
    /* ------------------------------------------------------------------ */

    public enum Phase { BETTING, DEALING, INSURANCE, PLAYER, DEALER, SETTLE }

    public static final class Engine {
        public final Shoe shoe;
        public final Hand dealer = new Hand();
        public final List<Hand> player = new ArrayList<>();
        public int activeHand = 0;
        public int bankroll;
        public int pendingBet;
        public int insuranceBet;
        public boolean dealerHitsSoft17 = false;
        public Phase phase = Phase.BETTING;
        public final SessionStats stats = new SessionStats();
        public final Deque<String> log = new ArrayDeque<>(256);

        public Engine(int bankroll, Random rng) {
            this.bankroll = bankroll;
            this.shoe     = new Shoe(6, rng);
            this.player.add(new Hand());
        }

        public Hand active() { return player.get(activeHand); }

        public void log(String s) {
            if (log.size() >= 250) log.pollFirst();
            log.addLast(s);
        }

        public boolean canBet(int amt)   { return amt > 0 && amt <= bankroll; }
        public boolean canSplit() {
            Hand h = active();
            return phase == Phase.PLAYER && h.cards.size() == 2 && h.isPair()
                    && bankroll >= h.bet && player.size() < 4;
        }
        public boolean canDouble() {
            Hand h = active();
            return phase == Phase.PLAYER && h.cards.size() == 2 && bankroll >= h.bet
                    && !h.splitAce;
        }
        public boolean canSurrender() {
            Hand h = active();
            return phase == Phase.PLAYER && h.cards.size() == 2 && player.size() == 1
                    && !h.fromSplit && !h.doubled;
        }
        public boolean canHit() {
            Hand h = active();
            return phase == Phase.PLAYER && !h.isBust() && !h.stood && !h.splitAce
                    && h.value() < 21;
        }
        public boolean canStand() {
            return phase == Phase.PLAYER && !active().isBust();
        }
    }

    /** Lifetime session stats. */
    public static final class SessionStats {
        public int hands, wins, losses, pushes, blackjacks, busts, doubles, splits, surrenders;
        public int peakBankroll = 1000;
        public int totalWagered, totalReturned;
        public double winRate() { return hands == 0 ? 0 : (double) wins / hands; }
    }

    /* ------------------------------------------------------------------ */
    /* Basic strategy (S17, no surrender chart for simplicity)            */
    /* ------------------------------------------------------------------ */

    /** Returns one of: H (hit), S (stand), D (double or hit), P (split), R (surrender or hit). */
    public static String basicStrategy(Hand h, GameCard dealerUp) {
        int dv = dealerUp.rank.value;
        if (dv == 11) dv = 11; // Ace shown
        if (h.isPair()) {
            int r = h.cards.get(0).rank.value;
            switch (r) {
                case 11: return "P";                 // A,A
                case 10: return "S";                 // 10,10
                case 9:  return (dv==7||dv==10||dv==11) ? "S" : "P";
                case 8:  return "P";                 // 8,8
                case 7:  return dv<=7 ? "P" : "H";
                case 6:  return dv<=6 ? "P" : "H";
                case 5:  return dv<=9 ? "D" : "H";
                case 4:  return (dv==5||dv==6) ? "P" : "H";
                case 3:
                case 2:  return dv<=7 ? "P" : "H";
            }
        }
        if (h.isSoft()) {
            int t = h.value();
            switch (t) {
                case 20: return "S";
                case 19: return (dv==6) ? "D" : "S";
                case 18: if (dv<=6) return "D";
                         if (dv<=8) return "S";
                         return "H";
                case 17: return dv<=6 ? "D" : "H";
                case 16:
                case 15: return (dv>=4 && dv<=6) ? "D" : "H";
                case 14:
                case 13: return (dv==5 || dv==6) ? "D" : "H";
            }
        }
        int t = h.value();
        if (t >= 17) return "S";
        if (t >= 13) return dv<=6 ? "S" : "H";
        if (t == 12) return (dv>=4 && dv<=6) ? "S" : "H";
        if (t == 11) return "D";
        if (t == 10) return dv<=9 ? "D" : "H";
        if (t ==  9) return (dv>=3 && dv<=6) ? "D" : "H";
        return "H";
    }

    /* ------------------------------------------------------------------ */
    /* Persistence (very small JSON-ish manual format - no Jackson dep)   */
    /* ------------------------------------------------------------------ */

    public static void saveBankroll(int bankroll, SessionStats s, Path file) {
        try {
            Files.createDirectories(file.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("bankroll=").append(bankroll).append('\n');
            sb.append("hands=").append(s.hands).append('\n');
            sb.append("wins=").append(s.wins).append('\n');
            sb.append("losses=").append(s.losses).append('\n');
            sb.append("pushes=").append(s.pushes).append('\n');
            sb.append("blackjacks=").append(s.blackjacks).append('\n');
            sb.append("busts=").append(s.busts).append('\n');
            sb.append("doubles=").append(s.doubles).append('\n');
            sb.append("splits=").append(s.splits).append('\n');
            sb.append("surrenders=").append(s.surrenders).append('\n');
            sb.append("peakBankroll=").append(s.peakBankroll).append('\n');
            sb.append("totalWagered=").append(s.totalWagered).append('\n');
            sb.append("totalReturned=").append(s.totalReturned).append('\n');
            Files.writeString(file, sb.toString());
        } catch (IOException ignored) { }
    }

    public static void loadBankroll(Engine e, Path file) {
        try {
            if (!Files.exists(file)) return;
            for (String line : Files.readAllLines(file)) {
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();
                int n; try { n = Integer.parseInt(v); } catch (NumberFormatException ex) { continue; }
                switch (k) {
                    case "bankroll":      e.bankroll = n; break;
                    case "hands":         e.stats.hands = n; break;
                    case "wins":          e.stats.wins = n; break;
                    case "losses":        e.stats.losses = n; break;
                    case "pushes":        e.stats.pushes = n; break;
                    case "blackjacks":    e.stats.blackjacks = n; break;
                    case "busts":         e.stats.busts = n; break;
                    case "doubles":       e.stats.doubles = n; break;
                    case "splits":        e.stats.splits = n; break;
                    case "surrenders":    e.stats.surrenders = n; break;
                    case "peakBankroll":  e.stats.peakBankroll = n; break;
                    case "totalWagered":  e.stats.totalWagered = n; break;
                    case "totalReturned": e.stats.totalReturned = n; break;
                }
            }
        } catch (IOException ignored) { }
    }

    /* ------------------------------------------------------------------ */
    /* Music (loads any *.wav from resources/music; ignores other types)  */
    /* ------------------------------------------------------------------ */

    public static final class Music {
        private final List<Path> tracks = new ArrayList<>();
        private int idx;
        private Clip clip;
        private float volume = 0.5f;
        private boolean muted;

        public Music(Path dir) {
            if (!Files.isDirectory(dir)) return;
            try (var s = Files.list(dir)) {
                s.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".wav"))
                 .forEach(tracks::add);
            } catch (IOException ignored) { }
        }

        public boolean hasTracks() { return !tracks.isEmpty(); }

        public void play() {
            if (tracks.isEmpty()) return;
            stop();
            try (AudioInputStream in = AudioSystem.getAudioInputStream(tracks.get(idx).toFile())) {
                clip = AudioSystem.getClip();
                clip.open(in);
                applyVolume();
                clip.addLineListener(ev -> { if (ev.getType() == LineEvent.Type.STOP) next(); });
                if (!muted) clip.start();
            } catch (Exception ignored) { }
        }

        public void next() {
            if (tracks.isEmpty()) return;
            idx = (idx + 1) % tracks.size();
            play();
        }

        public void stop() {
            if (clip != null) { clip.stop(); clip.close(); clip = null; }
        }

        public void toggleMute() {
            muted = !muted;
            if (clip == null) return;
            if (muted) clip.stop();
            else       clip.start();
        }

        public boolean isMuted() { return muted; }

        public void setVolume(float v) { volume = Math.max(0f, Math.min(1f, v)); applyVolume(); }

        private void applyVolume() {
            if (clip == null) return;
            try {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float db = volume <= 0.0001f ? gain.getMinimum()
                                              : (float)(20.0 * Math.log10(volume));
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
            } catch (Exception ignored) { }
        }
    }

    /* ------------------------------------------------------------------ */
    /* Card / chip renderers                                              */
    /* ------------------------------------------------------------------ */

    public static final int CARD_W = 96, CARD_H = 134;

    public static void paintCardBack(Graphics2D g, int x, int y) {
        Shape r = new RoundRectangle2D.Float(x, y, CARD_W, CARD_H, 14, 14);
        g.setColor(new Color(0x14213D));
        g.fill(r);
        g.setColor(new Color(0x1E3A5F));
        // crosshatch
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1.2f));
        Shape clip = g.getClip();
        g.setClip(r);
        for (int i = -CARD_H; i < CARD_W + CARD_H; i += 8) {
            g.drawLine(x + i, y, x + i - CARD_H, y + CARD_H);
            g.drawLine(x + i, y + CARD_H, x + i - CARD_H, y);
        }
        g.setClip(clip);
        g.setStroke(old);
        g.setColor(new Color(0xC9A227));
        g.draw(r);
        g.setColor(new Color(0xF1F1F1));
        Font f = g.getFont().deriveFont(Font.BOLD, 14f);
        g.setFont(f);
        String s = "BJ";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, x + (CARD_W - fm.stringWidth(s)) / 2, y + CARD_H / 2 + fm.getAscent() / 2 - 2);
    }

    public static void paintCardFace(Graphics2D g, int x, int y, GameCard card) {
        Shape r = new RoundRectangle2D.Float(x, y, CARD_W, CARD_H, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(r);
        g.setColor(new Color(0x2B2B2B));
        g.draw(r);

        g.setColor(card.suit.color);
        Font corner = g.getFont().deriveFont(Font.BOLD, 18f);
        g.setFont(corner);
        g.drawString(card.rank.label, x + 8, y + 22);
        Font glyph = g.getFont().deriveFont(Font.PLAIN, 16f);
        g.setFont(glyph);
        g.drawString(card.suit.glyph, x + 8, y + 40);

        // rotated bottom-right
        AffineTransform old = g.getTransform();
        g.translate(x + CARD_W, y + CARD_H);
        g.rotate(Math.PI);
        g.setFont(corner);
        g.drawString(card.rank.label, 8, 22);
        g.setFont(glyph);
        g.drawString(card.suit.glyph, 8, 40);
        g.setTransform(old);

        // center pip / face
        Font big = g.getFont().deriveFont(Font.BOLD, 48f);
        g.setFont(big);
        String mid = (card.rank == Rank.JACK || card.rank == Rank.QUEEN || card.rank == Rank.KING)
                ? card.rank.label : card.suit.glyph;
        FontMetrics fm = g.getFontMetrics();
        int sw = fm.stringWidth(mid);
        g.drawString(mid, x + (CARD_W - sw) / 2, y + CARD_H / 2 + fm.getAscent() / 2 - 6);
    }

    public static void paintChip(Graphics2D g, int cx, int cy, int radius, int value) {
        Color edge, face;
        switch (value) {
            case 1:    face = new Color(0xECECEC); edge = new Color(0x9E9E9E); break;
            case 5:    face = new Color(0xC0392B); edge = new Color(0x922B21); break;
            case 25:   face = new Color(0x2E7D32); edge = new Color(0x1B5E20); break;
            case 100:  face = new Color(0x1B1B1B); edge = new Color(0x000000); break;
            case 500:  face = new Color(0x6A1B9A); edge = new Color(0x4A148C); break;
            default:   face = new Color(0xF1C40F); edge = new Color(0xB7950B);
        }
        g.setColor(edge);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setColor(face);
        g.fillOval(cx - radius + 4, cy - radius + 4, radius * 2 - 8, radius * 2 - 8);
        g.setColor(Color.WHITE);
        Stroke os = g.getStroke();
        g.setStroke(new BasicStroke(2f));
        g.drawOval(cx - radius + 2, cy - radius + 2, radius * 2 - 4, radius * 2 - 4);
        g.setStroke(os);
        g.setColor(value == 100 || value == 500 ? Color.WHITE : Color.BLACK);
        Font f = g.getFont().deriveFont(Font.BOLD, 14f);
        g.setFont(f);
        String s = String.valueOf(value);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 2);
    }

    /* ------------------------------------------------------------------ */
    /* Table panel                                                        */
    /* ------------------------------------------------------------------ */

    final Engine engine;
    final Music music;
    final TablePanel table;
    final JLabel statusBar = new JLabel(" ");
    final JLabel bankLabel = new JLabel();
    final JLabel betLabel  = new JLabel();
    final JLabel shoeLabel = new JLabel();

    JButton bDeal, bHit, bStand, bDouble, bSplit, bSurrender, bHint, bMute, bNew;
    JButton bIns, bNoIns;
    JButton[] chipBtns;

    public BlackJackPro() {
        super("BlackJack Pro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        engine = new Engine(1000, new Random());
        loadBankroll(engine, Paths.get("resources", "save.txt"));
        engine.stats.peakBankroll = Math.max(engine.stats.peakBankroll, engine.bankroll);
        music  = new Music(Paths.get("resources", "music"));
        if (music.hasTracks()) music.play();

        table = new TablePanel();
        add(table, BorderLayout.CENTER);

        JPanel south = buildControlBar();
        add(south, BorderLayout.SOUTH);

        JMenuBar mb = buildMenuBar();
        setJMenuBar(mb);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBar.setForeground(new Color(0xF8E9A1));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(0x0E2E1A));
        add(statusBar, BorderLayout.NORTH);

        setSize(1200, 820);
        setLocationRelativeTo(null);
        updateUi("Place your bet to begin.");

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                saveBankroll(engine.bankroll, engine.stats, Paths.get("resources", "save.txt"));
                music.stop();
            }
        });
    }

    JPanel buildControlBar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(0x0D2A18));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.gridy  = 0;

        // chip buttons
        int[] chips = {1, 5, 25, 100, 500};
        chipBtns = new JButton[chips.length];
        for (int i = 0; i < chips.length; i++) {
            final int v = chips[i];
            JButton b = chipButton(v);
            b.addActionListener(e -> addBet(v));
            c.gridx = i;
            p.add(b, c);
            chipBtns[i] = b;
        }
        JButton clear = pirateButton("Clear");
        clear.addActionListener(e -> clearBet());
        c.gridx = chips.length;
        p.add(clear, c);

        // action buttons
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
        bHit.addActionListener(e -> playerHit());
        bStand.addActionListener(e -> playerStand());
        bDouble.addActionListener(e -> playerDouble());
        bSplit.addActionListener(e -> playerSplit());
        bSurrender.addActionListener(e -> playerSurrender());
        bHint.addActionListener(e -> showHint());
        bIns.addActionListener(e -> takeInsurance(true));
        bNoIns.addActionListener(e -> takeInsurance(false));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);
        actions.add(bDeal);
        actions.add(bHit);
        actions.add(bStand);
        actions.add(bDouble);
        actions.add(bSplit);
        actions.add(bSurrender);
        actions.add(bIns);
        actions.add(bNoIns);
        actions.add(bHint);

        c.gridx = 0; c.gridy = 1; c.gridwidth = chips.length + 1;
        c.anchor = GridBagConstraints.CENTER;
        p.add(actions, c);

        // status row
        c.gridy = 2; c.gridx = 0; c.gridwidth = chips.length + 1;
        JPanel info = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        info.setOpaque(false);
        bankLabel.setForeground(new Color(0xF8E9A1));
        betLabel.setForeground(new Color(0xF8E9A1));
        shoeLabel.setForeground(new Color(0xBDBDBD));
        Font hud = bankLabel.getFont().deriveFont(Font.BOLD, 16f);
        bankLabel.setFont(hud);
        betLabel.setFont(hud);
        info.add(bankLabel);
        info.add(betLabel);
        info.add(shoeLabel);
        p.add(info, c);

        return p;
    }

    JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu game = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Session (reset bankroll)");
        newGame.addActionListener(e -> resetSession());
        JMenuItem stats = new JMenuItem("Stats");
        stats.addActionListener(e -> showStats());
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(e -> {
            saveBankroll(engine.bankroll, engine.stats, Paths.get("resources", "save.txt"));
            System.exit(0);
        });
        game.add(newGame);
        game.add(stats);
        game.addSeparator();
        game.add(quit);

        JMenu opts = new JMenu("Options");
        JCheckBoxMenuItem h17 = new JCheckBoxMenuItem("Dealer hits soft 17");
        h17.setState(engine.dealerHitsSoft17);
        h17.addActionListener(e -> engine.dealerHitsSoft17 = h17.getState());
        bMute = new JButton();
        JMenuItem mute = new JMenuItem(music.isMuted() ? "Unmute music" : "Mute music");
        mute.addActionListener(e -> {
            music.toggleMute();
            mute.setText(music.isMuted() ? "Unmute music" : "Mute music");
        });
        JMenuItem nextTrack = new JMenuItem("Next track");
        nextTrack.addActionListener(e -> music.next());
        opts.add(h17);
        opts.addSeparator();
        opts.add(mute);
        opts.add(nextTrack);

        JMenu help = new JMenu("Help");
        JMenuItem rules = new JMenuItem("Rules");
        rules.addActionListener(e -> showRules());
        help.add(rules);

        bar.add(game);
        bar.add(opts);
        bar.add(help);
        return bar;
    }

    JButton chipButton(int v) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                paintChip(g2, getWidth()/2, getHeight()/2, Math.min(getWidth(), getHeight())/2 - 2, v);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(54, 54));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setToolTipText("Bet $" + v);
        return b;
    }

    JButton pirateButton(String text) {
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

    /* ------------------------------------------------------------------ */
    /* Game flow                                                          */
    /* ------------------------------------------------------------------ */

    void addBet(int v) {
        if (engine.phase != Phase.BETTING) return;
        if (!engine.canBet(v)) {
            flash("Not enough chips for that bet.");
            return;
        }
        engine.pendingBet += v;
        engine.bankroll  -= v;
        updateUi("Bet: $" + engine.pendingBet + ".  Deal when ready.");
    }

    void clearBet() {
        if (engine.phase != Phase.BETTING) return;
        engine.bankroll  += engine.pendingBet;
        engine.pendingBet = 0;
        updateUi("Bet cleared.");
    }

    void dealRound() {
        if (engine.phase != Phase.BETTING) return;
        if (engine.pendingBet <= 0) { flash("Place a bet first."); return; }
        if (engine.shoe.needsShuffle()) {
            engine.shoe.reshuffle();
            flash("Shoe shuffled.");
        }
        engine.player.clear();
        engine.dealer.clear();
        Hand h = new Hand();
        h.bet = engine.pendingBet;
        engine.stats.totalWagered += h.bet;
        engine.pendingBet = 0;
        engine.player.add(h);
        engine.activeHand = 0;
        engine.phase = Phase.DEALING;

        // Deal P, D, P, D
        h.cards.add(engine.shoe.deal());
        engine.dealer.cards.add(engine.shoe.deal());
        h.cards.add(engine.shoe.deal());
        engine.dealer.cards.add(engine.shoe.deal());
        engine.stats.hands++;

        // Check naturals & insurance
        if (engine.dealer.cards.get(0).rank == Rank.ACE) {
            engine.phase = Phase.INSURANCE;
            updateUi("Dealer shows an Ace. Insurance? Costs $" + (h.bet / 2) + ".");
            return;
        }
        afterInsuranceCheck();
    }

    void takeInsurance(boolean yes) {
        if (engine.phase != Phase.INSURANCE) return;
        Hand h = engine.player.get(0);
        if (yes) {
            int cost = h.bet / 2;
            if (engine.bankroll < cost) { flash("Not enough chips to insure."); return; }
            engine.bankroll -= cost;
            engine.insuranceBet = cost;
        } else {
            engine.insuranceBet = 0;
        }
        afterInsuranceCheck();
    }

    void afterInsuranceCheck() {
        Hand h = engine.player.get(0);
        boolean dealerBJ = engine.dealer.value() == 21;
        if (dealerBJ) {
            if (engine.insuranceBet > 0) {
                // insurance pays 2:1 -> get back insuranceBet * 3
                int pay = engine.insuranceBet * 3;
                engine.bankroll += pay;
                engine.stats.totalReturned += pay;
            }
            engine.insuranceBet = 0;
            engine.phase = Phase.SETTLE;
            settle();
            return;
        } else if (engine.insuranceBet > 0) {
            // dealer didn't have BJ -> insurance lost
            engine.insuranceBet = 0;
        }
        if (h.isBlackjack()) {
            engine.phase = Phase.SETTLE;
            settle();
            return;
        }
        engine.phase = Phase.PLAYER;
        updateUi("Your move.");
    }

    void playerHit() {
        if (!engine.canHit()) return;
        Hand h = engine.active();
        h.cards.add(engine.shoe.deal());
        if (h.isBust()) advanceHand();
        else if (h.value() == 21) advanceHand();
        else updateUi("Hit. " + describeActive());
    }

    void playerStand() {
        if (!engine.canStand()) return;
        engine.active().stood = true;
        advanceHand();
    }

    void playerDouble() {
        if (!engine.canDouble()) return;
        Hand h = engine.active();
        engine.bankroll -= h.bet;
        engine.stats.totalWagered += h.bet;
        h.bet *= 2;
        h.doubled = true;
        engine.stats.doubles++;
        h.cards.add(engine.shoe.deal());
        advanceHand();
    }

    void playerSplit() {
        if (!engine.canSplit()) return;
        Hand h = engine.active();
        Hand n = new Hand();
        n.cards.add(h.cards.remove(1));
        n.bet = h.bet;
        n.fromSplit = true;
        h.fromSplit = true;
        engine.bankroll -= h.bet;
        engine.stats.totalWagered += h.bet;
        engine.stats.splits++;
        engine.player.add(engine.activeHand + 1, n);
        // deal one to each
        h.cards.add(engine.shoe.deal());
        n.cards.add(engine.shoe.deal());
        if (h.cards.get(0).rank == Rank.ACE) {
            h.splitAce = n.splitAce = true;
            advanceHand(); // skip first
            advanceHand(); // skip second too — both freeze
            return;
        }
        updateUi("Hand split. Playing hand " + (engine.activeHand + 1) + " of " + engine.player.size() + ".");
    }

    void playerSurrender() {
        if (!engine.canSurrender()) return;
        Hand h = engine.active();
        h.surrendered = true;
        engine.stats.surrenders++;
        // return half the bet
        engine.bankroll += h.bet / 2;
        engine.stats.totalReturned += h.bet / 2;
        engine.phase = Phase.SETTLE;
        settle();
    }

    void advanceHand() {
        // advance until we find a playable hand or run out
        while (true) {
            engine.activeHand++;
            if (engine.activeHand >= engine.player.size()) {
                engine.phase = Phase.DEALER;
                playDealer();
                return;
            }
            Hand h = engine.active();
            // re-split? not supported; just play the dealt card.
            if (h.splitAce) {
                // already has its one card, keep moving
                continue;
            }
            updateUi("Playing hand " + (engine.activeHand + 1) + " of " + engine.player.size() + ".");
            return;
        }
    }

    void playDealer() {
        Hand d = engine.dealer;
        // dealer plays unless every player hand is bust or surrendered
        boolean anyLive = engine.player.stream().anyMatch(h -> !h.isBust() && !h.surrendered);
        if (anyLive) {
            while (true) {
                int v = d.value();
                if (v < 17) { d.cards.add(engine.shoe.deal()); continue; }
                if (v == 17 && d.isSoft() && engine.dealerHitsSoft17) {
                    d.cards.add(engine.shoe.deal()); continue;
                }
                break;
            }
        }
        engine.phase = Phase.SETTLE;
        settle();
    }

    void settle() {
        int dv = engine.dealer.value();
        boolean dealerBJ = engine.dealer.isBlackjack();
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < engine.player.size(); i++) {
            Hand h = engine.player.get(i);
            String tag = engine.player.size() > 1 ? ("Hand " + (i + 1) + ": ") : "";
            if (h.surrendered) {
                summary.append(tag).append("Surrendered (-$").append(h.bet / 2).append(")\n");
                engine.stats.losses++;
                continue;
            }
            int pv = h.value();
            if (pv > 21) {
                summary.append(tag).append("Bust  -$").append(h.bet).append('\n');
                engine.stats.losses++;
                engine.stats.busts++;
                continue;
            }
            if (h.isBlackjack() && !dealerBJ) {
                int payout = h.bet + (h.bet * 3) / 2; // stake + 3:2
                engine.bankroll += payout;
                engine.stats.totalReturned += payout;
                summary.append(tag).append("Blackjack! +$").append((h.bet * 3) / 2).append('\n');
                engine.stats.wins++;
                engine.stats.blackjacks++;
                continue;
            }
            if (dealerBJ) {
                if (h.isBlackjack()) {
                    engine.bankroll += h.bet;
                    engine.stats.totalReturned += h.bet;
                    summary.append(tag).append("Push (both BJ)\n");
                    engine.stats.pushes++;
                } else {
                    summary.append(tag).append("Dealer Blackjack -$").append(h.bet).append('\n');
                    engine.stats.losses++;
                }
                continue;
            }
            if (dv > 21 || pv > dv) {
                int payout = h.bet * 2;
                engine.bankroll += payout;
                engine.stats.totalReturned += payout;
                summary.append(tag).append("Win +$").append(h.bet).append('\n');
                engine.stats.wins++;
            } else if (pv == dv) {
                engine.bankroll += h.bet;
                engine.stats.totalReturned += h.bet;
                summary.append(tag).append("Push\n");
                engine.stats.pushes++;
            } else {
                summary.append(tag).append("Loss -$").append(h.bet).append('\n');
                engine.stats.losses++;
            }
        }
        engine.stats.peakBankroll = Math.max(engine.stats.peakBankroll, engine.bankroll);
        engine.phase = Phase.BETTING;
        updateUi(summary.toString().trim());

        if (engine.bankroll == 0 && engine.pendingBet == 0) {
            int yes = JOptionPane.showConfirmDialog(
                    this,
                    "You're out of chips! Reload $1000?",
                    "Bust",
                    JOptionPane.YES_NO_OPTION);
            if (yes == JOptionPane.YES_OPTION) {
                engine.bankroll = 1000;
                updateUi("Bankroll reloaded.");
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* UI helpers                                                         */
    /* ------------------------------------------------------------------ */

    String describeActive() {
        Hand h = engine.active();
        return "Hand: " + h.value() + (h.isSoft() ? " (soft)" : "");
    }

    void updateUi(String msg) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setText(msg);
            bankLabel.setText("Bankroll: $" + engine.bankroll);
            betLabel.setText ("Bet: $" + (engine.phase == Phase.BETTING ? engine.pendingBet
                                : (engine.player.isEmpty() ? 0 : engine.player.get(0).bet)));
            shoeLabel.setText("Shoe: " + engine.shoe.remaining() + " cards");
            boolean betting = engine.phase == Phase.BETTING;
            boolean playing = engine.phase == Phase.PLAYER;
            boolean insure  = engine.phase == Phase.INSURANCE;
            for (JButton b : chipBtns) b.setEnabled(betting);
            bDeal.setEnabled(betting && engine.pendingBet > 0);
            bHit.setEnabled(playing && engine.canHit());
            bStand.setEnabled(playing && engine.canStand());
            bDouble.setEnabled(playing && engine.canDouble());
            bSplit.setEnabled(playing && engine.canSplit());
            bSurrender.setEnabled(playing && engine.canSurrender());
            bIns.setEnabled(insure);
            bNoIns.setEnabled(insure);
            bHint.setEnabled(playing);
            table.repaint();
        });
    }

    void flash(String msg) {
        Color was = statusBar.getBackground();
        statusBar.setText(msg);
        statusBar.setBackground(new Color(0x6E3030));
        new Timer(900, e -> {
            statusBar.setBackground(was);
            ((Timer) e.getSource()).stop();
        }).start();
    }

    void showHint() {
        if (engine.phase != Phase.PLAYER) return;
        Hand h = engine.active();
        String s = basicStrategy(h, engine.dealer.cards.get(0));
        String advice;
        switch (s) {
            case "H": advice = "Basic strategy: HIT"; break;
            case "S": advice = "Basic strategy: STAND"; break;
            case "D": advice = engine.canDouble() ? "Basic strategy: DOUBLE (else hit)" : "Basic strategy: HIT"; break;
            case "P": advice = engine.canSplit()  ? "Basic strategy: SPLIT"  : "Basic strategy: HIT"; break;
            default:  advice = "Basic strategy: HIT";
        }
        JOptionPane.showMessageDialog(this, advice, "Hint", JOptionPane.INFORMATION_MESSAGE);
    }

    void showStats() {
        SessionStats s = engine.stats;
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
                + "Total returned: $" + s.totalReturned;
        JOptionPane.showMessageDialog(this, text, "Session stats", JOptionPane.INFORMATION_MESSAGE);
    }

    void showRules() {
        String text =
                "BlackJack Pro - house rules\n\n"
              + "  - 6-deck shoe, reshuffled at ~75% penetration\n"
              + "  - Blackjack pays 3:2\n"
              + "  - Dealer stands on soft 17 (toggle in Options)\n"
              + "  - Double on any first two cards\n"
              + "  - Split up to 4 hands; split aces get one card each\n"
              + "  - Late surrender on first two cards (half-bet refund)\n"
              + "  - Insurance offered on dealer Ace; pays 2:1\n\n"
              + "Chips: $1, $5, $25, $100, $500. Click to add to your bet.\n"
              + "Hint button shows the basic-strategy play.";
        JOptionPane.showMessageDialog(this, text, "Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    void resetSession() {
        int yes = JOptionPane.showConfirmDialog(this,
                "Reset bankroll to $1000 and clear stats?",
                "New Session", JOptionPane.YES_NO_OPTION);
        if (yes != JOptionPane.YES_OPTION) return;
        engine.bankroll = 1000;
        engine.pendingBet = 0;
        engine.player.clear();
        engine.player.add(new Hand());
        engine.dealer.clear();
        engine.activeHand = 0;
        engine.phase = Phase.BETTING;
        engine.stats.hands = engine.stats.wins = engine.stats.losses = engine.stats.pushes
                = engine.stats.blackjacks = engine.stats.busts = engine.stats.doubles
                = engine.stats.splits = engine.stats.surrenders = 0;
        engine.stats.totalWagered = engine.stats.totalReturned = 0;
        engine.stats.peakBankroll = 1000;
        engine.shoe.reshuffle();
        updateUi("New session. Place your bet.");
    }

    /* ------------------------------------------------------------------ */
    /* Table panel (the felt)                                             */
    /* ------------------------------------------------------------------ */

    final class TablePanel extends JPanel {
        TablePanel() {
            setBackground(new Color(0x0B3D2E));
            setPreferredSize(new Dimension(1200, 540));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            // felt gradient
            GradientPaint gp = new GradientPaint(0, 0, new Color(0x14513B),
                    0, h, new Color(0x062418));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // arc + table line
            g2.setColor(new Color(0xC9A227));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawArc(w / 2 - 480, 40, 960, 700, 0, 180);
            g2.setFont(g2.getFont().deriveFont(Font.ITALIC, 13f));
            g2.drawString("BLACKJACK PAYS 3 TO 2", w / 2 - 100, 80);
            g2.drawString("DEALER " + (engine.dealerHitsSoft17 ? "HITS" : "STANDS") + " ON SOFT 17",
                    w / 2 - 130, h - 36);

            // Dealer area
            String dealerCaption = "Dealer";
            int dvShow = engine.phase.ordinal() >= Phase.DEALER.ordinal()
                    ? engine.dealer.value() : engine.dealer.cards.isEmpty() ? 0 : engine.dealer.cards.get(0).rank.value;
            g2.setColor(new Color(0xF8E9A1));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
            g2.drawString(dealerCaption + " (" + (engine.phase.ordinal() >= Phase.DEALER.ordinal()
                    ? engine.dealer.value() + ")" : "showing " + dvShow + ")"),
                    w / 2 - 60, 110);

            // Dealer cards
            paintHand(g2, engine.dealer, w / 2, 130, false,
                    engine.phase.ordinal() < Phase.DEALER.ordinal() && !engine.phase.equals(Phase.SETTLE));

            // Player hands
            int n = engine.player.size();
            int yPlayer = h - CARD_H - 80;
            int handGap = Math.min(360, (w - 80) / Math.max(1, n));
            int startX = w / 2 - ((n - 1) * handGap) / 2;
            for (int i = 0; i < n; i++) {
                Hand hh = engine.player.get(i);
                paintHand(g2, hh, startX + i * handGap, yPlayer,
                        i == engine.activeHand && engine.phase == Phase.PLAYER,
                        false);
                String label = "Hand " + (n > 1 ? (i + 1) : "")
                        + "  (" + hh.value() + (hh.isSoft() ? "s" : "") + ")  $" + hh.bet
                        + (hh.surrendered ? " surrendered" : hh.doubled ? " doubled" : "");
                g2.setColor(new Color(0xF8E9A1));
                g2.drawString(label, startX + i * handGap - 60, yPlayer + CARD_H + 22);
            }

            // bet chip stack (during betting)
            if (engine.phase == Phase.BETTING && engine.pendingBet > 0) {
                paintBetStack(g2, w / 2, h / 2 + 20, engine.pendingBet);
            }
            g2.dispose();
        }

        void paintHand(Graphics2D g2, Hand hand, int cx, int topY, boolean highlight, boolean hideHole) {
            int count = hand.cards.size();
            int spread = 26;
            int totalW = (count - 1) * spread + CARD_W;
            int x = cx - totalW / 2;
            if (highlight) {
                g2.setColor(new Color(255, 230, 100, 80));
                g2.fillRoundRect(x - 8, topY - 8, totalW + 16, CARD_H + 16, 16, 16);
            }
            for (int i = 0; i < count; i++) {
                int cardX = x + i * spread;
                if (hideHole && i == 1) paintCardBack(g2, cardX, topY);
                else                    paintCardFace(g2, cardX, topY, hand.cards.get(i));
            }
        }

        void paintBetStack(Graphics2D g2, int cx, int cy, int amount) {
            // break into chip denominations
            int[] denoms = {500, 100, 25, 5, 1};
            int x = cx, y = cy;
            for (int d : denoms) {
                int count = amount / d;
                amount %= d;
                for (int i = 0; i < count && i < 8; i++) {
                    paintChip(g2, x, y - i * 5, 22, d);
                }
                x += 50;
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* main                                                               */
    /* ------------------------------------------------------------------ */

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new BlackJackPro().setVisible(true));
    }
}
