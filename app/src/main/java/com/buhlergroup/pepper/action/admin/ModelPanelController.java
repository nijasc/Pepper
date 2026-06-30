package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.llm.LlmModel;
import com.buhlergroup.pepper.llm.LlmProvider;
import com.buhlergroup.pepper.llm.ModelCatalog;
import com.buhlergroup.pepper.llm.ModelSettings;
import com.buhlergroup.pepper.llm.ModelTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

final class ModelPanelController {

    private static final String TAG = "ModelPanel";

    private final View root;
    private final Executor executor;
    private final PanelNavigator panelNav;
    private final LinearLayout providerList;
    private final LinearLayout taskList;
    private final Map<LlmProvider, EditText> keyEdits = new EnumMap<>(LlmProvider.class);
    private final Map<LlmProvider, TextView> statusViews = new EnumMap<>(LlmProvider.class);
    private final Map<ModelTask, Spinner> providerSpinners = new EnumMap<>(ModelTask.class);
    private final Map<ModelTask, Spinner> modelSpinners = new EnumMap<>(ModelTask.class);

    ModelPanelController(View root, Executor executor, PanelNavigator panelNav) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;
        this.providerList = root.findViewById(R.id.adminModelProviderList);
        this.taskList = root.findViewById(R.id.adminModelTaskList);
        root.findViewById(R.id.adminModelSave).setOnClickListener(v -> save());
    }

    void showModels() {
        DebugLog.get().d(TAG, "Modell-Panel wird geöffnet");
        try {
            buildProviderRows();
            buildTaskRows();
        } catch (Exception e) {
            DebugLog.get().e(TAG, "Aufbau des Modell-Panels fehlgeschlagen", e);
            Toast.makeText(ctx(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
        panelNav.show(PanelNavigator.PANEL_MODELS);
    }

    private void buildProviderRows() {
        providerList.removeAllViews();
        keyEdits.clear();
        statusViews.clear();
        for (LlmProvider provider : LlmProvider.values()) {
            LinearLayout card = AdminViewFactory.card(ctx());

            TextView name = new TextView(ctx());
            name.setText(provider.displayName);
            name.setTextColor(color(R.color.text_primary));
            name.setTextSize(17);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            card.addView(name);

            EditText key = new EditText(ctx());
            key.setHint(R.string.model_api_key_hint);
            key.setHintTextColor(color(R.color.text_muted));
            key.setTextColor(color(R.color.text_primary));
            key.setTextSize(15);
            key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            key.setSingleLine(true);
            key.setText(ModelSettings.getKey(ctx(), provider));
            LinearLayout.LayoutParams keyParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            keyParams.topMargin = dp(8);
            card.addView(key, keyParams);
            keyEdits.put(provider, key);

            LinearLayout actions = new LinearLayout(ctx());
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            actionParams.topMargin = dp(10);
            actions.setLayoutParams(actionParams);

            Button validate = new Button(ctx());
            validate.setText(R.string.model_validate);
            validate.setAllCaps(false);
            validate.setOnClickListener(v -> validate(provider));
            actions.addView(validate);

            TextView status = new TextView(ctx());
            status.setTextColor(color(R.color.text_muted));
            status.setTextSize(14);
            status.setPadding(dp(12), 0, 0, 0);
            boolean hasKey = ModelSettings.hasKey(ctx(), provider);
            status.setText(hasKey ? R.string.model_key_set : R.string.model_key_empty);
            actions.addView(status);
            statusViews.put(provider, status);

            card.addView(actions);
            providerList.addView(card);
        }
    }

    private void buildTaskRows() {
        taskList.removeAllViews();
        providerSpinners.clear();
        modelSpinners.clear();
        for (ModelTask task : ModelTask.values()) {
            LinearLayout card = AdminViewFactory.card(ctx());

            TextView name = new TextView(ctx());
            name.setText(taskLabel(task));
            name.setTextColor(color(R.color.text_primary));
            name.setTextSize(17);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            card.addView(name);

            TextView desc = new TextView(ctx());
            desc.setText(taskDescription(task));
            desc.setTextColor(color(R.color.text_muted));
            desc.setTextSize(13);
            LinearLayout.LayoutParams descParams = AdminViewFactory.rowParams();
            descParams.topMargin = dp(2);
            card.addView(desc, descParams);

            LlmProvider savedProvider = ModelSettings.getProvider(ctx(), task);
            String savedModelId = ModelSettings.getModel(ctx(), task);

            card.addView(AdminViewFactory.fieldLabel(ctx(), R.string.model_provider_label));
            Spinner providerSpinner = providerSpinner();
            providerSpinner.setSelection(savedProvider.ordinal());
            card.addView(providerSpinner, AdminViewFactory.rowParams());
            providerSpinners.put(task, providerSpinner);

            card.addView(AdminViewFactory.fieldLabel(ctx(), R.string.model_model_label));
            Spinner modelSpinner = AdminViewFactory.styledSpinner(ctx());
            card.addView(modelSpinner, AdminViewFactory.rowParams());
            modelSpinners.put(task, modelSpinner);

            TextView hint = new TextView(ctx());
            hint.setTextColor(color(R.color.buhler_teal));
            hint.setTextSize(13);
            LinearLayout.LayoutParams hintParams = AdminViewFactory.rowParams();
            hintParams.topMargin = dp(4);
            card.addView(hint, hintParams);

            fillModelSpinner(modelSpinner, hint, savedProvider, savedModelId);

            modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateHint(hint, parent.getSelectedItem());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    LlmProvider chosen = LlmProvider.values()[position];
                    if (chosen == modelSpinner.getTag()) {
                        return;
                    }
                    DebugLog.get().d(TAG, "Anbieter geändert: " + task + " → " + chosen.displayName);
                    fillModelSpinner(modelSpinner, hint, chosen, chosen.flagshipModel);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            taskList.addView(card);
        }
    }

    private void fillModelSpinner(Spinner spinner, TextView hint, LlmProvider provider, String preselectId) {
        List<LlmModel> models = new ArrayList<>(Arrays.asList(provider.models));
        int selection = 0;
        if (preselectId != null && !preselectId.isEmpty()) {
            int index = indexOfModel(models, preselectId);
            if (index < 0) {
                models.add(0, new LlmModel(preselectId, preselectId + " · (eigenes)",
                        "Manuell gesetztes Modell"));
                selection = 0;
            } else {
                selection = index;
            }
        }
        spinner.setAdapter(modelAdapter(models));
        spinner.setSelection(selection);
        spinner.setTag(provider);
        updateHint(hint, models.get(selection));
    }

    private void updateHint(TextView hint, Object model) {
        if (hint != null && model instanceof LlmModel) {
            hint.setText(((LlmModel) model).hint);
        }
    }

    private int indexOfModel(List<LlmModel> models, String id) {
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).id.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void validate(LlmProvider provider) {
        EditText edit = keyEdits.get(provider);
        TextView status = statusViews.get(provider);
        if (edit == null || status == null) {
            return;
        }
        String key = edit.getText().toString().trim();
        DebugLog.get().d(TAG, "Teste API-Key: " + provider.displayName);
        status.setTextColor(color(R.color.text_muted));
        status.setText(R.string.model_validating);
        executor.execute(() -> {
            ModelCatalog.Result result = ModelCatalog.validate(provider, key);
            root.post(() -> {
                if (result.ok) {
                    DebugLog.get().i(TAG, provider.displayName + ": Key gültig ("
                            + result.models.size() + " Modelle)");
                    status.setTextColor(color(R.color.status_ok));
                    status.setText(ctx().getString(R.string.model_valid, result.models.size()));
                } else {
                    DebugLog.get().w(TAG, provider.displayName + ": Key ungültig – "
                            + (result.error == null ? "Fehler" : result.error));
                    status.setTextColor(color(R.color.status_bad));
                    status.setText(ctx().getString(R.string.model_invalid,
                            result.error == null ? "Fehler" : result.error));
                }
            });
        });
    }

    private void save() {
        try {
            for (LlmProvider provider : LlmProvider.values()) {
                EditText edit = keyEdits.get(provider);
                if (edit != null) {
                    ModelSettings.setKey(ctx(), provider, edit.getText().toString().trim());
                }
            }
            for (ModelTask task : ModelTask.values()) {
                Spinner providerSpinner = providerSpinners.get(task);
                Spinner modelSpinner = modelSpinners.get(task);
                if (providerSpinner == null || modelSpinner == null) {
                    continue;
                }
                Object providerItem = providerSpinner.getSelectedItem();
                Object modelItem = modelSpinner.getSelectedItem();
                if (!(providerItem instanceof LlmProvider)) {
                    continue;
                }
                LlmProvider provider = (LlmProvider) providerItem;
                String modelId = modelItem instanceof LlmModel ? ((LlmModel) modelItem).id : "";
                ModelSettings.setChoice(ctx(), task, provider, modelId);
                DebugLog.get().i(TAG, "Gespeichert: " + task + " → "
                        + provider.displayName + " / " + modelId);
            }
            Toast.makeText(ctx(), R.string.model_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            DebugLog.get().e(TAG, "Speichern der Modell-Einstellungen fehlgeschlagen", e);
            Toast.makeText(ctx(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private Spinner providerSpinner() {
        Spinner spinner = AdminViewFactory.styledSpinner(ctx());
        spinner.setAdapter(providerAdapter());
        return spinner;
    }

    private ArrayAdapter<LlmProvider> providerAdapter() {
        return AdminViewFactory.whiteAdapter(ctx(), Arrays.asList(LlmProvider.values()));
    }

    private ArrayAdapter<LlmModel> modelAdapter(List<LlmModel> models) {
        return AdminViewFactory.whiteAdapter(ctx(), models);
    }

    private int color(int colorRes) {
        return AdminViewFactory.color(ctx(), colorRes);
    }

    private int dp(int value) {
        return AdminViewFactory.dp(ctx(), value);
    }

    private String taskLabel(ModelTask task) {
        switch (task) {
            case CONVERSATION:
                return ctx().getString(R.string.model_task_conversation);
            case CLASSIFICATION:
                return ctx().getString(R.string.model_task_classification);
            case REWRITE:
                return ctx().getString(R.string.model_task_rewrite);
            case GENERATION:
                return ctx().getString(R.string.model_task_generation);
            case DOCUMENTATION:
            default:
                return ctx().getString(R.string.model_task_documentation);
        }
    }

    private String taskDescription(ModelTask task) {
        switch (task) {
            case CONVERSATION:
                return ctx().getString(R.string.model_task_conversation_desc);
            case CLASSIFICATION:
                return ctx().getString(R.string.model_task_classification_desc);
            case REWRITE:
                return ctx().getString(R.string.model_task_rewrite_desc);
            case GENERATION:
                return ctx().getString(R.string.model_task_generation_desc);
            case DOCUMENTATION:
            default:
                return ctx().getString(R.string.model_task_documentation_desc);
        }
    }

    private Context ctx() {
        return root.getContext();
    }
}
