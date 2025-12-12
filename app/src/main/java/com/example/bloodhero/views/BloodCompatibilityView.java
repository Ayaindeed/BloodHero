package com.example.bloodhero.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view that draws animated connection lines between blood bag and blood type recipients.
 * Lines show which blood types the user can donate to.
 */
public class BloodCompatibilityView extends View {

    private Paint linePaint;
    private Paint glowPaint;
    private List<ConnectionLine> connectionLines = new ArrayList<>();
    private float animationProgress = 0f;
    private boolean isAnimating = false;

    // Blood bag position (center point at bottom of bag)
    private float bagCenterX = 0;
    private float bagBottomY = 0;

    // Target positions for each blood type
    private float[][] targetPositions = new float[8][2]; // 8 blood types, each with x,y

    public BloodCompatibilityView(Context context) {
        super(context);
        init();
    }

    public BloodCompatibilityView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BloodCompatibilityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#4CAF50")); // Green for donation lines
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(Color.parseColor("#804CAF50")); // Semi-transparent green
        glowPaint.setStrokeWidth(8f);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setConnectionLines(List<ConnectionLine> lines) {
        this.connectionLines = lines;
        invalidate();
    }

    public void setBagPosition(float centerX, float bottomY) {
        this.bagCenterX = centerX;
        this.bagBottomY = bottomY;
        invalidate();
    }

    public void setTargetPosition(int index, float x, float y) {
        if (index >= 0 && index < 8) {
            targetPositions[index][0] = x;
            targetPositions[index][1] = y;
        }
    }

    public void animateLines() {
        if (isAnimating) return;
        isAnimating = true;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (ConnectionLine line : connectionLines) {
            if (line.isActive) {
                drawConnectionLine(canvas, line);
            }
        }
    }

    private void drawConnectionLine(Canvas canvas, ConnectionLine line) {
        Path path = new Path();
        path.moveTo(line.startX, line.startY);

        // Create curved path using quadratic bezier
        float controlX = (line.startX + line.endX) / 2;
        float controlY = line.startY + (line.endY - line.startY) * 0.3f;

        path.quadTo(controlX, controlY, line.endX, line.endY);

        // Draw glow effect
        glowPaint.setColor(line.glowColor);
        canvas.drawPath(path, glowPaint);

        // Draw main line with animation
        if (animationProgress > 0) {
            PathMeasure measure = new PathMeasure(path, false);
            Path animatedPath = new Path();
            measure.getSegment(0, measure.getLength() * animationProgress, animatedPath, true);

            linePaint.setColor(line.lineColor);
            canvas.drawPath(animatedPath, linePaint);

            // Draw dot at end of animated line
            if (animationProgress > 0.1f) {
                float[] pos = new float[2];
                measure.getPosTan(measure.getLength() * animationProgress, pos, null);
                Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                dotPaint.setColor(line.lineColor);
                canvas.drawCircle(pos[0], pos[1], 6f, dotPaint);
            }
        }
    }

    public static class ConnectionLine {
        public float startX, startY;
        public float endX, endY;
        public int lineColor;
        public int glowColor;
        public boolean isActive;
        public String bloodType;

        public ConnectionLine(float startX, float startY, float endX, float endY,
                              int lineColor, int glowColor, boolean isActive, String bloodType) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.lineColor = lineColor;
            this.glowColor = glowColor;
            this.isActive = isActive;
            this.bloodType = bloodType;
        }
    }
}
