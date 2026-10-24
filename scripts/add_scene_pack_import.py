"""Patch the original CrawlerVision demo with phone-side scene JSON import."""
from pathlib import Path

p = Path("CrawlerVision/app/src/main/java/com/example/crawlervision/MainActivity.java")
s = p.read_text(encoding="utf-8")

def replace_once(old, new):
    global s
    amount = s.count(old)
    if amount != 1:
        raise ValueError(f"Expected one occurrence, got {amount}: {old[:120]!r}")
    s = s.replace(old, new, 1)

replace_once(
    "import android.media.MediaMetadata;\n",
    "import android.media.MediaMetadata;\nimport android.net.Uri;\n"
)
replace_once(
    "import java.util.List;\n",
    "import java.util.List;\nimport java.util.ArrayList;\nimport java.io.InputStream;\nimport java.io.ByteArrayOutputStream;\nimport java.nio.charset.StandardCharsets;\nimport org.json.JSONObject;\nimport org.json.JSONArray;\n"
)
replace_once(
    '    private static final String AMAZON_PACKAGE = "com.amazon.mp3";',
    '    private static final String AMAZON_PACKAGE = "com.amazon.mp3";\n    private static final int REQUEST_SCENE_PACK = 1402;'
)
replace_once(
    "    private TextView statusText, detailText, artworkTitle, timeText, modeText;",
    "    private TextView statusText, detailText, artworkTitle, timeText, modeText, packText;\n"
    "    private final ArrayList<Scene> scenePack = new ArrayList<>();\n"
    '    private String scenePackChapter = "";\n'
    "    private static class Scene {\n"
    "        long startMs, endMs;\n"
    "        String title;\n"
    "        Scene(long a, long b, String t) { startMs=a; endMs=b; title=t; }\n"
    "    }"
)
replace_once(
    "        buildUI();\n        showArtwork(0);",
    """        buildUI();
        String saved = getPreferences(MODE_PRIVATE).getString("chapter_scene_pack", "");
        if (!saved.isEmpty()) {
            try { applyScenePack(saved); }
            catch (Exception error) {
                getPreferences(MODE_PRIVATE).edit().remove("chapter_scene_pack").apply();
                packText.setText("Previous scene pack could not be restored. Import it again.");
            }
        }
        showArtwork(0);"""
)
replace_once(
    '        Button copy = button("Copy playback diagnostics");',
    """        packText = text("No scene pack imported — generic demo art every 15 seconds.", 12, 0xFFB9E2D8, false);
        root.addView(packText, margin(14));
        Button importPack = button("3  Import chapter scene JSON");
        root.addView(importPack, margin(9));
        importPack.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("*/*");
            pick.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain", "application/octet-stream"});
            try { startActivityForResult(pick, REQUEST_SCENE_PACK); }
            catch (Exception e) { Toast.makeText(this, "Could not open file picker.", Toast.LENGTH_LONG).show(); }
        });
        Button copy = button("Copy playback diagnostics");"""
)
replace_once(
    '    private void tick() {',
    """    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_SCENE_PACK || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        try (InputStream stream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            if (stream == null) throw new Exception("Could not open selected file");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = stream.read(buffer)) != -1) {
                bytes.write(buffer, 0, n);
                if (bytes.size() > 2_000_000) throw new Exception("JSON file is too large");
            }
            String json = bytes.toString(StandardCharsets.UTF_8.name());
            applyScenePack(json);
            getPreferences(MODE_PRIVATE).edit().putString("chapter_scene_pack", json).apply();
            Toast.makeText(this, "Imported " + scenePack.size() + " scenes for " + scenePackChapter, Toast.LENGTH_LONG).show();
            if (lastAutoPositionAvailable) updateSceneAndClock(lastAutoPosition, -1, false);
        } catch (Exception e) {
            Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void applyScenePack(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("scenes");
        if (arr.length() < 1 || arr.length() > 3000) throw new Exception("Invalid scene count");
        ArrayList<Scene> loaded = new ArrayList<>();
        for (int i=0;i<arr.length();i++) {
            JSONObject entry = arr.getJSONObject(i);
            long a = entry.getLong("estimated_start_ms");
            long b = entry.getLong("estimated_end_ms");
            String t = entry.optString("title", "Scene " + (i + 1));
            if (a < 0 || b < a) throw new Exception("Invalid timestamps in scene " + (i+1));
            loaded.add(new Scene(a,b,t));
        }
        loaded.sort((a,b)->Long.compare(a.startMs,b.startMs));
        scenePack.clear();
        scenePack.addAll(loaded);
        scenePackChapter = obj.optString("chapter", "Chapter 1").trim();
        currentArtwork=-1;
        packText.setText("Loaded: " + scenePackChapter + " • " + scenePack.size() + " scenes. Images are still demo placeholders.");
    }

    private boolean chapterMatches(String title) {
        if (title == null || title.trim().isEmpty()) return false;
        String expected = scenePackChapter.replaceAll("\\\\s+", " ").trim();
        String actual = title.replaceAll("\\\\s+", " ").trim();
        if (expected.equalsIgnoreCase(actual)) return true;
        return actual.matches("(?i).*\\\\b" + java.util.regex.Pattern.quote(expected) + "\\\\b.*");
    }

    private void tick() {"""
)
replace_once(
    '        if(anchorPosition<0)anchorPosition=position;\n',
    """        if (!scenePack.isEmpty() && (manualMode || chapterMatches(lastTrack))) {
            int sceneIndex=0;
            for (int i=0;i<scenePack.size();i++) {
                if (position >= scenePack.get(i).startMs) sceneIndex=i;
                else break;
            }
            Scene scene=scenePack.get(sceneIndex);
            showArtwork(sceneIndex % ART.length);
            artworkTitle.setText(scene.title);
            timeText.setText(clock(position)+(duration>0?" / "+clock(duration):"")+
                "   •   SCENE "+(sceneIndex+1)+" / "+scenePack.size()+
                (playing?"   ▶":"   ‖"));
            packText.setText("Scene pack active: " + scenePackChapter +
                " • Estimated scene boundaries • Demo images, not story artwork");
            return;
        }
        if (!scenePack.isEmpty() && !manualMode) {
            packText.setText("Imported "+scenePackChapter+" — waiting for that chapter in Amazon Music (current: "+n(lastTrack)+")");
        }
        if(anchorPosition<0)anchorPosition=position;
"""
)
replace_once("App: CrawlerVision 0.1", "App: CrawlerVision 0.2")
p.write_text(s, encoding="utf-8")
print("Applied scene-pack import and timestamp scene switching to MainActivity.java")
