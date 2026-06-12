package com.buhlergroup.pepper.action.dance.youtube;

import android.util.Log;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public final class YoutubeAudioDownloader {

    private static final String TAG = "YoutubeAudio";

    private static volatile boolean initialized;

    public static final class Result {
        public final String youtubeId;
        public final String title;
        public final File file;
        public final long durationMs;

        Result(String youtubeId, String title, File file, long durationMs) {
            this.youtubeId = youtubeId;
            this.title = title;
            this.file = file;
            this.durationMs = durationMs;
        }
    }

    private static synchronized void ensureInitialized() {
        if (!initialized) {
            NewPipe.init(NewPipeDownloader.getInstance());
            initialized = true;
        }
    }

    public Result download(String query, File targetDir) throws Exception {
        ensureInitialized();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        StreamingService youtube = ServiceList.YouTube;
        String searchUrl = youtube.getSearchQHFactory().fromQuery(query).getUrl();
        SearchInfo searchInfo = SearchInfo.getInfo(youtube, searchUrl);

        StreamInfoItem video = null;
        for (InfoItem item : searchInfo.getRelatedItems()) {
            if (item instanceof StreamInfoItem) {
                video = (StreamInfoItem) item;
                break;
            }
        }
        if (video == null) {
            throw new IOException("Kein passender Song auf YouTube gefunden.");
        }

        StreamInfo streamInfo = StreamInfo.getInfo(youtube, video.getUrl());
        AudioStream best = pickBestAudio(streamInfo.getAudioStreams());
        if (best == null) {
            throw new IOException("Kein abspielbarer Audio-Stream verfügbar.");
        }

        String videoId = streamInfo.getId();
        String suffix = best.getFormat() != null ? best.getFormat().getSuffix() : "m4a";
        File output = new File(targetDir, videoId + "." + suffix);
        downloadToFile(best.getContent(), output);

        long durationMs = streamInfo.getDuration() * 1000L;
        return new Result(videoId, streamInfo.getName(), output, durationMs);
    }

    private AudioStream pickBestAudio(List<AudioStream> streams) {
        AudioStream best = null;
        if (streams == null) {
            return null;
        }
        for (AudioStream stream : streams) {
            if (!stream.isUrl()) {
                continue;
            }
            if (best == null || stream.getAverageBitrate() > best.getAverageBitrate()) {
                best = stream;
            }
        }
        return best;
    }

    private void downloadToFile(String url, File output) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Download fehlgeschlagen (HTTP " + code + ").");
            }
            try (InputStream in = connection.getInputStream();
                 OutputStream out = new FileOutputStream(output)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            Log.i(TAG, "Downloaded audio to " + output.getAbsolutePath());
        } finally {
            connection.disconnect();
        }
    }
}
