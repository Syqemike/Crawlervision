# Publish the 1.12 Android APK

This guide is for a maintainer who **has the APK file locally** and has confirmed the rights to distribute all embedded artwork and other assets.

## Intended release

| Field | Value |
| --- | --- |
| Tag | `v1.12` |
| Name | `CrawlerVision 1.12 — Audible Sync + Doomsday Chapters 1–25` |
| APK filename | `CrawlerVision_1.12_Doomsday_Chapters_1-25.apk` |
| File size | 238,291,106 bytes (227.25 MiB) |
| SHA-256 | `b2e9e9082786f3ca0fdd4da463b134fd561ff68f5ddad783d86b607cbd214931` |
| Source status | Prebuilt APK; matching v1.12 Android source is **not yet committed** |
| Build status | APK ZIP container integrity checked; not Android-device tested |

The APK itself bundles two preconfigured scene packs: 470 images for the first *Dungeon Crawler Carl* book (chapters 1–47) and 250 images for *Carl's Doomsday Scenario* (chapters 1–25). These include book-based visual material. Publication requires appropriate permissions and attribution; the repository MIT license covers original project code, **not** third-party content.

## Option A — GitHub web browser

1. Open [Releases](https://github.com/Syqemike/Crawlervision/releases) and select **Draft a new release**.
2. Create a new tag `v1.12` targeting `main`.
3. Enter the release title above.
4. Attach the **real APK file** by dragging it into the release asset area.
5. Add release notes based on [FEATURES](FEATURES.md) and describe sync/scene timing limitations.
6. Once rights and APK are confirmed, publish the release. The public file will be available at:

   `https://github.com/Syqemike/Crawlervision/releases/download/v1.12/CrawlerVision_1.12_Doomsday_Chapters_1-25.apk`

This URL **does not work until the APK is actually attached to the published release**. Do not publish links to missing release assets.

## Option B — GitHub CLI

On a machine with [GitHub CLI](https://cli.github.com/) authenticated as a maintainer and the APK downloaded locally:

```bash
sha256sum CrawlerVision_1.12_Doomsday_Chapters_1-25.apk
gh release create v1.12 ./CrawlerVision_1.12_Doomsday_Chapters_1-25.apk \
  --repo Syqemike/Crawlervision \
  --title "CrawlerVision 1.12 — Audible Sync + Doomsday Chapters 1–25" \
  --notes-file docs/RELEASE_NOTES_v1.12.md
```

Compare the computed SHA-256 to the checksum above before uploading. To add the APK to an existing draft release:

```bash
gh release upload v1.12 ./CrawlerVision_1.12_Doomsday_Chapters_1-25.apk --repo Syqemike/Crawlervision
```

## Important build details

- This is a **debug-oriented sideloadable build**. It is not Play Store signed or Play Store reviewed.
- Its **signing certificate** may differ from an already installed CrawlerVision copy. If Android refuses to update, preserve any user data and verify the source/signing before deciding whether to uninstall the old build.
- The existing `build-apk.yml` builds the repository's legacy **v1.2 source kit**. It cannot reproduce v1.12 until the matching source, dependencies, and assets are available.
- Avoid checking a 238 MB APK into normal Git history. GitHub Releases is the correct binary download channel.
- Do **not** upload copyrighted book text, audiobook audio, unlicensed fan art, or proprietary illustrations in the public repository or release without rights.

## Verify after publishing

- [ ] v1.12 release is visible to a signed-out visitor.
- [ ] Asset name and file size match.
- [ ] Downloaded file SHA-256 matches the table above.
- [ ] Android installs the downloaded APK.
- [ ] Audible and Amazon Music permission/session behavior is checked on a real device.
- [ ] Book selector, chapter transitions, manual alignment, previous/next image, fullscreen, and transcript microphone toggle are exercised.
- [ ] README download link is updated **only after** the release asset exists.
