package com.plantpal.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

// A custom view that draws a water drop filled to a level between 0 and 1.
// 1 = full (just watered), 0 = empty (needs water).
public class ThirstView extends View {

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path dropPath = new Path();

    private float level = 1f;

    // Used when the view is created in code
    public ThirstView(Context context) {
        super(context);
        init();
    }

    // Used when the view is inflated from XML (this one is required for layouts)
    public ThirstView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.parseColor("#E3F2FD"));

        fillPaint.setStyle(Paint.Style.FILL);

        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(4f);
        outlinePaint.setColor(Color.parseColor("#546E7A"));
    }

    // Call this to change how full the drop is
    public void setLevel(float newLevel) {
        if (newLevel < 0f) newLevel = 0f;
        if (newLevel > 1f) newLevel = 1f;
        level = newLevel;
        invalidate(); // asks Android to redraw the view
    }

    // Called whenever the view's size is known or changes: build the drop shape here
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float pad = 6f;
        float left = pad;
        float right = w - pad;
        float top = pad;
        float bottom = h - pad;
        float width = right - left;
        float height = bottom - top;
        float cx = w / 2f;

        dropPath.reset();
        dropPath.moveTo(cx, top);
        dropPath.cubicTo(cx + width * 0.10f, top + height * 0.25f,
                right, top + height * 0.45f,
                right, top + height * 0.68f);
        dropPath.cubicTo(right, top + height * 0.86f,
                cx + width * 0.28f, bottom,
                cx, bottom);
        dropPath.cubicTo(cx - width * 0.28f, bottom,
                left, top + height * 0.86f,
                left, top + height * 0.68f);
        dropPath.cubicTo(left, top + height * 0.45f,
                cx - width * 0.10f, top + height * 0.25f,
                cx, top);
        dropPath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float pad = 6f;
        float top = pad;
        float bottom = getHeight() - pad;
        float height = bottom - top;

        // 1) light background inside the drop
        canvas.drawPath(dropPath, backgroundPaint);

        // 2) colored "water", drawn only inside the drop shape
        if (level > 0f) {
            if (level > 0.5f) {
                fillPaint.setColor(Color.parseColor("#1E88E5")); // blue
            } else if (level > 0.2f) {
                fillPaint.setColor(Color.parseColor("#FB8C00")); // orange
            } else {
                fillPaint.setColor(Color.parseColor("#C62828")); // red
            }
            float waterTop = bottom - (height * level);

            canvas.save();
            canvas.clipPath(dropPath);
            canvas.drawRect(0, waterTop, getWidth(), getHeight(), fillPaint);
            canvas.restore();
        }

        // 3) outline on top
        canvas.drawPath(dropPath, outlinePaint);
    }
}