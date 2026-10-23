package net.kdt.pojavlaunch.prefs;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_DISABLE_GESTURES;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ENABLE_GYRO;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_GYRO_INVERT_X;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_GYRO_INVERT_Y;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_GYRO_SENSITIVITY;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_LONGPRESS_TRIGGER;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_MOUSESPEED;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_SCALE_FACTOR;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.compose.ui.platform.ComposeView;

import com.kdt.SideDialogView;

import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.ui.screens.QuickSettingComposeBridge;

/**
 * Side dialog for quick settings that you can change in game
 * The implementation has to take action on some preference changes
 */
public abstract class QuickSettingSideDialog extends SideDialogView {

    private final ComposeView mComposeView;
    private final boolean mOriginalGyroEnabled, mOriginalGyroXEnabled, mOriginalGyroYEnabled, mOriginalGestureDisabled;
    private final float mOriginalGyroSensitivity, mOriginalMouseSpeed, mOriginalResolution;
    private final int mOriginalGestureDelay;

    public QuickSettingSideDialog(Context context, ViewGroup parent) {
        this(context, parent, createComposeView(context));
    }

    private QuickSettingSideDialog(Context context, ViewGroup parent, ComposeView composeView) {
        super(context, parent, composeView);
        mComposeView = composeView;

        mOriginalGyroEnabled = PREF_ENABLE_GYRO;
        mOriginalGyroXEnabled = PREF_GYRO_INVERT_X;
        mOriginalGyroYEnabled = PREF_GYRO_INVERT_Y;
        mOriginalGestureDisabled = PREF_DISABLE_GESTURES;

        mOriginalGyroSensitivity = PREF_GYRO_SENSITIVITY;
        mOriginalMouseSpeed = PREF_MOUSESPEED;
        mOriginalGestureDelay = PREF_LONGPRESS_TRIGGER;
        mOriginalResolution = PREF_SCALE_FACTOR;
    }

    private static ComposeView createComposeView(Context context) {
        ComposeView view = new ComposeView(context);
        view.setBackgroundColor(Color.TRANSPARENT);
        return view;
    }

    @Override
    protected void onInflate() {
        QuickSettingComposeBridge.setContent(
                mComposeView,
                this::onResolutionChanged,
                this::onGyroStateChanged
        );

        setStartButtonListener(android.R.string.cancel, v -> cancel());
        setEndButtonListener(android.R.string.ok, v -> {
            if (LauncherPreferences.DEFAULT_PREF != null) {
                LauncherPreferences.DEFAULT_PREF.edit().apply();
            }
            disappear(true);
        });
    }

    /** Resets all settings to their original values */
    public void cancel() {

        if(isDisplaying()) {
            PREF_ENABLE_GYRO = mOriginalGyroEnabled;
            PREF_GYRO_INVERT_X = mOriginalGyroXEnabled;
            PREF_GYRO_INVERT_Y = mOriginalGyroYEnabled;
            PREF_DISABLE_GESTURES = mOriginalGestureDisabled;

            PREF_GYRO_SENSITIVITY = mOriginalGyroSensitivity;
            PREF_MOUSESPEED = mOriginalMouseSpeed;
            PREF_LONGPRESS_TRIGGER = mOriginalGestureDelay;
            PREF_SCALE_FACTOR = mOriginalResolution;

            if (LauncherPreferences.DEFAULT_PREF != null) {
                LauncherPreferences.DEFAULT_PREF.edit()
                        .putBoolean("enableGyro", PREF_ENABLE_GYRO)
                        .putBoolean("gyroInvertX", PREF_GYRO_INVERT_X)
                        .putBoolean("gyroInvertY", PREF_GYRO_INVERT_Y)
                        .putBoolean("disableGestures", PREF_DISABLE_GESTURES)
                        .putInt("gyroSensitivity", (int)(PREF_GYRO_SENSITIVITY * 100))
                        .putInt("mousespeed", (int)(PREF_MOUSESPEED * 100))
                        .putInt("timeLongPressTrigger", PREF_LONGPRESS_TRIGGER)
                        .putInt("resolutionRatio", (int)(PREF_SCALE_FACTOR * 100))
                        .apply();
            }

            onGyroStateChanged();
            onResolutionChanged();
        }

        disappear(true);
    }

    /** Called when the resolution is changed. Use {@link LauncherPreferences#PREF_SCALE_FACTOR} */
    public abstract void onResolutionChanged();

    /** Called when the gyro state is changed.
     * Use {@link LauncherPreferences#PREF_ENABLE_GYRO}
     * Use {@link LauncherPreferences#PREF_GYRO_INVERT_X}
     * Use {@link LauncherPreferences#PREF_GYRO_INVERT_Y}
     */
    public abstract void onGyroStateChanged();

}
