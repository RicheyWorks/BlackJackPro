package com.richeyworks.blackjack.ui.swing;

import com.richeyworks.blackjack.plugin.BlackJackPlugin;
import com.richeyworks.blackjack.plugin.PluginRegistry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/** Inspector that shows every loaded plugin with its themes, AI, and side bets. */
public final class PluginManagerDialog extends JDialog {

    public PluginManagerDialog(JFrame parent, PluginRegistry registry) {
        super(parent, "Plugins", true);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (registry.all().isEmpty()) {
            JLabel empty = new JLabel("No plugins loaded.");
            empty.setFont(empty.getFont().deriveFont(Font.ITALIC));
            content.add(empty);
        } else {
            for (BlackJackPlugin p : registry.all()) content.add(card(p));
        }

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(close);

        add(new JScrollPane(content), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        setSize(new Dimension(520, 420));
        setLocationRelativeTo(parent);
    }

    private JPanel card(BlackJackPlugin p) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBDBDBD)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        c.setBackground(Color.WHITE);
        c.setAlignmentX(0);

        JLabel name = new JLabel(p.manifest().name() + "  v" + p.manifest().version());
        name.setFont(name.getFont().deriveFont(Font.BOLD, 14f));
        JLabel id   = new JLabel("id: " + p.manifest().id() + "   author: " + p.manifest().author());
        id.setForeground(new Color(0x707070));
        JLabel desc = new JLabel("<html>" + p.manifest().description() + "</html>");
        c.add(name); c.add(id); c.add(Box.createVerticalStrut(4)); c.add(desc);
        if (!p.themes().isEmpty())       c.add(new JLabel("Themes: "       + p.themes().size()));
        if (!p.aiStrategies().isEmpty()) c.add(new JLabel("AI personalities: " + p.aiStrategies().size()));
        if (!p.sideBets().isEmpty())     c.add(new JLabel("Side bets: "    + p.sideBets().size()));
        c.add(Box.createVerticalStrut(6));
        return c;
    }
}
