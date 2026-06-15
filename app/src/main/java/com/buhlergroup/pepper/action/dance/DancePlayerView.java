package com.buhlergroup.pepper.action.dance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.util.List;

public class DancePlayerView extends FrameLayout {

    private WebView webView;

    public DancePlayerView(Context context) {
        super(context);
        init(context);
    }

    public DancePlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DancePlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private static final String BASE_ORIGIN = "https://www.youtube.com";

    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        setBackgroundColor(0xFF000000);
        webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);
        String userAgent = settings.getUserAgentString();
        if (userAgent != null && userAgent.contains("; wv")) {
            settings.setUserAgentString(userAgent.replace("; wv", ""));
        }
        webView.setWebChromeClient(new WebChromeClient());
        addView(webView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setVisibility(GONE);
    }

    public void play(List<String> videoIds, int startSeconds) {
        post(() -> {
            webView.loadDataWithBaseURL(BASE_ORIGIN, buildPlayerHtml(videoIds, startSeconds),
                    "text/html", "utf-8", null);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    private String buildPlayerHtml(List<String> videoIds, int startSeconds) {
        StringBuilder ids = new StringBuilder("[");
        for (int i = 0; i < videoIds.size(); i++) {
            if (i > 0) {
                ids.append(',');
            }
            ids.append('"').append(videoIds.get(i).replaceAll("[^\\w-]", "")).append('"');
        }
        ids.append(']');
        int start = Math.max(0, startSeconds);
        return "<!DOCTYPE html><html><head>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<style>html,body{margin:0;padding:0;background:#000;width:100%;height:100%;overflow:hidden}"
                + "#player{position:absolute;top:0;left:0;width:100%;height:100%}</style>"
                + "</head><body>"
                + "<div id=\"player\"></div>"
                + "<script src=\"https://www.youtube.com/iframe_api\"></script>"
                + "<script>"
                + "var ids=" + ids + ";var startAt=" + start + ";var idx=0;var player;"
                + "function onYouTubeIframeAPIReady(){"
                + "player=new YT.Player('player',{width:'100%',height:'100%',videoId:ids[0],"
                + "playerVars:{autoplay:1,controls:0,rel:0,playsinline:1,modestbranding:1,"
                + "start:startAt,origin:'" + BASE_ORIGIN + "'},"
                + "events:{onReady:function(e){e.target.playVideo();},"
                + "onError:function(e){idx++;if(idx<ids.length){"
                + "player.loadVideoById({videoId:ids[idx],startSeconds:startAt});}}}});}"
                + "</script>"
                + "</body></html>";
    }

    public void stop() {
        post(() -> {
            webView.loadUrl("about:blank");
            setVisibility(GONE);
        });
    }
}
