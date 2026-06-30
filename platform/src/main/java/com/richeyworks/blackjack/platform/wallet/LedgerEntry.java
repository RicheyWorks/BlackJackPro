package com.richeyworks.blackjack.platform.wallet;

import com.richeyworks.blackjack.platform.common.Asset;

/**
 * One leg of a balanced, append-only double-entry posting. Balances are DERIVED by summing
 * entries — never mutated in place. Every money movement is two or more legs that sum to
 * zero per asset (e.g. a wager debits the player's available account and credits an escrow
 * account). Amounts are signed minor units (cents, satoshis, wei-scaled, etc.).
 */
public record LedgerEntry(
        String entryId,
        String transactionId,    // groups the legs of one atomic movement
        String account,          // e.g. "player:123:available", "player:123:escrow", "house:rake"
        Asset asset,
        long amountMinor,        // signed: debits negative, credits positive
        String idempotencyKey,
        long timestampEpochMs) {}
