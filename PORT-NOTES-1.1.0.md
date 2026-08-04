# MagicExpansion Legacy 1.1.0 Port Notes

This source tree is based on the supplied `SF_MagicExpansion` fork and selectively ports the important changes from the supplied new Yomicer upstream source (Build 84 / commit `b56aad4`).

## Compatibility goals

- Primary: Slimefun Legacy
- Also supported: Slimefun United and Slimefun Gugu API-compatible builds
- Server: Paper/Purpur 26.2
- Build JDK: Java 25 or newer
- Output bytecode: Java 21
- GuizhanLib: not required
- Networks and NetworksExpansion: optional soft dependencies only

## Ported upstream changes

- Between Water and Clouds fishing series
- Cyan Bamboo Rod and five specialized lures
- Dedicated fishing listener, weighted catches, special catches, sounds, and fireworks
- Unified quantum-storage compatibility helper using reflection and compatible PDC keys
- Direct quantum-storage item support for vivariums
- Void Touch binding to Networks quantum-storage blocks
- Etheric Vivarium Array quantum-storage support
- Output-slot capacity limiting and numeric overflow protection
- Draw Machine stale hologram cleanup

## Preserved fork behavior

- English-first player-facing text
- Guide category shown as `Magic`
- Existing item IDs retained for saved-world compatibility
- Local `ItemStackHelper` replacement instead of GuizhanLib
