# CrawlerVision 1.12 — Audible and Amazon Music companion

**Release candidate notes — not yet a public GitHub Release asset.**

CrawlerVision is an unofficial Android illustrated companion that follows audiobook media-session metadata and displays scene images in sync with chapter timing when enough playback information is available. Requires separate, legitimate audiobook playback; no audiobook audio is bundled.

## Included in the prebuilt APK

- Two selectable scene collections:
  - *Dungeon Crawler Carl*: chapters 1–47, with **470** illustration resources.
  - *Carl's Doomsday Scenario*: chapters 1–25, with **250** illustration resources.
- Audible and Amazon Music media-session detection.
- Automatic chapter tracking when the media player exposes identifiable chapter information.
- Manual chapter-start alignment and manual image browsing when tracking isn't reliable.
- Fullscreen image viewer with previous/next scene controls.
- Optional microphone-based live transcription via Android's speech recognition service.

## Known limitations

- Scene/image timings are estimated, not guaranteed to match each spoken event.
- Chapter transitions can fail or require manual alignment with some audiobook media sessions.
- Microphone transcription is an opt-in separate feature. It is not suitable for headphones and the Android speech service may transmit audio over the internet.
- This is a sideloadable debug-style APK, not an app-store reviewed build.
- **This GitHub repository's committed source is an earlier v1.2 build kit; it does not reproduce v1.12.**
- Embedded book-related media may be subject to third-party copyright, artwork and trademark restrictions. Obtain appropriate redistribution permissions before making the prebuilt APK publicly downloadable.

## Integrity

APK: `CrawlerVision_1.12_Doomsday_Chapters_1-25.apk`

Size: **238,291,106 bytes**.

SHA-256: `b2e9e9082786f3ca0fdd4da463b134fd561ff68f5ddad783d86b607cbd214931`.

Archive integrity: ZIP entries tested, no errors reported. Actual Android installation and playback haven't been independently tested.

For installation guidance and known privacy limits, see [Features](FEATURES.md). For release-upload instructions, see [Publishing](PUBLISHING.md).
