# CrawlerVision

**Illustrated audiobook companion for Android — experimental, unofficial, open-source software project.**

CrawlerVision shows a sequence of scene illustrations while you listen to an audiobook in **Audible or Amazon Music**. It reads Android's media-session playback metadata to follow the current chapter and approximate position, then advances illustrations as the story plays. The audiobook stays in your own player; CrawlerVision is a visual companion, not a source of audiobook audio.

> **Public-repository status (October 26, 2026):** The repository is public and currently includes the **legacy CrawlerVision 1.2 source/build kit**. The newest standalone **CrawlerVision 1.12** APK is **not yet hosted on GitHub**. The current build workflow **does not reproduce version 1.12**. See [How to publish the APK](docs/PUBLISHING.md).

## What it does

- Follows compatible Audible / Amazon Music Android playback sessions when metadata and a reliable chapter clock are available.
- Attempts to switch chapters automatically; also provides **manual chapter alignment** if the audiobook player's timeline cannot be mapped.
- Displays preselected chapter illustrations and advances them based on **estimated** scene timings.
- Provides book/chapter selection, previous/next picture controls and a fullscreen image viewer.
- Offers an **optional microphone-based live transcript** through the Android speech-recognition service. This is not a direct extraction of audiobook captions and may require network-based recognition depending on the phone.

**Important:** The app does not align illustrations to each exact spoken sentence. Scene timing is approximate, and automatic synchronization is not guaranteed for all playback sessions, seeks, or chapter transitions.

[Read the full feature list, privacy notes, and Android setup guide](docs/FEATURES.md).

## Latest standalone APK — CrawlerVision 1.12

The APK has been identified and its archive integrity checked, but it **has not been uploaded as a GitHub Release asset**.

| | |
| --- | --- |
| File | `CrawlerVision_1.12_Doomsday_Chapters_1-25.apk` |
| Size | 238,291,106 bytes (about 227 MiB) |
| SHA-256 | `b2e9e9082786f3ca0fdd4da463b134fd561ff68f5ddad783d86b607cbd214931` |
| *Dungeon Crawler Carl* | Chapters 1–47; 470 illustrations |
| *Carl's Doomsday Scenario* | Chapters 1–25; 250 illustrations |
| Total | 72 chapters; 720 illustrations |
| Source | Matching v1.12 source not yet committed to this repository |

### Downloads and builds

- **[Releases](https://github.com/Syqemike/Crawlervision/releases):** Publicly uploaded APKs, if present. **Check the version**; no v1.12 asset is currently published by this update.
- **[GitHub Actions — Build CrawlerVision APK](https://github.com/Syqemike/Crawlervision/actions/workflows/build-apk.yml):** Builds the older 1.2 source kit; **not** the 1.12 APK.
- **[Publish v1.12 to Releases](docs/PUBLISHING.md):** Maintainer instructions for uploading the real APK and verifying the checksum.
- **[v1.12 release notes](docs/RELEASE_NOTES_v1.12.md)**.

Do not trust filenames, third-party mirrors, or supposed release links unless you can verify the download and checksum.

## Install and use on Android

1. Obtain an APK from a verified release or from a maintainer you trust. For v1.12 verify its SHA-256.
2. Sideload on your Android phone, following the system's security prompts.
3. Start a legitimately obtained audiobook in Audible or Amazon Music.
4. Open CrawlerVision, grant **Notification access** only if you consent to Android media-session/notification monitoring, and select the correct book.
5. If the chapter does not align, select a chapter and use the manual start-alignment controls. Use previous/next scene and fullscreen as desired.
6. Only if you want optional live transcription, grant **Microphone** permission and start it manually. Phone-speaker audio is more suitable than headphones; the Android speech service may process audio online.

See [Feature guide](docs/FEATURES.md) for troubleshooting and limitations.

## Current source build

The **1.2** source kit is stored in base64 chunks under `source_chunks/`, reassembled by [the legacy build workflow](.github/workflows/build-apk.yml). You can inspect the workflow, run the job, and download its workflow artifact after a successful build. This is a legacy debug build and is not guaranteed to reflect the 1.12 app's features.

A future source refresh should replace the archive approach with a reviewed Android project containing ordinary Gradle files, chapter schemas, tests, and **only redistributable sample content**.

## Permissions, privacy and accessibility

- Notification-listener permission gives the app access to Android notification/session information. Grant only if you trust the app.
- Microphone permission is **optional** and used for the live transcript feature. Android speech recognition may transmit audio to the service provider if offline recognition is unavailable.
- Use your own audiobook subscription/player; CrawlerVision should not ask for Audible/Amazon login credentials.
- Fullscreen and manual navigation are intended for use while listening, not as a replacement for accessible audio.

## License and third-party content

Original CrawlerVision code and documentation are available under the [MIT License](LICENSE). That license **does not grant redistribution rights** for novels, audiobook recordings, characters, trademarks, fan art, or third-party illustrations. Existing app builds include illustrations tied to protected books, and the necessary public-redistribution rights have not been established here. **Review or replace those assets before releasing a bundled APK publicly.**

This is a fan/developer experiment; it is not endorsed by Matt Dinniman, Audible, Amazon, or an audiobook publisher.

## Contribute

Help with Android playback synchronization, reliable chapter detection, timing-editor tools, user-imported/local scene packs, accessibility, privacy, and source reproducibility. Keep unlicensed media out of pull requests and public builds.
