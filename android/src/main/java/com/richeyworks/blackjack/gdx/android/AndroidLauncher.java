package com.richeyworks.blackjack.gdx.android;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.richeyworks.blackjack.gdx.BlackJackGame;

/**
 * Android launcher. Wraps the libGDX {@link BlackJackGame} in an
 * AndroidApplication and supplies a Platform implementation that knows how to
 * vibrate the device, show toasts, and locate the app's private save folder.
 */
public final class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useAccelerometer = false;
        config.useCompass       = false;

        initialize(new BlackJackGame(new AndroidPlatform(this)), config);
    }

    /** Platform bridge for the Android target. */
    private static final class AndroidPlatform implements BlackJackGame.Platform {
        private final AndroidLauncher activity;

        AndroidPlatform(AndroidLauncher activity) { this.activity = activity; }

        @Override
        public void hapticTick() {
            Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null || !v.hasVibrator()) return;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(20);
            }
        }

        @Override
        public void toast(String msg) {
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());
        }

        @Override
        public String saveDir() {
            // Internal app storage — survives uninstall? No. App-private though.
            return activity.getFilesDir().getAbsolutePath();
        }
    }
}
