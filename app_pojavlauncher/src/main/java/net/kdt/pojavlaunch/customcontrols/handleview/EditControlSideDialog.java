package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.compose.ui.platform.ComposeView;

import com.kdt.SideDialogView;

import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.colorselector.ColorSelector;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlDrawerData;
import net.kdt.pojavlaunch.customcontrols.LayoutBitmaps;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlInterface;
import net.kdt.pojavlaunch.ui.screens.EditControlComposeBridge;
import net.kdt.pojavlaunch.utils.CropperUtils;

public class EditControlSideDialog extends SideDialogView {

    public boolean internalChanges = false;
    private ControlInterface mCurrentlyEditedButton;
    private ColorSelector mColorSelector;
    private final ViewGroup mParent;
    private final ComposeView mComposeView;

    public EditControlSideDialog(Context context, ViewGroup parent) {
        this(context, parent, createComposeView(context));
    }

    private static ComposeView createComposeView(Context context) {
        ComposeView view = new ComposeView(context);
        view.setBackgroundColor(Color.TRANSPARENT);
        return view;
    }

    private EditControlSideDialog(Context context, ViewGroup parent, ComposeView composeView) {
        super(context, parent, composeView);
        mParent = parent;
        mComposeView = composeView;
    }

    @Override
    protected void onInflate() {
        buildColorSelector();

        setStartButtonListener(android.R.string.cancel, v -> disappear(true));
        setEndButtonListener(R.string.global_save, v -> disappear(true));
    }

    @Override
    protected void onDestroy() {
        if (mColorSelector != null) mColorSelector.disappear(true);
    }

    private void buildColorSelector() {
        mColorSelector = new ColorSelector(mParent.getContext(), mParent, null);
    }

    public void appearColor(boolean fromRight, int color) {
        mColorSelector.show(fromRight, color == -1 ? Color.WHITE : color);
    }

    public void disappearColor() {
        mColorSelector.disappear(false);
    }

    public boolean disappearLayer() {
        if (mColorSelector != null && mColorSelector.isDisplaying()) {
            disappearColor();
            return false;
        } else {
            disappear(false);
            return true;
        }
    }

    public void adaptPanelPosition() {
        if(!mDisplaying) return;
        if(mCurrentlyEditedButton == null) return;

        boolean preferRight = shouldAppearOnRightSide();
        appear(preferRight);
        if (mColorSelector.isDisplaying()) {
            appearColor(preferRight, mCurrentlyEditedButton.getProperties().bgColor);
        }
    }

    /** Returns true if the button is on the left half of the screen, suggesting the dialog should be on the right */
    public boolean shouldAppearOnRightSide() {
        if (mCurrentlyEditedButton == null) return true;
        return mCurrentlyEditedButton.getControlView().getX() + mCurrentlyEditedButton.getControlView().getWidth() / 2f < mCurrentlyEditedButton.getControlLayoutParent().getWidth() / 2f;
    }

    public void setCurrentlyEditedButton(ControlInterface button) {
        mCurrentlyEditedButton = button;
        updateComposeContent();
    }

    public void updateComposeContent() {
        if (mCurrentlyEditedButton == null) return;
        EditControlComposeBridge.setContent(
                mComposeView,
                mCurrentlyEditedButton,
                new EditControlComposeBridge.OnColorPickListener() {
                    @Override
                    public void onColorPick(boolean isBg, int color, EditControlComposeBridge.OnColorResultListener resultListener) {
                        mColorSelector.setAlphaEnabled(isBg);
                        mColorSelector.setColorSelectionListener(resultListener::onResult);
                        appearColor(shouldAppearOnRightSide(), color);
                    }
                },
                this::startBitmapPicking
        );
    }

    private void startBitmapPicking() {
        final View targetView = mCurrentlyEditedButton.getControlView();
        CropperUtils.CropperReceiver receiver = new CropperUtils.CropperReceiver() {
            @Override
            public float getAspectRatio() {
                return (float) targetView.getWidth() / targetView.getHeight();
            }

            @Override
            public int getTargetMaxSide() {
                return Math.max(targetView.getWidth(), targetView.getHeight());
            }

            @Override
            public void onCropped(Bitmap contentBitmap) {
                ControlData buttonProperties = mCurrentlyEditedButton.getProperties();
                LayoutBitmaps storage = mCurrentlyEditedButton.getControlLayoutParent().getBitmaps();
                String oldTag = buttonProperties.bitmapTag;
                buttonProperties.bitmapTag = storage.putBitmap(contentBitmap, oldTag);
                mCurrentlyEditedButton.setBackground();
                updateComposeContent();
            }

            @Override
            public void onFailed(Exception exception) {
                Tools.showError(targetView.getContext(), exception);
            }
        };
        Context context = targetView.getContext();
        if (context instanceof CustomControlsActivity) {
            ((CustomControlsActivity) context).startCropping(receiver);
        }
    }

    public void loadValues(ControlData data) {}
    public void loadValues(ControlDrawerData data) {}
    public void loadJoystickValues(net.kdt.pojavlaunch.customcontrols.ControlJoystickData data) {
    }

    public void loadSubButtonValues(ControlData data, ControlDrawerData.Orientation orientation) {
    }
}
