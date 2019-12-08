package com.example.speed;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.speed.R;

/**
 * @author Andrey Kudryavtsev on 2019-11-26.
 */
public class SpeedometerView extends View {
    private static final Paint FRONT_ARC_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint ARROW_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT1_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final float STROKE_WIDTH = 50f;
    private static final int MAX_SPEED = 200;
    private static final float MAX_ANGLE = 240f;
    private static final float START_ANGLE = -210f;
    private float mFontSize = 58f;
    private static final String STRING_MIN = "0 km/h";
    private static final String STRING_MAX = "200 km/h";
    private static final String TAG = "SpeedometerView";
    private static final float MAGIC_MULTIPLIER = 1.29f;
    private Rect mTextBounds = new Rect();
    private Rect mTextMaxBounds = new Rect();
    private int mSpeed;
    private LinearGradient mFillGrad;
    @ColorInt
    private int[] mFillColor = new int[4];
    private RectF mArcRect;
    private Point point1_draw;
    private Point point2_draw;
    private Point point3_draw;

    public SpeedometerView(Context context) {
        this(context, null, 0);
    }

    public SpeedometerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure() called with: widthMeasureSpec = [" + MeasureSpec.toString(widthMeasureSpec) + "], heightMeasureSpec = [" + MeasureSpec.toString(heightMeasureSpec) + "]");
        final String maxSpeedString = formatString(MAX_SPEED);
        getTextBounds(maxSpeedString);
        float desiredWidth =
                Math.max(mTextBounds.width() + 2 * STROKE_WIDTH, getSuggestedMinimumWidth()) + getPaddingLeft() + getPaddingRight();
        float desiredHeight = Math.max(mTextBounds.height() + 2 * STROKE_WIDTH, getSuggestedMinimumWidth()) + getPaddingTop() + getPaddingBottom();
        int desiredSize = (int) (MAGIC_MULTIPLIER * Math.max(desiredHeight, desiredWidth));
        final int resolvedWidth = resolveSize(desiredSize, widthMeasureSpec);
        final int resolvedHeight = resolveSize(desiredSize, heightMeasureSpec);
        setMeasuredDimension(resolvedWidth, resolvedHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");
        final int size = Math.min(w, h);
        mArcRect = new RectF(getPaddingLeft() + STROKE_WIDTH / 2, STROKE_WIDTH / 2 + getPaddingTop(), size - STROKE_WIDTH / 2 - getPaddingRight(), size - STROKE_WIDTH / 2 - getPaddingBottom());
        point1_draw = new Point((int) (mArcRect.centerX()), (int) (mArcRect.centerY() - 10));
        point2_draw = new Point((int) (mArcRect.centerX() + STROKE_WIDTH - mArcRect.width() / 2), (int) (mArcRect.centerY()));
        point3_draw = new Point((int) (mArcRect.centerX()), (int) (mArcRect.centerY()) + 10);
        mFillGrad = new LinearGradient(point2_draw.x - STROKE_WIDTH, mArcRect.width() / 2, point2_draw.x + 2 * mArcRect.width() / 2, mArcRect.width() / 2,
                new int[]{mFillColor[1], mFillColor[2], mFillColor[3]},
                new float[]{0f, 0.4f, 0.9f}, Shader.TileMode.MIRROR);
        FRONT_ARC_PAINT.setShader(mFillGrad);
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int Speed) {
        mSpeed = Speed;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mArcRect, START_ANGLE, MAX_ANGLE, false, FRONT_ARC_PAINT);
        canvas.drawCircle(mArcRect.centerX(), mArcRect.centerY(), 20, ARROW_PAINT);
        drawText(canvas);
        drawArrow(canvas);
    }

    private void drawArrow(
            Canvas canvas) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1_draw.x, point1_draw.y);
        path.lineTo(point2_draw.x, point2_draw.y);
        path.lineTo(point3_draw.x, point3_draw.y);
        path.lineTo(point1_draw.x, point1_draw.y);
        path.close();
        canvas.rotate(MAX_ANGLE * mSpeed / MAX_SPEED - 30, mArcRect.centerX(), mArcRect.centerY());
        canvas.drawPath(path, ARROW_PAINT);
    }

    private void drawText(Canvas canvas) {
        final String SpeedString = formatString(mSpeed);
        getTextBounds(SpeedString);

        float x = mArcRect.width() / 2f - mTextBounds.width() / 2f - mTextBounds.left + mArcRect.left;
        float xMin = mArcRect.centerX() - mArcRect.width() / 2;
        float xMax = mArcRect.centerX() + mArcRect.width() / 2 - mTextMaxBounds.width();
        float y = mArcRect.centerY() + mArcRect.width() / 2;
        float yMinMax = mArcRect.centerY() + mArcRect.width() / 2 - mTextMaxBounds.height() * 2;
        canvas.drawText(SpeedString, x, y, TEXT_PAINT);

        canvas.drawText(STRING_MIN, xMin, yMinMax, TEXT1_PAINT);
        canvas.drawText(STRING_MAX, xMax, yMinMax, TEXT1_PAINT);
    }

    private String formatString(int speed) {
        return String.format("%d km/h", speed);
    }

    private void getTextBounds(@NonNull String SpeedString) {
        TEXT_PAINT.getTextBounds(SpeedString, 0, SpeedString.length(), mTextBounds);
        TEXT1_PAINT.getTextBounds(STRING_MAX, 0, STRING_MAX.length(), mTextMaxBounds);

    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {


        extractAttributes(context, attrs);
        configureFrontArc();
        configureArrow();
        configureText();

    }

    private void extractAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            final Resources.Theme theme = context.getTheme();
            final TypedArray typedArray = theme.obtainStyledAttributes(attrs, R.styleable.SpeedometerView, 0, R.style.SpeedometerViewDefault);
            try {
                mSpeed = typedArray.getInt(R.styleable.SpeedometerView_speed, 0);
                mFillColor[0] = typedArray.getColor(R.styleable.SpeedometerView_fill_color, getResources().getColor(R.color.colorAccent));
                mFillColor[1] = typedArray.getColor(R.styleable.SpeedometerView_fill_gradient1, getResources().getColor(R.color.colorGradient1));
                mFillColor[2] = typedArray.getColor(R.styleable.SpeedometerView_fill_gradient2, getResources().getColor(R.color.colorGradient2));
                mFillColor[3] = typedArray.getColor(R.styleable.SpeedometerView_fill_gradient3, getResources().getColor(R.color.colorGradient3));

            } finally {
                typedArray.recycle();
            }
        }
    }

    private void configureText() {
        TEXT_PAINT.setColor(mFillColor[0]);
        TEXT_PAINT.setStyle(Paint.Style.FILL);
        TEXT_PAINT.setTextSize(mFontSize);
        TEXT1_PAINT.setColor(mFillColor[0]);
        TEXT1_PAINT.setStyle(Paint.Style.FILL);
        TEXT1_PAINT.setTextSize(2 * mFontSize / 3);

    }

    private void configureFrontArc() {

        FRONT_ARC_PAINT.setStyle(Paint.Style.STROKE);
        FRONT_ARC_PAINT.setStrokeWidth(STROKE_WIDTH);
    }

    private void configureArrow() {
        ARROW_PAINT.setStrokeWidth(2);
        ARROW_PAINT.setColor(mFillColor[0]);
        ARROW_PAINT.setStyle(Paint.Style.FILL_AND_STROKE);
        ARROW_PAINT.setAntiAlias(true);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState superState = new SavedState(super.onSaveInstanceState());

        superState.progress=mSpeed;
        return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ourState = (SavedState) state;
        super.onRestoreInstanceState(ourState.getSuperState());
        mSpeed=ourState.progress;
        invalidate();
    }

    private  static class SavedState extends BaseSavedState
    {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>()
        {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }


            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
private int progress;


        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
        }

        private SavedState(Parcel source) {
            super(source);
            progress=source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

    }
}

