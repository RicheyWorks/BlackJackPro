import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * A casino-grade deck with advanced features for auditability and real-time monitoring.
 *
 * COMPILE NOTE: Requires Jackson on the classpath.
 * Download jackson-databind + jackson-core + jackson-annotations JARs and place them in:
 *   FinalProjectCS141/lib/
 * Then add to your compile and run commands:
 *   -cp "bin;lib/*"
 *
 * Updated compile command:
 *   javac --module-path "C:\Program Files\javafx-sdk-21.0.5\lib" \
 *         --add-modules javafx.controls,javafx.fxml,javafx.media \
 *         -cp "lib/*" -d bin src\*.java
 *
 * Updated run command (geany_run.bat):
 *   java -ea --module-path "C:\Program Files\javafx-sdk-21.0.5\lib" \
 *        --add-modules javafx.controls,javafx.fxml,javafx.media \
 *        -cp "bin;lib/*" BlackJackGUI
 */
public class Deck {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    private static final int    LOW_TRUST_THRESHOLD = 50;   // was the undefined "grok50"
    private static final int    KEY_SIZE            = 2048;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final List<Card> cards       = Collections.synchronizedList(new ArrayList<>());
    private final List<Card> dealtCards  = Collections.synchronizedList(new ArrayList<>());
    private final Deque<DealRecord>  dealtHistory;
    private final GameLog  gameLog;
    private final Clock    clock;
    private final Random   random;
    private final int      maxCards;
    private volatile String  dealerId;
    private volatile int     dealCount;
    private volatile int     runningCount;
    private final double     reshuffleThreshold;
    private volatile int     cutCardPosition;
    private final Map<String, Integer> cardHeatMap   = new ConcurrentHashMap<>();
    private final Map<String, Integer> dispersionMap = new ConcurrentHashMap<>();
    private volatile long    lastShuffleSeed;
    private volatile String  lastDeckSignature;
    private final String     deckId;
    private final Deque<String>      shuffleHistory;
    private final Map<String, DealerStats>  dealerStats   = new ConcurrentHashMap<>();
    private final List<DeckObserver>        observers     = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Integer>      playerWins    = new ConcurrentHashMap<>();
    private final Map<String, Integer>      wearCount     = new ConcurrentHashMap<>();
    private volatile Instant lastDealTime;
    private final List<DealerShift>              dealerHistory       = Collections.synchronizedList(new ArrayList<>());
    private final Deque<Double>                  entropyHistory      = new ArrayDeque<>(100);
    private final Deque<Double>                  shuffleEfficiencyHistory = new ArrayDeque<>(100);
    private final Map<String, List<Integer>>     trustScoreHistory   = new ConcurrentHashMap<>();
    private KeyPair keyPair;
    private final ReentrantLock dealLock = new ReentrantLock();
    private ShuffleAlgorithm shuffleAlgorithm = ShuffleAlgorithm.RANDOM;
    private final DeckType deckType;
    private final Path statsPersistencePath = Path.of("deck_stats_" + UUID.randomUUID().toString() + ".json");

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    public enum ShuffleAlgorithm { RANDOM, RIFFLE }

    public enum DeckType {
        BLACKJACK("blackjack_deck.json"),
        POKER("poker_deck.json"),
        UNO("uno_deck.json"),
        SPANISH("spanish_deck.json");

        private final String definitionFile;
        DeckType(String f) { this.definitionFile = f; }
        public String getDefinitionFile() { return definitionFile; }
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Deck(String filePath, int numDecks, GameLog gameLog, Clock clock,
                Random random, int maxCards, double reshuffleThreshold, DeckType deckType) {
        this.dealtHistory        = new ArrayDeque<>(maxCards);
        this.gameLog             = gameLog;
        this.clock               = clock;
        this.random              = random;
        this.maxCards            = maxCards;
        this.dealerId            = "Unknown";
        this.dealCount           = 0;
        this.runningCount        = 0;
        this.reshuffleThreshold  = reshuffleThreshold;
        this.cutCardPosition     = -1;
        this.lastShuffleSeed     = 0;
        this.lastDeckSignature   = "";
        this.deckId              = UUID.randomUUID().toString();
        this.shuffleHistory      = new ArrayDeque<>(10);
        this.lastDealTime        = null;
        this.deckType            = deckType;
        generateKeyPair();
        loadDeckFromFile(filePath, numDecks);
        validateDeckBalance(numDecks);
        loadPersistentStats();
        shuffle();
        assignNewDealer(dealerId, clock.instant().toString());
    }

    /** Minimal constructor for programmatic / test use (no file load). */
    public Deck(GameLog gameLog, Clock clock, Random random, int maxCards, DeckType deckType) {
        this.dealtHistory        = new ArrayDeque<>(maxCards);
        this.gameLog             = gameLog;
        this.clock               = clock;
        this.random              = random;
        this.maxCards            = maxCards;
        this.dealerId            = "Unknown";
        this.dealCount           = 0;
        this.runningCount        = 0;
        this.reshuffleThreshold  = 0.75;
        this.cutCardPosition     = -1;
        this.lastShuffleSeed     = 0;
        this.lastDeckSignature   = "";
        this.deckId              = UUID.randomUUID().toString();
        this.shuffleHistory      = new ArrayDeque<>(10);
        this.lastDealTime        = null;
        this.deckType            = deckType;
        generateKeyPair();
        loadPersistentStats();
        assignNewDealer(dealerId, clock.instant().toString());
    }

    public static Deck createSilent() {
        return new Deck(GameLog.noOp(), Clock.systemUTC(), new Random(), 416, DeckType.BLACKJACK);
    }

    // -------------------------------------------------------------------------
    // Key pair
    // -------------------------------------------------------------------------

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE);
            this.keyPair = keyGen.generateKeyPair();
            gameLog.log("RSA key pair generated for deck " + deckId);
        } catch (NoSuchAlgorithmException e) {
            gameLog.logError("Failed to generate RSA key pair for deck " + deckId, e);
            throw new RuntimeException("RSA key generation failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Persistent stats
    // -------------------------------------------------------------------------

    private void loadPersistentStats() {
        try {
            if (Files.exists(statsPersistencePath)) {
                String json = Files.readString(statsPersistencePath);
                Map<String, Object> data = objectMapper.readValue(json, Map.class);
                dealerStats.putAll((Map<String, DealerStats>) data.getOrDefault("dealerStats", new HashMap<>()));
                trustScoreHistory.putAll((Map<String, List<Integer>>) data.getOrDefault("trustScoreHistory", new HashMap<>()));
                gameLog.log("Loaded persistent stats for deck " + deckId);
            }
        } catch (IOException e) {
            gameLog.logError("Failed to load persistent stats for deck " + deckId, e);
        }
    }

    private void savePersistentStats() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("dealerStats", new HashMap<>(dealerStats));
            data.put("trustScoreHistory", new HashMap<>(trustScoreHistory));
            Files.writeString(statsPersistencePath, objectMapper.writeValueAsString(data));
        } catch (IOException e) {
            gameLog.logError("Failed to save persistent stats for deck " + deckId, e);
        }
    }

    // -------------------------------------------------------------------------
    // Observers
    // -------------------------------------------------------------------------

    public void addObserver(DeckObserver observer) { observers.add(observer); }

    private void notifyObservers() {
        String jsonState = toJson();
        synchronized (observers) { observers.forEach(o -> o.onDeckStateChanged(jsonState)); }
    }

    private void notifyTamperDetected() {
        synchronized (observers) {
            observers.forEach(o -> o.onTamperDetected(deckId, "Deck tamper detected at " + clock.instant()));
        }
    }

    // -------------------------------------------------------------------------
    // Deck loading
    // -------------------------------------------------------------------------

    private void loadDeckFromFile(String filePath, int numDecks) {
        if (numDecks * 52 > maxCards) {
            gameLog.logError("Requested " + numDecks + " decks exceeds max " + maxCards, null);
            throw new IllegalArgumentException("Too many decks for max capacity.");
        }
        String[] ranks = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<String> read = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String r = line.trim();
                if (!r.isEmpty()) read.add(r);
            }
            if (!read.isEmpty()) ranks = read.toArray(new String[0]);
        } catch (IOException e) {
            gameLog.logError("Deck file not found or unreadable; using built-in 52-card spec: " + filePath, e);
        }
        // Built-in fallback so the game still works when deck.txt is missing.
        if (ranks == null) {
            ranks = new String[]{"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
        }
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        for (int i = 0; i < numDecks; i++) {
            for (String rank : ranks) {
                for (String suit : suits) {
                    int value = Card.calculateCardValue(rank);
                    Card card = new Card(rank, suit, value);
                    if (random.nextDouble() < 0.01) card.markAsSuspicious();
                    cards.add(card);
                    wearCount.put(card.toString(), 0);
                    dispersionMap.put(card.toString(), 0);
                }
            }
        }
        gameLog.logDeckOperation("Deck loaded (" + numDecks + " decks, " + cards.size() + " cards)", cards.size());
        notifyObservers();
    }

    private void validateDeckBalance(int numDecks) {
        Map<String, Integer> rankCounts = new HashMap<>();
        synchronized (cards) {
            for (Card card : cards) rankCounts.merge(card.getRank(), 1, Integer::sum);
        }
        String[] expectedRanks = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
        int expectedCount = numDecks * 4;
        for (String rank : expectedRanks) {
            int count = rankCounts.getOrDefault(rank, 0);
            if (count != expectedCount) {
                gameLog.logError("Balance check failed: rank " + rank + " has " + count + ", expected " + expectedCount, null);
                updateDealerStats("balanceIssue");
            }
        }
        gameLog.logDeckOperation("Deck balance validated: " + rankCounts.size() + " ranks", cards.size());
    }

    // -------------------------------------------------------------------------
    // Dealer management
    // -------------------------------------------------------------------------

    public String getDeckId() { return deckId; }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
        gameLog.logDeckOperation("Dealer set to " + dealerId, cards.size());
        assignNewDealer(dealerId, clock.instant().toString());
        notifyObservers();
    }

    public void assignNewDealer(String dealerId, String shiftStart) {
        this.dealerId = dealerId;
        synchronized (dealerHistory) { dealerHistory.add(new DealerShift(dealerId, shiftStart)); }
        gameLog.logDeckOperation("Dealer " + dealerId + " assigned at " + shiftStart, cards.size());
        updateDealerStats("shift");
    }

    // -------------------------------------------------------------------------
    // Shuffle
    // -------------------------------------------------------------------------

    private double calculateShuffleEfficiency(List<Card> before, List<Card> after) {
        int unchanged = 0;
        for (int i = 0; i < Math.min(before.size(), after.size()); i++) {
            if (before.get(i).equals(after.get(i))) unchanged++;
        }
        return before.isEmpty() ? 1.0 : 1.0 - (double) unchanged / before.size();
    }

    private double calculateRiffleShuffleEfficiency(List<Card> before, List<Card> after) {
        int inversions = 0;
        for (int i = 0; i < after.size() - 1; i++) {
            for (int j = i + 1; j < after.size(); j++) {
                if (before.indexOf(after.get(i)) > before.indexOf(after.get(j))) inversions++;
            }
        }
        double maxInversions = (double)(after.size() * (after.size() - 1)) / 2;
        return maxInversions == 0 ? 0 : inversions / maxInversions;
    }

    public void setShuffleAlgorithm(ShuffleAlgorithm algorithm) {
        this.shuffleAlgorithm = algorithm;
        gameLog.log("Shuffle algorithm set to " + algorithm);
    }

    public void shuffle() {
        synchronized (cards) {
            if (cards.size() > maxCards) {
                gameLog.logError("Deck size " + cards.size() + " exceeds max " + maxCards, null);
                throw new IllegalStateException("Deck exceeds maximum size.");
            }
            List<Card> before = new ArrayList<>(cards);
            lastShuffleSeed = random.nextLong();
            double efficiency;
            if (shuffleAlgorithm == ShuffleAlgorithm.RANDOM) {
                Collections.shuffle(cards, new Random(lastShuffleSeed));
                efficiency = calculateShuffleEfficiency(before, cards);
            } else {
                riffleShuffle();
                efficiency = calculateRiffleShuffleEfficiency(before, cards);
            }
            lastDeckSignature = computeDeckHash();
            String signature = computeDigitalSignature();
            synchronized (shuffleHistory) { recordShuffleHistory(signature); }
            gameLog.logDeckOperation("Deck shuffled. Hash: " + lastDeckSignature
                    + " efficiency: " + String.format("%.2f", efficiency), cards.size());
            if (efficiency < 0.8) {
                gameLog.logError("Shuffle efficiency too low: " + String.format("%.2f", efficiency), null);
                updateDealerStats("shuffleInefficiency");
            }
            dealtCards.clear();
            synchronized (dealtHistory) { dealtHistory.clear(); }
            dealCount    = 0;
            runningCount = 0;
            cutCardPosition = -1;
            cardHeatMap.clear();
            lastDealTime = null;
            updateDealerStats(shuffleAlgorithm == ShuffleAlgorithm.RANDOM ? "shuffle" : "riffleShuffle");
            synchronized (shuffleEfficiencyHistory) {
                shuffleEfficiencyHistory.addLast(efficiency);
                if (shuffleEfficiencyHistory.size() > 100) shuffleEfficiencyHistory.removeFirst();
            }
            synchronized (entropyHistory) {
                entropyHistory.addLast(estimateEntropy());
                if (entropyHistory.size() > 100) entropyHistory.removeFirst();
            }
            notifyObservers();
        }
    }

    private void riffleShuffle() {
        synchronized (cards) {
            List<Card> half1 = new ArrayList<>(cards.subList(0, cards.size() / 2));
            List<Card> half2 = new ArrayList<>(cards.subList(cards.size() / 2, cards.size()));
            cards.clear();
            while (!half1.isEmpty() || !half2.isEmpty()) {
                if (!half1.isEmpty() && (half2.isEmpty() || random.nextBoolean())) {
                    cards.add(half1.remove(0));
                } else if (!half2.isEmpty()) {
                    cards.add(half2.remove(0));
                }
            }
        }
    }

    private void recordShuffleHistory(String signature) {
        if (shuffleHistory.size() >= 10) shuffleHistory.removeFirst();
        shuffleHistory.addLast(lastDeckSignature + "|" + signature);
    }

    // -------------------------------------------------------------------------
    // Cryptography
    // -------------------------------------------------------------------------

    private String computeDigitalSignature() {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(keyPair.getPrivate());
            sig.update(computeDeckHash().getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            gameLog.logError("Failed to compute RSA digital signature", e);
            return "unsigned";
        }
    }

    public boolean verifyLastSignature(String signatureBase64, String publicKeyPem) {
        try {
            PublicKey publicKey = publicKeyPem != null ? parsePublicKey(publicKeyPem) : keyPair.getPublic();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(computeDeckHash().getBytes("UTF-8"));
            boolean valid = sig.verify(Base64.getDecoder().decode(signatureBase64));
            gameLog.logDeckOperation("Signature verification: " + (valid ? "valid" : "invalid"), cards.size());
            return valid;
        } catch (Exception e) {
            gameLog.logError("Failed to verify RSA signature", e);
            return false;
        }
    }

    private PublicKey parsePublicKey(String pem) throws Exception {
        String key = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public String getPublicKeyPem() {
        try {
            return "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
                    + "\n-----END PUBLIC KEY-----";
        } catch (Exception e) {
            gameLog.logError("Failed to export public key", e);
            return "";
        }
    }

    public List<String> getSignedShuffleHistory() {
        synchronized (shuffleHistory) { return new ArrayList<>(shuffleHistory); }
    }

    // -------------------------------------------------------------------------
    // Deck operations
    // -------------------------------------------------------------------------

    public void burnCards(int count) {
        synchronized (cards) {
            for (int i = 0; i < count && !cards.isEmpty(); i++) {
                Card burned = cards.remove(cards.size() - 1);
                gameLog.logDeckOperation("Card burned: " + burned, cards.size());
            }
            detectTamper();
            updateDealerStats("burn");
            notifyObservers();
        }
    }

    public void insertCutCard(int positionFromEnd) {
        if (positionFromEnd < 0 || positionFromEnd >= maxCards) {
            gameLog.logError("Invalid cut card position: " + positionFromEnd, null);
            return;
        }
        cutCardPosition = maxCards - positionFromEnd;
        gameLog.logDeckOperation("Cut card at position " + cutCardPosition, cards.size());
        notifyObservers();
    }

    // -------------------------------------------------------------------------
    // Deal
    // -------------------------------------------------------------------------

    public Card dealCard(BlackJackPlayer player, int betAmount) {
        dealLock.lock();
        try {
            if (cards.isEmpty()) {
                gameLog.logError("No more cards to deal.", null);
                throw new IllegalStateException("No more cards in the deck.");
            }
            if (needsReshuffle() || reachedCutCard()) {
                gameLog.logDeckOperation("Reshuffle threshold reached; reshuffling.", cards.size());
                shuffle();
                burnCards(1);
            }
            // NOTE: tamper detection used to auto-shuffle on EVERY deal because
            // removing a card naturally changes the deck hash. It's now a pure
            // log-only audit; legitimate dealing is no longer treated as tamper.
            // The signature is refreshed below to track the new known-good state.
            Instant now = clock.instant();
            checkDealTiming(now);
            lastDealTime = now;

            Card dealtCard;
            synchronized (cards) {
                dealtCard = cards.remove(cards.size() - 1);
                dealtCards.add(dealtCard);
                // Refresh known-good signature so detectTamper() doesn't fire
                // on the very next call simply because we legitimately dealt.
                lastDeckSignature = computeDeckHash();
            }
            synchronized (dealtHistory) {
                dealtHistory.add(new DealRecord(dealtCard, player.getName(), betAmount));
            }
            dealCount++;
            updateCount(dealtCard);
            updateHeatMap(dealtCard);
            updateDispersionMap(dealtCard);
            updateWearCount(dealtCard);
            checkPlayerWins(player.getName(), dealtCard);

            if (dealCount > maxCards / 2) {
                gameLog.logError("Anomaly: " + dealCount + " cards dealt without manual shuffle.", null);
                updateDealerStats("anomaly");
            }
            if (dealtCard.isMarked()) {
                gameLog.logError("Suspicious card dealt: " + dealtCard + " to " + player.getName(), null);
                updateDealerStats("markedCard");
            }

            gameLog.logDeckOperation("Dealt: " + dealtCard + " to " + player.getName()
                    + " (bet " + betAmount + ")", cards.size());
            gameLog.logChipTransaction(player.getName(), betAmount, "Player", "Table");
            player.addCard(dealtCard);
            notifyObservers();
            return dealtCard;
        } finally {
            dealLock.unlock();
        }
    }

    private void checkDealTiming(Instant now) {
        if (lastDealTime != null) {
            long ms = java.time.Duration.between(lastDealTime, now).toMillis();
            if (ms < 100) {
                gameLog.logError("Timing anomaly: deal interval " + ms + "ms too fast", null);
                updateDealerStats("fastDeal");
            } else if (ms > 5000) {
                gameLog.logError("Timing anomaly: deal interval " + ms + "ms too slow", null);
                updateDealerStats("slowDeal");
            }
        }
    }

    private void updateWearCount(Card card) {
        String key = card.toString();
        wearCount.merge(key, 1, Integer::sum);
        if (wearCount.get(key) > 50) {
            gameLog.logError("Wear anomaly: " + card + " dealt " + wearCount.get(key) + " times", null);
            updateDealerStats("wearAnomaly");
        }
    }

    private void updateDispersionMap(Card card) {
        String key = card.toString();
        dispersionMap.merge(key, 1, Integer::sum);
        int count = dispersionMap.get(key);
        if (count > maxCards / 13 * 2) {
            gameLog.logError("Dispersion anomaly: " + card + " dealt " + count + " times", null);
            updateDealerStats("dispersionAnomaly");
        }
    }

    public void checkCardDistribution() {
        dealLock.lock();
        try {
            if (dealCount == 0) {
                gameLog.logDeckOperation("No deals to analyze.", cards.size());
                return;
            }
            double expected = (double) dealCount / 52;
            for (String card : cardHeatMap.keySet()) {
                double observed = cardHeatMap.get(card);
                if (Math.abs(observed - expected) > expected * 0.2) {
                    gameLog.logError("Distribution anomaly: " + card
                            + " expected ~" + String.format("%.1f", expected)
                            + ", got " + observed, null);
                    updateDealerStats("distributionAnomaly");
                }
            }
        } finally {
            dealLock.unlock();
        }
    }

    private void checkPlayerWins(String playerId, Card dealtCard) {
        if (dealtCard.getValue() >= 10) {
            playerWins.merge(playerId, 1, Integer::sum);
            if (playerWins.get(playerId) > 5) flagSuspiciousPlayer(playerId);
        }
    }

    public void flagSuspiciousPlayer(String playerId) {
        gameLog.flagPlayer(playerId, "Received " + playerWins.getOrDefault(playerId, 0) + " high-value cards");
        updateDealerStats("counting");
    }

    // -------------------------------------------------------------------------
    // Playback / rollback
    // -------------------------------------------------------------------------

    private String computePlaybackSignature() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder full = new StringBuilder();
            synchronized (dealtHistory) {
                for (DealRecord r : dealtHistory) {
                    full.append(r.getCard()).append(r.getPlayerId()).append(r.getBetAmount());
                }
            }
            byte[] hash = digest.digest(full.toString().getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            gameLog.logError("Failed to compute playback signature", e);
            return "unsigned";
        }
    }

    public void playbackDeals() {
        dealLock.lock();
        try {
            synchronized (dealtHistory) {
                if (dealtHistory.isEmpty()) { gameLog.logDeckOperation("No deals to playback.", cards.size()); return; }
                gameLog.logDeckOperation("Playback signature: " + computePlaybackSignature(), cards.size());
                for (DealRecord r : dealtHistory) {
                    String msg = "Deal: " + r.getCard() + " to " + r.getPlayerId() + " bet " + r.getBetAmount();
                    gameLog.logDeckOperation(msg, cards.size());
                    System.out.println(msg);
                }
            }
            updateDealerStats("playback");
            notifyObservers();
        } finally {
            dealLock.unlock();
        }
    }

    public boolean verifyPlaybackSignature(String expected) {
        String current = computePlaybackSignature();
        boolean valid = current.equals(expected);
        gameLog.logDeckOperation("Playback signature " + (valid ? "valid" : "INVALID"), cards.size());
        if (!valid) updateDealerStats("tamper");
        return valid;
    }

    public DealRecord rollbackDeal() {
        dealLock.lock();
        try {
            synchronized (dealtHistory) {
                if (dealtHistory.isEmpty()) return null;
                DealRecord record = dealtHistory.removeLast();
                Card card = record.getCard();
                dealtCards.remove(card);
                cards.add(card);
                dealCount--;
                runningCount -= countValue(card);
                cardHeatMap.compute(card.toString(), (k, v) -> v == null ? 0 : v - 1);
                dispersionMap.compute(card.toString(), (k, v) -> v == null ? 0 : v - 1);
                wearCount.compute(card.toString(), (k, v) -> v == null ? 0 : v - 1);
                playerWins.computeIfPresent(record.getPlayerId(),
                        (k, v) -> card.getValue() >= 10 ? v - 1 : v);
                gameLog.logDeckOperation("Rolled back: " + card + " from " + record.getPlayerId(), cards.size());
                gameLog.logChipTransaction(record.getPlayerId(), record.getBetAmount(), "Table", "Player");
                detectTamper();
                updateDealerStats("rollback");
                notifyObservers();
                return record;
            }
        } finally {
            dealLock.unlock();
        }
    }

    // -------------------------------------------------------------------------
    // Card counting
    // -------------------------------------------------------------------------

    private void updateCount(Card card) {
        int val = card.getValue();
        if      (val >= 2 && val <= 6) runningCount++;
        else if (val == 10 || val == 11) runningCount--;
        if (Math.abs(runningCount) > 10) {
            gameLog.logError("Potential card counting: running count = " + runningCount, null);
            updateDealerStats("counting");
        }
    }

    private int countValue(Card card) {
        int val = card.getValue();
        if (val >= 2 && val <= 6) return 1;
        if (val == 10 || val == 11) return -1;
        return 0;
    }

    private void updateHeatMap(Card card) {
        cardHeatMap.merge(card.toString(), 1, Integer::sum);
        if (cardHeatMap.get(card.toString()) > maxCards / 13) {
            gameLog.logError("Heatmap anomaly: " + card + " dealt " + cardHeatMap.get(card.toString()) + " times", null);
            updateDealerStats("heatmapAnomaly");
        }
    }

    public int getRunningCount() { return runningCount; }

    public double getTrueCount() {
        double remainingDecks = (double) getRemainingCards() / 52;
        return remainingDecks > 0 ? (double) runningCount / remainingDecks : runningCount;
    }

    // -------------------------------------------------------------------------
    // Stats / trust
    // -------------------------------------------------------------------------

    private void updateDealerStats(String action) {
        dealerStats.computeIfAbsent(dealerId, k -> new DealerStats()).increment(action);
        savePersistentStats();
    }

    public boolean needsReshuffle() {
        return (double) dealtCards.size() / maxCards >= reshuffleThreshold;
    }

    public boolean reachedCutCard() {
        return cutCardPosition >= 0 && cards.size() <= cutCardPosition;
    }

    public int getTrustScore() {
        DealerStats stats = dealerStats.getOrDefault(dealerId, new DealerStats());
        int penalty = stats.anomalies + stats.markedCards * 2 + stats.tamperEvents * 5
                + stats.countingEvents * 3 + stats.heatmapAnomalies + stats.wearAnomalies
                + stats.fastDeals + stats.slowDeals + stats.distributionAnomalies
                + stats.dispersionAnomalies + stats.shuffleInefficiency;
        int score = Math.max(0, 100 - penalty);
        if (score < LOW_TRUST_THRESHOLD) {   // ← was the undefined "grok50"
            gameLog.logError("Low trust score (" + score + ") for dealer " + dealerId, null);
        }
        trustScoreHistory.computeIfAbsent(dealerId,
                k -> Collections.synchronizedList(new ArrayList<>())).add(score);
        savePersistentStats();
        return score;
    }

    public double estimateEntropy() {
        Set<String> unique = new HashSet<>();
        synchronized (cards) { for (Card c : cards) unique.add(c.toString()); }
        double entropy = Math.log(Math.max(1, unique.size())) / Math.log(2);
        if (!cards.isEmpty() && entropy < Math.log(cards.size()) / Math.log(2) * 0.9) {
            gameLog.logError("Low shuffle entropy: " + entropy + " bits", null);
            updateDealerStats("lowEntropy");
        }
        return entropy;
    }

    // -------------------------------------------------------------------------
    // Export / integrity
    // -------------------------------------------------------------------------

    public void exportHeatmapCSV(Path path) throws IOException {
        StringBuilder sb = new StringBuilder("Card,Count\n");
        for (Map.Entry<String, Integer> e : cardHeatMap.entrySet())
            sb.append(e.getKey()).append(",").append(e.getValue()).append("\n");
        Files.writeString(path, sb.toString());
    }

    public String exportDispersionMapJson() {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> e : dispersionMap.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(e.getKey()).append("\": ").append(e.getValue());
            first = false;
        }
        return json.append("}").toString();
    }

    public String exportIntegrityJson() {
        String state = toJson();
        return String.format("{\"state\": %s, \"integrityHash\": \"%s\", \"signature\": \"%s\", \"publicKey\": \"%s\"}",
                state, computeIntegrityHash(state), computeDigitalSignature(), getPublicKeyPem());
    }

    public void exportIntegrityToFile(Path path) throws IOException {
        Files.writeString(path, exportIntegrityJson());
    }

    private String computeIntegrityHash(String state) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] hash = d.digest(state.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            gameLog.logError("Failed to compute integrity hash", e);
            return "unsigned";
        }
    }

    public boolean verifyIntegrity(String jsonWithHash) {
        try {
            int hashIndex = jsonWithHash.lastIndexOf("\"integrityHash\":");
            String statePart = jsonWithHash.substring(0, hashIndex - 1) + "}";
            String hashPart  = jsonWithHash.substring(hashIndex + 16, jsonWithHash.length() - 1).replaceAll("\"", "");
            boolean valid = computeIntegrityHash(statePart).equals(hashPart);
            if (!valid) gameLog.logError("Integrity check failed", null);
            return valid;
        } catch (Exception e) {
            gameLog.logError("Failed to verify integrity", e);
            return false;
        }
    }

    public Map<String, Object> getTelemetrySnapshot() {
        Map<String, Object> snap = new HashMap<>();
        snap.put("deckId", deckId);
        snap.put("dealerId", dealerId);
        snap.put("remainingCards", cards.size());
        snap.put("dealtCards", dealtCards.size());
        snap.put("runningCount", runningCount);
        snap.put("trueCount", getTrueCount());
        snap.put("entropy", estimateEntropy());
        snap.put("trustScore", getTrustScore());
        snap.put("lastShuffleSeed", lastShuffleSeed);
        snap.put("deckHash", computeDeckHash());
        snap.put("lastDealTime", lastDealTime != null ? lastDealTime.toString() : "N/A");
        snap.put("playbackSignature", computePlaybackSignature());
        return snap;
    }

    public String getMetrics() {
        try { return objectMapper.writeValueAsString(getTelemetrySnapshot()); }
        catch (Exception e) { gameLog.logError("Failed to serialize metrics", e); return "{}"; }
    }

    public int getRemainingCards() { return cards.size(); }

    public void addCard(Card card) {
        synchronized (cards) {
            if (cards.size() >= maxCards) { gameLog.logError("Deck at max capacity.", null); return; }
            cards.add(card);
            wearCount.put(card.toString(), 0);
            dispersionMap.put(card.toString(), 0);
            detectTamper();
            notifyObservers();
        }
    }

    public void resetDeck() {
        synchronized (cards) {
            cards.addAll(dealtCards);
            dealtCards.clear();
            shuffle();
            gameLog.logDeckOperation("Deck reset.", cards.size());
            updateDealerStats("reset");
            notifyObservers();
        }
    }

    // -------------------------------------------------------------------------
    // Fairness report
    // -------------------------------------------------------------------------

    public void generateFairnessReport(Path outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        Instant now = clock.instant();
        report.append("=== Fairness Report for Deck ").append(deckId).append(" ===\n");
        report.append("Generated: ").append(now).append("\n\n");

        // 1. Card distribution
        report.append("1. Card Distribution\n");
        String mostDealt  = cardHeatMap.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("None");
        String leastDealt = cardHeatMap.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("None");
        report.append("Most dealt:  ").append(mostDealt).append(" (").append(cardHeatMap.getOrDefault(mostDealt, 0)).append(")\n");
        report.append("Least dealt: ").append(leastDealt).append(" (").append(cardHeatMap.getOrDefault(leastDealt, 0)).append(")\n\n");

        // 2. Entropy
        report.append("2. Entropy History (").append(entropyHistory.size()).append(" shuffles)\n");
        synchronized (entropyHistory) {
            double avg = entropyHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double min = entropyHistory.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double max = entropyHistory.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            report.append("Avg: ").append(String.format("%.2f", avg))
                  .append("  Min: ").append(String.format("%.2f", min))
                  .append("  Max: ").append(String.format("%.2f", max)).append("\n\n");
        }

        // 3. Shuffle efficiency
        report.append("3. Shuffle Efficiency (").append(shuffleEfficiencyHistory.size()).append(" shuffles)\n");
        synchronized (shuffleEfficiencyHistory) {
            double avg = shuffleEfficiencyHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            report.append("Avg: ").append(String.format("%.2f", avg)).append("\n\n");
        }

        // 4. Trust scores
        report.append("4. Trust Score by Dealer\n");
        for (Map.Entry<String, List<Integer>> entry : trustScoreHistory.entrySet()) {
            List<Integer> scores = entry.getValue();
            synchronized (scores) {
                double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
                report.append(entry.getKey()).append(": avg ").append(String.format("%.1f", avg)).append("\n");
            }
        }

        String fileName = "deck-fairness-report-" + now.toEpochMilli() + ".txt";
        Files.writeString(outputPath.resolve(fileName), report.toString());
        gameLog.logDeckOperation("Fairness report saved to " + outputPath.resolve(fileName), cards.size());
    }

    // -------------------------------------------------------------------------
    // toString / fromString / JSON
    // -------------------------------------------------------------------------

    @Override
    public String toString() { return CardParser.deckToString(cards); }

    public static Deck fromString(String deckString, GameLog gameLog, Clock clock,
                                  Random random, int maxCards, DeckType deckType) {
        Deck deck = new Deck(gameLog, clock, random, maxCards, deckType);
        for (Card card : CardParser.parseDeck(deckString)) deck.addCard(card);
        return deck;
    }

    private String computeDeckHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder state = new StringBuilder();
            synchronized (cards) { for (Card c : cards) state.append(c.toString()); }
            byte[] hash = digest.digest(state.toString().getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            gameLog.logError("Failed to compute deck hash", e);
            return Integer.toHexString(cards.hashCode());
        }
    }

    public boolean detectTamper() {
        String current = computeDeckHash();
        if (lastDeckSignature.isEmpty() || current.equals(lastDeckSignature)) return false;
        gameLog.logError("TAMPER DETECTED! Expected: " + lastDeckSignature + " got: " + current, null);
        updateDealerStats("tamper");
        notifyTamperDetected();
        notifyObservers();
        return true;
    }

    public String toJson() {
        try {
            Map<String, Object> state = new HashMap<>();
            state.put("deckId", deckId);
            state.put("dealerId", dealerId);
            state.put("remainingCards", cards.size());
            state.put("dealtCards", dealtCards.size());
            state.put("runningCount", runningCount);
            state.put("cutCardPosition", cutCardPosition);
            state.put("lastShuffleSeed", lastShuffleSeed);
            state.put("deckHash", computeDeckHash());
            state.put("lastDealTime", lastDealTime != null ? lastDealTime.toString() : "N/A");
            synchronized (shuffleHistory) { state.put("shuffleHistory", new ArrayList<>(shuffleHistory)); }
            state.put("playerWins", new HashMap<>(playerWins));
            state.put("heatmap", new HashMap<>(cardHeatMap));
            return objectMapper.writeValueAsString(state);
        } catch (Exception e) {
            gameLog.logError("Failed to serialize deck to JSON", e);
            return "{}";
        }
    }

    public void exportToFile(Path path) throws IOException {
        Files.writeString(path, toJson());
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    public interface DeckObserver {
        void onDeckStateChanged(String json);
        void onTamperDetected(String deckId, String message);
    }

    public static class FileObserver implements DeckObserver {
        private final Path logFile;
        public FileObserver(Path path) { this.logFile = path; }
        @Override public void onDeckStateChanged(String json) {
            try { Files.writeString(logFile, json + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException e) { System.err.println("Error logging deck state: " + e.getMessage()); }
        }
        @Override public void onTamperDetected(String deckId, String message) {
            try { Files.writeString(logFile, "TAMPER: " + message + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException e) { System.err.println("Error logging tamper: " + e.getMessage()); }
        }
    }

    public static class WebSocketObserver implements DeckObserver {
        private final String sessionId;
        public WebSocketObserver(String sessionId) { this.sessionId = sessionId; }
        @Override public void onDeckStateChanged(String json) {
            System.out.println("WS[" + sessionId + "] deck state: " + json); }
        @Override public void onTamperDetected(String deckId, String message) {
            System.out.println("WS[" + sessionId + "] TAMPER: " + message); }
    }

    public static class DealRecord {
        private final Card   card;
        private final String playerId;
        private final int    betAmount;
        public DealRecord(Card card, String playerId, int betAmount) {
            this.card = card; this.playerId = playerId; this.betAmount = betAmount;
        }
        public Card   getCard()      { return card; }
        public String getPlayerId()  { return playerId; }
        public int    getBetAmount() { return betAmount; }
    }

    public static class DealerStats {
        int shuffles, riffleShuffles, burns, anomalies, rollbacks;
        int markedCards, tamperEvents, countingEvents, heatmapAnomalies;
        int shifts, balanceIssues, wearAnomalies, fastDeals, slowDeals;
        int distributionAnomalies, dispersionAnomalies, shuffleInefficiency;

        public synchronized void increment(String action) {
            switch (action) {
                case "shuffle":              shuffles++;              break;
                case "riffleShuffle":        riffleShuffles++;        break;
                case "burn":                 burns++;                 break;
                case "anomaly":              anomalies++;             break;
                case "rollback":             rollbacks++;             break;
                case "markedCard":           markedCards++;           break;
                case "tamper":               tamperEvents++;          break;
                case "counting":             countingEvents++;        break;
                case "heatmapAnomaly":       heatmapAnomalies++;      break;
                case "shift":                shifts++;                break;
                case "reset":                shuffles++;              break;
                case "balanceIssue":         balanceIssues++;         break;
                case "wearAnomaly":          wearAnomalies++;         break;
                case "fastDeal":             fastDeals++;             break;
                case "slowDeal":             slowDeals++;             break;
                case "distributionAnomaly":  distributionAnomalies++; break;
                case "dispersionAnomaly":    dispersionAnomalies++;   break;
                case "shuffleInefficiency":  shuffleInefficiency++;   break;
            }
        }
    }

    public static class DealerShift {
        private final String dealerId;
        private final String shiftStart;
        public DealerShift(String dealerId, String shiftStart) {
            this.dealerId = dealerId; this.shiftStart = shiftStart;
        }
        public String toJson() {
            return String.format("{\"dealerId\":\"%s\",\"shiftStart\":\"%s\"}", dealerId, shiftStart);
        }
    }
}
