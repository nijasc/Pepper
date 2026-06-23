package com.buhlergroup.pepper.llm;

import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;

import java.io.IOException;
import java.util.Map;

public interface LlmService {

    String chat(ModelTask task, Map<String, Object> body) throws IOException;

    String generate(ModelTask task, String systemInstructions, String userInput, int maxTokens)
            throws IOException;
}
