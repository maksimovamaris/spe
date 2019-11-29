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
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private static final float RADIUS = 300f;
    private static final RectF ARC_RECT = new RectF(STROKE_WIDTH / 2, STROKE_WIDTH / 2, 2 * RADIUS, 2 * RADIUS);
    private static final int MAX_SPEED = 200;
    private static final float MAX_ANGLE = 240f;
    private static final float START_ANGLE = -210f;
    private static final float FONT_SIZE = 58f;
    private static final String STRING_MIN = "0 km/h";
    private static final String STRING_MAX = "200 km/h";
    private static Point point1_draw = new Point((int) (ARC_RECT.centerX()), (int) (ARC_RECT.centerY() - 10));
    private static Point point2_draw = new Point((int) (ARC_RECT.centerX() + STROKE_WIDTH - RADIUS), (int) (ARC_RECT.centerY()));
    private static Point point3_draw = new Point((int) (ARC_RECT.centerX()), (int) (ARC_RECT.centerY()) + 10);
    private Rect mTextBounds = new Rect();
    private Rect mTextMaxBounds = new Rect();
    private int mSpeed;
    private LinearGradient mFillGrad;
    @ColorInt
    private int[] mFillColor = new int[4];

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

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int Speed) {
        mSpeed = Speed;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(ARC_RECT, START_ANGLE, MAX_ANGLE, false, FRONT_ARC_PAINT);
        canvas.drawCircle(ARC_RECT.centerX(), ARC_RECT.centerY(), 20, ARROW_PAINT);
        drawText(canvas);
        drawArrow(canvas);
    }

    private void drawArrow(
            Canvas canvas)
    {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1_draw.x, point1_draw.y);
        path.lineTo(point2_draw.x, point2_draw.y);
        path.lineTo(point3_draw.x, point3_draw.y);
        path.lineTo(point1_draw.x, point1_draw.y);
        path.close();
        canvas.rotate(MAX_ANGLE*mSpeed / MAX_SPEED-30,ARC_RECT.centerX(),ARC_RECT.centerY());
        canvas.drawPath(path, ARROW_PAINT);
    }
    private void drawText(Canvas canvas) {
        final String SpeedString = formatString(mSpeed);
        getTextBounds(SpeedString);

        float x = ARC_RECT.width() / 2f - mTextBounds.width() / 2f - mTextBounds.left + ARC_RECT.left;
        float xMin = ARC_RECT.centerX()-RADIUS;
        float xMax=ARC_RECT.centerX()+RADIUS-mTextMaxBounds.width();
        float y = ARC_RECT.centerY()+RADIUS/2;
        float yMinMax = ARC_RECT.centerY()+RADIUS-mTextMaxBounds.height()*2;
        canvas.drawText(SpeedString, x, y, TEXT_PAINT);

        canvas.drawText(STRING_MIN,xMin,yMinMax,TEXT1_PAINT);
        canvas.drawText(STRING_MAX,xMax,yMinMax,TEXT1_PAINT);
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

    private void extractAttributes(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        if (attrs != null)
        {
            final Resources.Theme theme = context.getTheme();
            final TypedArray typedArray = theme.obtainStyledAttributes(attrs, R.styleable.SpeedometerView, 0, R.style.SpeedometerViewDefault);
            try
            {
                mSpeed = typedArray.getInt(R.styleable.SpeedometerView_speed, 0);
                mFillColor[0] = typedArray.getColor(R.styleable.SpeedometerView_fill_color, getResources().getColor(R.color.colorAccent));
                mFillColor[1] = typedArray.getColor(R.styleable.SpeedometerView_fill_gradient1, getResources().getColor(R.color.colorGradient1));
                mFillColor[2] = typedArray.getColor(R.styleable.SpeedometerView_fill_gradient2, getResources().getColor(R.color.colorGradient2));
                mFillColor[3] = typedArray.getColor(R.styleable.SpeedometerView_fill_gradient3, getResources().getColor(R.color.colorGradient3));

            }
            finally
            {
                typedArray.recycle();
            }
        }
    }

    private void configureText()
    {
        TEXT_PAINT.setColor(mFillColor[0]);
        TEXT_PAINT.setStyle(Paint.Style.FILL);
        TEXT_PAINT.setTextSize(FONT_SIZE);
        TEXT1_PAINT.setColor(mFillColor[0]);
        TEXT1_PAINT.setStyle(Paint.Style.FILL);
        TEXT1_PAINT.setTextSize(2*FONT_SIZE/3);

    }
    private void configureFrontArc()
    {
        mFillGrad=new LinearGradient(0, RADIUS,2*RADIUS, RADIUS,
                new int[] { mFillColor[1],mFillColor[2],mFillColor[3]},
                new float[] { 0f, 0.4f, 0.9f }, Shader.TileMode.MIRROR);
        FRONT_ARC_PAINT.setShader(mFillGrad);
        FRONT_ARC_PAINT.setStyle(Paint.Style.STROKE);
        FRONT_ARC_PAINT.setStrokeWidth(STROKE_WIDTH);
    }

    private void configureArrow()
    {
        ARROW_PAINT.setStrokeWidth(2);
        ARROW_PAINT.setColor(mFillColor[0]);
        ARROW_PAINT.setStyle(Paint.Style.FILL_AND_STROKE);
        ARROW_PAINT.setAntiAlias(true);

    }

}
