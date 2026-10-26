# CrawlerVision

An unofficial Android visual companion experiment for audiobook listening. It displays scenes based on Android media-session playback position; it does not record or upload audiobook audio.

## Current build kit

The current repository contains CrawlerVision 1.2, with a base64-chunked Android source archive, chapter timing data, and bundled scene artwork. The GitHub Actions workflow reconstructs the archive and builds a debug APK. This repository may not reflect newer versions of the app developed elsewhere.

## Building

Run the **Build CrawlerVision APK** GitHub Actions workflow, or inspect `.github/workflows/build-apk.yml` for its Android SDK and Gradle setup. The workflow reconstructs `CrawlerVision_Android_Source.zip` from `source_chunks/*.b64` and builds the APK.

## License and third-party content

Original CrawlerVision software code and project documentation are licensed under the MIT License (see `LICENSE`).

**Important:** The MIT license does **not** grant any rights to third-party books, audiobook recordings, characters, trademarks, or associated illustrations. The currently bundled *Dungeon Crawler Carl* scene illustrations and other book-specific media have not been cleared for public redistribution. Do not publish or redistribute the full source archive or APK with these assets until you have established the necessary rights or removed/replaced them.

CrawlerVision is an unofficial fan/development project and is not endorsed by the author, audiobook publishers, or Audible.

## Contributions

Contributions to improve audiobook playback synchronization, scene mapping, UI accessibility, and support for user-owned or properly licensed scene packs are welcome once the repository is prepared for public distribution.
