package com.example.bloodhero.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Custom animated blood bag view with flowing blood effect
 */
public class AnimatedBloodBagView extends View {

    private Paint bagPaint;
    private Paint bloodPaint;
    private Paint tubePaint;
    private Paint highlightPaint;
    private Paint textPaint;
    private Paint dropPaint;

    private Path bagPath;
    private Path tubePath;
    private RectF bagRect;

    private float bloodLevel = 0.7f; // 0 to 1
    private float waveOffset = 0f;
    private float dropY = 0f;
    private boolean showDrop = false;
    
    private String bloodType = "O+";
    private int bloodColor = Color.parseColor("#E31837");
    private int bloodColorLight = Color.parseColor("#FF5252");

    private ValueAnimator waveAnimator;
    private ValueAnimator dropAnimator;

    public AnimatedBloodBagView(Context context) {
        super(context);
        init();
    }

    public AnimatedBloodBagView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedBloodBagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Bag outline paint
        bagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bagPaint.setColor(Color.parseColor("#FFFFFF"));
        bagPaint.setStyle(Paint.Style.FILL);
        bagPaint.setShadowLayer(12, 0, 6, Color.parseColor("#40000000"));
        setLayerType(LAYER_TYPE_SOFTWARE, bagPaint);

        // Blood paint
        bloodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bloodPaint.setColor(bloodColor);
        bloodPaint.setStyle(Paint.Style.FILL);

        // Tube paint
        tubePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tubePaint.setColor(bloodColor);
        tubePaint.setStyle(Paint.Style.STROKE);
        tubePaint.setStrokeWidth(12);
        tubePaint.setStrokeCap(Paint.Cap.ROUND);

        // Highlight paint
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#40FFFFFF"));
        highlightPaint.setStyle(Paint.Style.FILL);

        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Drop paint
        dropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dropPaint.setColor(bloodColor);
        dropPaint.setStyle(Paint.Style.FILL);

        bagPath = new Path();
        tubePath = new Path();
        bagRect = new RectF();

        startAnimations();
    }

    private void startAnimations() {
        // Wave animation
        waveAnimator = ValueAnimator.ofFloat(0, (float) (2 * Math.PI));
        waveAnimator.setDuration(2000);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(animation -> {
            waveOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
        waveAnimator.start();

        // Drop animation
        dropAnimator = ValueAnimator.ofFloat(0, 1);
        dropAnimator.setDuration(1500);
        dropAnimator.setRepeatCount(ValueAnimator.INFINITE);
        dropAnimator.setRepeatMode(ValueAnimator.RESTART);
        dropAnimator.setStartDelay(500);
        dropAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            dropY = progress;
            showDrop = progress < 0.9f;
            invalidate();
        });
        dropAnimator.start();
    }

    public void setBloodType(String type) {
        this.bloodType = type;
        invalidate();
    }

    public void setBloodLevel(float level) {
        this.bloodLevel = Math.max(0, Math.min(1, level));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float padding = 20;

        // Calculate bag dimensions
        float bagWidth = width * 0.7f;
        float bagHeight = height * 0.65f;
        float bagLeft = (width - bagWidth) / 2;
        float bagTop = height * 0.2f;

        // Draw tube at top
        float tubeStartX = width / 2;
        float tubeStartY = 0;
        float tubeEndY = bagTop + 20;

        tubePaint.setColor(bloodColor);
        canvas.drawLine(tubeStartX, tubeStartY, tubeStartX, tubeEndY, tubePaint);

        // Draw tube connector
        Paint connectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        connectorPaint.setColor(Color.parseColor("#D0D0D0"));
        canvas.drawCircle(tubeStartX, tubeStartY + 15, 18, connectorPaint);

        // Draw bag body
        bagRect.set(bagLeft, bagTop, bagLeft + bagWidth, bagTop + bagHeight);
        float cornerRadius = 30;

        // Bag shadow
        bagPaint.setColor(Color.parseColor("#F5F5F5"));
        canvas.drawRoundRect(bagRect, cornerRadius, cornerRadius, bagPaint);

        // Blood inside bag with wave effect
        canvas.save();
        canvas.clipRect(bagRect);

        float bloodTop = bagTop + bagHeight * (1 - bloodLevel);
        
        // Create gradient for blood
        LinearGradient bloodGradient = new LinearGradient(
                bagLeft, bloodTop, bagLeft, bagTop + bagHeight,
                bloodColorLight, bloodColor, Shader.TileMode.CLAMP);
        bloodPaint.setShader(bloodGradient);

        // Draw blood with wave
        Path bloodPath = new Path();
        bloodPath.moveTo(bagLeft, bagTop + bagHeight);

        // Create wave effect
        for (float x = bagLeft; x <= bagLeft + bagWidth; x += 2) {
            float waveHeight = 8 * (float) Math.sin(waveOffset + x * 0.03);
            float y = bloodTop + waveHeight;
            if (x == bagLeft) {
                bloodPath.lineTo(x, y);
            } else {
                bloodPath.lineTo(x, y);
            }
        }

        bloodPath.lineTo(bagLeft + bagWidth, bagTop + bagHeight);
        bloodPath.close();

        canvas.drawPath(bloodPath, bloodPaint);
        bloodPaint.setShader(null);
        
        canvas.restore();

        // Draw bag outline
        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.parseColor("#E0E0E0"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        canvas.drawRoundRect(bagRect, cornerRadius, cornerRadius, outlinePaint);

        // Draw highlight
        RectF highlightRect = new RectF(bagLeft + 15, bagTop + 15, 
                bagLeft + 40, bagTop + bagHeight * 0.6f);
        canvas.drawRoundRect(highlightRect, 10, 10, highlightPaint);

        // Draw blood type text in center of blood
        float textY = bagTop + bagHeight * 0.6f;
        textPaint.setTextSize(bagWidth * 0.25f);
        textPaint.setColor(Color.WHITE);
        textPaint.setShadowLayer(4, 0, 2, Color.parseColor("#40000000"));
        canvas.drawText(bloodType, width / 2, textY, textPaint);

        // Draw blood drop falling from bottom
        if (showDrop) {
            float dropSize = 15;
            float dropX = width / 2;
            float dropStartY = bagTop + bagHeight + 10;
            float dropEndY = height - 20;
            float currentDropY = dropStartY + (dropEndY - dropStartY) * dropY;

            // Draw drop shape
            Path dropPath = new Path();
            dropPath.moveTo(dropX, currentDropY - dropSize * 2);
            dropPath.quadTo(dropX + dropSize, currentDropY - dropSize,
                    dropX, currentDropY);
            dropPath.quadTo(dropX - dropSize, currentDropY - dropSize,
                    dropX, currentDropY - dropSize * 2);
            dropPath.close();

            dropPaint.setColor(bloodColor);
            dropPaint.setAlpha((int) (255 * (1 - dropY)));
            canvas.drawPath(dropPath, dropPaint);
            dropPaint.setAlpha(255);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (waveAnimator != null) waveAnimator.cancel();
        if (dropAnimator != null) dropAnimator.cancel();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (waveAnimator != null && !waveAnimator.isRunning()) waveAnimator.start();
        if (dropAnimator != null && !dropAnimator.isRunning()) dropAnimator.start();
    }
}
