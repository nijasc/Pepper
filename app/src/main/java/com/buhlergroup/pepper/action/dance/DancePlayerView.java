package com.buhlergroup.pepper.action.dance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

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

    public void play(String videoId) {
        post(() -> {
            webView.loadDataWithBaseURL(BASE_ORIGIN, buildEmbedHtml(videoId),
                    "text/html", "utf-8", null);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    private String buildEmbedHtml(String videoId) {
        String src = "https://www.youtube-nocookie.com/embed/" + videoId
                + "?autoplay=1&playsinline=1&controls=0&rel=0&enablejsapi=1&origin=" + BASE_ORIGIN;
        return "<!DOCTYPE html><html><head>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<style>html,body{margin:0;padding:0;background:#000;width:100%;height:100%;overflow:hidden}"
                + "iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0}</style>"
                + "</head><body>"
                + "<iframe src=\"" + src + "\" "
                + "allow=\"autoplay; encrypted-media\" "
                + "referrerpolicy=\"strict-origin-when-cross-origin\" "
                + "allowfullscreen></iframe>"
                + "</body></html>";
    }

    public void stop() {
        post(() -> {
            webView.loadUrl("about:blank");
            setVisibility(GONE);
        });
    }
}
