package com.example.planforplant.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SimpleBarChartView extends View {

    private List<StatsPoint> data = new ArrayList<>();
    private Paint barPaint;
    private Paint textPaint;
    private Paint axisPaint;
    private final int barColor = Color.parseColor("#4CAF50");

    // cached bar rects for touch detection
    private final List<RectF> barRects = new ArrayList<>();
    private int selectedIndex = -1;

    // responsive sizes
    private float labelTextSizePx;
    private float valueTextSizePx;

    public SimpleBarChartView(Context context) {
        super(context);
        init(context);
    }

    public SimpleBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimpleBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        labelTextSizePx = 12 * dm.density; // sp -> px approx
        valueTextSizePx = 12 * dm.density;

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(barColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(labelTextSizePx);
        textPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.LTGRAY);
        axisPaint.setStrokeWidth(2f * dm.density);
    }

    public void setData(List<StatsPoint> points) {
        if (points == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = points;
        }
        selectedIndex = -1;
        // ensure barRects capacity
        if (barRects.size() < this.data.size()) {
            for (int i = barRects.size(); i < this.data.size(); i++) {
                barRects.add(new RectF());
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int usableWidth = width - paddingLeft - paddingRight;
        int usableHeight = height - paddingTop - paddingBottom - (int)(40 * getResources().getDisplayMetrics().density); // reserve for labels

        // draw baseline
        float baselineY = paddingTop + usableHeight;
        canvas.drawLine(paddingLeft, baselineY, paddingLeft + usableWidth, baselineY, axisPaint);

        if (data == null || data.isEmpty()) return;

        // find max value
        int max = 0;
        for (StatsPoint p : data) {
            if (p.getCount() > max) max = p.getCount();
        }
        if (max == 0) max = 1; // avoid division by zero

        int count = data.size();
        float gap = Math.max(8f, usableWidth * 0.06f / Math.max(1, count));
        float barAreaWidth = usableWidth - gap * (count + 1);
        float barWidth = Math.max(8f, barAreaWidth / Math.max(1, count));

        // reuse existing RectF objects
        for (int i = 0; i < count; i++) {
            StatsPoint p = data.get(i);
            float left = paddingLeft + gap + i * (barWidth + gap);
            float right = left + barWidth;

            float valueRatio = (float) p.getCount() / (float) max;
            float top = baselineY - valueRatio * usableHeight;

            RectF rect;
            if (i < barRects.size()) {
                rect = barRects.get(i);
                rect.set(left, top, right, baselineY);
            } else {
                rect = new RectF(left, top, right, baselineY);
                barRects.add(rect);
            }

            // draw bar
            canvas.drawRect(rect, barPaint);

            // draw value above bar
            textPaint.setTextSize(valueTextSizePx);
            canvas.drawText(String.valueOf(p.getCount()), rect.centerX(), rect.top - 8f, textPaint);

            // draw label
            textPaint.setTextSize(labelTextSizePx);
            float labelY = baselineY + 24f;
            canvas.drawText(p.getLabel(), rect.centerX(), labelY, textPaint);
        }

        // draw tooltip if selected
        if (selectedIndex >= 0 && selectedIndex < barRects.size()) {
            RectF r = barRects.get(selectedIndex);
            String value = String.valueOf(data.get(selectedIndex).getCount());
            drawTooltip(canvas, r.centerX(), r.top - 10f, value);
        }
    }

    private void drawTooltip(Canvas canvas, float cx, float cy, String text) {
        float padding = 8f * getResources().getDisplayMetrics().density;
        textPaint.setTextSize(valueTextSizePx);
        float textWidth = textPaint.measureText(text);
        float rectWidth = textWidth + padding * 2;
        float rectHeight = valueTextSizePx + padding * 2;

        float left = cx - rectWidth / 2f;
        float top = cy - rectHeight;
        float right = cx + rectWidth / 2f;
        float bottom = cy;

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.WHITE);
        bg.setStyle(Paint.Style.FILL);
        bg.setShadowLayer(4f, 0, 2f, Color.GRAY);

        // draw background rect
        canvas.drawRoundRect(new RectF(left, top, right, bottom), 8f, 8f, bg);

        // draw border
        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setColor(Color.LTGRAY);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(1f);
        canvas.drawRoundRect(new RectF(left, top, right, bottom), 8f, 8f, border);

        // draw text
        textPaint.setColor(Color.BLACK);
        canvas.drawText(text, cx, top + padding + valueTextSizePx * 0.8f, textPaint);
        textPaint.setColor(Color.DKGRAY);

        // clear shadow
        bg.clearShadowLayer();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            int idx = -1;
            for (int i = 0; i < barRects.size(); i++) {
                if (barRects.get(i).contains(x, y)) {
                    idx = i;
                    break;
                }
            }
            if (idx != selectedIndex) {
                selectedIndex = idx;
                invalidate();
                performClick();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
