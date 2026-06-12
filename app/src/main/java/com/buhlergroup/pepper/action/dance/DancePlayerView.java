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

    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        setBackgroundColor(0xFF000000);
        webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        addView(webView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setVisibility(GONE);
    }

    public void play(String videoId) {
        post(() -> {
            String url = "https://www.youtube.com/embed/" + videoId
                    + "?autoplay=1&playsinline=1&controls=0&rel=0";
            webView.loadUrl(url);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    public void stop() {
        post(() -> {
            webView.loadUrl("about:blank");
            setVisibility(GONE);
        });
    }
}
