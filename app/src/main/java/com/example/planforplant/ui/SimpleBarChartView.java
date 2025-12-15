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

    // ===== DATA =====
    private final List<StatsPoint> data = new ArrayList<>();

    // ===== PAINT =====
    private Paint barPaint;
    private Paint valuePaint;
    private Paint axisPaint;
    private Paint labelPaint;
    private Paint yAxisLabelPaint;
    private Paint gridPaint;

    // ===== CONFIG =====
    private float animationProgress = 1f;
    private String yAxisLabel = "Số lần hoạt động";

    private final int paddingLeft = 110;
    private final int paddingBottom = 90;
    private final int paddingTop = 50;
    private final int paddingRight = 40;

    private final int yAxisSteps = 5;

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

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(Color.DKGRAY);
        valuePaint.setTextSize(26f);
        valuePaint.setTextAlign(Paint.Align.CENTER);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.GRAY);
        labelPaint.setTextSize(24f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        yAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yAxisLabelPaint.setColor(Color.GRAY);
        yAxisLabelPaint.setTextSize(26f);
        yAxisLabelPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.LTGRAY);
        axisPaint.setStrokeWidth(2f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#EEEEEE"));
        gridPaint.setStrokeWidth(1.5f);
    }

    // ===== PUBLIC API =====
    public void setData(List<StatsPoint> stats) {
        data.clear();
        if (stats != null) data.addAll(stats);
        startAnimation();
    }

    public void setYAxisLabel(String label) {
        this.yAxisLabel = label;
        invalidate();
    }

    // ===== ANIMATION =====
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
        if (maxValue <= 0) return;

        // ===== GRID + Y AXIS VALUES =====
        for (int i = 0; i <= yAxisSteps; i++) {
            float y = paddingTop + chartHeight * i / yAxisSteps;
            canvas.drawLine(
                    paddingLeft,
                    y,
                    width - paddingRight,
                    y,
                    gridPaint
            );

            int value = maxValue - (maxValue * i / yAxisSteps);
            canvas.drawText(
                    String.valueOf(value),
                    paddingLeft - 20,
                    y + 8,
                    valuePaint
            );
        }

        // ===== AXES =====
        canvas.drawLine(
                paddingLeft,
                paddingTop,
                paddingLeft,
                height - paddingBottom,
                axisPaint
        );

        canvas.drawLine(
                paddingLeft,
                height - paddingBottom,
                width - paddingRight,
                height - paddingBottom,
                axisPaint
        );

        // ===== Y AXIS LABEL =====
        canvas.save();
        canvas.rotate(-90);
        canvas.drawText(
                yAxisLabel,
                -(paddingTop + chartHeight / 2),
                40,
                yAxisLabelPaint
        );
        canvas.restore();

        // ===== BARS =====
        float barSpace = chartWidth / data.size();
        float barWidth = barSpace * 0.55f;

        for (int i = 0; i < data.size(); i++) {
            StatsPoint p = data.get(i);

            float left = paddingLeft + i * barSpace + (barSpace - barWidth) / 2;
            float right = left + barWidth;

            float ratio = (float) p.getValue() / maxValue;
            float barHeight = chartHeight * ratio * animationProgress;

            float top = paddingTop + (chartHeight - barHeight);
            float bottom = height - paddingBottom;

            LinearGradient gradient = new LinearGradient(
                    0, top, 0, bottom,
                    getResources().getColor(R.color.green_primary),
                    getResources().getColor(R.color.green_light),
                    Shader.TileMode.CLAMP
            );
            barPaint.setShader(gradient);

            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, 18f, 18f, barPaint);

            // VALUE
            canvas.drawText(
                    String.valueOf(p.getValue()),
                    (left + right) / 2,
                    top - 10,
                    valuePaint
            );

            // X LABEL
            canvas.drawText(
                    p.getLabel(),
                    (left + right) / 2,
                    height - paddingBottom + 32,
                    labelPaint
            );
        }
    }

    private int getMaxValue() {
        int max = 0;
        for (StatsPoint p : data) {
            max = Math.max(max, p.getValue());
        }
        return max;
    }
}
