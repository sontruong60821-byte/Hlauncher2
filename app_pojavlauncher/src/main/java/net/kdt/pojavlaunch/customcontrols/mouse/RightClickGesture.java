package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;

import net.kdt.pojavlaunch.CallbackBridge;

public class RightClickGesture extends DistanceGesture {
    private boolean mGestureEnabled = true;
    private boolean mGestureValid = true;

    public RightClickGesture(Handler mHandler) {
        super(mHandler);
    }

    @Override
    void onGestureSubmitted() {
        mGestureEnabled = false;
        mGestureValid = true;
    }

    @Override
    boolean shouldSubmitGesture() {
        return mGestureEnabled;
    }

    @Override
    protected int getGestureDelay() {
        return 150;
    }

    @Override
    public boolean checkAndTrigger() {

        mGestureValid = false;

        return true;
    }

    @Override
    public void onGestureCancelled(boolean isSwitching) {
        mGestureEnabled = true;
        if(!mGestureValid || isSwitching) return;
        boolean fingerStill = travelBelowThreshold(LeftClickGesture.FINGER_STILL_THRESHOLD);
        System.out.println("Right click: " + fingerStill);
        if(!fingerStill) return;
        CallbackBridge.sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT, true);
        CallbackBridge.sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT, false);
    }
}
