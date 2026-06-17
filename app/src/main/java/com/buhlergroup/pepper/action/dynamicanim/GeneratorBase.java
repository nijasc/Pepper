package com.buhlergroup.pepper.action.dynamicanim;

import android.util.Log;

import com.buhlergroup.pepper.openai.ModelSelector;
import com.buhlergroup.pepper.openai.OpenAIService;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

abstract class GeneratorBase {

    protected static final String TAG = "DynAnim";
    protected static final String MODEL =
            ModelSelector.modelFor(ModelSelector.ModelTask.GENERATION);
    protected static final int MAX_SECONDS = 30;

    protected final OpenAIService openAi = OpenAIService.shared();

    protected Map<String, String> message(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    protected String postProcess(Document doc, String original, boolean normalizeBody) {
        try {
            QianimLooper.expand(doc);
            if (normalizeBody) {
                QianimPostProcessor.normalizeBodyJoints(doc);
            }
            QianimPostProcessor.ensureTangents(doc);
            return XmlUtils.serialize(doc);
        } catch (Exception e) {
            Log.w(TAG, "Post-processing failed, using validated qianim: " + e.getMessage());
            return original;
        }
    }
}
