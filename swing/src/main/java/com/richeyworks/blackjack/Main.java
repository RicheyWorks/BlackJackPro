package com.richeyworks.blackjack;

import com.richeyworks.blackjack.ui.swing.BlackJackProApp;

/** Application entry point. Defers everything to the Swing app launcher. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        BlackJackProApp.launch();
    }
}
