package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.ashmeet.hyperlauncher.R;

public abstract class WebViewCompletionFragment extends Fragment {
    private final String mTrackedUrl;
    private final String mAuthUrl;
    private WebView mWebview;

    private boolean mBlankClient = true;
    private boolean mIsCompleted = false;

    protected WebViewCompletionFragment(String mTrackedUrl, String mAuthUrl) {
        this.mTrackedUrl = mTrackedUrl;
        this.mAuthUrl = mAuthUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mWebview = (WebView) inflater.inflate(R.layout.fragment_microsoft_login, container, false);
        setWebViewSettings();
        if(savedInstanceState == null) startNewSession();
        else restoreWebViewState(savedInstanceState);
        return mWebview;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewSettings() {
        WebSettings settings = mWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewTrackClient());
        mBlankClient = false;
    }

    private void startNewSession() {
        CookieManager.getInstance().removeAllCookies((b)->{
            mWebview.clearHistory();
            mWebview.clearCache(true);
            mWebview.clearFormData();
            mWebview.clearHistory();
            mWebview.loadUrl(mAuthUrl);
        });
    }

    private void restoreWebViewState(Bundle savedInstanceState) {
        Log.i("MSAuthFragment","Restoring state...");
        if(mWebview.restoreState(savedInstanceState) == null) {
            Log.w("MSAuthFragment", "Failed to restore state, starting afresh");

            startNewSession();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mBlankClient) mWebview.setWebViewClient(new WebViewTrackClient());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        mWebview.setWebViewClient(new WebViewClient());

        mBlankClient = true;
        super.onSaveInstanceState(outState);
        mWebview.saveState(outState);
    }

    /* Expose webview actions to others */
    public boolean canGoBack(){ return mWebview.canGoBack();}
    public void goBack(){ mWebview.goBack();}

    /** Client to track when to sent the data to the launcher */
    class WebViewTrackClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith(mTrackedUrl)) {
                internalSignalCompletion(url);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {}

        @Override
        public void onPageFinished(WebView view, String url) {
            if(url.startsWith(mTrackedUrl)) {
                internalSignalCompletion(url);
            }
        }
    }

    private void internalSignalCompletion(String fullUrl) {
        if(mIsCompleted) return;
        mIsCompleted = true;
        signalCompletion(fullUrl);
    }

    protected abstract void signalCompletion(String fullUrl);
}
