# platform/ — online real-money platform skeleton

Design and reference implementations for the server-authoritative, compliance-first
platform described in
[`docs/architecture/ADR-0001-real-money-crypto-platform.md`](../docs/architecture/ADR-0001-real-money-crypto-platform.md).

## What is real code today

| Piece | Implementation | Notes |
|-------|----------------|-------|
| `ComplianceGate` | `DefaultComplianceGate` | Fail-closed; every decision audited |
| `AuditLog` | `FileAuditLog` + test no-op | Append-only file sink for local/dev |
| `Wallet` | `InMemoryWallet` | Double-entry, idempotent holds/posts — **not durable** |
| `GameRoundService` | `DefaultGameRoundService` | Reuses `:core` Engine; hold/escrow/settle |
| `Rng` | `ProvablyFairRng` | Full commit → HMAC draw → reveal + verify helpers |
| `CryptoCashier` | Interface only | Needs regulated custodian |
| HTTP / auth / DB | **None** | Not a deployable server |

## What is intentionally not here

- State licensing, FinCEN MSB registration, KYC/geo vendors
- Postgres (or any durable ledger)
- API gateway, sessions, rate limits, geofencing productization
- Certified lab RNG (GLI-19 etc.) — `ProvablyFairRng` is a cryptographic
  reference, not a certification

> **Do not operate without state licensing, FinCEN MSB registration, and gaming-law counsel.**
> Serving players outside licensed states, or weakening KYC/AML/geofencing, is illegal and is
> deliberately not implementable through these interfaces alone.
