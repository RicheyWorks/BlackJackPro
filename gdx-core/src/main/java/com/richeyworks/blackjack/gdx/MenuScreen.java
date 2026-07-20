package com.richeyworks.blackjack.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.richeyworks.blackjack.achievement.Achievement;
import com.richeyworks.blackjack.engine.SessionStats;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings, session stats, and achievements for the mobile build.
 *
 * <p>The port had no way to reach any of this: house rules could only be changed
 * by editing {@code settings.properties} by hand, {@link GameSession#reset()}
 * was unreachable, and lifetime stats and achievement progress — both tracked
 * and persisted — were invisible to the player.
 *
 * <p>Drawn with the same manual {@link ShapeRenderer} + {@link BitmapFont}
 * approach as {@link TableScreen} rather than scene2d.ui, which would need a
 * skin atlas this project doesn't ship.
 */
public final class MenuScreen extends InputAdapter implements Screen {

    private static final float WORLD_W = 1280f, WORLD_H = 720f;
    private static final Color PANEL   = new Color(0x0d2a18ff);
    private static final Color ON      = new Color(0x4caf50ff);
    private static final Color OFF     = new Color(0x60605aff);
    private static final Color DANGER  = new Color(0x8b3030ff);
    private static final Color MUTED   = new Color(0x9a9a90ff);

    private final BlackJackGame game;
    private final TableScreen   table;
    private final GameSession   session;

    private final OrthographicCamera camera   = new OrthographicCamera();
    private final Viewport           viewport = new FitViewport(WORLD_W, WORLD_H, camera);
    private final ShapeRenderer      shapes   = new ShapeRenderer();
    private final SpriteBatch        batch    = new SpriteBatch();
    private final BitmapFont         font     = new BitmapFont();
    private final BitmapFont         bigFont  = new BitmapFont();

    private final List<Row> rows = new ArrayList<>();

    /** Reset is destructive and there is no undo, so it takes two taps. */
    private boolean confirmingReset;
    private String  notice = "";

    /**
     * @param table the live table screen, kept so returning preserves the hand
     *              in progress instead of building a new one.
     */
    public MenuScreen(BlackJackGame game, TableScreen table) {
        this.game    = game;
        this.table   = table;
        this.session = game.session();
        bigFont.getData().setScale(1.6f);
        buildRows();
    }

    private void buildRows() {
        float x = 60, w = 540, h = 54, y = WORLD_H - 150;
        for (GameSession.Rule rule : GameSession.Rule.values()) {
            rows.add(new Row(new Rectangle(x, y, w, h), rule));
            y -= h + 14;
        }
        y -= 20;
        rows.add(new Row(new Rectangle(x, y, w, h), Action.RESET));
        y -= h + 14;
        rows.add(new Row(new Rectangle(x, y, w, h), Action.BACK));
    }

    /* ---------- render ---------- */

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(BlackJackGame.FELT.r, BlackJackGame.FELT.g, BlackJackGame.FELT.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawRows();
        drawText();
    }

    private void drawRows() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Row r : rows) {
            Color fill;
            if (r.rule != null)                fill = session.rule(r.rule) ? ON : OFF;
            else if (r.action == Action.RESET) fill = confirmingReset ? DANGER : PANEL;
            else                               fill = PANEL;
            shapes.setColor(fill);
            shapes.rect(r.bounds.x, r.bounds.y, r.bounds.width, r.bounds.height);
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BlackJackGame.ACCENT);
        for (Row r : rows) shapes.rect(r.bounds.x, r.bounds.y, r.bounds.width, r.bounds.height);
        shapes.end();
    }

    private void drawText() {
        batch.begin();
        bigFont.setColor(BlackJackGame.TEXT);
        bigFont.draw(batch, "Settings", 60, WORLD_H - 60);

        boolean rulesLocked = !session.canChangeRules();
        for (Row r : rows) {
            font.setColor(BlackJackGame.TEXT);
            String label = r.rule != null
                    ? r.rule.label() + "   [" + (session.rule(r.rule) ? "ON" : "OFF") + "]"
                    : r.action == Action.RESET
                        ? (confirmingReset ? "Tap again to confirm reset" : "New session (reset bankroll & stats)")
                        : "Back to table";
            font.draw(batch, label, r.bounds.x + 16, r.bounds.y + r.bounds.height / 2 + 6);
        }

        if (rulesLocked) {
            font.setColor(MUTED);
            font.draw(batch, "House rules are locked until the current hand finishes.",
                    60, WORLD_H - 118);
        }
        if (!notice.isEmpty()) {
            font.setColor(BlackJackGame.ACCENT);
            font.draw(batch, notice, 60, 60);
        }

        drawStats();
        drawAchievements();
        batch.end();
    }

    private void drawStats() {
        SessionStats s = session.engine().stats();
        float x = 660, y = WORLD_H - 150;
        font.setColor(BlackJackGame.ACCENT);
        font.draw(batch, "Session", x, y + 34);
        font.setColor(BlackJackGame.TEXT);
        String[] lines = {
                "Bankroll:  $" + session.engine().bankroll(),
                "Hands:     " + s.hands,
                "W/L/P:     " + s.wins + " / " + s.losses + " / " + s.pushes,
                "Blackjacks:" + s.blackjacks,
                "Win rate:  " + String.format("%.1f%%", s.winRate() * 100),
                "Peak:      $" + s.peakBankroll,
                "Net:       $" + s.net(),
        };
        for (String line : lines) { font.draw(batch, line, x, y); y -= 22; }
    }

    private void drawAchievements() {
        float x = 660, y = WORLD_H - 340;
        int unlocked = 0, total = 0;
        for (Achievement a : session.achievements().all()) { total++; if (a.unlocked()) unlocked++; }

        font.setColor(BlackJackGame.ACCENT);
        font.draw(batch, "Achievements  " + unlocked + "/" + total, x, y + 34);
        for (Achievement a : session.achievements().all()) {
            font.setColor(a.unlocked() ? BlackJackGame.TEXT : MUTED);
            String mark = a.unlocked() ? "*" : "-";
            String prog = a.goal() > 1 && !a.unlocked() ? "  (" + a.progress() + "/" + a.goal() + ")" : "";
            font.draw(batch, mark + " " + a.name() + prog, x, y);
            y -= 20;
        }
    }

    /* ---------- input ---------- */

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 world = viewport.unproject(new Vector3(screenX, screenY, 0));
        for (Row r : rows) {
            if (!r.bounds.contains(world.x, world.y)) continue;
            activate(r);
            return true;
        }
        // A tap anywhere else abandons a pending reset, so the confirm state
        // can't linger and catch a later stray tap.
        confirmingReset = false;
        return false;
    }

    private void activate(Row r) {
        if (r.rule != null) {
            boolean applied = session.setRule(r.rule, !session.rule(r.rule));
            notice = applied
                    ? r.rule.label() + " " + (session.rule(r.rule) ? "on" : "off") + " — saved"
                    : "Finish the current hand before changing house rules.";
            confirmingReset = false;
            return;
        }
        if (r.action == Action.RESET) {
            if (!confirmingReset) {
                confirmingReset = true;
                notice = "This clears your bankroll, stats, and streak. Tap again to confirm.";
                return;
            }
            session.reset();
            confirmingReset = false;
            notice = "New session started — bankroll $" + session.engine().bankroll() + ".";
            table.onSessionReset();
            return;
        }
        back();
    }

    private void back() {
        session.persist();
        game.setScreen(table);
    }

    @Override
    public boolean keyDown(int keycode) {
        // Android's system back button, and Escape on the desktop preview.
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) { back(); return true; }
        return false;
    }

    /* ---------- lifecycle ---------- */

    @Override public void show() {
        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);   // handle back ourselves
        confirmingReset = false;
        notice = "";
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause()    { session.persist(); }
    @Override public void resume()   { }
    @Override public void hide()     { session.persist(); }

    @Override public void dispose()  {
        shapes.dispose(); batch.dispose(); font.dispose(); bigFont.dispose();
    }

    /* ---------- rows ---------- */

    private enum Action { RESET, BACK }

    /** A tappable row: either a rule toggle or a plain action. */
    private static final class Row {
        final Rectangle          bounds;
        final GameSession.Rule   rule;      // null for an action row
        final Action             action;    // null for a rule row

        Row(Rectangle bounds, GameSession.Rule rule)   { this.bounds = bounds; this.rule = rule; this.action = null; }
        Row(Rectangle bounds, Action action)           { this.bounds = bounds; this.rule = null; this.action = action; }
    }
}
