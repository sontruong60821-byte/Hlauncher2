package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlDrawer;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlInterface;

public class AddSubButton extends MaterialButton implements ActionButtonInterface {
    public AddSubButton(Context context) {super(context); init();}
    public AddSubButton(Context context, @Nullable AttributeSet attrs) {super(context, attrs); init();}

    public void init() {
        setText(R.string.customctrl_addsubbutton);
        setIconResource(R.drawable.ic_add);
        setOnClickListener(this);
        setPadding(0, 0, 0, 0);
        setInsetBottom(0);
        setInsetTop(0);
    }

    private ControlInterface mCurrentlySelectedButton = null;

    @Override
    public boolean shouldBeVisible() {
        return mCurrentlySelectedButton != null && mCurrentlySelectedButton instanceof ControlDrawer;
    }

    @Override
    public void setFollowedView(ControlInterface view) {
        mCurrentlySelectedButton = view;
    }

    @Override
    public void onClick() {
        if(mCurrentlySelectedButton instanceof ControlDrawer){
            ((ControlDrawer)mCurrentlySelectedButton).getControlLayoutParent().addSubButton(
                    (ControlDrawer)mCurrentlySelectedButton,
                    new ControlData()
            );
        }
    }
}
