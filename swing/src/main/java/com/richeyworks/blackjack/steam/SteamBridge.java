package com.richeyworks.blackjack.steam;

import com.richeyworks.blackjack.achievement.Achievement;
import com.richeyworks.blackjack.achievement.AchievementService;

/**
 * Optional Steamworks integration. The Steam SDK isn't on the classpath in
 * normal builds — this class uses reflection to call into
 * {@code com.codedisaster.steamworks.SteamAPI} if it's present so the rest of
 * the game can call {@link #init(int)} unconditionally.
 *
 * To enable Steam integration in production:
 *   1. Add to {@code build.gradle.kts}:
 *        implementation("com.code-disaster.steamworks4j:steamworks4j:1.9.0")
 *   2. Place {@code steam_appid.txt} (containing your Steam App ID) next to
 *      the executable.
 *   3. Call {@link #init(int)} once at startup, and {@link #shutdown()} at exit.
 *   4. Whenever an {@link Achievement} unlocks locally, also forward it to
 *      {@link #grantAchievement(String)} so the Steam stat is set.
 *
 * Without the SDK on the classpath every method is a no-op and the game
 * functions normally.
 */
public final class SteamBridge {

    private static boolean enabled;
    private static Object  userStats;

    private SteamBridge() {}

    /** Returns true iff Steam was successfully initialized. */
    public static boolean init(int appId) {
        try {
            Class<?> api = Class.forName("com.codedisaster.steamworks.SteamAPI");
            // SteamAPI.loadLibraries() — optional in steamworks4j 1.9+
            try { api.getMethod("loadLibraries").invoke(null); } catch (NoSuchMethodException ignored) {}
            boolean ok = (Boolean) api.getMethod("init").invoke(null);
            if (ok) {
                Class<?> us = Class.forName("com.codedisaster.steamworks.SteamUserStats");
                userStats = us.getDeclaredConstructor().newInstance();
                enabled = true;
            }
            return ok;
        } catch (ClassNotFoundException missing) {
            // SDK simply isn't shipped — fine.
            return false;
        } catch (Exception e) {
            System.err.println("Steam init failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean isEnabled() { return enabled; }

    public static void shutdown() {
        if (!enabled) return;
        try {
            Class<?> api = Class.forName("com.codedisaster.steamworks.SteamAPI");
            api.getMethod("shutdown").invoke(null);
        } catch (Exception ignored) { }
        enabled   = false;
        userStats = null;
    }

    /** Mark an achievement as unlocked on the user's Steam profile. */
    public static void grantAchievement(String id) {
        if (!enabled || userStats == null) return;
        try {
            userStats.getClass().getMethod("setAchievement", String.class).invoke(userStats, id);
            userStats.getClass().getMethod("storeStats").invoke(userStats);
        } catch (Exception ignored) { }
    }

    /** Hook this up to {@link AchievementService#onUnlock}. */
    public static void wire(AchievementService svc) {
        svc.onUnlock(a -> grantAchievement(a.id()));
    }
}
