package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.achievement.Achievement;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;

/**
 * Slide-in achievement notification. Auto-dismisses after a few seconds.
 * Stateless static helper — fire-and-forget.
 */
public final class AchievementToast {

    private AchievementToast() {}

    private static final java.util.List<JWindow> ACTIVE = new java.util.ArrayList<>();

    public static void show(Component owner, Achievement a) {
        Window parent = owner instanceof Window w ? w : javax.swing.SwingUtilities.getWindowAncestor(owner);
        JWindow toast = new JWindow(parent);
        toast.setAlwaysOnTop(true);
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBackground(new Color(0x1F2A19));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC9A227), 2, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel medal = new JLabel("★");
        medal.setForeground(new Color(0xF1C40F));
        medal.setFont(medal.getFont().deriveFont(Font.BOLD, 28f));
        JPanel text = new JPanel(new GridLayout(0, 1));
        text.setOpaque(false);
        JLabel title = new JLabel("Achievement Unlocked");
        title.setForeground(new Color(0xF8E9A1));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 11f));
        JLabel name = new JLabel(a.name());
        name.setForeground(Color.WHITE);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 15f));
        JLabel desc = new JLabel(a.description());
        desc.setForeground(new Color(0xCDD6C7));
        desc.setFont(desc.getFont().deriveFont(Font.PLAIN, 11f));
        text.add(title); text.add(name); text.add(desc);
        panel.add(medal, BorderLayout.WEST);
        panel.add(text,  BorderLayout.CENTER);
        toast.setContentPane(panel);
        toast.pack();

        int stack = 0;
        for (JWindow w : ACTIVE) stack += w.getHeight() + 8;
        Point loc = parent == null
                ? new Point(800, 600 - stack)
                : new Point(parent.getX() + parent.getWidth() - toast.getWidth() - 24,
                            parent.getY() + parent.getHeight() - toast.getHeight() - 80 - stack);
        toast.setLocation(loc);
        ACTIVE.add(toast);
        toast.setVisible(true);

        Timer fade = new Timer(4000, e -> { toast.dispose(); ACTIVE.remove(toast); });
        fade.setRepeats(false);
        fade.start();
    }
}
