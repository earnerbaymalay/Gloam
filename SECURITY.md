# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| Latest (main) | ✅ Yes |

## Reporting a Vulnerability

**Do NOT open a public issue for security vulnerabilities.**

1. **Email:** [security@gloam.app](mailto:security@gloam.app) *(placeholder)*
2. **Include:** Description, reproduction steps, impact assessment, suggested fix

## Response Timeline

- **Acknowledgment:** Within 48 hours
- **Initial assessment:** Within 7 days
- **Fix:** Within 30 days (critical: 7 days)

## What We Consider a Vulnerability

- PIN bypass possibilities
- Database passphrase leakage
- Plaintext journal data written to disk
- Location data transmitted or stored insecurely
- Notification content visible on lock screen

## What Is NOT a Vulnerability

- UI glitches that don't expose data
- Feature requests (please use GitHub Issues)
- Dependency vulnerabilities we've already patched

## Security Design

- **Database:** SQLCipher AES-256 encryption with random 32-character passphrase stored in SharedPreferences
- **PIN:** SHA-256 hashed, never stored in plaintext
- **Backups:** Explicitly excluded from Android backup, cloud backup, and device transfer
- **Location:** Used only for sun calculation — never transmitted, never persisted long-term
- **Export:** JSON export is user-initiated — no automatic data sharing

---

*Your journal is private. Period.*
