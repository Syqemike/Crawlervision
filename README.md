# CrawlerVision

An unofficial, open-source Android visual companion for audiobooks. CrawlerVision uses Android media-session metadata and playback position to advance illustrated scenes alongside audio playing in another app. It does **not** supply audiobook recordings or require users to upload audio.

> **Project status — October 26, 2026:** This public repository contains an older **1.2 Android build kit**, not the newest standalone APK. A separately built **CrawlerVision 1.12** APK exists, but it has **not been uploaded to GitHub Releases**. Do not confuse an existing repository release with version 1.12.

## Download

- **[GitHub Releases](https://github.com/Syqemike/Crawlervision/releases)** — public APKs currently hosted by GitHub. Check the release version before downloading; the newest local v1.12 APK is not there yet.
- **[Build CrawlerVision APK workflow](https://github.com/Syqemike/Crawlervision/actions/workflows/build-apk.yml)** — builds the source currently committed to this repository. Builds from this repository are **not equivalent to v1.12**.
- **[How to publish the latest APK](docs/PUBLISHING.md)** — verified v1.12 package name, checksum, and GitHub Release upload steps.

### Most recent standalone APK (not yet on GitHub)

| Item | Value |
| --- | --- |
| Name | `CrawlerVision_1.12_Doomsday_Chapters_1-25.apk` |
| Build label | 1.12 |
| Intended content | *Carl's Doomsday Scenario*, chapters 1–25 |
| File size | 238,291,106 bytes |
| SHA-256 | `b2e9e9082786f3ca0fdd4da463b134fd561ff68f5ddad783d86b607cbd214931` |
| Availability | Stored separately; **not** included in this GitHub repository or its current releases |

The APK archive passed a ZIP integrity check; installation, audiovisual synchronization, and specific functionality have not been independently tested on an Android device.

## How it works

CrawlerVision is intended to read Android playback-session position (including supported Audible or Amazon Music sessions), select a story/chapter, and display local illustrations. Playback-session availability varies with the audiobook app, Android version, and permissions. The app is an illustrated companion, **not** a transcription service or audiobook distributor.

## Build the code currently in this repository

The 1.2 source kit is stored as base64 chunks in `source_chunks/`. The GitHub Actions workflow reassembles `CrawlerVision_Android_Source.zip`, unpacks it, installs Java 17 and Android SDK tools, and builds a sideloadable debug APK.

1. Open **Actions → Build CrawlerVision APK**.
2. Run the workflow (or inspect the workflow and its source before building).
3. Download the `CrawlerVision-Test-APK` workflow artifact once the build succeeds.

**Important:** This does not rebuild v1.12. The current repository needs an updated, reviewed, source-only Android project before the newer app can be reproduced by outside contributors.

## Open source and third-party rights

CrawlerVision's original project code and documentation are released under the [MIT License](LICENSE). **This license does not cover** third-party novels, audiobook audio, characters, trademarks, publisher materials, fan artwork, or illustrations not owned by the project.

**Public distribution warning:** Earlier source archives and APK builds may contain illustrations depicting protected book content or other material without documented redistribution rights. Making a repository public does not establish permission to redistribute those assets. Review and remove or replace unlicensed media **before** uploading an APK as a public release or promoting it for redistribution. Contributors should use original, permissively licensed, or user-provided local scene packs.

This is an unofficial community project; it is not endorsed by Matt Dinniman, Audible, Amazon, audiobook publishers, or other rights holders.

## Contributing

Contributions to playback synchronization, Android compatibility, accurate chapter timing, scene-pack importing, accessibility, and privacy are welcome. Keep third-party audiovisual media and copyrighted text out of contributions unless you can document rights to redistribute it.
