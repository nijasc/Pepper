package com.buhlergroup.pepper.llm;

import java.util.HashMap;
import java.util.Map;

public final class ChatMessages {

    private ChatMessages() {
    }

    public static Map<String, String> of(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }
}
