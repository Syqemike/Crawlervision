# Features and user guide (CrawlerVision 1.12)

## What CrawlerVision is

CrawlerVision is an **unofficial Android visual companion** designed to display chapter-specific illustrations while you listen to an audiobook in a separate player. It is **not** an audiobook player and does **not** include a licensed audiobook. Listeners must obtain audio from their chosen provider independently.

CrawlerVision is intended for personal sideloading. The project is currently experimental: scene timing is approximate and media-session information supplied by third-party audiobook players varies.

## What is inside the 1.12 APK

| Scene collection | Coverage | Images |
| --- | --- | ---: |
| *Dungeon Crawler Carl* | Chapters 1–47 | 470 |
| *Carl's Doomsday Scenario* | Chapters 1–25 | 250 |
| **Total** | **72 chapters** | **720** |

The above was verified by inspecting the APK's bundled `assets/*.json` scene packs and Android drawable names. Timing varies by chapter and is generally **estimated**, not a word-accurate transcript alignment.

## Main functions

- **Audiobook media-session monitoring:** Supports Android media sessions from Audible (`com.audible.application`) and Amazon Music (`com.amazon.mp3`) where Android exposes usable playback metadata, durations, and positions.
- **Automatic chapter tracking:** Attempts to recognize numbered chapter transitions based on the player metadata and measured playback state. When a player omits a reliable chapter clock, the app may ask the listener to align the chapter start manually.
- **Scene slideshow:** Advances chapter-specific images according to the scene pack and the measured or manually aligned chapter clock.
- **Two-book selector:** Switch between available built-in scene collections.
- **Manual fallback:** Select a chapter, align the chapter start, or run in manual timing mode if session tracking is unavailable.
- **Previous / next scene:** Browse around or correct a mismatched image.
- **Fullscreen viewing:** Use **Watch fullscreen** and **Exit fullscreen** within the app.
- **Optional live transcript overlay:** Uses the Android speech-recognition service and the device microphone, initiated by the user. It is intended for audio playing through the device speaker; headphone playback typically is not audible to the microphone.

### Limitations

- **Not forced-aligned:** Pack timing is an estimate. Images may not match the spoken sentence or action at every moment, even when chapter synchronization works.
- **Audible/Amazon Music changes:** Auto-tracking depends on the current player/Android media-session behavior. It cannot guarantee automatic chapter transitions or recovery from all seeks.
- **Transcript limitations:** The optional microphone-based transcript may miss words, may fail because of noise or audio processing, and cannot reliably transcribe headphones. Some Android speech-recognition services send audio to remote servers when offline speech recognition is unavailable.
- **No audiobook audio inside the app.** It is only a visual companion.
- **No Play Store approval.** The APK is sideloaded and has not been independently validated on every device.

## Install and run (Android)

1. Obtain the APK from a **published, verified release**. Check filename and SHA-256 against [PUBLISHING](PUBLISHING.md); v1.12 is not currently attached to a GitHub release.
2. On Android, open the downloaded APK and follow the **Install unknown apps** prompt only if you trust the app and its source.
3. Launch your legitimate Audible or Amazon Music audiobook in the separate app and start playback.
4. Open CrawlerVision and grant **Notification access** if you want automatic media-session synchronization. This sensitive Android permission allows access to notification/session information; grant it only if comfortable with that access.
5. Select the book if necessary. Confirm the chapter and image selection; if playback timing is not readable, select the chapter and use **Align current chapter start** or manual sync.
6. Use **Previous image**, **Next image**, or **Watch fullscreen** as desired.
7. If you choose to enable transcription, grant **Microphone** permission. Playback through speaker is more likely to be recognized than headphones. The phone's Android speech service may require internet access.

## Privacy and permissions

- **Notification listener access:** Required to observe compatible media sessions and their metadata. Notification access may expose details beyond the playing audiobook; review the permission before granting.
- **Microphone access:** Requested only for the optional live transcript feature. It is not necessary to display still images.
- **Speech recognition:** Uses the Android speech-recognition service. That service may process microphone audio online depending on device configuration.
- **No credentials needed:** Users authenticate to their audiobook subscription in their own Audible/Amazon Music app. Do not provide login credentials to CrawlerVision.

The repository's older build kit is **not** complete reproducible source for the prebuilt 1.12 APK. The rights to distribute embedded book-specific artwork must be reviewed separately from the open-source code license.
