package com.buhlergroup.pepper.action.dynamicanim;

public final class SongPlan {
    public final String query;
    public final int startSeconds;
    public final String mood;

    public SongPlan(String query, int startSeconds, String mood) {
        this.query = query;
        this.startSeconds = startSeconds;
        this.mood = mood;
    }
}
