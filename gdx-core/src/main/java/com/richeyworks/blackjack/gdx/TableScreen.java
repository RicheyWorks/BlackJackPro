package com.richeyworks.blackjack.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.richeyworks.blackjack.achievement.AchievementService;
import com.richeyworks.blackjack.engine.BasicStrategy;
import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Outcome;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.engine.Rank;
import com.richeyworks.blackjack.engine.SessionStats;
import com.richeyworks.blackjack.table.Personas;
import com.richeyworks.blackjack.table.Remark;
import com.richeyworks.blackjack.table.TableChatter;
import com.richeyworks.blackjack.table.TableEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Touch-friendly libGDX table that consumes the same {@link Engine} the Swing
 * version uses. Renders the felt, the dealer hand, every player hand, and a
 * bottom action bar with chip buttons (1/5/25/100/500) and gameplay buttons
 * (Deal, Hit, Stand, Double, Split, Surrender, Insure, Decline, Hint).
 *
 * Designed for landscape phone/tablet screens. Uses a {@link FitViewport} at
 * 1280×720 virtual resolution so the layout scales identically across devices.
 */
public final class TableScreen extends InputAdapter implements Screen {

    private static final float WORLD_W = 1280f, WORLD_H = 720f;
    private static final float CARD_W  = 110f, CARD_H = 154f;

    private final BlackJackGame game;
    private final GameSession   session;
    private final Engine        engine;

    /** Rounds already post-processed, so a result is announced exactly once. */
    private int processedHands;
    /** Consecutive winning rounds, for the streak achievement. */
    private int winStreak;
    /** Built on first use and reused, so returning keeps the hand in progress. */
    private MenuScreen menu;

    /** The characters at the table. They talk; they never play a hand. */
    private final TableChatter chatter =
            new TableChatter(Personas.defaults(), new java.util.Random());
    /** Consecutive losing rounds, the mirror of winStreak. */
    private int lossStreak;
    private int lastShoeSeen;
    private boolean greeted;

    /** Live lookup, not a field: switching theme must apply on the next frame. */
    private GdxPalette pal() { return game.palette(); }

    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport           viewport = new FitViewport(WORLD_W, WORLD_H, camera);
    private final ShapeRenderer      shapes = new ShapeRenderer();
    private final SpriteBatch        batch  = new SpriteBatch();
    private final BitmapFont         font   = new BitmapFont();
    private final BitmapFont         bigFont= new BitmapFont();
    private final GlyphLayout        layout = new GlyphLayout();

    private final List<Button> buttons = new ArrayList<>();
    private String  statusText = "Place your bet to begin.";
    private long    flashUntil;
    private String  flashText;

    public TableScreen(BlackJackGame game) {
        this.game    = game;
        this.session = game.session();
        // The engine comes from the session, which has already restored the
        // bankroll and lifetime stats from disk. Constructing a fresh
        // Engine(1000, ...) here is what used to throw away every player's
        // progress on each launch.
        this.engine  = session.engine();
        this.processedHands = engine.stats().hands;
        bigFont.getData().setScale(2f);
        buildButtons();
        statusText = engine.stats().hands > 0
                ? "Welcome back — bankroll $" + engine.bankroll() + ". Place your bet."
                : "Place your bet to begin.";

        // Android has a native notification affordance; the desktop Platform
        // falls back to stdout. Either way the player finds out they unlocked
        // something, which the port had no way of telling them before.
        session.achievements().onUnlock(
                a -> game.platform().toast("Achievement unlocked: " + a.name()));
    }

    /* ---------- button layout ---------- */

    private void buildButtons() {
        // chip row
        int[] chipVals = { 1, 5, 25, 100, 500 };
        float x = 30, y = 30, size = 70, gap = 12;
        for (int v : chipVals) {
            Button b = chipButton(x, y, size, v);
            buttons.add(b);
            x += size + gap;
        }
        buttons.add(action(x, y, 90, 50, "Clear",   () -> { engine.clearBet();     statusText = "Bet cleared."; }));
        x += 100;

        // action row
        float ay = 100;
        buttons.add(action(30,        ay, 90, 50, "Deal",      () -> safe(engine::deal,       "Place a bet first.")));
        buttons.add(action(130,       ay, 90, 50, "Hit",       () -> safe(engine::hit,        null)));
        buttons.add(action(230,       ay, 90, 50, "Stand",     () -> safe(engine::stand,      null)));
        buttons.add(action(330,       ay, 90, 50, "Double",    () -> safe(engine::doubleDown, "Cannot double.")));
        buttons.add(action(430,       ay, 90, 50, "Split",     () -> safe(engine::split,      "Cannot split.")));
        buttons.add(action(530,       ay, 90, 50, "Surrender", () -> safe(engine::surrender,  null)));
        buttons.add(action(630,       ay, 90, 50, "Insure",    () -> safe(() -> engine.takeInsurance(true),  "Not insurance time.")));
        buttons.add(action(730,       ay, 90, 50, "Decline",   () -> safe(() -> engine.takeInsurance(false), "Not insurance time.")));
        buttons.add(action(830,       ay, 90, 50, "Hint",      this::showHint));
        buttons.add(action(930,       ay, 90, 50, "Menu",      this::openMenu));
    }

    /**
     * Open settings/stats. The menu keeps a reference back to this screen, so a
     * hand in progress survives the round trip.
     */
    private void openMenu() {
        if (menu == null) menu = new MenuScreen(game, this);
        game.setScreen(menu);
    }

    /** Called by {@link MenuScreen} after a reset so stale counters don't leak. */
    void onSessionReset() {
        processedHands = engine.stats().hands;
        winStreak      = 0;
        statusText     = "New session. Place your bet.";
    }

    private Button chipButton(float x, float y, float size, int value) {
        return new Button(new Rectangle(x, y, size, size),
                "$" + value,
                () -> { try { engine.addBet(value); game.platform().hapticTick(); }
                        catch (Exception ex) { flash("Not enough chips."); } },
                value);
    }

    private Button action(float x, float y, float w, float h, String label, Runnable run) {
        return new Button(new Rectangle(x, y, w, h), label, run, -1);
    }

    /* ---------- engine wrappers ---------- */

    private void safe(Runnable r, String fallback) {
        try {
            r.run();
            postAction();
        }
        catch (RuntimeException ex) { flash(fallback != null ? fallback : ex.getMessage()); }
    }

    /**
     * Run after any engine action. Fires {@link #onRoundComplete()} exactly once
     * per round, keyed on the engine's hand counter rather than a phase
     * transition — the engine settles synchronously, so a round that ends during
     * the deal (a dealt natural, or a dealer blackjack) or straight out of the
     * insurance prompt never presents an observable SETTLE phase to watch for.
     */
    private void postAction() {
        if (engine.shoe().remaining() > lastShoeSeen) say(TableEvent.SHUFFLE);
        lastShoeSeen = engine.shoe().remaining();
        if (engine.phase() == Phase.INSURANCE) say(TableEvent.INSURANCE_OFFERED);
        else if (engine.phase() == Phase.PLAYER && dealerShowsWeakCard())
            say(TableEvent.DEALER_WEAK_CARD);
        if (engine.phase() == Phase.BETTING && engine.stats().hands > processedHands) {
            processedHands = engine.stats().hands;
            onRoundComplete();
        }
    }

    /** Offer one event to the table; the chatter decides whether anyone speaks. */
    private void say(TableEvent event) {
        chatter.react(event, System.currentTimeMillis());
    }

    /**
     * The single most remark-worthy thing about a finished round. One event
     * rather than several — the chatter paces remarks, so offering it five
     * things just means four get dropped in whatever order the code checked.
     */
    private TableEvent mostNotable(List<Outcome> outcomes, boolean won, boolean lost) {
        if (outcomes.contains(Outcome.BLACKJACK))  return TableEvent.PLAYER_BLACKJACK;

        // Rarities first, or they would never be heard over an ordinary result.
        if (won && engine.hands().stream().anyMatch(h -> h.size() >= 5))
            return TableEvent.FIVE_CARD_HAND;
        if (won && engine.hands().stream().anyMatch(Hand::doubled))
            return TableEvent.DOUBLE_WIN;
        if (engine.lastNet() >= Math.max(100, engine.bankroll() / 4))
            return TableEvent.BIG_WIN;
        if (won && engine.hands().stream().anyMatch(h -> h.value() == 21 && h.size() >= 3))
            return TableEvent.TWENTY_ONE;

        if (engine.dealer().isBust() && won)       return TableEvent.DEALER_BUST;
        if (engine.dealer().isBlackjack())         return TableEvent.DEALER_BLACKJACK;
        if (lost && !outcomes.contains(Outcome.BUST) && isCloseCall())
            return TableEvent.CLOSE_CALL;
        if (outcomes.contains(Outcome.BUST))       return TableEvent.PLAYER_BUST;
        if (outcomes.contains(Outcome.SURRENDER))  return TableEvent.PLAYER_SURRENDER;
        if (winStreak  >= 3)                       return TableEvent.HOT_STREAK;
        if (lossStreak >= 3)                       return TableEvent.COLD_STREAK;
        if (engine.bankroll() > 0 && engine.bankroll() <= 100) return TableEvent.LOW_CHIPS;
        if (engine.stats().hands >= 60 && engine.stats().hands % 25 == 0)
            return TableEvent.LONG_SESSION;
        if (engine.bankroll() >= 2000) return TableEvent.RUNNING_WELL;
        if (won)  return TableEvent.PLAYER_WIN;
        if (lost) return TableEvent.PLAYER_LOSS;
        return TableEvent.PUSH;
    }

    /** A surviving hand lost to the dealer by exactly one point. */
    private boolean isCloseCall() {
        int dv = engine.dealer().value();
        if (dv > 21) return false;
        return engine.hands().stream()
                .anyMatch(h -> !h.isBust() && !h.surrendered() && dv - h.value() == 1);
    }

    /** Dealer showing 4, 5, or 6 -- the up-cards that bust them most often. */
    private boolean dealerShowsWeakCard() {
        if (engine.dealer().isEmpty()) return false;
        int up = engine.dealer().first().rank().value();
        return up >= 4 && up <= 6;
    }

    private void onRoundComplete() {
        List<Outcome> outcomes = engine.lastOutcomes();
        if (outcomes.isEmpty()) return;

        // The engine assigns each hand an outcome as it pays it out, so what the
        // player reads always matches what they were paid. Re-deriving it here
        // from hand values would be a second copy of the settlement rules.
        boolean won   = false, pushed = false, natural = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < outcomes.size(); i++) {
            Outcome o = outcomes.get(i);
            won     |= o.isWin();
            pushed  |= o == Outcome.PUSH;
            natural |= o == Outcome.BLACKJACK;
            if (outcomes.size() > 1) sb.append("Hand ").append(i + 1).append(": ");
            sb.append(o.label()).append("   ");
        }
        int net = engine.lastNet();
        sb.append(net > 0 ? "+$" + net : net < 0 ? "-$" + (-net) : "even");
        statusText = sb.toString().trim();

        if (won) game.platform().hapticTick();

        if (won)          lossStreak = 0;
        else if (!pushed) lossStreak++;
        say(mostNotable(outcomes, won, !won && !pushed));

        AchievementService a = session.achievements();
        SessionStats       s = engine.stats();
        a.increment("first_hand");
        if (won)     { a.increment("first_win"); a.increment("ten_wins"); a.increment("fifty_wins"); }
        if (natural)   a.increment("first_blackjack");
        if (s.splits     > 0) a.setProgress("first_split", 1);
        if (s.doubles    > 0) a.setProgress("first_double", 1);
        if (s.surrenders > 0) a.setProgress("first_surrender", 1);
        if (won && engine.dealer().isBust()) a.increment("survived_bust");
        a.setProgress("bankroll_5k",  Math.min(5000,  engine.bankroll()));
        a.setProgress("bankroll_10k", Math.min(10000, engine.bankroll()));
        // A push keeps a streak alive; only a loss breaks it.
        if (won)               winStreak++;
        else if (!pushed)      winStreak = 0;
        a.setProgress("survived_bust_streak", winStreak);

        // Money changed hands and achievements may have unlocked, so make it
        // durable now rather than trusting the app to get a pause() later.
        session.persist();
    }

    private void showHint() {
        if (engine.phase() != Phase.PLAYER) { flash("No hand to advise."); return; }
        var action = BasicStrategy.recommend(engine.active(), engine.dealer().first());
        switch (action) {
            case H: statusText = "Basic strategy: HIT"; break;
            case S: statusText = "Basic strategy: STAND"; break;
            case D: statusText = "Basic strategy: DOUBLE (else hit)"; break;
            case P: statusText = "Basic strategy: SPLIT"; break;
            case R: statusText = "Basic strategy: SURRENDER (else hit)"; break;
        }
    }

    private void flash(String msg) { flashText = msg; flashUntil = System.currentTimeMillis() + 1500; }

    /* ---------- render loop ---------- */

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(pal().feltBottom.r, pal().feltBottom.g, pal().feltBottom.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawTable();
        drawHands();
        drawButtons();
        drawHud();
        drawChatter();
    }

    /**
     * Draw whatever the table is saying.
     *
     * <p>Bubbles sit against the left and right edges at mid height, clear of
     * the dealer along the top and the action bar along the bottom. Drawn last
     * so they sit above the cards rather than under them.
     */
    private void drawChatter() {
        long now = System.currentTimeMillis();
        List<Remark> remarks = chatter.visibleAt(now);
        if (remarks.isEmpty()) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        for (Remark r : remarks) {
            int seat = r.speaker().seat();
            boolean left = seat != Personas.SEAT_RIGHT;
            float bw = 300f, bh = 76f;
            float bx = left ? 24f : WORLD_W - bw - 24f;
            float by = switch (seat) {
                case Personas.SEAT_LEFT  -> WORLD_H * 0.46f;
                case Personas.SEAT_RIGHT -> WORLD_H * 0.46f;
                default                  -> WORLD_H * 0.62f;
            };
            float a = r.opacityAt(now);

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(pal().bubbleFill.r, pal().bubbleFill.g, pal().bubbleFill.b, a);
            shapes.rect(bx, by, bw, bh);
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(pal().bubbleName.r, pal().bubbleName.g, pal().bubbleName.b, a);
            shapes.rect(bx, by, bw, bh);
            shapes.end();

            batch.begin();
            font.setColor(pal().bubbleName.r, pal().bubbleName.g, pal().bubbleName.b, a);
            font.draw(batch, r.speaker().name(), bx + 12, by + bh - 10);
            font.setColor(pal().bubbleInk.r, pal().bubbleInk.g, pal().bubbleInk.b, a);
            // Let libGDX wrap inside the bubble rather than measuring by hand.
            font.draw(batch, r.text(), bx + 12, by + bh - 30, bw - 24, -1, true);
            batch.end();
        }
        // Leave the colour clean for the next frame's opaque passes.
        font.setColor(pal().text);
    }

    private void drawTable() {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(pal().accent);
        shapes.arc(WORLD_W / 2, 0, 700, 0, 180);
        shapes.end();
    }

    private void drawHands() {
        // Dealer (top). Reveal the hole once the dealer acts / the round settles
        // (mirrors the Swing fix): hide only during the player's decision phases,
        // otherwise the synchronously-resolved dealer turn means the hole card
        // would never be shown in the post-round view.
        Phase phase = engine.phase();
        boolean hideHole = phase == Phase.DEALING || phase == Phase.INSURANCE || phase == Phase.PLAYER;
        drawHand(engine.dealer(), WORLD_W / 2, WORLD_H - CARD_H - 40, false, hideHole, "Dealer");

        // Player hands (middle)
        List<Hand> hands = engine.hands();
        int n = hands.size();
        float spread = Math.min(360, (WORLD_W - 100) / Math.max(1, n));
        float startX = WORLD_W / 2 - ((n - 1) * spread) / 2f;
        for (int i = 0; i < n; i++) {
            Hand h = hands.get(i);
            String label = (n > 1 ? "Hand " + (i + 1) : "You") + " (" + h.value() + (h.isSoft() ? "s" : "") + ")";
            drawHand(h, startX + i * spread, WORLD_H / 2 - 40,
                    i == engine.activeIndex() && engine.phase() == Phase.PLAYER, false, label);
        }
    }

    private void drawHand(Hand hand, float cx, float topY, boolean highlight, boolean hideHole, String label) {
        int count = hand.size();
        float totalW = Math.max(CARD_W, (count - 1) * 30f + CARD_W);
        float x = cx - totalW / 2f;

        if (highlight) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 0.9f, 0.4f, 0.25f);
            shapes.rect(x - 6, topY - 6, totalW + 12, CARD_H + 12);
            shapes.end();
        }

        for (int i = 0; i < count; i++) {
            float cardX = x + i * 30f;
            if (hideHole && i == 1) drawCardBack(cardX, topY);
            else                    drawCardFace(cardX, topY, hand.cards().get(i));
        }

        batch.begin();
        font.setColor(pal().text);
        layout.setText(font, label);
        font.draw(batch, label, cx - layout.width / 2f, topY - 8);
        batch.end();
    }

    private void drawCardFace(float x, float y, Card card) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.WHITE);
        shapes.rect(x, y, CARD_W, CARD_H);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(x, y, CARD_W, CARD_H);
        shapes.end();

        boolean red = card.suit().isRed();
        batch.begin();
        font.setColor(red ? Color.RED : Color.BLACK);
        font.draw(batch, card.rank().label(), x + 8, y + CARD_H - 8);
        font.draw(batch, suitLetter(card), x + 8, y + CARD_H - 26);
        bigFont.setColor(red ? Color.RED : Color.BLACK);
        String mid = (card.rank() == Rank.JACK || card.rank() == Rank.QUEEN
                   || card.rank() == Rank.KING) ? card.rank().label() : suitLetter(card);
        layout.setText(bigFont, mid);
        bigFont.draw(batch, mid, x + (CARD_W - layout.width) / 2f, y + CARD_H / 2 + layout.height / 2f);
        batch.end();
    }

    private static String suitLetter(Card card) {
        switch (card.suit()) {
            case SPADES:   return "S";
            case HEARTS:   return "H";
            case DIAMONDS: return "D";
            case CLUBS:    return "C";
            default:       return "?";
        }
    }

    private void drawCardBack(float x, float y) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(pal().backFill);
        shapes.rect(x, y, CARD_W, CARD_H);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(pal().accent);
        shapes.rect(x, y, CARD_W, CARD_H);
        for (int i = 0; i < 12; i++) {
            shapes.line(x, y + i * (CARD_H / 12), x + CARD_W, y + i * (CARD_H / 12));
        }
        shapes.end();
    }

    private void drawButtons() {
        for (Button b : buttons) b.draw(shapes, batch, font, engine, pal());
    }

    private void drawHud() {
        batch.begin();
        font.setColor(pal().text);
        font.draw(batch, "Bankroll: $" + engine.bankroll(),    20,  WORLD_H - 12);
        font.draw(batch, "Bet: $"      + currentBet(),        220,  WORLD_H - 12);
        font.draw(batch, "Shoe: "      + engine.shoe().remaining(), 380, WORLD_H - 12);
        long now = System.currentTimeMillis();
        if (now < flashUntil) {
            font.setColor(1f, 0.5f, 0.5f, 1f);
            font.draw(batch, flashText, WORLD_W / 2 - 200, WORLD_H - 30);
        } else {
            font.setColor(pal().text);
            font.draw(batch, statusText, WORLD_W / 2 - 200, WORLD_H - 30);
        }
        batch.end();
    }

    private int currentBet() {
        if (engine.phase() == Phase.BETTING) return engine.pendingBet();
        return engine.hands().isEmpty() ? 0 : engine.hands().get(0).bet();
    }

    /* ---------- input ---------- */

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 world = viewport.unproject(new Vector3(screenX, screenY, 0));
        for (Button b : buttons) {
            if (b.bounds.contains(world.x, world.y) && b.isEnabled(engine)) {
                b.action.run();
                return true;
            }
        }
        return false;
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }

    /**
     * Claim input here rather than in the constructor: coming back from
     * {@link MenuScreen} has to restore this screen's processor, and a
     * constructor only runs once.
     */
    @Override public void show() {
        Gdx.input.setInputProcessor(this);
        if (!greeted) { greeted = true; say(TableEvent.SESSION_START); }
    }

    @Override public void resume()   { }

    /**
     * Android delivers this on every task switch, incoming call, and screen-off,
     * and it is the last callback guaranteed before the process can be killed.
     * {@link BlackJackGame#pause()} forwards here, so the save happens whether
     * the platform notifies the game or the screen.
     */
    @Override public void pause()    { session.persist(); }

    /** Leaving the screen for another is also a good moment to be durable. */
    @Override public void hide()     { session.persist(); }

    @Override public void dispose()  {
        session.persist();
        if (menu != null) { menu.dispose(); menu = null; }
        shapes.dispose(); batch.dispose(); font.dispose(); bigFont.dispose();
    }

    /* ---------- button helper ---------- */

    private static final class Button {
        final Rectangle bounds;
        final String    label;
        final Runnable  action;
        final int       chipValue; // -1 for action button

        Button(Rectangle bounds, String label, Runnable action, int chipValue) {
            this.bounds = bounds; this.label = label;
            this.action = action; this.chipValue = chipValue;
        }

        boolean isEnabled(Engine e) {
            if (chipValue > 0) return e.canBet(chipValue);
            switch (label) {
                case "Deal":      return e.canDeal();
                case "Hit":       return e.canHit();
                case "Stand":     return e.canStand();
                case "Double":    return e.canDouble();
                case "Split":     return e.canSplit();
                case "Surrender": return e.canSurrender();
                case "Insure":
                case "Decline":   return e.phase() == Phase.INSURANCE;
                default:          return true;
            }
        }

        void draw(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font, Engine e,
                  GdxPalette pal) {
            boolean enabled = isEnabled(e);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            if (chipValue > 0) {
                // One definition of chip colours for both front ends -- these
                // stay constant across themes on purpose, so a player only
                // learns "green is 25" once.
                Color face = GdxPalette.chip(chipValue);
                if (!enabled) face = new Color(face.r * 0.4f, face.g * 0.4f, face.b * 0.4f, 1f);
                shapes.setColor(face);
                shapes.circle(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2,
                        bounds.width / 2);
            } else {
                Color bf = pal.buttonFace;
                shapes.setColor(enabled ? bf
                                        : new Color(bf.r * 0.4f, bf.g * 0.4f, bf.b * 0.4f, 1f));
                shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(pal.accent);
            if (chipValue > 0) {
                shapes.circle(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2,
                        bounds.width / 2);
            } else {
                shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            shapes.end();

            batch.begin();
            Color ink = chipValue > 0 ? GdxPalette.chipInk(chipValue) : pal.buttonText;
            font.setColor(enabled ? ink : new Color(0.6f, 0.6f, 0.6f, 1f));
            GlyphLayout l = new GlyphLayout(font, label);
            font.draw(batch, label, bounds.x + (bounds.width - l.width) / 2f,
                    bounds.y + (bounds.height + l.height) / 2f);
            batch.end();
        }
    }
}
