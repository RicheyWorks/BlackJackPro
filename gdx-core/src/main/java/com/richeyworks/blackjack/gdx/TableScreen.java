package com.richeyworks.blackjack.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.richeyworks.blackjack.engine.BasicStrategy;
import com.richeyworks.blackjack.engine.Card;
import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Hand;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.engine.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final Engine        engine;

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
        this.game   = game;
        this.engine = new Engine(1000, new Random());
        bigFont.getData().setScale(2f);
        Gdx.input.setInputProcessor(this);
        buildButtons();
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
        try { r.run(); }
        catch (RuntimeException ex) { flash(fallback != null ? fallback : ex.getMessage()); }
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
        Gdx.gl.glClearColor(BlackJackGame.FELT.r, BlackJackGame.FELT.g, BlackJackGame.FELT.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawTable();
        drawHands();
        drawButtons();
        drawHud();
    }

    private void drawTable() {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BlackJackGame.ACCENT);
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
        font.setColor(BlackJackGame.TEXT);
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
        font.draw(batch, card.suit().glyph(), x + 8, y + CARD_H - 26);
        bigFont.setColor(red ? Color.RED : Color.BLACK);
        String mid = (card.rank() == Rank.JACK || card.rank() == Rank.QUEEN
                   || card.rank() == Rank.KING) ? card.rank().label() : card.suit().glyph();
        layout.setText(bigFont, mid);
        bigFont.draw(batch, mid, x + (CARD_W - layout.width) / 2f, y + CARD_H / 2 + layout.height / 2f);
        batch.end();
    }

    private void drawCardBack(float x, float y) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.13f, 0.24f, 1f);
        shapes.rect(x, y, CARD_W, CARD_H);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BlackJackGame.ACCENT);
        shapes.rect(x, y, CARD_W, CARD_H);
        for (int i = 0; i < 12; i++) {
            shapes.line(x, y + i * (CARD_H / 12), x + CARD_W, y + i * (CARD_H / 12));
        }
        shapes.end();
    }

    private void drawButtons() {
        for (Button b : buttons) b.draw(shapes, batch, font, engine);
    }

    private void drawHud() {
        batch.begin();
        font.setColor(BlackJackGame.TEXT);
        font.draw(batch, "Bankroll: $" + engine.bankroll(),    20,  WORLD_H - 12);
        font.draw(batch, "Bet: $"      + currentBet(),        220,  WORLD_H - 12);
        font.draw(batch, "Shoe: "      + engine.shoe().remaining(), 380, WORLD_H - 12);
        long now = System.currentTimeMillis();
        if (now < flashUntil) {
            font.setColor(1f, 0.5f, 0.5f, 1f);
            font.draw(batch, flashText, WORLD_W / 2 - 200, WORLD_H - 30);
        } else {
            font.setColor(BlackJackGame.TEXT);
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
    @Override public void show()     { }
    @Override public void hide()     { }
    @Override public void pause()    { }
    @Override public void resume()   { }
    @Override public void dispose()  {
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

        void draw(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font, Engine e) {
            boolean enabled = isEnabled(e);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            if (chipValue > 0) {
                Color face;
                switch (chipValue) {
                    case 1:    face = new Color(0.92f, 0.92f, 0.92f, 1f); break;
                    case 5:    face = new Color(0.75f, 0.23f, 0.17f, 1f); break;
                    case 25:   face = new Color(0.18f, 0.49f, 0.20f, 1f); break;
                    case 100:  face = new Color(0.11f, 0.11f, 0.11f, 1f); break;
                    case 500:  face = new Color(0.42f, 0.10f, 0.60f, 1f); break;
                    default:   face = Color.GOLD;
                }
                if (!enabled) face = new Color(face.r * 0.4f, face.g * 0.4f, face.b * 0.4f, 1f);
                shapes.setColor(face);
                shapes.circle(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2,
                        bounds.width / 2);
            } else {
                shapes.setColor(enabled ? new Color(0.55f, 0.37f, 0.17f, 1f)
                                        : new Color(0.18f, 0.14f, 0.10f, 1f));
                shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(BlackJackGame.ACCENT);
            if (chipValue > 0) {
                shapes.circle(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2,
                        bounds.width / 2);
            } else {
                shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            shapes.end();

            batch.begin();
            font.setColor(enabled ? Color.WHITE : new Color(0.6f, 0.6f, 0.6f, 1f));
            GlyphLayout l = new GlyphLayout(font, label);
            font.draw(batch, label, bounds.x + (bounds.width - l.width) / 2f,
                    bounds.y + (bounds.height + l.height) / 2f);
            batch.end();
        }
    }
}
