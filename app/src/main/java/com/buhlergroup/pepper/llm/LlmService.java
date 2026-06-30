package com.buhlergroup.pepper.llm;

import android.content.Context;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.openai.OpenAIService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LlmService {

    void setContext(Context context);

    String chat(ModelTask task, Map<String, Object> body) throws IOException;

    String chat(ModelTask task, Map<String, Object> body, int timeoutMs) throws IOException;

    String chatStrongest(ModelTask task, Map<String, Object> body, int timeoutMs) throws IOException;

    String generate(ModelTask task, String systemInstructions, String userInput, int maxTokens)
            throws IOException;

    @Nullable
    String streamChat(ModelTask task, List<Map<String, String>> messages, int maxTokens,
                      OpenAIService.StreamListener listener) throws IOException;
}
