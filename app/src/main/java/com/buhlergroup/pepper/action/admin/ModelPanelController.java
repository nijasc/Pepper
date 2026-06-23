package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.llm.LlmProvider;
import com.buhlergroup.pepper.llm.ModelCatalog;
import com.buhlergroup.pepper.llm.ModelSettings;
import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

final class ModelPanelController {

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
        buildProviderRows();
        buildTaskRows();
        panelNav.show(PanelNavigator.PANEL_MODELS);
    }

    private void buildProviderRows() {
        providerList.removeAllViews();
        keyEdits.clear();
        statusViews.clear();
        for (LlmProvider provider : LlmProvider.values()) {
            LinearLayout row = verticalRow();

            TextView label = new TextView(ctx());
            label.setText(provider.displayName);
            label.setTextColor(ctx().getColor(R.color.white));
            label.setTextSize(16);
            row.addView(label);

            EditText key = new EditText(ctx());
            key.setHint(R.string.model_api_key_hint);
            key.setHintTextColor(ctx().getColor(R.color.text_muted));
            key.setTextColor(ctx().getColor(R.color.white));
            key.setText(ModelSettings.getKey(ctx(), provider));
            row.addView(key);
            keyEdits.put(provider, key);

            LinearLayout actions = new LinearLayout(ctx());
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(android.view.Gravity.CENTER_VERTICAL);

            Button validate = new Button(ctx());
            validate.setText(R.string.model_validate);
            validate.setOnClickListener(v -> validate(provider));
            actions.addView(validate);

            TextView status = new TextView(ctx());
            status.setTextColor(ctx().getColor(R.color.text_muted));
            status.setTextSize(13);
            status.setPadding(16, 0, 0, 0);
            actions.addView(status);
            statusViews.put(provider, status);

            row.addView(actions);
            providerList.addView(row);
        }
    }

    private void buildTaskRows() {
        taskList.removeAllViews();
        providerSpinners.clear();
        modelSpinners.clear();
        for (ModelTask task : ModelTask.values()) {
            LinearLayout row = verticalRow();

            TextView label = new TextView(ctx());
            label.setText(taskLabel(task));
            label.setTextColor(ctx().getColor(R.color.white));
            label.setTextSize(16);
            row.addView(label);

            Spinner providerSpinner = new Spinner(ctx());
            providerSpinner.setAdapter(stringAdapter(providerNames()));
            LlmProvider savedProvider = ModelSettings.getProvider(ctx(), task);
            providerSpinner.setSelection(savedProvider.ordinal());
            row.addView(providerSpinner);
            providerSpinners.put(task, providerSpinner);

            Spinner modelSpinner = new Spinner(ctx());
            row.addView(modelSpinner);
            modelSpinners.put(task, modelSpinner);
            populateModels(modelSpinner, savedProvider, ModelSettings.getModel(ctx(), task));

            providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    populateModels(modelSpinner, LlmProvider.values()[position], null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            taskList.addView(row);
        }
    }

    private void populateModels(Spinner spinner, LlmProvider provider, String preselect) {
        List<String> models = new ArrayList<>(ModelCatalog.models(provider));
        if (preselect != null && !preselect.isEmpty() && !models.contains(preselect)) {
            models.add(0, preselect);
        }
        spinner.setAdapter(stringAdapter(models));
        if (preselect != null) {
            int index = models.indexOf(preselect);
            spinner.setSelection(Math.max(index, 0));
        }
    }

    private void validate(LlmProvider provider) {
        EditText edit = keyEdits.get(provider);
        TextView status = statusViews.get(provider);
        if (edit == null || status == null) {
            return;
        }
        String key = edit.getText().toString().trim();
        status.setText(R.string.model_validating);
        executor.execute(() -> {
            ModelCatalog.Result result = ModelCatalog.validate(provider, key);
            root.post(() -> {
                if (result.ok) {
                    status.setText(ctx().getString(R.string.model_valid, result.models.size()));
                    refreshModelsForProvider(provider);
                } else {
                    status.setText(ctx().getString(R.string.model_invalid,
                            result.error == null ? "Fehler" : result.error));
                }
            });
        });
    }

    private void refreshModelsForProvider(LlmProvider provider) {
        for (ModelTask task : ModelTask.values()) {
            Spinner providerSpinner = providerSpinners.get(task);
            Spinner modelSpinner = modelSpinners.get(task);
            if (providerSpinner == null || modelSpinner == null) {
                continue;
            }
            if (LlmProvider.values()[providerSpinner.getSelectedItemPosition()] == provider) {
                Object current = modelSpinner.getSelectedItem();
                populateModels(modelSpinner, provider, current == null ? null : current.toString());
            }
        }
    }

    private void save() {
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
            LlmProvider provider = LlmProvider.values()[providerSpinner.getSelectedItemPosition()];
            Object model = modelSpinner.getSelectedItem();
            ModelSettings.setChoice(ctx(), task, provider, model == null ? "" : model.toString());
        }
        Toast.makeText(ctx(), R.string.model_saved, Toast.LENGTH_SHORT).show();
        panelNav.show(PanelNavigator.PANEL_MENU);
    }

    private ArrayAdapter<String> stringAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ctx(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private List<String> providerNames() {
        List<String> names = new ArrayList<>();
        for (LlmProvider provider : LlmProvider.values()) {
            names.add(provider.displayName);
        }
        return names;
    }

    private LinearLayout verticalRow() {
        LinearLayout row = new LinearLayout(ctx());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 8, 0, 16);
        return row;
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

    private Context ctx() {
        return root.getContext();
    }
}
