package com.example.bloodhero.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animated blood compatibility view showing flowing connections
 * from central blood bag to compatible blood types
 */
public class FlowingBloodCompatibilityView extends View {

    private static final Map<String, List<String>> COMPATIBILITY_MAP = new HashMap<>();
    
    static {
        COMPATIBILITY_MAP.put("O-", Arrays.asList("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
        COMPATIBILITY_MAP.put("O+", Arrays.asList("O+", "A+", "B+", "AB+"));
        COMPATIBILITY_MAP.put("A-", Arrays.asList("A-", "A+", "AB-", "AB+"));
        COMPATIBILITY_MAP.put("A+", Arrays.asList("A+", "AB+"));
        COMPATIBILITY_MAP.put("B-", Arrays.asList("B-", "B+", "AB-", "AB+"));
        COMPATIBILITY_MAP.put("B+", Arrays.asList("B+", "AB+"));
        COMPATIBILITY_MAP.put("AB-", Arrays.asList("AB-", "AB+"));
        COMPATIBILITY_MAP.put("AB+", Arrays.asList("AB+"));
    }

    private String selectedBloodType = "O+";
    private Paint bagPaint, bloodPaint, linePaint, particlePaint, textPaint, circlePaint;
    private ValueAnimator flowAnimator;
    private float flowProgress = 0f;
    
    private Map<String, PointF> bloodTypePositions = new HashMap<>();
    private List<FlowParticle> particles = new ArrayList<>();
    
    private int bloodColor = Color.parseColor("#E31837");
    private int compatibleColor = Color.parseColor("#4CAF50");
    private int incompatibleColor = Color.parseColor("#9E9E9E");

    public FlowingBloodCompatibilityView(Context context) {
        super(context);
        init();
    }

    public FlowingBloodCompatibilityView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlowingBloodCompatibilityView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Bag paint
        bagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bagPaint.setColor(Color.WHITE);
        bagPaint.setStyle(Paint.Style.FILL);
        bagPaint.setShadowLayer(8, 0, 4, Color.parseColor("#40000000"));
        setLayerType(LAYER_TYPE_SOFTWARE, bagPaint);

        // Blood paint
        bloodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bloodPaint.setColor(bloodColor);
        bloodPaint.setStyle(Paint.Style.FILL);

        // Line paint for connections
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // Particle paint
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setColor(bloodColor);
        particlePaint.setStyle(Paint.Style.FILL);

        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Circle paint for blood type nodes
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);

        startFlowAnimation();
    }

    private void startFlowAnimation() {
        flowAnimator = ValueAnimator.ofFloat(0, 1);
        flowAnimator.setDuration(2000);
        flowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flowAnimator.setInterpolator(new LinearInterpolator());
        flowAnimator.addUpdateListener(animation -> {
            flowProgress = (float) animation.getAnimatedValue();
            updateParticles();
            invalidate();
        });
        flowAnimator.start();
    }

    public void setBloodType(String type) {
        this.selectedBloodType = type;
        particles.clear();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateBloodTypePositions(w, h);
    }

    private void calculateBloodTypePositions(float width, float height) {
        bloodTypePositions.clear();
        
        // Define blood types and their positions in a circular layout
        String[] bloodTypes = {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        
        float centerX = width / 2;
        float centerY = height / 2;
        float radius = Math.min(width, height) * 0.35f;
        
        // Position blood types in a circle around the center
        for (int i = 0; i < bloodTypes.length; i++) {
            double angle = (2 * Math.PI * i / bloodTypes.length) - Math.PI / 2; // Start from top
            float x = centerX + (float) (radius * Math.cos(angle));
            float y = centerY + (float) (radius * Math.sin(angle));
            bloodTypePositions.put(bloodTypes[i], new PointF(x, y));
        }
    }

    private void updateParticles() {
        // Add new particles periodically
        if (flowProgress % 0.1f < 0.02f) {
            List<String> compatibleTypes = COMPATIBILITY_MAP.get(selectedBloodType);
            if (compatibleTypes != null) {
                for (String type : compatibleTypes) {
                    if (!type.equals(selectedBloodType) && bloodTypePositions.containsKey(type)) {
                        particles.add(new FlowParticle(type, 0f));
                    }
                }
            }
        }

        // Update existing particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            FlowParticle particle = particles.get(i);
            particle.progress += 0.015f;
            if (particle.progress > 1f) {
                particles.remove(i);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bloodTypePositions.isEmpty()) {
            calculateBloodTypePositions(getWidth(), getHeight());
        }

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2;
        float centerY = height / 2;

        List<String> compatibleTypes = COMPATIBILITY_MAP.get(selectedBloodType);

        // Draw connection lines first (behind everything)
        drawConnectionLines(canvas, centerX, centerY, compatibleTypes);

        // Draw flowing particles
        drawFlowingParticles(canvas, centerX, centerY);

        // Draw blood type nodes
        drawBloodTypeNodes(canvas, compatibleTypes);

        // Draw central blood bag (on top)
        drawCentralBloodBag(canvas, centerX, centerY);
    }

    private void drawConnectionLines(Canvas canvas, float centerX, float centerY, List<String> compatibleTypes) {
        for (Map.Entry<String, PointF> entry : bloodTypePositions.entrySet()) {
            String bloodType = entry.getKey();
            PointF pos = entry.getValue();
            
            boolean isCompatible = compatibleTypes != null && compatibleTypes.contains(bloodType);
            
            if (isCompatible) {
                linePaint.setColor(compatibleColor);
                linePaint.setAlpha(100);
                linePaint.setStrokeWidth(3);
                
                // Draw curved path
                Path path = new Path();
                path.moveTo(centerX, centerY);
                
                // Control point for curve
                float controlX = (centerX + pos.x) / 2;
                float controlY = (centerY + pos.y) / 2;
                
                path.quadTo(controlX, controlY, pos.x, pos.y);
                canvas.drawPath(path, linePaint);
            }
        }
    }

    private void drawFlowingParticles(Canvas canvas, float centerX, float centerY) {
        for (FlowParticle particle : particles) {
            PointF targetPos = bloodTypePositions.get(particle.targetType);
            if (targetPos != null) {
                // Calculate current position along the path
                float currentX = centerX + (targetPos.x - centerX) * particle.progress;
                float currentY = centerY + (targetPos.y - centerY) * particle.progress;
                
                // Draw particle
                particlePaint.setColor(bloodColor);
                particlePaint.setAlpha((int) (255 * (1 - particle.progress)));
                canvas.drawCircle(currentX, currentY, 6, particlePaint);
            }
        }
    }

    private void drawBloodTypeNodes(Canvas canvas, List<String> compatibleTypes) {
        for (Map.Entry<String, PointF> entry : bloodTypePositions.entrySet()) {
            String bloodType = entry.getKey();
            PointF pos = entry.getValue();
            
            boolean isCompatible = compatibleTypes != null && compatibleTypes.contains(bloodType);
            boolean isSelected = bloodType.equals(selectedBloodType);
            
            float nodeRadius = isSelected ? 40 : 35;
            
            // Draw node circle
            if (isSelected) {
                circlePaint.setColor(bloodColor);
            } else if (isCompatible) {
                circlePaint.setColor(compatibleColor);
            } else {
                circlePaint.setColor(incompatibleColor);
            }
            
            canvas.drawCircle(pos.x, pos.y, nodeRadius, circlePaint);
            
            // Draw blood type text
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(isSelected ? 24 : 20);
            float textY = pos.y - (textPaint.descent() + textPaint.ascent()) / 2;
            canvas.drawText(bloodType, pos.x, textY, textPaint);
        }
    }

    private void drawCentralBloodBag(Canvas canvas, float centerX, float centerY) {
        float bagSize = 100;
        
        // Draw bag body
        RectF bagRect = new RectF(
            centerX - bagSize / 2,
            centerY - bagSize / 2,
            centerX + bagSize / 2,
            centerY + bagSize / 2
        );
        
        bagPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(bagRect, 15, 15, bagPaint);
        
        // Draw blood inside
        RectF bloodRect = new RectF(
            bagRect.left + 10,
            bagRect.top + 30,
            bagRect.right - 10,
            bagRect.bottom - 10
        );
        
        LinearGradient gradient = new LinearGradient(
            bloodRect.left, bloodRect.top,
            bloodRect.left, bloodRect.bottom,
            Color.parseColor("#FF5252"), bloodColor,
            Shader.TileMode.CLAMP
        );
        bloodPaint.setShader(gradient);
        canvas.drawRoundRect(bloodRect, 10, 10, bloodPaint);
        bloodPaint.setShader(null);
        
        // Draw outline
        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.parseColor("#E0E0E0"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        canvas.drawRoundRect(bagRect, 15, 15, outlinePaint);
        
        // Draw selected blood type
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28);
        textPaint.setShadowLayer(3, 0, 2, Color.parseColor("#40000000"));
        float textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2;
        canvas.drawText(selectedBloodType, centerX, textY, textPaint);
        textPaint.setShadowLayer(0, 0, 0, 0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (flowAnimator != null) flowAnimator.cancel();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (flowAnimator != null && !flowAnimator.isRunning()) flowAnimator.start();
    }

    // Particle class for flowing animation
    private static class FlowParticle {
        String targetType;
        float progress;

        FlowParticle(String targetType, float progress) {
            this.targetType = targetType;
            this.progress = progress;
        }
    }
}
