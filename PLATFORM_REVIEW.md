# Platform Module — Code Review

**Scope:** the `platform/` module only — compliance gate, double-entry wallet, RNG seam, crypto cashier interface, and the server-authoritative round service. 664 LOC of main source across 15 files, plus 255 LOC of tests.
**Reviewed at:** `claude/gradle-foundation` (commit `b31c17e`).
**Method:** full read of every source, cross-checked against `core` (`Engine`, `Shoe`, `BlackjackRules`). Every Medium-or-worse finding was reproduced with a JDK 21 probe driving the real classes. Findings that did not reproduce were dropped.

## Verdict

The architecture is right, and notably better than most first attempts at this: a single fail-closed chokepoint, an append-only double-entry ledger with derived balances, idempotency keys throughout, and the game engine reused rather than reimplemented. The documentation is unusually honest about what is stub and what is real.

But **`DefaultGameRoundService` has a money-losing bug reachable by an ordinary network retry** (PL-1), and it is invisible to the ledger's own zero-sum invariant. The module is explicitly not operable without licensing and counsel, so nothing here is live — which is exactly why it is cheap to fix now.

> **Status, 2026-07-20.** All ten code findings (PL-1, PL-2, PL-3, PL-7 … PL-13) are
> fixed and covered by tests; the platform suite is 41 tests, and 223 pass across
> `core` + `platform`. PL-4, PL-5 and PL-6 stay open on purpose — they are decisions
> for counsel and product rather than code defects.

One thing worth stating plainly: this review covers **code correctness only**. Whether the licensed-state list, the withdrawal rules, or the crypto handling actually satisfy any regulator is a question for gaming counsel, not for me. Where a finding touches that, I have described the mechanism and left the legal conclusion open.

## Findings

| # | Severity | Summary | Status |
|---|----------|---------|--------|
| PL-1 | **High** | A retried `startRound` plays two rounds on one stake | ✅ Fixed |
| PL-2 | Medium | The notional bankroll silently caps what a high-stakes player may do | ✅ Fixed |
| PL-3 | Medium | Abandoned rounds strand escrow and grow without bound | ✅ Fixed |
| PL-4 | Medium | Self-exclusion and location checks also block withdrawals | ⏸ Counsel |
| PL-5 | Medium | Crypto permission is deny-listed, against the module's own fail-closed rule | ⏸ Counsel |
| PL-6 | Medium | The provably-fair commitment is computed and discarded | ⏸ Product |
| PL-7 | Low | Insurance premium recomputed instead of asked for | ✅ Fixed (latent) |
| PL-8 | Low | `RoundRng`'s guarantee rests on an unenforced assumption | ✅ Fixed |
| PL-9 | Low | `post()` accepts a null idempotency key and silently forgoes replay protection | ✅ Fixed |
| PL-10 | Low | Idempotent replay is not checked against the original request | ✅ Fixed |
| PL-11 | Info | Balance lookups are O(ledger) | ✅ Fixed |
| PL-12 | Info | Ledger sums use unchecked `long` arithmetic | ✅ Fixed |
| PL-13 | Info | Audit-sink failure has undefined behaviour | ✅ Fixed |

**Every code defect in this review is now fixed. The three left open are policy
questions, not code** — what withdrawals a self-excluded player may make, whether
crypto is permitted, and whether to ship provably-fair verification are decisions for
counsel and product, and this review deliberately does not make them.

### How the fixes were verified

A soak — `platform/src/test/java/.../sim/MoneySimulator.java` — drives the wallet,
compliance gate and round service under the conditions a real client produces,
including retries and mid-hand disconnects. **10,000 rounds across 50 seeds, with
2,451 retries**, all invariants holding.

Its central design decision is worth recording, because it is the reason the
original bug was invisible. The ledger is **zero-sum by construction**: `post()`
rejects any set of legs that does not net to zero. So "does the ledger balance"
stays true even when money has gone badly wrong, and monitoring built on it would
have reported green forever while PL-1 paid a player 35,000 on a 10,000 stake. What
PL-1 actually leaves behind is *an escrow account holding a debt nobody funded*, so
the load-bearing check is **per-account, not global**: escrow is a transient, and
once nothing is in flight every player's escrow must be exactly zero.

Against the unfixed module the simulator failed **50 of 50 seeds**, on seed 0 within
18 rounds. Each fix additionally has a named regression test that was confirmed to
fail against `HEAD` before the fix — except PL-7, which is honestly recorded below as
latent rather than live.

---

### Money

**PL-1 · High · A retried `startRound` plays two rounds on one stake.**

`startRound` passes its `idempotencyKey` to `wallet.hold`, which is correctly idempotent and escrows nothing on a replay. But `startRound` itself is not: it goes on to mint a new `roundId`, build a new `Engine`, and deal a second hand anyway. Both rounds then settle against an escrow that only ever received one stake.

> **✅ Fixed.** The service now keeps a `key -> roundId` map and returns the existing
> `RoundState` on replay, so idempotency covers the whole operation rather than only
> its money leg. Pinned by `aRetriedStartReturnsTheSameRoundRatherThanDealingASecondOne`,
> with `twoDifferentKeysStillStartTwoRounds` guarding against the opposite error of
> collapsing genuinely distinct requests.

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

> **✅ Fixed.** The engine is now built with `min(stake × 8, MAX_VALUE/2)`.
> The regression test is a **scale-invariance** check rather than a staged deal:
> `whatThePlayerMayDoDoesNotDependOnHowBigTheirStakeIs` plays 60 rounds from the same
> seed at 1,000 and at 100,000,000 and asserts the sequence of permitted actions is
> byte-identical. Against `HEAD` the two traces diverge, which is exactly the symptom —
> identical cards, different legal moves, because the notional figure and not the
> wallet refused one.

**PL-3 · Medium · Abandoned rounds strand escrow and grow without bound.**

`rounds` is a plain `HashMap` that is never pruned. A settled round stays in it forever, and — more importantly — a round the player never finishes keeps their stake in escrow indefinitely. There is no expiry, no abandonment settlement, and no operator path to release the funds.

For a real-money service this is both an operational problem (player funds stuck after a dropped connection) and an unbounded memory leak in a long-running process.

*Fix:* a round timeout that force-stands or voids and settles, plus eviction of settled rounds once persisted.

> **✅ Fixed.** `expireRounds(ttlMs)` stands out any live hand so the round settles
> through the normal path — the escrow is released by a balanced posting rather than
> written off — then evicts it, and `openRounds()` exposes the depth for monitoring.
> It is deliberately an operator-driven sweep rather than a background timer, so it
> can never run mid-round.
>
> This one the soak measured rather than argued: of 10,000 rounds it started, **555
> were abandoned mid-hand**, each still holding a stake. The sweep recovers all of
> them and leaves **0 open**, with every player's escrow back to exactly zero.
> `aLiveRoundIsNotSweptOutFromUnderThePlayer` guards the other direction.

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

> **✅ Fixed — and worth being honest about what that's worth.** `BlackjackRules`
> is `final` and `insurancePremium` is still literally `bet / 2`, so there is *no
> behavioural difference to observe*: the regression test passes against `HEAD`
> too. This finding was **latent, not live**. The value of the change is that the
> second independent copy of a money calculation is gone;
> `theEscrowedPremiumIsWhateverTheRulesSayItIs` pins the coupling so a future
> change to the rule cannot leave the platform behind. Recorded this way rather
> than counted as a caught bug.

**PL-8 · Low · `RoundRng`'s guarantee rests on an unenforced assumption.**

`RoundRng extends Random` and overrides only `nextInt(int)`. The javadoc explains that `Collections.shuffle` uses only that method — and I confirmed `Shoe` shuffles via `Collections.shuffle` and calls nothing else on the RNG, so the claim holds today.

But it is an assumption about two implementations, neither of which is obliged to keep it. Adding `rng.nextDouble()` to `Shoe` — for a randomised cut-card position, say, which is a realistic thing to want — would silently draw from the inherited `java.util.Random` PRNG instead of the certified source. No error, no audit trail, and the provably-fair reveal would no longer reproduce the deal.

*Fix:* override the other `Random` methods to throw. A draw that escapes the certified source should fail loudly rather than quietly work.

> **✅ Fixed, and better than the proposed fix.** Rather than making the other
> methods throw, `RoundRng` now overrides `next(int bits)` — the primitive that
> `nextLong`, `nextDouble`, `nextBoolean`, `nextFloat`, the `ints()/doubles()`
> streams and `nextInt(origin, bound)` are all built from — so those methods keep
> *working* and are simply routed through the committed source, each draw nonced so
> the reveal still replays. Throwing would have turned a future `Shoe` change into a
> crash; this turns it into correct behaviour. `setSeed` does throw, since reseeding
> would hand control of the shuffle to the caller.
>
> Measured against `HEAD`: six calls that a caller would reasonably expect to be fair
> consumed the certified source **once**. The other five silently drew from a
> `java.util.Random` seeded from `System.nanoTime()`.

**PL-9 · Low · `post()` accepts a null idempotency key and silently forgoes replay protection.**

`if (key != null) txByIdempotencyKey.put(key, tx)` — a posting with no key is accepted and applied, but never recorded, so replaying it applies it again. For a money ledger, an unkeyed posting is more likely a caller mistake than an intentional opt-out.

*Fix:* require a key, or record unkeyed postings as such so the gap is visible.

> **✅ Fixed.** `post()` now rejects a null or blank key outright. Nothing in the
> codebase needed an unkeyed posting, so offering a mode that is never safe was pure
> downside.

**PL-10 · Low · Idempotent replay is not checked against the original request.**

`post` returns early whenever the key is known, without verifying the incoming legs match what was originally stored. A caller that accidentally reuses a key for a *different* movement gets a silent no-op: no money moves, no error, and the caller believes it succeeded.

*Fix:* store a fingerprint of the legs alongside the transaction id and reject a replay that disagrees — the approach payment processors use.

> **✅ Fixed** as proposed: the wallet stores a fingerprint (account, asset and amount
> of every leg, in a stable order) and a replay that disagrees is rejected rather than
> silently ignored. A genuine replay is still a no-op —
> `aGenuineReplayIsStillANoOp` guards that, since breaking it would defeat the point
> of the keys.

### Informational

**PL-11 · Info · Balance lookups are O(ledger).** `balanceOf` scans every entry ever posted, and `hold` calls it on every wager. Correct and appropriate for the in-memory reference, but the production mapping needs a materialised balance or a snapshot, and the interface should probably say so.

> **✅ Fixed.** Balances are now maintained incrementally in the same synchronized block
> that appends the legs which moved them, so a read is a map lookup. The ledger stays
> authoritative and the map is explicitly an index over it, not a second copy — nothing
> writes to it except `post`, and a new `reconcile()` re-derives every balance from the
> ledger and throws on the first divergence. `Wallet.availableMinor` now states the
> contract that makes this legitimate: implementations may serve a materialised figure
> **provided it is updated in the same atomic unit as the posting**. A balance updated
> separately from the ledger is a second source of truth, not a faster read of the first.
>
> Measured on the pre-fix code, a read cost 2,835 ns at 2,000 entries, 38,121 ns at
> 20,000, and 291,946 ns at 100,000 — linear, as advertised. After the fix it is flat at
> ~50 ns. Covered by `materialisedBalancesAgreeWithTheLedgerSum`, which compares against
> an independent naive re-derivation.
>
> Recorded honestly: **this test passes against `HEAD` too**, because PL-11 was never a
> wrong-answer bug — the old code returned the same numbers, slowly. The test is a guard
> on the new index, not a caught defect. The linear-to-flat measurement above is the
> actual evidence the fix did anything.

**PL-12 · Info · Ledger sums use unchecked `long` arithmetic.** `sum += e.amountMinor()`. Cents will never overflow, but `Asset` includes wei-scaled tokens where the headroom is far smaller. `Math.addExact` would turn a silent wrap into an exception.

> **✅ Fixed.** Every accumulation — the zero-sum check, the balance update, and
> `reconcile()`'s re-derivation — goes through `Math.addExact` with an account/asset in
> the message. Against the old code the probe is unambiguous: one posting past
> `Long.MAX_VALUE` returned normally and left the account holding
> **−9,223,372,036,854,775,808**. Not a rounding artifact — a maximally negative balance,
> indistinguishable from a legitimate debt, and no exception anywhere.
>
> The fix also had to be **all-or-nothing**, which the first draft was not. The overflow
> fires on the second leg, and the old `post` had already appended the first: the probe
> shows the ledger going from 2 entries to 4 on a posting that failed. A half-applied
> transaction breaks zero-sum permanently, which is worse than the overflow that caused
> it. `post` now computes every resulting balance before mutating anything, so a rejected
> posting appends nothing and — deliberately — does not burn its idempotency key either,
> or the retry that would have succeeded gets silently swallowed as a replay.
>
> Pinned by `aBalanceThatWouldOverflowThrowsRatherThanWrappingNegative` and
> `anOverflowingPostingLeavesNoPartialState`, both confirmed to fail against `HEAD`.

**PL-13 · Info · Audit-sink failure has undefined behaviour.** `authorize` records then returns; if the sink throws, the exception propagates and no `Decision` is produced. Whether that ends up fail-closed depends entirely on the caller. For a system where "every decision is audited" is a regulatory claim, an unavailable audit log should produce an explicit denial. Relatedly, `audit.record(action, decision)` is called with a null `action` when the action was null, which would NPE most implementations at the exact moment something odd is happening.

> **✅ Fixed.** A sink failure now yields `Decision.deny(AUDIT_UNAVAILABLE)` — a new
> reason whose javadoc says plainly that it is not a judgement about the player. The
> gate cannot demonstrate the action was recorded, and a fail-closed gate does not
> authorize what it cannot evidence.
>
> The trade-off is one-directional and stated as a test rather than left as a comment:
> an audit outage **stops play**; it never permits play that nobody can evidence.
> `anUnavailableAuditSinkNeverTurnsADenialIntoAnAllow` walks a licensed wager, a
> withdrawal, and a null action through a dead sink and asserts all three are denied.
>
> One consequence worth naming rather than hiding: when the underlying decision was
> *already* a denial, the specific reason is replaced by `AUDIT_UNAVAILABLE`. That loses
> detail for player messaging, and it is still the right answer — we cannot show the real
> reason was recorded, so it is the only claim the gate can stand behind. Both outcomes
> are denials, so no money moves either way.
>
> The null-`action` case is handled by contract rather than by a guard. `AuditLog` now
> documents that `action` may be null, that this is exactly what a malformed request
> looks like and therefore the case most worth logging, and that implementations must
> not throw on it. A sink that throws anyway is no longer an NPE escaping the gate — the
> probe confirms `HEAD` propagates `NullPointerException` out of `authorize` where the
> fixed code returns a denial. Note the catch is `RuntimeException`, not `Throwable`:
> an `OutOfMemoryError` is not a compliance decision and still propagates.

## What's solid

- **The double-entry model is right.** Balances are derived by summing an append-only ledger and never stored, postings must net to zero per asset, and the invariant is enforced on every write rather than checked periodically. PL-1 slips past it, but that is a limitation of what a balance invariant *can* see, not a flaw in the invariant.
- **The gate has no bypass.** Every path through `evaluate` returns a `Decision`, `authorize` audits before returning, and the checks run in a fixed order with first-failure-wins. Null action, null player, null and blank located state all deny.
- **Order of operations is correct in the round service.** Compliance is checked, then money is held, then the engine acts — for the base wager and for each in-round double, split, and insurance. A hold that exceeds available funds throws before the engine is touched, so the game state cannot get ahead of the money.
- **The engine is reused, not reimplemented.** The same `:core` rules the desktop runs, which means the settlement logic has one implementation and the 249-test suite covers it.
- **`hold` is genuinely idempotent and correctly ordered** — the replay check precedes the funds check, so a retry after a successful hold returns the original transaction rather than failing on balance.
- **The documentation is honest.** It states what is stub, that `SecureRandom` is a floor rather than a deliverable, that the licensed-state list is a living artifact needing counsel, and that operating this requires MSB registration. That is rarer than it should be and made this review much faster.

## Recommended order

1. ~~**PL-1** — the only finding that moves real money incorrectly, and reachable by a plain retry.~~ ✅
2. ~~**PL-3, PL-2** — stranded player funds, then the silent stake cap.~~ ✅
3. **PL-4, PL-5** — compliance behaviour that should be a deliberate decision rather than a side effect. Both want counsel's input, not just a code change. **Still open, deliberately.**
4. **PL-6** — either deliver the provably-fair flow or stop describing it as one. **Still open** — a product decision. PL-8's fix means the plumbing would now actually hold up if you chose to deliver it.
5. ~~**PL-7 through PL-10** — small, and each removes a way for two pieces of money code to disagree.~~ ✅
6. ~~**PL-11 through PL-13** — revisit when the production storage mapping is written.~~ ✅
   Done ahead of that mapping, because two of the three turned out to be load-bearing
   for it: `Wallet` now *specifies* what a materialised balance is allowed to be, and
   `post` is all-or-nothing under arithmetic failure. Both are cheaper to state now than
   to retrofit onto a schema that already assumed otherwise.

## What this exercise actually taught

The review found PL-1 by reading. What it could not tell me was how *reachable* it
was, and reachability is the whole difference between a note and an incident. The
simulator answered that: an ordinary client retry rate of one in four broke the
module on **every seed tried**, within eighteen rounds on the first one.

That is the second time on this project a soak has said something a careful read did
not. The first was `canHit()` — a full module review and 249 passing tests, and it
still threw after 78% of completed rounds, found in the first two hands of someone
actually playing. The pattern in both cases is the same: unit tests call the method
under test, and a real caller calls *everything* after *every* action, in states
nobody thought to construct.

Worth noting what the simulator did **not** find: nothing in the compliance gate,
and nothing in the ledger's core postings. Those were correct as written. The bugs
clustered where two components meet and each assumes the other is handling
something — the round service assuming `hold()`'s idempotency was the whole story,
`RoundRng` assuming `Shoe` would only ever call one method.

The PL-11/12/13 round added a third data point, and it points the other way from the
first two. Those were found by *running* things; these were found by reading, and were
filed as Info — "revisit later" — precisely because nothing was observably wrong. What
running them later established was not that they were bugs, but **how far from harmless
they were**: PL-12 does not degrade at the boundary, it hands back `Long.MIN_VALUE` and
keeps going, and the old `post` had already committed half the transaction by the time
it got there. The severity was right; "revisit later" was the part that was wrong.

So the rule is not "soak everything." It is that the *reachability* of a defect and its
*blast radius* are separate questions, and reading answers neither. PL-1 needed a
simulator to show it was reachable. PL-12 needed a nine-line probe to show that when it
is reached, it is not a rounding error.

*Findings PL-1, PL-2, PL-3 and PL-7 through PL-13 have been fixed and are covered by
tests. PL-4, PL-5 and PL-6 remain open by choice — they are questions for counsel and
product, not defects.*
