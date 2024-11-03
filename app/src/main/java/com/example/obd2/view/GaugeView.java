package com.example.obd2.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.obd2.R;

@SuppressWarnings("FieldCanBeLocal")
public class GaugeView extends View implements OBDCommandView {

    private static final String TAG = GaugeView.class.getSimpleName();

    private final Paint paint = new Paint();
    private final RectF arcRect = new RectF();
    private final float startAngle = 45;
    private final float sweepAngle = 270;
    private final float strokeWidth = 10;
    private int wheelColor;
    private int needleColor;
    private int textColor;
    private float textSize;
    private final float textOffsetRatio = 0.8f;
    private String unitOfMeasure;

    private final PointF lineStart = new PointF();
    private final PointF lineEnd = new PointF();
    private float lineAngle;
    private float lineLength;
    private final float lineMargin = 3 * strokeWidth;
    private final float smallSegmentRatio = 1.0f / 3;

    private float minValue;
    private float maxValue;
    private float currentValue;
    private boolean roundValueToInt;


    public GaugeView(Context context) {
        this(context, null, 0);
    }


    public GaugeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        extractXMLAttributes(context, attrs, defStyleAttr);

        paint.setStrokeWidth(strokeWidth);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
    }


    private void extractXMLAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyleAttr, 0)) {

            minValue = a.getFloat(R.styleable.GaugeView_minValue, 0);
            maxValue = a.getFloat(R.styleable.GaugeView_maxValue, 100);
            currentValue = minValue;
            if (minValue >= maxValue) {
                throw new IllegalArgumentException("minValue must be less than maxValue");
            }

            roundValueToInt = a.getBoolean(R.styleable.GaugeView_roundValueToInt, true);
            unitOfMeasure = a.getString(R.styleable.GaugeView_unitOfMeasure);

            wheelColor = a.getColor(R.styleable.GaugeView_wheelColor, Color.WHITE);
            needleColor = a.getColor(R.styleable.GaugeView_needleColor, Color.WHITE);
            textColor = a.getColor(R.styleable.GaugeView_textColor, Color.WHITE);

        } catch (Exception e) {
            Log.e(TAG, "Failed to obtain styled attributes", e);
        }

        setValue(minValue);
    }


    @Override
    public void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(wheelColor);
        canvas.drawArc(arcRect, startAngle, -sweepAngle, false, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(needleColor);
        canvas.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y, paint);

        final int bigSegmentCount = 4;
        final int smallSegmentCount = 4;
        final int segmentCount = bigSegmentCount * smallSegmentCount;

        final float segmentLineAngle = sweepAngle / segmentCount;
        final float smallLineLength = arcRect.right / 10;
        for (int i = 0; i < segmentCount + 1; i++) {
            final float angle = startAngle - i * segmentLineAngle;
            final float x1 = lineStart.x + lineLength * (float) Math.cos(Math.toRadians(angle));
            final float y1 = lineStart.y + lineLength * (float) Math.sin(Math.toRadians(angle));

            float x2 = lineStart.x;
            float y2 = lineStart.y;
            if (i % smallSegmentCount == 0) {
                x2 += (lineLength - smallLineLength) * (float) Math.cos(Math.toRadians(angle));
                y2 += (lineLength - smallLineLength) * (float) Math.sin(Math.toRadians(angle));
            } else {
                x2 += (lineLength - smallLineLength * smallSegmentRatio) * (float) Math.cos(Math.toRadians(angle));
                y2 += (lineLength - smallLineLength * smallSegmentRatio) * (float) Math.sin(Math.toRadians(angle));
            }

            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        paint.setColor(textColor);
        paint.setTextSize(textSize);
        if (roundValueToInt) {
            int val = Math.round(currentValue);
            canvas.drawText(String.valueOf(val), arcRect.centerX(), arcRect.bottom * textOffsetRatio, paint);
        } else {
            float rounded = Math.round(currentValue * 100) / 100.0f;
            canvas.drawText(String.valueOf(rounded), arcRect.centerX(), arcRect.bottom * textOffsetRatio, paint);
        }

        if (unitOfMeasure != null) {
            paint.setTextSize(textSize / 2);
            canvas.drawText(unitOfMeasure, arcRect.centerX(), arcRect.bottom * textOffsetRatio + textSize / 2, paint);
        }
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        super.setMeasuredDimension(size, size);

        // Log.d(TAG, String.format("onMeasure(%d, %d)", width, height));
    }


    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        int size = Math.min(width, height);
        updateRectangle(0, 0, size, size);

        // Log.d(TAG, String.format("onSizeChanged(%d, %d, %d, %d)", width, height, oldWidth, oldHeight));
    }


    @SuppressWarnings("SameParameterValue")
    private void updateRectangle(int left, int top, int right, int bottom) {
        arcRect.left = left + strokeWidth;
        arcRect.top = top + strokeWidth;
        arcRect.right = right - strokeWidth;
        arcRect.bottom = bottom - strokeWidth;
        lineStart.x = (float) (right - left) / 2;
        lineStart.y = (float) (bottom - top) / 2;
        lineLength = (float) Math.min(right - left, bottom - top) / 2 - lineMargin;
        textSize = (float) right / 8;
        paint.setTextSize(textSize);
        setLineAngle(lineAngle);
    }


    @Override
    public void setValue(float value) {
        currentValue = value;
        final float ratio = (currentValue - minValue) / (maxValue - minValue);
        final float newAngle = (startAngle - sweepAngle) + sweepAngle * ratio;
        setLineAngle(newAngle);
        super.invalidate();
    }


    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }


    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }


    public void setUnitOfMeasure(String unit) {
        this.unitOfMeasure = unit;
    }


    private void setLineAngle(float angle) {
        lineAngle = angle;
        lineEnd.x = lineStart.x + lineLength * (float) Math.cos(Math.toRadians(angle));
        lineEnd.y = lineStart.y + lineLength * (float) Math.sin(Math.toRadians(angle));
    }
}
