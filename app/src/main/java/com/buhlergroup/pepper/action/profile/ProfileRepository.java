package com.buhlergroup.pepper.action.profile;

import android.content.Context;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.profile.data.ProfileDao;
import com.buhlergroup.pepper.action.profile.data.ProfileDatabase;
import com.buhlergroup.pepper.action.profile.data.ProfileEntity;
import com.buhlergroup.pepper.action.profile.data.ResourceDao;
import com.buhlergroup.pepper.action.profile.data.ResourceEntity;
import com.buhlergroup.pepper.action.profile.data.ResourceType;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.openai.OpenAIService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ProfileRepository {

    private static final String TAG = "ProfileRepository";
    private static final String BUILTIN_ID = "builtin-pepper-generic";
    private static final String BUILTIN_NAME = "Pepper Generic";
    private static final int MAX_SUMMARY_INPUT_CHARS = 12000;
    private static final int SUMMARY_MAX_TOKENS = 500;
    private static final String SUMMARY_INSTRUCTIONS =
            "Du erstellst eine kompakte Wissensbasis für einen Roboter-Assistenten bei Bühler. "
                    + "Fasse den folgenden Inhalt in knappen, faktischen deutschen Stichpunkten zusammen. "
                    + "Nur Fakten, die für Besucherfragen relevant sind. Keine Einleitung, keine Meta-Kommentare.";

    private static final ExecutorService maintenanceExecutor = Executors.newSingleThreadExecutor();
    private static volatile ProfileRepository instance;

    private final Context appContext;
    private final ProfileDao profileDao;
    private final ResourceDao resourceDao;
    private volatile ProfileEntity cachedActive;

    private ProfileRepository(Context context) {
        this.appContext = context.getApplicationContext();
        ProfileDatabase database = ProfileDatabase.get(context);
        this.profileDao = database.profileDao();
        this.resourceDao = database.resourceDao();
    }

    public static ProfileRepository get(Context context) {
        if (instance == null) {
            synchronized (ProfileRepository.class) {
                if (instance == null) {
                    instance = new ProfileRepository(context);
                }
            }
        }
        return instance;
    }

    public static void ensureSeededAsync(Context context) {
        Context app = context.getApplicationContext();
        maintenanceExecutor.submit(() -> {
            try {
                get(app).ensureSeeded();
            } catch (Exception e) {
                DebugLog.get().w(TAG, "Seeding fehlgeschlagen: " + e.getMessage());
            }
        });
    }

    private void ensureSeeded() {
        if (ProfileSettings.isSeeded(appContext) && profileDao.count() > 0) {
            return;
        }
        if (profileDao.findById(BUILTIN_ID) == null) {
            long now = System.currentTimeMillis();
            ProfileEntity builtin = new ProfileEntity(
                    BUILTIN_ID, BUILTIN_NAME, readRawInstructions(), true, now, now);
            profileDao.insert(builtin);
        }
        if (ProfileSettings.getActiveProfileId(appContext).isEmpty()) {
            ProfileSettings.setActiveProfileId(appContext, BUILTIN_ID);
        }
        ProfileSettings.setSeeded(appContext, true);
        invalidateCache();
    }

    public String getActiveInstructions(Context context) {
        ProfileEntity active = getActiveProfile();
        if (active != null && active.instructions != null && !active.instructions.trim().isEmpty()) {
            return active.instructions;
        }
        return readRawInstructions();
    }

    public String getActiveContentSummary(Context context) {
        ProfileEntity active = getActiveProfile();
        if (active == null || active.contentSummary == null || active.contentSummary.trim().isEmpty()) {
            return null;
        }
        return active.contentSummary;
    }

    private ProfileEntity getActiveProfile() {
        String activeId = ProfileSettings.getActiveProfileId(appContext);
        ProfileEntity cached = cachedActive;
        if (cached != null && cached.id.equals(activeId)) {
            return cached;
        }
        ProfileEntity active = activeId.isEmpty() ? null : profileDao.findById(activeId);
        if (active == null) {
            active = profileDao.getBuiltin();
        }
        cachedActive = active;
        return active;
    }

    public List<ProfileEntity> listProfiles() {
        return profileDao.getAll();
    }

    public ProfileEntity findProfile(String id) {
        return profileDao.findById(id);
    }

    public List<ResourceEntity> resources(String profileId) {
        return resourceDao.getForProfile(profileId);
    }

    public String activeProfileId() {
        return ProfileSettings.getActiveProfileId(appContext);
    }

    public String createProfile(String name) {
        long now = System.currentTimeMillis();
        String id = newId();
        profileDao.insert(new ProfileEntity(id, safeName(name), "", false, now, now));
        return id;
    }

    public String cloneProfile(String sourceId) {
        ProfileEntity source = profileDao.findById(sourceId);
        if (source == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        String id = newId();
        profileDao.insert(new ProfileEntity(id, source.name + " (Kopie)", source.instructions, false, now, now));
        for (ResourceEntity res : resourceDao.getForProfile(sourceId)) {
            try {
                String text = readFile(new File(res.textPath));
                String path = writeResourceText(id, text);
                resourceDao.insert(new ResourceEntity(newId(), id, res.type, res.title,
                        res.sourceUri, path, text.length(), now));
            } catch (IOException e) {
                DebugLog.get().w(TAG, "Ressource konnte nicht kopiert werden: " + e.getMessage());
            }
        }
        markDirty(id);
        rebuildSummaryAsync(id);
        return id;
    }

    public boolean deleteProfile(String id) {
        ProfileEntity profile = profileDao.findById(id);
        if (profile == null || profile.builtin) {
            return false;
        }
        resourceDao.deleteForProfile(id);
        deleteDirQuietly(profileDir(id));
        profileDao.deleteById(id);
        if (id.equals(ProfileSettings.getActiveProfileId(appContext))) {
            ProfileEntity builtin = profileDao.getBuiltin();
            ProfileSettings.setActiveProfileId(appContext, builtin != null ? builtin.id : "");
        }
        invalidateCache();
        return true;
    }

    public void renameProfile(String id, String name) {
        profileDao.setName(id, safeName(name), System.currentTimeMillis());
        invalidateCache();
    }

    public void updateInstructions(String id, String instructions) {
        profileDao.setInstructions(id, instructions == null ? "" : instructions, System.currentTimeMillis());
        invalidateCache();
    }

    public void setActive(String id) {
        ProfileSettings.setActiveProfileId(appContext, id);
        invalidateCache();
    }

    public void addTextResource(String profileId, ResourceType type, String title, String text)
            throws IOException {
        String path = writeResourceText(profileId, text == null ? "" : text);
        insertResource(profileId, type, title, null, path, text == null ? 0 : text.length());
    }

    public void addUrlResource(String profileId, String title, String url) throws IOException {
        String text = TextExtractor.fetchUrl(url);
        String path = writeResourceText(profileId, text);
        insertResource(profileId, ResourceType.URL, title, url, path, text.length());
    }

    public void addFileResource(String profileId, ResourceType type, String title, String displayName,
                                byte[] bytes) throws IOException {
        String text = TextExtractor.fromBytes(type, bytes, appContext);
        String path = writeResourceText(profileId, text);
        insertResource(profileId, type, title, displayName, path, text.length());
    }

    private void insertResource(String profileId, ResourceType type, String title, String sourceUri,
                                String textPath, int charCount) {
        resourceDao.insert(new ResourceEntity(newId(), profileId, type, safeName(title), sourceUri,
                textPath, charCount, System.currentTimeMillis()));
        markDirty(profileId);
        rebuildSummaryAsync(profileId);
    }

    public void removeResource(String resourceId) {
        ResourceEntity res = resourceDao.findById(resourceId);
        if (res == null) {
            return;
        }
        deleteFileQuietly(new File(res.textPath));
        resourceDao.deleteById(resourceId);
        markDirty(res.profileId);
        rebuildSummaryAsync(res.profileId);
    }

    public void markDirty(String profileId) {
        profileDao.setDirty(profileId, true, System.currentTimeMillis());
        invalidateCache();
    }

    public void rebuildSummaryAsync(String profileId) {
        maintenanceExecutor.submit(() -> {
            try {
                rebuildSummary(profileId);
            } catch (Exception e) {
                DebugLog.get().w(TAG, "Summary-Aufbau fehlgeschlagen: " + e.getMessage());
            }
        });
    }

    private void rebuildSummary(String profileId) throws IOException {
        List<ResourceEntity> resources = resourceDao.getForProfile(profileId);
        if (resources.isEmpty()) {
            profileDao.setSummary(profileId, "", System.currentTimeMillis());
            invalidateCache();
            return;
        }
        StringBuilder joined = new StringBuilder();
        for (ResourceEntity res : resources) {
            if (joined.length() >= MAX_SUMMARY_INPUT_CHARS) {
                break;
            }
            joined.append("# ").append(res.title).append('\n');
            joined.append(readFile(new File(res.textPath))).append("\n\n");
        }
        String input = joined.length() > MAX_SUMMARY_INPUT_CHARS
                ? joined.substring(0, MAX_SUMMARY_INPUT_CHARS) : joined.toString();
        OpenAIService.shared().setC(appContext);
        String summary = OpenAIService.shared().generateText(SUMMARY_INSTRUCTIONS, input, SUMMARY_MAX_TOKENS);
        profileDao.setSummary(profileId, summary == null ? "" : summary.trim(), System.currentTimeMillis());
        invalidateCache();
    }

    private void invalidateCache() {
        cachedActive = null;
    }

    private String readRawInstructions() {
        try (InputStream in = appContext.getResources().openRawResource(R.raw.instructions)) {
            byte[] buffer = new byte[in.available()];
            int read = 0;
            int r;
            while (read < buffer.length && (r = in.read(buffer, read, buffer.length - read)) >= 0) {
                read += r;
            }
            return new String(buffer, 0, read, StandardCharsets.UTF_8);
        } catch (IOException e) {
            DebugLog.get().w(TAG, "instructions.md konnte nicht gelesen werden: " + e.getMessage());
            return "";
        }
    }

    private String writeResourceText(String profileId, String text) throws IOException {
        File dir = profileDir(profileId);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Verzeichnis konnte nicht angelegt werden: " + dir);
        }
        File file = new File(dir, newId() + ".txt");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return file.getAbsolutePath();
    }

    private String readFile(File file) throws IOException {
        if (!file.exists()) {
            return "";
        }
        try (InputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int read = 0;
            int r;
            while (read < bytes.length && (r = in.read(bytes, read, bytes.length - read)) >= 0) {
                read += r;
            }
            return new String(bytes, 0, read, StandardCharsets.UTF_8);
        }
    }

    private File profileDir(String profileId) {
        return new File(new File(appContext.getFilesDir(), "profiles"), profileId);
    }

    private void deleteFileQuietly(File file) {
        try {
            if (file.exists() && !file.delete()) {
                DebugLog.get().w(TAG, "Datei nicht löschbar: " + file);
            }
        } catch (Exception ignored) {
        }
    }

    private void deleteDirQuietly(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFileQuietly(child);
            }
        }
        deleteFileQuietly(dir);
    }

    private String safeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Profil";
        }
        return name.trim();
    }

    private String newId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
