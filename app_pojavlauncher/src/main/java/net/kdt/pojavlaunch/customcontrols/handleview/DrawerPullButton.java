package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

public class DrawerPullButton extends View implements SharedPreferences.OnSharedPreferenceChangeListener {
    public DrawerPullButton(Context context) {super(context); init();}
    public DrawerPullButton(Context context, @Nullable AttributeSet attrs) {super(context, attrs); init();}

    private final Paint mBackgroundPaint = new Paint();
    private Drawable mDrawable;
    private float mInitialX, mInitialY;
    private float mInitialTouchX, mInitialTouchY;

    private void init(){
        updateProperties();
        if (LauncherPreferences.DEFAULT_PREF != null) {
            LauncherPreferences.DEFAULT_PREF.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (LauncherPreferences.DEFAULT_PREF != null) {
            LauncherPreferences.DEFAULT_PREF.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    public void updateProperties() {
        if (LauncherPreferences.PREF_DRAWER_BUTTON_ICON_PATH != null) {
            try {
                mDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(LauncherPreferences.PREF_DRAWER_BUTTON_ICON_PATH));
            } catch (Exception e) {
                mDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_sharp_settings_24, null);
            }
        } else {
            mDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_sharp_settings_24, null);
        }

        setAlpha(LauncherPreferences.PREF_DRAWER_BUTTON_ICON_OPACITY);
        mBackgroundPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, (int) (LauncherPreferences.PREF_DRAWER_BUTTON_BG_OPACITY * 255)));
        mBackgroundPaint.setAntiAlias(true);

        int size = (int) (LauncherPreferences.PREF_DRAWER_BUTTON_SIZE * getResources().getDisplayMetrics().density);

        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(size, size);
        } else {
            params.width = size;
            params.height = size;
        }
        setLayoutParams(params);

        updatePosition();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int size = (int) (LauncherPreferences.PREF_DRAWER_BUTTON_SIZE * getResources().getDisplayMetrics().density);
        setMeasuredDimension(size, size);
    }

    private void updatePosition() {
        post(() -> {
            if (!(getParent() instanceof View)) return;
            View parent = (View) getParent();
            int parentWidth = parent.getWidth();
            int parentHeight = parent.getHeight();

            float x = (LauncherPreferences.PREF_DRAWER_BUTTON_X / 100f) * (parentWidth - getWidth());
            float y = (LauncherPreferences.PREF_DRAWER_BUTTON_Y / 100f) * (parentHeight - getHeight());

            setX(x);
            setY(y);
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!LauncherPreferences.PREF_DRAWER_BUTTON_MOVABLE) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = getX();
                mInitialY = getY();
                mInitialTouchX = event.getRawX();
                mInitialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - mInitialTouchX;
                float dy = event.getRawY() - mInitialTouchY;

                float newX = mInitialX + dx;
                float newY = mInitialY + dy;

                View parent = (View) getParent();
                newX = Math.max(0, Math.min(newX, parent.getWidth() - getWidth()));
                newY = Math.max(0, Math.min(newY, parent.getHeight() - getHeight()));

                setX(newX);
                setY(newY);

                LauncherPreferences.PREF_DRAWER_BUTTON_X = (newX / (parent.getWidth() - getWidth())) * 100f;
                LauncherPreferences.PREF_DRAWER_BUTTON_Y = (newY / (parent.getHeight() - getHeight())) * 100f;
                LauncherPreferences.PREF_DRAWER_BUTTON_PRESET = "custom";

                return true;
            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getRawX() - mInitialTouchX) < 10 && Math.abs(event.getRawY() - mInitialTouchY) < 10) {
                    performClick();
                } else {
                    LauncherPreferences.DEFAULT_PREF.edit()
                        .putInt("drawerButtonX", (int) LauncherPreferences.PREF_DRAWER_BUTTON_X)
                        .putInt("drawerButtonY", (int) LauncherPreferences.PREF_DRAWER_BUTTON_Y)
                        .putString("drawerButtonPreset", "custom")
                        .apply();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = LauncherPreferences.PREF_DRAWER_BUTTON_CORNER_RADIUS * getResources().getDisplayMetrics().density;
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius, mBackgroundPaint);

        if (mDrawable != null) {

            int iconPadding = getWidth() / 10;
            mDrawable.setBounds(iconPadding, iconPadding, getWidth() - iconPadding, getHeight() - iconPadding);
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.startsWith("drawerButton")) {
            LauncherPreferences.loadPreferences(getContext());
            updateProperties();
        }
    }
}
