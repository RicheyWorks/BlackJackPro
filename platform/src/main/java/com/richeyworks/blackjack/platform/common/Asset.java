package com.richeyworks.blackjack.platform.common;

/**
 * Supported settlement assets. Fiat and crypto share one double-entry ledger;
 * {@link #isCrypto()} drives FinCEN/MSB + AML handling and per-state feature gating
 * (crypto is disabled in states that prohibit it).
 */
public enum Asset {
    USD(false),
    BTC(true),
    ETH(true),
    USDT(true),
    USDC(true),
    SOL(true);

    private final boolean crypto;

    Asset(boolean crypto) { this.crypto = crypto; }

    public boolean isCrypto() { return crypto; }
}
