package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Build;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.RequiresApi;

import net.kdt.pojavlaunch.MinecraftGLSurface;
import net.kdt.pojavlaunch.Tools;

import net.kdt.pojavlaunch.CallbackBridge;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import git.artdeell.dnbootstrap.glfw.GLFW;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AndroidPointerCapture implements ViewTreeObserver.OnWindowFocusChangeListener, View.OnCapturedPointerListener {
    private static final float TOUCHPAD_SCROLL_THRESHOLD = 1;
    private final View mTouchpadView;
    private final View mHostView;
    private final float mMousePrescale = Tools.dpToPx(1);
    private final PointerTracker mPointerTracker = new PointerTracker();
    private final Scroller mScroller = new Scroller(TOUCHPAD_SCROLL_THRESHOLD);
    private final float[] mVector = mPointerTracker.getMotionVector();

    private int mInputDeviceIdentifier;
    private boolean mDeviceSupportsRelativeAxis;

    public AndroidPointerCapture(View touchpad, View hostView) {
        this.mTouchpadView = touchpad;
        this.mHostView = hostView;
        hostView.setOnCapturedPointerListener(this);
        hostView.getViewTreeObserver().addOnWindowFocusChangeListener(this);
    }

    private void enableTouchpadIfNecessary() {
        if(mTouchpadView.getVisibility() != View.VISIBLE) mTouchpadView.setVisibility(View.VISIBLE);
    }

    public void handleAutomaticCapture() {
        if(!LauncherPreferences.PREF_ENABLE_PHYSICAL_MOUSE) return;
        if(!mHostView.hasWindowFocus()) {
            mHostView.requestFocus();
        } else {
            mHostView.requestPointerCapture();
        }
    }

    private void accumulateHistoricalValues(MotionEvent motionEvent, int axisX, int axisY) {
        float relX = motionEvent.getAxisValue(axisX),
                relY = motionEvent.getAxisValue(axisY);

        if(motionEvent.getHistorySize() > 1) for(int i = 0; i < motionEvent.getHistorySize(); i++) {
            relX += motionEvent.getHistoricalAxisValue(axisX, i);
            relY += motionEvent.getHistoricalAxisValue(axisY, i);
        }

        mVector[0] = relX;
        mVector[1] = relY;
    }

    @Override
    public boolean onCapturedPointer(View view, MotionEvent event) {
        checkSameDevice(event.getDevice());

        if((event.getSource() & InputDevice.SOURCE_CLASS_TRACKBALL) != 0) {

            if(mDeviceSupportsRelativeAxis) {

                accumulateHistoricalValues(event, MotionEvent.AXIS_RELATIVE_X, MotionEvent.AXIS_RELATIVE_Y);
            }else {

                accumulateHistoricalValues(event, MotionEvent.AXIS_X, MotionEvent.AXIS_Y);
            }
        }else {

            mPointerTracker.trackEvent(event);

        }

        if(!GLFW.isGrabbing()) {
            enableTouchpadIfNecessary();

            mVector[0] *= mMousePrescale;
            mVector[1] *= mMousePrescale;
            if(event.getPointerCount() < 2) {
                applyMotionVector(view, LauncherPreferences.PREF_MOUSESPEED);
                mScroller.resetScrollOvershoot();
            } else {
                mScroller.performScroll(mVector);
            }
        } else {

            applyMotionVector(view, 1);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_BUTTON_PRESS:
                return MinecraftGLSurface.sendMouseButtonUnconverted(event.getActionButton(), true);
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return MinecraftGLSurface.sendMouseButtonUnconverted(event.getActionButton(), false);
            case MotionEvent.ACTION_SCROLL:
                CallbackBridge.sendScroll(
                        event.getAxisValue(MotionEvent.AXIS_HSCROLL),
                        event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                );
                return true;
            case MotionEvent.ACTION_UP:
                mPointerTracker.cancelTracking();
                return true;
            default:
                return false;
        }
    }

    private void applyMotionVector(View view, float speed) {
        GLFW.cursorX += mVector[0] * speed / view.getWidth();
        GLFW.cursorY += mVector[1] * speed / view.getHeight();
        GLFW.sendMousePos();
    }

    private void checkSameDevice(InputDevice inputDevice) {
        int newIdentifier;
        if(inputDevice != null) newIdentifier = inputDevice.getId();
        else newIdentifier = Integer.MAX_VALUE;
        if(mInputDeviceIdentifier != newIdentifier) {
            reinitializeDeviceSpecificProperties(inputDevice);
            mInputDeviceIdentifier = newIdentifier;
        }
    }

    private void reinitializeDeviceSpecificProperties(InputDevice inputDevice) {
        mPointerTracker.cancelTracking();
        if(inputDevice == null) {
            mDeviceSupportsRelativeAxis = false;
            return;
        }
        boolean relativeXSupported = inputDevice.getMotionRange(MotionEvent.AXIS_RELATIVE_X) != null;
        boolean relativeYSupported = inputDevice.getMotionRange(MotionEvent.AXIS_RELATIVE_Y) != null;
        mDeviceSupportsRelativeAxis = relativeXSupported && relativeYSupported;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus && Tools.isAndroid8OrHigher() && LauncherPreferences.PREF_ENABLE_PHYSICAL_MOUSE) {
            mHostView.requestPointerCapture();
        }
    }

    public void detach() {
        mHostView.setOnCapturedPointerListener(null);
        mHostView.getViewTreeObserver().removeOnWindowFocusChangeListener(this);
    }
}
