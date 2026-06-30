# ADR-0001: Real-money, crypto-enabled online platform for BlackJack Pro

**Status:** Proposed
**Date:** 2026-06-29
**Deciders:** Product owner, gaming-compliance counsel (external), engineering lead, finance/treasury
**Supersedes:** n/a — first platform ADR. Builds on the security hardening in `AUDIT.md`.

> ⚠️ **This is an engineering blueprint, not legal advice.** Real-money online gambling and crypto money transmission are licensed activities. Nothing here authorizes operation. Engage gaming-law counsel and obtain the required licenses/registrations *before* any real-money launch. Several sections deliberately make legal prerequisites hard gates rather than features.

## Context

BlackJack Pro today is an offline, single-player Java desktop game: a clean `:core` rules engine consumed by Swing and libGDX front-ends, with local file persistence (recently hardened — see `AUDIT.md`). There is **no server, no accounts, no money**.

The request is to add an online, **real-money** platform that accepts both fiat and **cryptocurrency** (BTC, Lightning, ETH/ERC-20, USDT/USDC, Solana), with **custodial** wallets, targeting the **US** market.

This is the most heavily regulated combination in the space, and the regulatory facts drive every downstream decision:

- **Only 8 US states** permit real-money online casino (iGaming) as of mid-2026: New Jersey, Pennsylvania, Michigan, West Virginia, Connecticut, Delaware, Rhode Island, and Maine (Maine not yet operational). Offering real-money casino play to residents of any other state is unlicensed gambling.
- **Crypto acceptance makes the operator a Money Services Business (MSB)** under FinCEN/BSA: federal registration, a written AML program, a designated Compliance Officer, a Customer Identification Program (CIP/KYC), and **SAR/CTR** filing are mandatory. FinCEN proposed a further AML overhaul in April 2026.
- **Some states prohibit crypto gambling outright** — e.g., California's ban on crypto gambling transactions took effect January 2026 — so crypto must be independently gateable per state.
- **Stablecoin on-ramps** (USD→USDC/USDT) can trigger GENIUS-Act stablecoin obligations on top of the above.
- Real-money gaming RNG must typically be **independently certified** (e.g., GLI-19) and/or **provably fair**; the `SecureRandom` upgrade from the audit is necessary but **not sufficient** for certification.

The forces at play: a hard legal/licensing perimeter, a money-handling system that must be auditable to the cent, a server-authoritative game engine (the client can never be trusted with outcomes or balances), and player-protection obligations (responsible gambling, self-exclusion). The existing `:core` engine is an asset — it can become the server-side source of truth — but everything else is greenfield.

## Decision

Build a **compliance-first, server-authoritative platform** in which **no real-money wager, deposit, or withdrawal can occur unless a mandatory, fail-closed compliance gate passes** (identity verified, player located in a licensed state, crypto permitted in that state, within responsible-gambling limits, not self-excluded). The `:core` rules engine is promoted to the **server's** authoritative game logic; clients become thin renderers.

Concretely:

1. **License the regulated path, gate everything else.** Target the 8 iGaming-legal states; geofence everyone else out. Treat crypto as a *separately licensed* MSB capability layered on top, disabled per-state where prohibited.
2. **Custodial wallets via a regulated custody/PSP partner**, fronted by a **double-entry ledger** that is the single source of truth for balances. The game never touches chain keys.
3. **Reuse `:core` server-side** for deterministic, auditable round resolution; pair it with a **certified/provably-fair RNG** service.
4. **Build the compliance, AML, and responsible-gambling controls before the game features** — they are launch-blocking, not backlog.

Explicitly **out of scope and rejected**: serving players in non-licensed states, operating without licensing/MSB registration, or any design that weakens KYC/AML/geofencing. Those constitute illegal unlicensed gambling and money transmission and will not be built.

## Options Considered

### Option A: Licensed fiat iGaming first; crypto deferred

Launch as a licensed fiat operator (or under a licensed operator's skin) in permitted states; add crypto later once MSB registration and per-state legality are in hand.

| Dimension | Assessment |
|-----------|------------|
| Complexity | Medium |
| Cost | High (licensing) but lowest *incremental* |
| Scalability | Good — standard iGaming stack |
| Regulatory risk | Lowest |
| Team familiarity | Medium |

**Pros:** Fastest legal path to revenue; crypto AML risk deferred; matches how every regulated US operator actually works today.
**Cons:** Doesn't deliver the crypto requirement at launch; crypto added as a phase-2 capability.

### Option B: Licensed iGaming + crypto as a registered MSB, gated per state (recommended)

Everything in A, plus a crypto cashier operated as a registered MSB with full AML (CIP, SAR/CTR, sanctions/OFAC screening, chain-analytics), custodial wallets via a regulated custodian, and per-state crypto feature-gating (crypto off where banned).

| Dimension | Assessment |
|-----------|------------|
| Complexity | High |
| Cost | High (licensing + MSB + custody + chain-analytics vendors) |
| Scalability | Good with the right custody/PSP partners |
| Regulatory risk | Manageable *only* with counsel + licensing + AML program |
| Team familiarity | Low (crypto AML, custody) |

**Pros:** Delivers the full real-money + crypto requirement on a defensible legal footing; clean separation of game vs. money vs. compliance.
**Cons:** Highest build + compliance burden; depends on third-party custody, KYC, chain-analytics, and geolocation vendors; crypto economics (volatility, gas/network fees, withdrawal latency) add product complexity.

### Option C: Offshore crypto casino accepting US players — REJECTED

Operate under a permissive offshore license (e.g., Curaçao) and accept US crypto players without US state licensing.

| Dimension | Assessment |
|-----------|------------|
| Regulatory risk | **Disqualifying** |

**Cons:** This is unlicensed gambling and unregistered money transmission as to US players — exposure under UIGEA, the Wire Act, state criminal law, and BSA/FinCEN. **Not designed for here.** Listed only to be explicitly ruled out.

## Trade-off analysis

The core tension is that the requested combination — *US + real-money + crypto* — is precisely where the legal perimeter is tightest. The honest trade-off is **time/cost-to-launch vs. delivering crypto on day one**:

- Option A trades the crypto feature for the shortest compliant path.
- Option B keeps crypto but accepts a materially larger compliance surface (MSB program, custody partner, per-state gating, AML staffing). It only works *with* licensing and counsel; the architecture cannot substitute for them.
- Option C "solves" the cost by ignoring the law; it trades a one-time build saving for criminal/civil liability. Rejected.

Architecturally, A and B are the same system — B simply enables a crypto cashier module behind the same compliance gate. So we **build for B, but the gate lets us ship A first** (crypto feature-flagged off) and turn crypto on per-state as licensing lands. That keeps the day-one legal risk low without re-architecting later.

## Architecture skeleton

Server-authoritative, with three hard-separated planes: **Compliance**, **Money**, and **Game**. The client renders and sends *intents*; it never computes outcomes or balances.

```
                         ┌─────────────────────────────┐
        client  ───────▶ │   API / session gateway     │  TLS, authN, rate-limit, device + geo signals
       (thin UI)         └──────────────┬──────────────┘
                                         │ every real-money intent
                                         ▼
                         ┌─────────────────────────────┐
                         │   ComplianceGate (FAIL-CLOSED)│  KYC ✓ · state allow-listed ✓ · crypto allowed ✓
                         │   identity · geo · RG · excl. │  · within limits ✓ · not self-excluded ✓
                         └───────┬───────────────┬───────┘
                          allow  │               │ deny → audited refusal
                                 ▼               ▼
        ┌────────────────────────────┐   ┌────────────────────────────┐
        │  Game plane                │   │  Money plane               │
        │  GameRoundService          │   │  Wallet + double-entry      │
        │  ├─ :core Engine (reused)  │   │  Ledger (source of truth)   │
        │  └─ Rng (certified/        │   │  CryptoCashier ─▶ custodian │
        │       provably-fair)       │   │  FiatCashier   ─▶ PSP       │
        └────────────┬───────────────┘   └──────────────┬─────────────┘
                     └──────────────┬───────────────────┘
                                    ▼
                  Settlement → Ledger postings → immutable audit log
                                    │
        AML/RG sidecar: sanctions/OFAC + chain-analytics screening, SAR/CTR, velocity & loss limits
```

### Bounded contexts (each a service or module)

- **Identity & KYC** — registration, CIP/KYC via a vendor (e.g., a licensed IDV provider), document/PEP/sanctions checks, age verification. Produces the verified `PlayerComplianceState`.
- **Geolocation & licensing** — per-session state determination (geolocation vendor + device signals), mapped to an allow-list of licensed states and per-state feature flags (e.g., `crypto_enabled=false` in CA). Fail-closed when location is uncertain.
- **ComplianceGate** — the single chokepoint every real-money action passes through. Pure decision function over the player's compliance state; returns allow or an audited `DenialReason`. **No bypass path exists in the design.**
- **Wallet & ledger** — append-only **double-entry** ledger; balances are derived, never mutated in place. Holds/escrow for in-flight wagers. The authoritative record for reconciliation and audit.
- **Crypto cashier** — deposits/withdrawals through a **regulated custodian** (keys never in-app). On every movement: sanctions/OFAC + chain-analytics screening, travel-rule data where applicable, CTR/SAR triggers. Per-asset confirmation policies (BTC confirmations, Lightning, ERC-20/stablecoin, Solana).
- **Fiat cashier** — ACH/card/PayNearMe-style rails via a PSP for the fiat side.
- **Game round service** — server-authoritative rounds using the reused `:core` `Engine`, drawing from the certified RNG; emits settlement instructions to the ledger. Deterministic and fully logged for replay/audit.
- **RNG service** — certified (GLI-19-style) and/or **provably-fair** (server-seed commitment + client seed + nonce, revealed for verification). `SecureRandom` is the floor, not the deliverable.
- **Responsible gambling** — deposit/loss/session limits, cool-off, **self-exclusion** (and integration with state/national self-exclusion lists), reality checks. Enforced inside the gate.
- **Audit & reporting** — immutable, tamper-evident event log; regulatory reporting (per-state, FinCEN SAR/CTR); reconciliation between ledger and custodian/PSP.

### Reuse of the existing codebase

`:core` (`Engine`, `Shoe`, `Hand`, `BasicStrategy`, rules) becomes the **server's** game logic — its determinism and existing test suite are exactly what a server-authoritative design needs. The shoe RNG is swapped from `SecureRandom` to the certified/provably-fair `Rng` service. Swing/libGDX clients are repurposed as thin renderers that send intents and display server-returned state.

### Technology choices (proposed, non-binding)

- **Language/runtime:** JVM server (Java 21 / Spring Boot or Quarkus) to reuse `:core` directly.
- **Datastores:** Postgres for the ledger and compliance state (strong consistency, auditability); an append-only event store/WAL for the audit log; Redis for sessions/limits counters.
- **Money integrity:** integer minor units only (never floats); idempotency keys on every money operation; double-entry invariant enforced in code and DB constraints.
- **Third-party (all diligence-gated):** regulated crypto custodian, KYC/IDV vendor, geolocation vendor, chain-analytics/sanctions vendor, fiat PSP, RNG certification lab.

## Consequences

**Becomes easier**
- Shipping fiat-only in licensed states first (Option A) is just the gate with crypto flagged off — no re-architecture to add crypto later.
- Auditing and reconciliation: a double-entry ledger + immutable log make regulator/financial audits tractable.
- Reusing `:core` server-side gives a tested, deterministic game core for free.

**Becomes harder**
- Operational and compliance burden: MSB registration, AML staffing (Compliance Officer, SAR/CTR), per-state licensing, certification, and multiple vendor integrations.
- Crypto product complexity: volatility, network fees, confirmation latency, withdrawal review, travel rule, OFAC screening.
- The client can no longer be trusted with anything of value — all of today's local logic moves server-side.

**Will need to revisit**
- The licensed-states list and per-state crypto legality change frequently — the geofencing/feature-flag config is a living artifact.
- FinCEN's 2026 AML overhaul and GENIUS-Act stablecoin rules may shift cashier obligations.
- RNG certification scope as game types expand.

## Action items

1. [ ] **Engage gaming-law counsel** and confirm the target-state list + the crypto/MSB path. *(Blocks everything below.)*
2. [ ] Begin **state licensing** and **FinCEN MSB registration**; stand up the written AML program and name a Compliance Officer.
3. [ ] Select diligence-vetted vendors: custodian, KYC/IDV, geolocation, chain-analytics/sanctions, fiat PSP, RNG cert lab.
4. [ ] Stand up the **platform skeleton module** (see `/platform` stubs) and implement `ComplianceGate` as fail-closed with full audit logging.
5. [ ] Promote `:core` to a server module behind `GameRoundService`; integrate the certified/provably-fair `Rng`.
6. [ ] Implement the double-entry `Wallet`/ledger with idempotency and reconciliation; integrate fiat cashier first.
7. [ ] Implement responsible-gambling limits + self-exclusion inside the gate.
8. [ ] Add the crypto cashier behind the per-state feature flag; integrate AML screening, SAR/CTR, travel rule.
9. [ ] Independent security review + RNG certification before any real-money launch.

## Non-negotiable invariants

- The compliance gate is **fail-closed**: unknown/uncertain ⇒ deny.
- **No real-money action** (wager, deposit, withdrawal) bypasses the gate.
- Players outside licensed states, unverified players, and self-excluded players are **always** denied — there is no override path in the design.
- Balances change **only** through balanced double-entry ledger postings.
- Game outcomes are computed **server-side**; client input is treated as untrusted.
