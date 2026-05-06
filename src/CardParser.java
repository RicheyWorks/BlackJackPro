import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing and converting {@code Card} objects to and from string representations.
 */
public class CardParser {

    private static final GameLog logger = GameLog.noOp();

    // Prevent instantiation — this is a static utility class
    private CardParser() {}

    /**
     * Parses a card string into a {@code Card} object.
     * Accepts both formats:
     *   "10 of Hearts"
     *   "10 of Hearts (Value: 10)"
     *
     * @param cardString the string representation of the card.
     * @return a {@code Card} object.
     * @throws IllegalArgumentException if the format is invalid.
     */
    public static Card parseCard(String cardString) {
        try {
            String[] parts = cardString.split(" of ", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid card string format: " + cardString);
            }

            String rank = parts[0].trim();

            // Strip optional "(Value: N)" suffix to get the bare suit
            String suitPart = parts[1].trim();
            String suit = suitPart.contains(" (Value:")
                    ? suitPart.substring(0, suitPart.indexOf(" (Value:")).trim()
                    : suitPart;

            if (!isValidSuit(suit)) {
                throw new IllegalArgumentException("Invalid suit: " + suit);
            }

            int value = Card.calculateCardValue(rank);
            return new Card(rank, suit, value);

        } catch (IllegalArgumentException e) {
            logger.logError("Failed to parse card string: " + cardString, e);
            throw e;
        } catch (Exception e) {
            logger.logError("Unexpected error parsing card string: " + cardString, e);
            throw new IllegalArgumentException("Failed to parse card string: " + cardString, e);
        }
    }

    /**
     * Converts a {@code Card} to a short string: "10 of Hearts"
     * Used for serialisation (save/load, network state).
     */
    public static String cardToString(Card card) {
        return card.getRank() + " of " + card.getSuit();
    }

    /**
     * Parses a comma-separated deck string into a list of {@code Card} objects.
     * Invalid entries are skipped with a log warning rather than aborting.
     *
     * @param deckString e.g. "10 of Hearts, A of Spades, 3 of Clubs"
     * @return list of successfully parsed cards (never null, may be empty)
     */
    public static List<Card> parseDeck(String deckString) {
        List<Card> cards = new ArrayList<>();
        if (deckString == null || deckString.isBlank()) return cards;

        for (String cardString : deckString.split(", ")) {
            try {
                cards.add(parseCard(cardString.trim()));
            } catch (IllegalArgumentException e) {
                logger.logError("Skipping invalid card during deck parsing: " + cardString, e);
            }
        }
        return cards;
    }

    /**
     * Converts a list of {@code Card} objects to a comma-separated string.
     * e.g. "10 of Hearts, A of Spades"
     */
    public static String deckToString(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cardToString(cards.get(i)));
            if (i < cards.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * Validates a deck string without creating objects.
     *
     * @return true if every card entry parses successfully.
     */
    public static boolean validateDeck(String deckString) {
        if (deckString == null || deckString.isBlank()) return false;
        for (String cardString : deckString.split(", ")) {
            try {
                parseCard(cardString.trim());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean isValidSuit(String suit) {
        return "Hearts".equals(suit)
                || "Diamonds".equals(suit)
                || "Clubs".equals(suit)
                || "Spades".equals(suit);
    }
}
