# Contributing to CrawlerVision

CrawlerVision is a community project for pairing chapter-based scene illustrations with **legitimately acquired** audiobooks. Improvements to the application code are welcome.

## Before opening a pull request

1. Open an issue describing the bug or enhancement, including Android version and the audiobook player when relevant.
2. Keep changes small and explain their effect on timing, chapter transitions, manual fallbacks, and privacy.
3. Include tests or reproducible steps where feasible.
4. Do not commit credentials, signing keys, book manuscripts, commercial audio, copied illustrations, or third-party assets without permission.
5. Do not claim support for a function unless it has been tested with real session metadata.

## Known source caveat

As of October 26, 2026, the repository's GitHub Actions build produces the **legacy v1.2** source kit, **not** the independently built CrawlerVision v1.12 APK. Do not label contributions or build artifacts as v1.12 without matching source and verification.

The ideal next development step is a reviewed Android Gradle source tree with ordinary source files, importable scene-pack JSON, openly redistributable placeholder assets, and automated tests. Avoid embedding copyrighted novel content into the repository.

## Useful contributions

- More reliable Audible / Amazon Music chapter detection and fallback timing
- Playback-seek, pause, chapter-transition, and process-restart tests
- Manual timing corrections and scene editor
- User-imported local image packs and portable scene manifest format
- UI accessibility and fullscreen behavior
- Opt-in, privacy-aware speech recognition

## Submitting

Fork the repository, create a descriptive branch, and open a pull request with the motivation, implementation notes, and verification steps. Use the existing MIT license for your original contributions. That license does **not** extend to third-party copyrighted works, illustrations, audiobooks, or trademarks.

This is an unofficial project and is not affiliated with the author or audiobook distributors.
