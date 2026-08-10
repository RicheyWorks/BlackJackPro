package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Engine;
import com.richeyworks.blackjack.engine.Phase;
import com.richeyworks.blackjack.media.MusicService;
import com.richeyworks.blackjack.media.SoundFx;
import com.richeyworks.blackjack.settings.GameSettings;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Settings panel reachable from the menu bar. Edits a {@link GameSettings}
 * instance live; clicking Save persists it and applies side-effects (engine
 * rules toggle, volume change, etc.).
 */
public final class SettingsDialog extends JDialog {

    /**
     * @param onSaved run after a successful save so the owner can re-sync any
     *                duplicate controls (the soft-17 menu item mirrors a
     *                checkbox in here and would otherwise show a stale value).
     */
    public SettingsDialog(JFrame parent,
                          GameSettings settings,
                          Engine engine,
                          MusicService music,
                          SoundFx sfx,
                          Runnable onSaved) {
        super(parent, "Settings", true);
        // Default is HIDE_ON_CLOSE: a dialog closed with the title-bar X would
        // stay alive, reachable from the parent's owned-window list, and this
        // one is constructed fresh on every open.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        root.add(header("Gameplay"));
        JCheckBox h17 = new JCheckBox("Dealer hits soft 17", settings.dealerHitsSoft17);
        JCheckBox lsr = new JCheckBox("Allow late surrender", settings.lateSurrender);
        JCheckBox ins = new JCheckBox("Offer insurance on dealer Ace", settings.offerInsurance);
        // House rules must not move mid-hand — same gate as the soft-17 menu item
        // and GameSession.setRule. Leaving these live while the dealer is about to
        // draw changes the odds after the player has already committed chips.
        boolean rulesOpen = engine.phase() == Phase.BETTING;
        h17.setEnabled(rulesOpen);
        lsr.setEnabled(rulesOpen);
        ins.setEnabled(rulesOpen);
        if (!rulesOpen) {
            JLabel locked = new JLabel("House rules unlock between hands.");
            locked.setForeground(new Color(0x8B5E2B));
            root.add(locked);
        }
        root.add(h17); root.add(lsr); root.add(ins);

        root.add(Box.createVerticalStrut(10));
        root.add(header("Audio"));
        JCheckBox sfxOn   = new JCheckBox("Sound effects",      settings.sfxEnabled);
        JCheckBox musicOn = new JCheckBox("Background music",   settings.musicEnabled);
        JSlider   sfxVol  = volumeSlider(settings.sfxVolume);
        JSlider   muVol   = volumeSlider(settings.musicVolume);
        root.add(sfxOn);   root.add(labeled("SFX volume",   sfxVol));
        root.add(musicOn); root.add(labeled("Music volume", muVol));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        JButton save   = new JButton("Save");
        buttons.add(cancel); buttons.add(save);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            // Audio always applies. Rules only apply between hands — if the
            // dialog was opened mid-round the checkboxes are disabled, but
            // re-check phase so a stale click cannot rewrite a live hand.
            if (engine.phase() == Phase.BETTING) {
                settings.dealerHitsSoft17 = h17.isSelected();
                settings.lateSurrender    = lsr.isSelected();
                settings.offerInsurance   = ins.isSelected();
                engine.rules().dealerHitsSoft17 = settings.dealerHitsSoft17;
                engine.rules().lateSurrender    = settings.lateSurrender;
                engine.rules().offerInsurance   = settings.offerInsurance;
            }

            settings.sfxEnabled       = sfxOn.isSelected();
            settings.musicEnabled     = musicOn.isSelected();
            settings.sfxVolume        = sfxVol.getValue() / 100f;
            settings.musicVolume      = muVol.getValue()  / 100f;

            sfx.setMuted(!settings.sfxEnabled);
            sfx.setVolume(settings.sfxVolume);
            // Apply volume even when muted so a later unmute is not stuck at the
            // previous level. Then align mute state with the checkbox.
            music.setVolume(settings.musicVolume);
            if (settings.musicEnabled) {
                if (music.isMuted()) music.toggleMute();
            } else {
                if (!music.isMuted()) music.toggleMute();
            }


            settings.save();
            if (onSaved != null) onSaved.run();
            dispose();
        });

        add(root, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private static JLabel header(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        l.setForeground(new Color(0x3B5928));
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    private static JSlider volumeSlider(float v) {
        JSlider s = new JSlider(0, 100, Math.round(v * 100));
        s.setMajorTickSpacing(25);
        s.setMinorTickSpacing(5);
        s.setPaintTicks(true);
        return s;
    }

    private static JPanel labeled(String text, JSlider slider) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(text), BorderLayout.WEST);
        row.add(slider, BorderLayout.CENTER);
        return row;
    }
}
