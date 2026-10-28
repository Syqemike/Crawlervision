# Book 3 offline audio playback with saved progress

**Implementation module only — not a built APK.** The repository contains the 1.2 legacy source archive and a 1.12 APK, but not the user's current working 1.13 source. This branch keeps the new code isolated to avoid replacing later functionality with older code.

## Book and intent

**The Dungeon Anarchist's Cookbook — Book 3.**

- Import audiobook MP3, M4A, M4B, AAC, OGG, WAV, FLAC or MP4 directly from Android's Google Drive or Files picker. Decoder support depends on the device.
- Audio copies to the app's private storage for offline playback; no API charges, credentials, upload, or audiobook bundled in APK.
- One long audiobook file or multiple chapter-part files, sorted by numeric ordering.
- Embedded playback controls: play/pause, seek, 15-second skip, previous/next part, part selector.
- File and millisecond timestamp checkpointed every 2 seconds and whenever playback is paused, skipped, stopped or parts change.
- On relaunch the last file and location are restored; the user taps Play to resume.
- Callback conveys precise playback position to the existing Book 3 image timeline.

## How to integrate with the CURRENT app

1. Move Book3AudioPlayer.java into the application's Java source package under app/src/main/java/com/example/crawlervision/ (update package if necessary).
2. In the real MainActivity, construct new Book3AudioPlayer(this, listener) after super.onCreate(). The Listener receives onPlaybackPosition(int partIndex, String filename, long positionMs, long durationMs, boolean playing), and onMessage(String text). Adapt the former to the existing Book 3 scene/image scheduler; keep Books 1/2 connected to the external Audible/Amazon Music tracker.
3. On the Book 3 layout, add the view returned by book3Audio.controls(). Keep it hidden from Book 1 and Book 2.
4. In Activity.onActivityResult(), pass requestCode, resultCode and Intent to book3Audio.onActivityResult(...); if it returns true, the import result was handled. Preserve all existing handlers.
5. In Activity.onStop(), call book3Audio.onStop(). In onDestroy(), call book3Audio.close(). This pauses and checkpoints progress when the app is closed.
6. While Book 3 in-app audio is selected, use the **local audio player's** position as the image clock, not a second media-session clock.

This module is intentionally not wired into the legacy 1.2 Android project because doing so would create a misleading/obsolete APK. Integrate into the actual 1.13 source before building a releasable update.

## Scene alignment matters

If each imported file is exactly one chapter, the file index (zero-based) can identify the chapter *after verifying the filenames and any prologue*. If the book is one continuous M4B or other audio file, the module reports its full-book timestamp and partIndex 0. Do not pretend that is Chapter 1 for the whole recording.

For exact chapter/image alignment in a single file, extract verified chapter markers or create a checked cumulative chapter-start timestamp map. Do not guess timestamps or characters. The repository branch does NOT contain the missing Book 3 image pack from the current app; the existing scene assets and accurate character appearances must be carried forward.

## Test on Galaxy S24 Ultra

1. Choose Book 3, import a file using Google Drive in Android's native picker, then play within CrawlerVision.
2. Turn off connectivity: the imported local audio should continue working.
3. Play for 2 minutes, close the app, reopen. Confirm the same file and timestamp (within the 2-second checkpoint interval), then press Play to continue.
4. Seek to a new location, pause, close, reopen and verify the updated position.
5. Import chapter 2 and chapter 10; confirm correct numeric sorting. Verify auto-next at the end of a track.
6. Switch to Books 1 and 2; check Audible/Amazon Music syncing, fullscreen, transcription and existing controls.
7. Verify the scenes for Book 3 against the actual audio; no invented chapter offset or future characters.
8. Install an update with the **same package/signing identity**, then confirm imported audio and saved progress remain intact.

## Important limits

- Android uninstall removes app-private audio files and progress. Do **not** uninstall the current working APK to test this branch.
- This module pauses playback when its host activity stops; full background and lockscreen audio service is not yet provided.
- Some M4B codecs are device dependent. A single M4B does not automatically supply chapter boundaries.
- Interrupted imports leave incomplete .partial files ignored by the player.
- Google Drive is accessed through Android's on-device file picker; this does not mean ChatGPT can browse your connected Drive audio in the development session.
