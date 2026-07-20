# Platform Module — Code Review

**Scope:** the `platform/` module only — compliance gate, double-entry wallet, RNG seam, crypto cashier interface, and the server-authoritative round service. 664 LOC of main source across 15 files, plus 255 LOC of tests.
**Reviewed at:** `claude/gradle-foundation` (commit `b31c17e`).
**Method:** full read of every source, cross-checked against `core` (`Engine`, `Shoe`, `BlackjackRules`). Every Medium-or-worse finding was reproduced with a JDK 21 probe driving the real classes. Findings that did not reproduce were dropped.

## Verdict

The architecture is right, and notably better than most first attempts at this: a single fail-closed chokepoint, an append-only double-entry ledger with derived balances, idempotency keys throughout, and the game engine reused rather than reimplemented. The documentation is unusually honest about what is stub and what is real.

But **`DefaultGameRoundService` has a money-losing bug reachable by an ordinary network retry** (PL-1), and it is invisible to the ledger's own zero-sum invariant. The module is explicitly not operable without licensing and counsel, so nothing here is live — which is exactly why it is cheap to fix now.

One thing worth stating plainly: this review covers **code correctness only**. Whether the licensed-state list, the withdrawal rules, or the crypto handling actually satisfy any regulator is a question for gaming counsel, not for me. Where a finding touches that, I have described the mechanism and left the legal conclusion open.

## Findings

| # | Severity | Summary |
|---|----------|---------|
| PL-1 | **High** | A retried `startRound` plays two rounds on one stake |
| PL-2 | Medium | The notional bankroll silently caps what a high-stakes player may do |
| PL-3 | Medium | Abandoned rounds strand escrow and grow without bound |
| PL-4 | Medium | Self-exclusion and location checks also block withdrawals |
| PL-5 | Medium | Crypto permission is deny-listed, against the module's own fail-closed rule |
| PL-6 | Medium | The provably-fair commitment is computed and discarded |
| PL-7 | Low | Insurance premium recomputed instead of asked for |
| PL-8 | Low | `RoundRng`'s guarantee rests on an unenforced assumption |
| PL-9 | Low | `post()` accepts a null idempotency key and silently forgoes replay protection |
| PL-10 | Low | Idempotent replay is not checked against the original request |
| PL-11 | Info | Balance lookups are O(ledger) |
| PL-12 | Info | Ledger sums use unchecked `long` arithmetic |
| PL-13 | Info | Audit-sink failure has undefined behaviour |

---

### Money

**PL-1 · High · A retried `startRound` plays two rounds on one stake.**

`startRound` passes its `idempotencyKey` to `wallet.hold`, which is correctly idempotent and escrows nothing on a replay. But `startRound` itself is not: it goes on to mint a new `roundId`, build a new `Engine`, and deal a second hand anyway. Both rounds then settle against an escrow that only ever received one stake.

Verified end to end — same key twice, both rounds played out:

```
stake held once, rounds played: 2
  available  100,000 -> 135,000
  escrow     -10,000   *** should be 0 after every round settles ***
  house pnl  -25,000
  ledger still sums to zero: true (0)
```

The player is up 35,000 having staked 10,000 once. Note the last line: **the ledger's zero-sum invariant still holds**, because every individual posting is balanced. The invariant is doing its job and cannot see this — the tell is the escrow account left holding a debt nobody funded. Any monitoring built only on "does the ledger balance" would miss it.

This is not an exotic input. A retry is the *normal* behaviour of a client that times out, and the idempotency key exists precisely because retries are expected.

*Fix:* key the round map on the idempotency key as well as the round id, and have `startRound` return the existing `RoundState` when the key is already known. That makes the operation idempotent as a whole rather than only in its wallet leg. Worth auditing every other multi-step operation for the same shape: an idempotent inner call does not make the outer one idempotent.

**PL-2 · Medium · The notional bankroll silently caps what a high-stakes player may do.**

The engine is constructed with `NOTIONAL_BANKROLL = Integer.MAX_VALUE / 4`, documented as being "purely so its own affordability checks pass". At the documented `MAX_STAKE` they do not:

```
MAX_STAKE             100,000,000
NOTIONAL_BANKROLL     536,870,911
after base bet        436,870,911
hands affordable      5   (a 4-way split with a double on each needs 8)
```

So a player betting near the cap is refused a split or double that their *actual* wallet could fund, and the refusal comes from a fictitious bankroll rather than their real balance. The wallet is meant to be the authority; here the notional figure quietly overrides it. Worse, the failure is silent — `canSplit()` simply returns false and the client sees an unavailable action with no explanation.

*Fix:* size the notional bankroll from the stake (`stake × 8` covers the worst case of four hands doubled), or drop the notional model and let the wallet hold be the only affordability check.

**PL-3 · Medium · Abandoned rounds strand escrow and grow without bound.**

`rounds` is a plain `HashMap` that is never pruned. A settled round stays in it forever, and — more importantly — a round the player never finishes keeps their stake in escrow indefinitely. There is no expiry, no abandonment settlement, and no operator path to release the funds.

For a real-money service this is both an operational problem (player funds stuck after a dropped connection) and an unbounded memory leak in a long-running process.

*Fix:* a round timeout that force-stands or voids and settles, plus eviction of settled rounds once persisted.

### Compliance

**PL-4 · Medium · Self-exclusion and location checks also block withdrawals.**

`DefaultComplianceGate.evaluate` runs one check sequence for every `Action.Type`. A `WITHDRAWAL` is therefore denied for `SELF_EXCLUDED`, and for `STATE_NOT_LICENSED` if the player has since moved.

The mechanism is worth flagging regardless of the legal answer: it means a player who self-excludes, or who relocates, cannot retrieve money they already hold. Responsible-gambling programmes generally treat "stop letting them wager" and "stop letting them have their money" as very different things, and locking the second is the kind of thing that draws regulatory attention rather than deflecting it.

*Fix:* branch the check sequence by action type. Whatever the final policy, it should be an explicit decision per action rather than a side effect of one shared list. **Confirm the actual rules with gaming counsel** — I am describing the code's behaviour, not the requirement.

**PL-5 · Medium · Crypto permission is deny-listed, against the module's own rule.**

```java
public boolean cryptoAllowed(String state) {
    return state != null && !cryptoProhibitedStates.contains(state);
}
```

Any state not explicitly listed as prohibited is permitted. `ComplianceGate`'s own contract says "Unknown or uncertain inputs MUST deny", and this is the opposite polarity: a state nobody has assessed yet defaults to allowing crypto wagering.

In practice the blast radius is limited, because `isLicensed(state)` runs first and only eight states pass. But the safety comes from an unrelated check happening to run earlier, not from this method being right — and reordering or reusing it elsewhere would remove that protection silently.

*Fix:* invert to an allow-list (`cryptoPermittedStates`), matching the licensing check beside it.

**PL-6 · Medium · The provably-fair commitment is computed and discarded.**

`startRound` calls `rng.commitServerSeed(roundId)` and throws the return value away. `reveal()` is never called anywhere, and `RoundState` carries neither the commitment hash nor the seed.

The whole point of the commit–reveal scheme is that the player receives the commitment *before* the round and the seed *after*, so they can recompute the draws. As wired, nobody outside the server ever sees either, so it provides exactly no verifiability — while the class documentation describes it as a feature.

Separately, `clientSeed = idempotencyKey` means the "client" seed is a value the server chose. The comment acknowledges this is a placeholder; noting it because a provably-fair scheme where the server picks both seeds is worse than no scheme, since it looks like a guarantee.

*Fix:* return the commitment in `RoundState`, accept a real client seed on `startRound`, and expose the reveal at settlement.

### Correctness

**PL-7 · Low · Insurance premium recomputed instead of asked for.**

`holdExtra(r, e.hands().get(0).bet() / 2)` computes the premium inline. `core` now exposes `rules.insurancePremium(bet)` for exactly this, and the two agree only by coincidence — they are the same expression today. If the rule ever changes (it already moved once, when the rounding was fixed), the platform would escrow one amount while the engine charged another.

This is the same defect shape as the desktop bug where `canInsure()` and `takeInsurance()` each computed `bet/2` independently.

*Fix:* call `rules.insurancePremium(...)`.

**PL-8 · Low · `RoundRng`'s guarantee rests on an unenforced assumption.**

`RoundRng extends Random` and overrides only `nextInt(int)`. The javadoc explains that `Collections.shuffle` uses only that method — and I confirmed `Shoe` shuffles via `Collections.shuffle` and calls nothing else on the RNG, so the claim holds today.

But it is an assumption about two implementations, neither of which is obliged to keep it. Adding `rng.nextDouble()` to `Shoe` — for a randomised cut-card position, say, which is a realistic thing to want — would silently draw from the inherited `java.util.Random` PRNG instead of the certified source. No error, no audit trail, and the provably-fair reveal would no longer reproduce the deal.

*Fix:* override the other `Random` methods to throw. A draw that escapes the certified source should fail loudly rather than quietly work.

**PL-9 · Low · `post()` accepts a null idempotency key and silently forgoes replay protection.**

`if (key != null) txByIdempotencyKey.put(key, tx)` — a posting with no key is accepted and applied, but never recorded, so replaying it applies it again. For a money ledger, an unkeyed posting is more likely a caller mistake than an intentional opt-out.

*Fix:* require a key, or record unkeyed postings as such so the gap is visible.

**PL-10 · Low · Idempotent replay is not checked against the original request.**

`post` returns early whenever the key is known, without verifying the incoming legs match what was originally stored. A caller that accidentally reuses a key for a *different* movement gets a silent no-op: no money moves, no error, and the caller believes it succeeded.

*Fix:* store a fingerprint of the legs alongside the transaction id and reject a replay that disagrees — the approach payment processors use.

### Informational

**PL-11 · Info · Balance lookups are O(ledger).** `balanceOf` scans every entry ever posted, and `hold` calls it on every wager. Correct and appropriate for the in-memory reference, but the production mapping needs a materialised balance or a snapshot, and the interface should probably say so.

**PL-12 · Info · Ledger sums use unchecked `long` arithmetic.** `sum += e.amountMinor()`. Cents will never overflow, but `Asset` includes wei-scaled tokens where the headroom is far smaller. `Math.addExact` would turn a silent wrap into an exception.

**PL-13 · Info · Audit-sink failure has undefined behaviour.** `authorize` records then returns; if the sink throws, the exception propagates and no `Decision` is produced. Whether that ends up fail-closed depends entirely on the caller. For a system where "every decision is audited" is a regulatory claim, an unavailable audit log should produce an explicit denial. Relatedly, `audit.record(action, decision)` is called with a null `action` when the action was null, which would NPE most implementations at the exact moment something odd is happening.

## What's solid

- **The double-entry model is right.** Balances are derived by summing an append-only ledger and never stored, postings must net to zero per asset, and the invariant is enforced on every write rather than checked periodically. PL-1 slips past it, but that is a limitation of what a balance invariant *can* see, not a flaw in the invariant.
- **The gate has no bypass.** Every path through `evaluate` returns a `Decision`, `authorize` audits before returning, and the checks run in a fixed order with first-failure-wins. Null action, null player, null and blank located state all deny.
- **Order of operations is correct in the round service.** Compliance is checked, then money is held, then the engine acts — for the base wager and for each in-round double, split, and insurance. A hold that exceeds available funds throws before the engine is touched, so the game state cannot get ahead of the money.
- **The engine is reused, not reimplemented.** The same `:core` rules the desktop runs, which means the settlement logic has one implementation and the 249-test suite covers it.
- **`hold` is genuinely idempotent and correctly ordered** — the replay check precedes the funds check, so a retry after a successful hold returns the original transaction rather than failing on balance.
- **The documentation is honest.** It states what is stub, that `SecureRandom` is a floor rather than a deliverable, that the licensed-state list is a living artifact needing counsel, and that operating this requires MSB registration. That is rarer than it should be and made this review much faster.

## Recommended order

1. **PL-1** — the only finding that moves real money incorrectly, and reachable by a plain retry.
2. **PL-3, PL-2** — stranded player funds, then the silent stake cap.
3. **PL-4, PL-5** — compliance behaviour that should be a deliberate decision rather than a side effect. Both want counsel's input, not just a code change.
4. **PL-6** — either deliver the provably-fair flow or stop describing it as one.
5. **PL-7 through PL-10** — small, and each removes a way for two pieces of money code to disagree.
6. **PL-11 through PL-13** — revisit when the production storage mapping is written.

*This review made no code changes.*
