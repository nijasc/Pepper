package com.buhlergroup.pepper.action.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.profile.ProfileRepository;
import com.buhlergroup.pepper.action.profile.data.ProfileEntity;
import com.buhlergroup.pepper.action.profile.data.ResourceEntity;
import com.buhlergroup.pepper.action.profile.data.ResourceType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;

final class ProfilePanelController {

    private final View root;
    private final Executor executor;
    private final PanelNavigator panelNav;
    private final ProfileAdapter adapter;
    private final EditText nameEdit;
    private final EditText instructionsEdit;
    private final LinearLayout resourceList;
    private final TextView summaryStatus;
    private final TextView emptyText;
    private volatile String currentProfileId;

    ProfilePanelController(View root, Executor executor, PanelNavigator panelNav) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;
        this.nameEdit = root.findViewById(R.id.adminProfileName);
        this.instructionsEdit = root.findViewById(R.id.adminProfileInstructions);
        this.resourceList = root.findViewById(R.id.adminProfileResourceList);
        this.summaryStatus = root.findViewById(R.id.adminProfileSummaryStatus);
        this.emptyText = root.findViewById(R.id.adminProfileEmpty);

        adapter = new ProfileAdapter(this::openEditor);
        RecyclerView list = root.findViewById(R.id.adminProfileList);
        list.setLayoutManager(new LinearLayoutManager(ctx()));
        list.setAdapter(adapter);

        root.findViewById(R.id.adminProfileNew).setOnClickListener(v -> promptNewProfile());
        root.findViewById(R.id.adminProfileSave).setOnClickListener(v -> save());
        root.findViewById(R.id.adminProfileAddResource).setOnClickListener(v -> promptAddResource());
        root.findViewById(R.id.adminProfileRebuild).setOnClickListener(v -> rebuild());
        root.findViewById(R.id.adminProfileActivate).setOnClickListener(v -> activate());
        root.findViewById(R.id.adminProfileClone).setOnClickListener(v -> cloneCurrent());
        root.findViewById(R.id.adminProfileDelete).setOnClickListener(v -> confirmDelete());
    }

    void showProfiles() {
        executor.execute(() -> {
            ProfileRepository repo = ProfileRepository.get(ctx());
            List<ProfileEntity> profiles = repo.listProfiles();
            String activeId = repo.activeProfileId();
            root.post(() -> {
                adapter.setData(profiles, activeId);
                emptyText.setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
                panelNav.show(PanelNavigator.PANEL_PROFILES);
            });
        });
    }

    private void openEditor(ProfileEntity profile) {
        currentProfileId = profile.id;
        loadEditor();
        panelNav.show(PanelNavigator.PANEL_PROFILE_EDIT);
    }

    private void loadEditor() {
        String id = currentProfileId;
        if (id == null) {
            return;
        }
        executor.execute(() -> {
            ProfileRepository repo = ProfileRepository.get(ctx());
            ProfileEntity profile = repo.findProfile(id);
            List<ResourceEntity> resources = repo.resources(id);
            root.post(() -> {
                if (profile == null) {
                    showProfiles();
                    return;
                }
                nameEdit.setText(profile.name);
                instructionsEdit.setText(profile.instructions);
                renderResources(resources);
                updateSummaryStatus(profile, resources);
            });
        });
    }

    private void renderResources(List<ResourceEntity> resources) {
        resourceList.removeAllViews();
        for (ResourceEntity res : resources) {
            LinearLayout row = new LinearLayout(ctx());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, 8, 0, 8);

            TextView label = new TextView(ctx());
            label.setText(res.title + "  (" + res.type.name() + ")");
            label.setTextColor(ctx().getColor(R.color.text_primary));
            label.setTextSize(15);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(label, labelParams);

            Button remove = new Button(ctx());
            remove.setText(R.string.profile_resource_remove);
            remove.setTextSize(13);
            remove.setMinWidth(0);
            remove.setMinHeight(0);
            int hp = (int) (12 * ctx().getResources().getDisplayMetrics().density);
            int vp = (int) (4 * ctx().getResources().getDisplayMetrics().density);
            remove.setPadding(hp, vp, hp, vp);
            remove.setOnClickListener(v -> removeResource(res.id));
            row.addView(remove);

            resourceList.addView(row);
        }
    }

    private void updateSummaryStatus(ProfileEntity profile, List<ResourceEntity> resources) {
        if (resources.isEmpty()) {
            summaryStatus.setText(R.string.profile_summary_empty);
        } else if (profile.summaryDirty) {
            summaryStatus.setText(R.string.profile_summary_stale);
        } else {
            summaryStatus.setText(R.string.profile_summary_fresh);
        }
    }

    private void promptNewProfile() {
        EditText input = new EditText(ctx());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.profile_new_name_hint);
        new AlertDialog.Builder(ctx())
                .setTitle(R.string.profile_new)
                .setView(input)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_save, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        return;
                    }
                    executor.execute(() -> {
                        String id = ProfileRepository.get(ctx()).createProfile(name);
                        root.post(() -> openEditor(stub(id, name)));
                    });
                })
                .show();
    }

    private void save() {
        String id = currentProfileId;
        if (id == null) {
            return;
        }
        String name = nameEdit.getText().toString().trim();
        String instructions = instructionsEdit.getText().toString();
        executor.execute(() -> {
            ProfileRepository repo = ProfileRepository.get(ctx());
            repo.renameProfile(id, name);
            repo.updateInstructions(id, instructions);
            root.post(() -> {
                Toast.makeText(ctx(), R.string.profile_saved, Toast.LENGTH_SHORT).show();
                showProfiles();
            });
        });
    }

    private void promptAddResource() {
        if (currentProfileId == null) {
            return;
        }
        CharSequence[] options = {
                ctx().getString(R.string.profile_resource_url),
                ctx().getString(R.string.profile_resource_text),
                ctx().getString(R.string.profile_resource_file)
        };
        new AlertDialog.Builder(ctx())
                .setTitle(R.string.profile_resource_type_title)
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        promptUrlResource();
                    } else if (which == 1) {
                        promptTextResource();
                    } else {
                        launchDocumentPicker();
                    }
                })
                .show();
    }

    private void promptUrlResource() {
        LinearLayout box = verticalBox();
        EditText title = hintedEdit(R.string.profile_resource_title_hint, InputType.TYPE_CLASS_TEXT);
        EditText url = hintedEdit(R.string.profile_resource_url_hint, InputType.TYPE_TEXT_VARIATION_URI);
        box.addView(title);
        box.addView(url);
        new AlertDialog.Builder(ctx())
                .setTitle(R.string.profile_resource_url)
                .setView(box)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_save, (d, w) -> {
                    String t = title.getText().toString().trim();
                    String u = url.getText().toString().trim();
                    if (u.isEmpty()) {
                        return;
                    }
                    String profileId = currentProfileId;
                    executor.execute(() -> {
                        try {
                            ProfileRepository.get(ctx()).addUrlResource(profileId,
                                    t.isEmpty() ? u : t, u);
                            root.post(this::onResourceAdded);
                        } catch (Exception e) {
                            root.post(() -> Toast.makeText(ctx(),
                                    R.string.profile_resource_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .show();
    }

    private void promptTextResource() {
        LinearLayout box = verticalBox();
        EditText title = hintedEdit(R.string.profile_resource_title_hint, InputType.TYPE_CLASS_TEXT);
        EditText text = hintedEdit(R.string.profile_resource_text_hint,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        text.setMinLines(4);
        box.addView(title);
        box.addView(text);
        new AlertDialog.Builder(ctx())
                .setTitle(R.string.profile_resource_text)
                .setView(box)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_save, (d, w) -> {
                    String t = title.getText().toString().trim();
                    String body = text.getText().toString();
                    if (body.trim().isEmpty()) {
                        return;
                    }
                    String profileId = currentProfileId;
                    executor.execute(() -> {
                        try {
                            ProfileRepository.get(ctx()).addTextResource(profileId, ResourceType.MD,
                                    t.isEmpty() ? ctx().getString(R.string.profile_resource_text) : t, body);
                            root.post(this::onResourceAdded);
                        } catch (Exception e) {
                            root.post(() -> Toast.makeText(ctx(),
                                    R.string.profile_resource_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .show();
    }

    private void launchDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"application/pdf", "text/plain", "text/markdown"});
        try {
            ((Activity) ctx()).startActivityForResult(intent, AdminController.REQUEST_PROFILE_DOCUMENT);
        } catch (Exception e) {
            Toast.makeText(ctx(), R.string.profile_resource_failed, Toast.LENGTH_SHORT).show();
        }
    }

    void onDocumentPicked(Uri uri) {
        String profileId = currentProfileId;
        if (uri == null || profileId == null) {
            return;
        }
        executor.execute(() -> {
            try {
                String displayName = queryDisplayName(uri);
                ResourceType type = typeFor(displayName);
                byte[] bytes = readUri(uri);
                ProfileRepository.get(ctx()).addFileResource(profileId, type, displayName, displayName, bytes);
                root.post(this::onResourceAdded);
            } catch (Exception e) {
                root.post(() -> Toast.makeText(ctx(),
                        R.string.profile_resource_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void onResourceAdded() {
        Toast.makeText(ctx(), R.string.profile_resource_added, Toast.LENGTH_SHORT).show();
        loadEditor();
    }

    private void removeResource(String resourceId) {
        executor.execute(() -> {
            ProfileRepository.get(ctx()).removeResource(resourceId);
            root.post(this::loadEditor);
        });
    }

    private void rebuild() {
        String id = currentProfileId;
        if (id == null) {
            return;
        }
        ProfileRepository.get(ctx()).rebuildSummaryAsync(id);
        summaryStatus.setText(R.string.profile_summary_stale);
    }

    private void activate() {
        String id = currentProfileId;
        if (id == null) {
            return;
        }
        executor.execute(() -> {
            ProfileRepository.get(ctx()).setActive(id);
            root.post(() -> Toast.makeText(ctx(), R.string.profile_activated, Toast.LENGTH_SHORT).show());
        });
    }

    private void cloneCurrent() {
        String id = currentProfileId;
        if (id == null) {
            return;
        }
        executor.execute(() -> {
            String newId = ProfileRepository.get(ctx()).cloneProfile(id);
            root.post(() -> {
                if (newId != null) {
                    openEditor(stub(newId, ""));
                }
            });
        });
    }

    private void confirmDelete() {
        String id = currentProfileId;
        if (id == null) {
            return;
        }
        new AlertDialog.Builder(ctx())
                .setTitle(R.string.profile_delete_confirm_title)
                .setMessage(R.string.profile_delete_confirm_message)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.profile_delete, (d, w) -> executor.execute(() -> {
                    boolean deleted = ProfileRepository.get(ctx()).deleteProfile(id);
                    root.post(() -> {
                        Toast.makeText(ctx(),
                                deleted ? R.string.profile_deleted : R.string.profile_delete_builtin,
                                Toast.LENGTH_SHORT).show();
                        if (deleted) {
                            showProfiles();
                        }
                    });
                }))
                .show();
    }

    private LinearLayout verticalBox() {
        LinearLayout box = new LinearLayout(ctx());
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * ctx().getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);
        return box;
    }

    private EditText hintedEdit(int hintRes, int inputType) {
        EditText edit = new EditText(ctx());
        edit.setHint(hintRes);
        edit.setInputType(inputType);
        return edit;
    }

    private ProfileEntity stub(String id, String name) {
        long now = System.currentTimeMillis();
        return new ProfileEntity(id, name, "", false, now, now);
    }

    private ResourceType typeFor(String name) {
        String lower = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return ResourceType.PDF;
        }
        if (lower.endsWith(".md")) {
            return ResourceType.MD;
        }
        return ResourceType.TXT;
    }

    private String queryDisplayName(Uri uri) {
        try (Cursor cursor = ctx().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : "Datei";
    }

    private byte[] readUri(Uri uri) throws Exception {
        try (InputStream in = ctx().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) {
                throw new Exception("Stream null");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private Context ctx() {
        return root.getContext();
    }
}
