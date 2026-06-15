package com.buhlergroup.pepper.action.dance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.util.List;

public class DancePlayerView extends FrameLayout {

    private static final String TAG = "DancePlayer";

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

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
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
        Log.i(TAG, "WebView user-agent: " + settings.getUserAgentString());
        webView.addJavascriptInterface(new JsBridge(), "PepperDance");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                Log.i(TAG, "[console " + message.messageLevel() + "] " + message.message()
                        + " (" + message.sourceId() + ":" + message.lineNumber() + ")");
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                Log.w(TAG, "[net error " + error.getErrorCode() + "] " + error.getDescription()
                        + " -> " + request.getUrl()
                        + (request.isForMainFrame() ? " [main]" : ""));
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            WebResourceResponse response) {
                Log.w(TAG, "[net http " + response.getStatusCode() + "] " + request.getUrl()
                        + (request.isForMainFrame() ? " [main]" : ""));
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.w(TAG, "[net ssl error " + error.getPrimaryError() + "] on " + error.getUrl());
                handler.cancel();
            }
        });
        addView(webView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setVisibility(GONE);
    }

    public void play(List<String> videoIds, int startSeconds) {
        Log.i(TAG, "play() candidates=" + videoIds + " startSeconds=" + startSeconds);
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
                + "<script>"
                + "function plog(l,m){try{PepperDance.log(l,String(m));}catch(e){}}"
                + "window.onerror=function(m,s,l){plog('error','window.onerror: '+m+' @'+s+':'+l);return false;};"
                + "var ids=" + ids + ";var startAt=" + start + ";var idx=0;var player;"
                + "plog('info','page init, candidates='+ids.length+', startAt='+startAt);"
                + "function onYouTubeIframeAPIReady(){"
                + "plog('info','iframe_api ready, loading '+ids[0]);"
                + "player=new YT.Player('player',{width:'100%',height:'100%',videoId:ids[0],"
                + "playerVars:{autoplay:1,controls:0,rel:0,playsinline:1,modestbranding:1,"
                + "start:startAt,origin:'" + BASE_ORIGIN + "'},"
                + "events:{"
                + "onReady:function(e){plog('info','onReady, playing '+ids[idx]);e.target.playVideo();},"
                + "onStateChange:function(e){plog('info','state='+e.data);},"
                + "onError:function(e){plog('error','onError code='+e.data+' id='+ids[idx]);"
                + "idx++;if(idx<ids.length){plog('info','falling back to '+ids[idx]);"
                + "player.loadVideoById({videoId:ids[idx],startSeconds:startAt});}"
                + "else{plog('error','all '+ids.length+' candidate(s) failed');}}"
                + "}});}"
                + "setTimeout(function(){if(typeof YT==='undefined'||!window.YT||!YT.Player){"
                + "plog('error','YT not initialized after 8s (iframe_api blocked or WebView too old)');}},8000);"
                + "</script>"
                + "<script src=\"https://www.youtube.com/iframe_api\"></script>"
                + "</body></html>";
    }

    public void stop() {
        post(() -> {
            webView.loadUrl("about:blank");
            setVisibility(GONE);
        });
    }

    private final class JsBridge {
        @JavascriptInterface
        public void log(String level, String message) {
            if ("error".equals(level)) {
                Log.w(TAG, "[js] " + message);
            } else {
                Log.i(TAG, "[js] " + message);
            }
        }
    }
}
