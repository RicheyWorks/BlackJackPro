# platform/ — online real-money platform skeleton

Design stubs for the server-authoritative, compliance-first platform described in
[`docs/architecture/ADR-0001-real-money-crypto-platform.md`](../docs/architecture/ADR-0001-real-money-crypto-platform.md).

These are **interface stubs only** — no implementations, no money-moving logic. They anchor
the bounded contexts and make the non-negotiable invariants explicit in code:

- `compliance/ComplianceGate` — the single FAIL-CLOSED gate every real-money action passes through.
- `compliance/PlayerComplianceState` — KYC + geolocation + responsible-gambling snapshot.
- `wallet/Wallet`, `wallet/LedgerEntry` — append-only double-entry ledger; balances are derived, never mutated.
- `game/GameRoundService` — server-authoritative rounds reusing the `:core` engine.
- `rng/Rng` — certified / provably-fair randomness (`SecureRandom` is the floor, not the deliverable).
- `payments/CryptoCashier` — custodial crypto via a regulated custodian, with AML screening.

Not yet wired into Gradle — see the ADR action items.

> **Do not operate without state licensing, FinCEN MSB registration, and gaming-law counsel.**
> Serving players outside licensed states, or weakening KYC/AML/geofencing, is illegal and is
> deliberately not implementable through these interfaces.
