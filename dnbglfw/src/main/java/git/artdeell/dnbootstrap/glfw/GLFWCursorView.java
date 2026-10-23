package git.artdeell.dnbootstrap.glfw;

import androidx.annotation.NonNull;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import git.artdeell.dnbglfw.R;


public class GLFWCursorView extends View implements CursorImplementor {
    private Drawable cursorDrawable;
    private Drawable defaultCursorDrawable;
    private final Paint customCursorPaint = new Paint();
    private boolean noDraw = false;
    private float hotX = 0;
    private float hotY = 0;
    private float cursorScale = 1f;

    public GLFWCursorView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GLFWCursorView(Context context) {
        this(context, null);
    }

    public GLFWCursorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GLFWCursorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        GLFW.setCursorImpl(this);
        if(attrs != null) {
            try(TypedArray arr = context.obtainStyledAttributes(attrs,R.styleable.GLFWCursorView)) {
                defaultCursorDrawable = arr.getDrawable(R.styleable.GLFWCursorView_defaultCursorDrawable);
            }
        }
        if(defaultCursorDrawable == null) defaultCursorDrawable = new FallbackCursorDrawable();
        cursorDrawable = defaultCursorDrawable;
        cursorDrawable.setBounds(0, 0, 36, 54);
    }

    public void setCustomCursor(Drawable drawable, float hX, float hY) {
        if (drawable == null) {
            cursorDrawable = defaultCursorDrawable;
            cursorDrawable.setBounds(0, 0, 36, 54);
            hotX = 0;
            hotY = 0;
        } else {
            cursorDrawable = drawable;
            // Normalize size: make it roughly the same height as the default cursor (54px)
            // This prevents high-res PNGs from being "huge" by default.
            float aspect = (float)drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
            int h = 54;
            int w = (int)(h * aspect);
            cursorDrawable.setBounds(0, 0, w, h);
            
            // Adjust hotspot to the normalized size
            hotX = hX * ((float)w / drawable.getIntrinsicWidth());
            hotY = hY * ((float)h / drawable.getIntrinsicHeight());
        }
        postInvalidate();
    }

    public void setCursorScale(float scale) {
        this.cursorScale = scale;
        postInvalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if(noDraw) return;
        canvas.translate((int)(GLFW.cursorX * getWidth()), (int)(GLFW.cursorY * getHeight()));
        GLFWCursor cursor = GLFW.getCursor();
        if(cursor == null) {
            canvas.scale(cursorScale, cursorScale);
            canvas.translate(-hotX, -hotY);
            cursorDrawable.draw(canvas);
        }else {
            canvas.scale(1.15f * cursorScale, 1.15f * cursorScale);
            canvas.drawBitmap(cursor.bitmap, -cursor.hotX, -cursor.hotY, customCursorPaint);
        }
    }

    @Override
    public void onCursorPosition() {
        if(!noDraw) post(this::invalidate);
    }

    @Override
    public void onCursorChanged() {
        post(this::invalidate);
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        noDraw = isGrabbing;
        invalidate();
    }
}
