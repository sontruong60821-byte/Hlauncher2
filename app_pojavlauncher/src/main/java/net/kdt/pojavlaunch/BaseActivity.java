package net.kdt.pojavlaunch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.LocaleUtils;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_IGNORE_NOTCH;

public abstract class BaseActivity extends AppCompatActivity {

    private final SharedPreferences.OnSharedPreferenceChangeListener mThemeListener = (prefs, key) -> {
        if ("appTheme".equals(key)) {
            applyAppTheme();
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Tools.checkStorageRoot(this)) {
            LauncherPreferences.loadPreferences(this);
        }

        applyAppTheme();
        super.onCreate(savedInstanceState);
        LocaleUtils.setLocale(this);
        applyWindowSettings();
        Tools.getDisplayMetrics(this);

        if (LauncherPreferences.DEFAULT_PREF != null) {
            LauncherPreferences.DEFAULT_PREF.registerOnSharedPreferenceChangeListener(mThemeListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (LauncherPreferences.DEFAULT_PREF != null) {
            LauncherPreferences.DEFAULT_PREF.unregisterOnSharedPreferenceChangeListener(mThemeListener);
        }
    }

    protected void applyAppTheme() {
        String theme = LauncherPreferences.PREF_APP_THEME;
        int mode;
        if ("dark".equals(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if ("light".equals(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyWindowSettings();
    }

    @Override
    public void startActivity(Intent i) {
        super.startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tools.checkStorageInteractive(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        applyWindowSettings();
        Tools.getDisplayMetrics(this);
    }

    private void applyWindowSettings() {
        boolean fullscreen  = setFullscreen();
        boolean ignoreNotch = shouldIgnoreNotch();

        Window window = getWindow();

        if (fullscreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            if (ignoreNotch) {

                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            }
            window.setAttributes(lp);
        }

        WindowCompat.setDecorFitsSystemWindows(window, !fullscreen);

        View decorView = window.getDecorView();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);

        if (fullscreen) {

            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                          | View.SYSTEM_UI_FLAG_FULLSCREEN
                          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    /** @return Whether the activity should be fullscreen */
    public boolean setFullscreen() {
        return true;
    }

    /** @return Whether the notch area should be used */
    protected boolean shouldIgnoreNotch() {
        return PREF_IGNORE_NOTCH;
    }

    public static Bitmap getBackgroundBitmap() {
        return null;
    }
}
