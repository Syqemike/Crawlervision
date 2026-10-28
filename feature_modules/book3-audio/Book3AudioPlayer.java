package com.example.crawlervision;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline, user-imported Book 3 audiobook playback with durable resume.
 *
 * Google Drive is accessed through Android's Storage Access Framework (no Drive
 * API key or permissions). Audio is copied to app-private storage, never bundled
 * into an APK or uploaded to the repository.
 *
 * Host integration: mount controls() inside the Book 3 screen; forward
 * onActivityResult(); call onStop() from Activity.onStop(), and close() onDestroy().
 *
 * The listener is the bridge to CrawlerVision's existing image timeline. For
 * chapter-per-file imports its partIndex is zero-based; for a single M4B it is
 * the whole-file position, NOT a reliable chapter number.
 */
public final class Book3AudioPlayer {
    public static final int PICK_AUDIO_REQUEST = 39031;
    private static final String PREFS = "book3_local_audio_v1";
    private static final String KEY_FILE = "file";
    private static final String KEY_MS = "position_ms";
    private static final String KEY_PART = "part";
    private static final Pattern NUMBER = Pattern.compile("(\\d+)");
    private static final String[] EXTENSIONS =
            {".mp3", ".m4a", ".m4b", ".aac", ".ogg", ".wav", ".flac", ".mp4"};

    public interface Listener {
        void onPlaybackPosition(int partIndex, String fileName, long positionMs,
                                long durationMs, boolean playing);
        void onMessage(String message);
    }

    private final Activity activity;
    private final Listener listener;
    private final SharedPreferences preferences;
    private final File storageDirectory;
    private final ExecutorService copying = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<File> parts = new ArrayList<>();

    private LinearLayout controls;
    private Spinner partPicker;
    private TextView status;
    private SeekBar seek;
    private Button playPause;

    private MediaPlayer player;
    private int currentPart = 0;
    private long currentMs = 0L;
    private long durationMs = 0L;
    private boolean prepared = false;
    private boolean preparing = false;
    private boolean playWhenPrepared = false;
    private boolean scrubbing = false;
    private boolean updatingPicker = false;
    private boolean closed = false;

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            if (closed) return;
            if (prepared && player != null) {
                try {
                    currentMs = player.getCurrentPosition();
                    durationMs = player.getDuration();
                    checkpoint();
                } catch (IllegalStateException ignored) { }
            }
            render();
            mainHandler.postDelayed(this, 2000L);
        }
    };

    public Book3AudioPlayer(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
        this.preferences = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.storageDirectory = new File(activity.getFilesDir(), "book3_audio");
        if (!storageDirectory.exists() && !storageDirectory.mkdirs())
            notifyMessage("Cannot create local audiobook storage.");
        reloadParts();
        String storedFile = preferences.getString(KEY_FILE, "");
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).getName().equals(storedFile)) {
                currentPart = i;
                break;
            }
        }
        currentMs = Math.max(0L, preferences.getLong(KEY_MS, 0L));
        if (parts.isEmpty()) currentMs = 0L;
        mainHandler.post(ticker);
    }

    /** Add this view ONLY to the Book 3 screen; Book 1/2 keep their existing players. */
    public View controls() {
        if (controls != null) return controls;
        final float density = activity.getResources().getDisplayMetrics().density;
        int padding = (int) (12 * density);
        controls = new LinearLayout(activity);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(activity);
        title.setText("Book 3 — The Dungeon Anarchist's Cookbook");
        title.setTextSize(18f);
        controls.addView(title);

        Button importButton = new Button(activity);
        importButton.setText("Import audio from Google Drive / Files");
        importButton.setOnClickListener(v -> requestImport());
        controls.addView(importButton);

        partPicker = new Spinner(activity);
        partPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) { }
            @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                 int position, long id) {
                if (updatingPicker || position == currentPart || parts.isEmpty()) return;
                switchTo(position, false, 0L);
            }
        });
        controls.addView(partPicker);

        status = new TextView(activity);
        status.setTextSize(14f);
        controls.addView(status);

        seek = new SeekBar(activity);
        seek.setMax(1000);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) { }
            @Override public void onStartTrackingTouch(SeekBar bar) { scrubbing = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                long target = durationMs > 0L ? durationMs * bar.getProgress() / 1000L : 0L;
                seekTo(target);
                scrubbing = false;
            }
        });
        controls.addView(seek);

        LinearLayout buttons = new LinearLayout(activity);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        Button previous = new Button(activity);
        previous.setText("◀ Part");
        previous.setOnClickListener(v -> {
            if (currentPart > 0) switchTo(currentPart - 1, true, 0L);
            else seekTo(0L);
        });
        buttons.addView(previous, new LinearLayout.LayoutParams(0, -2, 1));

        Button backward = new Button(activity);
        backward.setText("−15s");
        backward.setOnClickListener(v -> seekTo(Math.max(0L, currentMs - 15000L)));
        buttons.addView(backward, new LinearLayout.LayoutParams(0, -2, 1));

        playPause = new Button(activity);
        playPause.setText("Play");
        playPause.setOnClickListener(v -> toggle());
        buttons.addView(playPause, new LinearLayout.LayoutParams(0, -2, 1));

        Button forward = new Button(activity);
        forward.setText("+15s");
        forward.setOnClickListener(v -> seekTo(currentMs + 15000L));
        buttons.addView(forward, new LinearLayout.LayoutParams(0, -2, 1));

        Button next = new Button(activity);
        next.setText("Part ▶");
        next.setOnClickListener(v -> {
            if (currentPart + 1 < parts.size()) switchTo(currentPart + 1, true, 0L);
        });
        buttons.addView(next, new LinearLayout.LayoutParams(0, -2, 1));
        controls.addView(buttons);

        refreshPicker();
        render();
        return controls;
    }

    /** Android system picker can show Google Drive without any API credentials. */
    public void requestImport() {
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("*/*"); // Some Drive providers report M4B as application/octet-stream.
        pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(pick, PICK_AUDIO_REQUEST);
    }

    /** Forward the host Activity's onActivityResult here. Returns true if handled. */
    public boolean onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode != PICK_AUDIO_REQUEST) return false;
        if (resultCode != Activity.RESULT_OK || result == null) return true;
        List<ImportItem> selected = new ArrayList<>();
        ClipData clip = result.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) {
                Uri uri = clip.getItemAt(i).getUri();
                selected.add(new ImportItem(uri, displayName(uri)));
            }
        } else if (result.getData() != null) {
            Uri uri = result.getData();
            selected.add(new ImportItem(uri, displayName(uri)));
        }
        if (selected.isEmpty()) {
            notifyMessage("No audio files selected.");
            return true;
        }
        Collections.sort(selected, (a, b) -> naturalCompare(a.name, b.name));
        notifyMessage("Importing " + selected.size() + " audio file(s) to offline storage...");
        copying.execute(() -> {
            String firstImported = null;
            int count = 0;
            String error = null;
            for (ImportItem item : selected) {
                if (closed) break;
                try {
                    String name = safeName(item.name);
                    if (!isSupported(name)) throw new IOException("Unsupported type: " + name);
                    File destination = uniqueDestination(name);
                    File temp = new File(storageDirectory, destination.getName() + ".partial");
                    try {
                        try (InputStream source = activity.getContentResolver().openInputStream(item.uri);
                             FileOutputStream output = new FileOutputStream(temp)) {
                            if (source == null) throw new IOException("Cannot open " + name);
                            byte[] buffer = new byte[65536];
                            int read;
                            while ((read = source.read(buffer)) != -1) output.write(buffer, 0, read);
                            output.getFD().sync();
                        }
                        if (!temp.renameTo(destination))
                            throw new IOException("Failed to save " + name);
                    } finally {
                        if (temp.exists()) temp.delete();
                    }
                    if (firstImported == null) firstImported = destination.getName();
                    count++;
                } catch (Exception e) {
                    error = e.getMessage();
                }
            }
            final String first = firstImported, failure = error;
            final int imported = count;
            mainHandler.post(() -> {
                if (closed) return;
                releasePlayer();
                reloadParts();
                if (first != null) {
                    for (int i = 0; i < parts.size(); i++) {
                        if (parts.get(i).getName().equals(first)) {
                            currentPart = i;
                            break;
                        }
                    }
                    currentMs = 0L;
                    durationMs = 0L;
                    checkpoint();
                }
                refreshPicker();
                render();
                notifyMessage("Imported " + imported + " file(s)." +
                        (failure == null ? "" : " Error: " + failure));
            });
        });
        return true;
    }

    private void toggle() {
        if (parts.isEmpty()) {
            notifyMessage("Import Book 3 audio first.");
            return;
        }
        if (prepared && player != null) {
            if (player.isPlaying()) {
                player.pause();
                checkpoint();
            } else {
                player.start();
            }
            render();
        } else if (!preparing) {
            switchTo(currentPart, true, currentMs);
        } else {
            playWhenPrepared = !playWhenPrepared;
        }
    }

    private void switchTo(int index, boolean start, long position) {
        if (index < 0 || index >= parts.size() || closed) return;
        checkpoint();
        releasePlayer();
        currentPart = index;
        currentMs = Math.max(0L, position);
        durationMs = 0L;
        playWhenPrepared = start;
        preparing = true;
        checkpoint();
        try {
            final MediaPlayer next = new MediaPlayer();
            player = next;
            next.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            next.setDataSource(parts.get(index).getAbsolutePath());
            next.setOnPreparedListener(mp -> {
                if (closed || mp != player) return;
                prepared = true;
                preparing = false;
                durationMs = mp.getDuration();
                long target = durationMs > 0 ?
                        Math.min(currentMs, Math.max(0L, durationMs - 1000L)) : currentMs;
                // Begin only after the restored seek completes; avoids replaying from 0.
                mp.setOnSeekCompleteListener(seeked -> {
                    if (closed || seeked != player) return;
                    if (playWhenPrepared) seeked.start();
                    render();
                });
                if (target > 0L) mp.seekTo((int) Math.min(Integer.MAX_VALUE, target));
                else if (playWhenPrepared) mp.start();
                render();
            });
            next.setOnCompletionListener(mp -> {
                currentMs = durationMs;
                checkpoint();
                if (currentPart + 1 < parts.size()) {
                    switchTo(currentPart + 1, true, 0L);
                } else {
                    releasePlayer();
                    render();
                }
            });
            next.setOnErrorListener((mp, what, extra) -> {
                notifyMessage("Cannot play audio (error " + what + ").");
                releasePlayer();
                render();
                return true;
            });
            next.prepareAsync();
        } catch (Exception e) {
            releasePlayer();
            notifyMessage("Unable to open audio: " + e.getMessage());
        }
        refreshPicker();
        render();
    }

    private void seekTo(long requested) {
        currentMs = Math.max(0L, durationMs > 0L ?
                Math.min(requested, durationMs) : requested);
        if (prepared && player != null) {
            player.seekTo((int) Math.min(Integer.MAX_VALUE, currentMs));
        }
        checkpoint();
        render();
    }

    /** Save on every periodic tick AND on pause, seek, part change and Activity stop. */
    public void onStop() {
        if (closed) return;
        if (prepared && player != null) {
            try {
                currentMs = player.getCurrentPosition();
                player.pause();
            } catch (IllegalStateException ignored) { }
        }
        checkpoint();
        releasePlayer();
        render();
    }

    public void close() {
        if (closed) return;
        onStop();
        closed = true;
        mainHandler.removeCallbacks(ticker);
        copying.shutdownNow();
    }

    public int currentPartIndex() { return currentPart; }
    public long currentPositionMs() { return currentMs; }
    public boolean hasImportedAudio() { return !parts.isEmpty(); }

    private void checkpoint() {
        if (parts.isEmpty()) return;
        preferences.edit()
                .putString(KEY_FILE, parts.get(currentPart).getName())
                .putInt(KEY_PART, currentPart)
                .putLong(KEY_MS, currentMs)
                .apply();
    }

    private void releasePlayer() {
        MediaPlayer old = player;
        player = null;
        prepared = false;
        preparing = false;
        playWhenPrepared = false;
        if (old != null) {
            try { old.reset(); } catch (Exception ignored) { }
            try { old.release(); } catch (Exception ignored) { }
        }
    }

    private void reloadParts() {
        parts.clear();
        File[] found = storageDirectory.listFiles(file ->
                file.isFile() && isSupported(file.getName()));
        if (found != null) {
            Collections.addAll(parts, found);
            Collections.sort(parts, (a, b) ->
                    naturalCompare(a.getName(), b.getName()));
        }
        if (currentPart >= parts.size()) currentPart = Math.max(0, parts.size() - 1);
    }

    private void refreshPicker() {
        if (partPicker == null) return;
        List<String> names = new ArrayList<>();
        for (File part : parts) names.add(part.getName());
        updatingPicker = true;
        partPicker.setAdapter(new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_dropdown_item, names));
        if (!parts.isEmpty()) partPicker.setSelection(currentPart);
        updatingPicker = false;
    }

    private void render() {
        boolean playing = prepared && player != null && player.isPlaying();
        if (status != null) {
            String part = parts.isEmpty() ? "No Book 3 audio imported"
                    : "Part " + (currentPart + 1) + " of " + parts.size();
            status.setText(part + "   " + clock(currentMs) + " / " + clock(durationMs) +
                    (playing ? "  • Playing" : "  • Paused"));
        }
        if (playPause != null) playPause.setText(playing ? "Pause" : "Play");
        if (seek != null && !scrubbing) {
            seek.setProgress(durationMs > 0L ?
                    (int) Math.min(1000L, currentMs * 1000L / durationMs) : 0);
        }
        if (listener != null && !parts.isEmpty()) {
            listener.onPlaybackPosition(currentPart, parts.get(currentPart).getName(),
                    currentMs, durationMs, playing);
        }
    }

    private void notifyMessage(String message) {
        if (listener != null) listener.onMessage(message);
    }

    private String displayName(Uri uri) {
        try (Cursor cursor = activity.getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.trim().isEmpty()) return name;
            }
        } catch (Exception ignored) { }
        String segment = uri.getLastPathSegment();
        return segment == null ? "book3_audio.m4b" : segment;
    }

    private File uniqueDestination(String name) {
        File candidate = new File(storageDirectory, name);
        if (!candidate.exists()) return candidate;
        int dot = name.lastIndexOf('.');
        String stem = dot >= 0 ? name.substring(0, dot) : name;
        String ext = dot >= 0 ? name.substring(dot) : "";
        for (int n = 2; n < 10000; n++) {
            candidate = new File(storageDirectory, stem + " (" + n + ")" + ext);
            if (!candidate.exists()) return candidate;
        }
        return new File(storageDirectory, System.currentTimeMillis() + ext);
    }

    private static String safeName(String name) {
        String clean = name == null ? "book3_audio.m4b" :
                name.replaceAll("[\\\\/\\p{Cntrl}]+", "_").trim();
        return clean.length() > 180 ? clean.substring(clean.length() - 180) : clean;
    }

    private static boolean isSupported(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String ext : EXTENSIONS) if (lower.endsWith(ext)) return true;
        return false;
    }

    private static int naturalCompare(String a, String b) {
        // Compare ALL numeric tokens so Book3_Chapter2 precedes Book3_Chapter10.
        Matcher ma = NUMBER.matcher(a), mb = NUMBER.matcher(b);
        while (ma.find() && mb.find()) {
            try {
                long na = Long.parseLong(ma.group(1));
                long nb = Long.parseLong(mb.group(1));
                if (na != nb) return Long.compare(na, nb);
            } catch (NumberFormatException ignored) { }
        }
        return a.compareToIgnoreCase(b);
    }

    private static String clock(long ms) {
        long seconds = Math.max(0, ms) / 1000L;
        return String.format(Locale.US, "%d:%02d:%02d",
                seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }

    private static final class ImportItem {
        final Uri uri;
        final String name;
        ImportItem(Uri uri, String name) { this.uri = uri; this.name = name; }
    }
}
