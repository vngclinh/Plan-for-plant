package com.example.planforplant.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.planforplant.R;

import java.util.ArrayList;
import java.util.List;

public class SimpleBarChartView extends View {

    private List<StatsPoint> data = new ArrayList<>();

    private Paint barPaint;
    private Paint textPaint;
    private Paint axisPaint;
    private Paint labelPaint;

    private float animationProgress = 1f;

    private final int paddingLeft = 80;
    private final int paddingBottom = 80;
    private final int paddingTop = 40;
    private final int paddingRight = 40;

    public SimpleBarChartView(Context context) {
        super(context);
        init();
    }

    public SimpleBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.GRAY);
        labelPaint.setTextSize(24f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.LTGRAY);
        axisPaint.setStrokeWidth(2f);
    }

    /**
     * Gọi từ Activity / Fragment
     */
    public void setData(List<StatsPoint> stats) {
        this.data.clear();
        if (stats != null) this.data.addAll(stats);
        startAnimation();
    }

    private void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.addUpdateListener(a -> {
            animationProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        int maxValue = getMaxValue();
        if (maxValue == 0) return;

        // ===== Draw Y axis =====
        canvas.drawLine(
                paddingLeft,
                paddingTop,
                paddingLeft,
                height - paddingBottom,
                axisPaint
        );

        // ===== Draw X axis =====
        canvas.drawLine(
                paddingLeft,
                height - paddingBottom,
                width - paddingRight,
                height - paddingBottom,
                axisPaint
        );

        int count = data.size();
        float barSpacing = chartWidth / count;
        float barWidth = barSpacing * 0.55f;

        for (int i = 0; i < count; i++) {
            StatsPoint p = data.get(i);

            float left = paddingLeft + i * barSpacing + (barSpacing - barWidth) / 2;
            float right = left + barWidth;

            float valueRatio = (float) p.getValue() / maxValue;
            float barHeight = chartHeight * valueRatio * animationProgress;

            float top = paddingTop + (chartHeight - barHeight);
            float bottom = height - paddingBottom;

            // Gradient cho cột
            LinearGradient gradient = new LinearGradient(
                    0, top, 0, bottom,
                    getResources().getColor(R.color.green_primary),
                    getResources().getColor(R.color.green_light),
                    Shader.TileMode.CLAMP
            );
            barPaint.setShader(gradient);

            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, 14f, 14f, barPaint);

            // ===== Value text =====
            canvas.drawText(
                    String.valueOf(p.getValue()),
                    (left + right) / 2,
                    top - 10,
                    textPaint
            );

            // ===== X label =====
            canvas.drawText(
                    p.getLabel(),
                    (left + right) / 2,
                    height - paddingBottom + 30,
                    labelPaint
            );
        }
    }

    private int getMaxValue() {
        int max = 0;
        for (StatsPoint p : data) {
            if (p.getValue() > max) max = p.getValue();
        }
        return max;
    }
}
