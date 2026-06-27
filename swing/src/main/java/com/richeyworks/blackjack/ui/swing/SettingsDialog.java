package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.engine.Engine;
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

    public SettingsDialog(JFrame parent,
                          GameSettings settings,
                          Engine engine,
                          MusicService music,
                          SoundFx sfx) {
        super(parent, "Settings", true);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        root.add(header("Gameplay"));
        JCheckBox h17 = new JCheckBox("Dealer hits soft 17", settings.dealerHitsSoft17);
        JCheckBox lsr = new JCheckBox("Allow late surrender", settings.lateSurrender);
        JCheckBox ins = new JCheckBox("Offer insurance on dealer Ace", settings.offerInsurance);
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
            settings.dealerHitsSoft17 = h17.isSelected();
            settings.lateSurrender    = lsr.isSelected();
            settings.offerInsurance   = ins.isSelected();
            settings.sfxEnabled       = sfxOn.isSelected();
            settings.musicEnabled     = musicOn.isSelected();
            settings.sfxVolume        = sfxVol.getValue() / 100f;
            settings.musicVolume      = muVol.getValue()  / 100f;

            engine.rules().dealerHitsSoft17 = settings.dealerHitsSoft17;
            engine.rules().lateSurrender    = settings.lateSurrender;
            engine.rules().offerInsurance   = settings.offerInsurance;

            sfx.setMuted(!settings.sfxEnabled);
            sfx.setVolume(settings.sfxVolume);
            if (settings.musicEnabled) {
                if (music.isMuted()) music.toggleMute();
                music.setVolume(settings.musicVolume);
            } else {
                if (!music.isMuted()) music.toggleMute();
            }

            settings.save();
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
