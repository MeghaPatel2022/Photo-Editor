package com.github.veritas1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by DastanIqbal on 23/5/2017.
 */

public class HorizontalSlideColorPicker extends View {
    private final static int PANEL_SAT_VAL = 0;
    private final static int PANEL_HUE = 1;
    private final static int PANEL_ALPHA = 2;

    /**
     * The width in pixels of the border
     * surrounding all color panels.
     */
    private final static float BORDER_WIDTH_PX = 1;

    /**
     * The width in dp of the hue panel.
     */
    private float HUE_PANEL_WIDTH = 0f;
    /**
     * The distance in dp between the different
     * color panels.
     */
    private float PANEL_SPACING = 10f;
    /**
     * The radius in dp of the color palette tracker circle.
     */
    private float PALETTE_CIRCLE_TRACKER_RADIUS = 5f;
    /**
     * The dp which the tracker of the hue or alpha panel
     * will extend outside of its bounds.
     */
    private float RECTANGLE_TRACKER_OFFSET = 2f;


    private float mDensity = 1f;

    private OnColorChangedListener mListener;

    private Paint mHuePaint;
    private Paint mHueTrackerPaint;

    private Paint mBorderPaint;

    private Shader mHueShader;

    private int mAlpha = 0xff;
    private float mHue = 360f;
    private float mSat = 1f;
    private float mVal = 1f;

    private int mSliderTrackerColor = 0xff1c1c1c;
    private int mBorderColor = 0xff6E6E6E;

    /*
     * To remember which panel that has the "focus" when
     * processing hardware button data.
     */
    private int mLastTouchedPanel = PANEL_SAT_VAL;

    /**
     * Offset from the edge we must have or else
     * the finger tracker will get clipped when
     * it is drawn outside of the view.
     */
    private float mDrawingOffset;


    /*
     * Distance form the edges of the view
     * of where we are allowed to draw.
     */
    private RectF mDrawingRect;

    private RectF mHueRect;

    private Point mStartTouchPoint = null;

    public interface OnColorChangedListener {
        void onColorChanged(String color);
    }

    public HorizontalSlideColorPicker(Context context) {
        this(context, null);
    }

    public HorizontalSlideColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalSlideColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        PALETTE_CIRCLE_TRACKER_RADIUS *= mDensity;
        RECTANGLE_TRACKER_OFFSET *= mDensity;
        HUE_PANEL_WIDTH *= mDensity;
        PANEL_SPACING = PANEL_SPACING * mDensity;

        mDrawingOffset = calculateRequiredOffset();

        initPaintTools();

        //Needed for receiving trackball motion events.
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initPaintTools() {

        mHuePaint = new Paint();
        mHueTrackerPaint = new Paint();
        mBorderPaint = new Paint();

        mHueTrackerPaint.setColor(mSliderTrackerColor);
        mHueTrackerPaint.setStyle(Paint.Style.STROKE);
        mHueTrackerPaint.setStrokeWidth(2f * mDensity);
        mHueTrackerPaint.setAntiAlias(true);
    }

    private float calculateRequiredOffset() {
        float offset = Math.max(PALETTE_CIRCLE_TRACKER_RADIUS, RECTANGLE_TRACKER_OFFSET);
        offset = Math.max(offset, BORDER_WIDTH_PX * mDensity);

        return offset * 1.5f;
    }

    private int[] buildHueColorArray() {

        int[] hue = new int[361];

        int count = 0;
        for (int i = hue.length - 1; i >= 0; i--, count++) {
            hue[count] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }

        return hue;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (mDrawingRect.width() <= 0 || mDrawingRect.height() <= 0) return;

        drawHuePanel(canvas);

    }

    private void drawHuePanel(Canvas canvas) {

        final RectF rect = mHueRect;


        mBorderPaint.setColor(mBorderColor);
        canvas.drawRect(rect.left - BORDER_WIDTH_PX,
                rect.top - BORDER_WIDTH_PX,
                rect.right + BORDER_WIDTH_PX,
                rect.bottom + BORDER_WIDTH_PX,
                mBorderPaint);

        if (mHueShader == null) {
            mHueShader = new LinearGradient(rect.left, rect.bottom, rect.right, rect.bottom, buildHueColorArray(), null, Shader.TileMode.CLAMP);
            mHuePaint.setShader(mHueShader);
        }

        canvas.drawRect(rect, mHuePaint);

        float rectHeight = 4 * mDensity / 2;

        Point p = hueToPoint(mHue);

        RectF r = new RectF();
        r.left = p.y - rectHeight;
        r.right = p.y + rectHeight;
        r.top = rect.left - RECTANGLE_TRACKER_OFFSET;
        r.bottom = rect.right + RECTANGLE_TRACKER_OFFSET;


        canvas.drawRoundRect(r, 2, 2, mHueTrackerPaint);

    }


    private Point hueToPoint(float hue) {

        final RectF rect = mHueRect;
        final float width = rect.width();

        Point p = new Point();

        p.y = (int) (width - (hue * width / 360f) + rect.top);
        p.x = (int) rect.left;

        return p;
    }


    private float pointToHue(float x) {

        final RectF rect = mHueRect;

        float width = rect.width();

        if (x < rect.left) {
            x = 0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x = x - rect.left;
        }

        return 360f - (x * 360f / width);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        boolean update = false;


        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            switch (mLastTouchedPanel) {

                case PANEL_SAT_VAL:

                    float sat, val;

                    sat = mSat + x / 50f;
                    val = mVal - y / 50f;

                    if (sat < 0f) {
                        sat = 0f;
                    } else if (sat > 1f) {
                        sat = 1f;
                    }

                    if (val < 0f) {
                        val = 0f;
                    } else if (val > 1f) {
                        val = 1f;
                    }

                    mSat = sat;
                    mVal = val;

                    update = true;

                    break;

                case PANEL_HUE:

                    float hue = mHue - y * 10f;

                    if (hue < 0f) {
                        hue = 0f;
                    } else if (hue > 360f) {
                        hue = 360f;
                    }

                    mHue = hue;

                    update = true;

                    break;
            }


        }


        if (update) {

            if (mListener != null) {
                mListener.onColorChanged(convertToARGB(Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal})));
            }

            invalidate();
            return true;
        }


        return super.onTrackballEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean update = false;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                mStartTouchPoint = new Point((int) event.getX(), (int) event.getY());

                update = moveTrackersIfNeeded(event);

                break;

            case MotionEvent.ACTION_MOVE:

                update = moveTrackersIfNeeded(event);

                break;

            case MotionEvent.ACTION_UP:

                mStartTouchPoint = null;

                update = moveTrackersIfNeeded(event);

                break;

        }

        if (update) {

            if (mListener != null) {
                mListener.onColorChanged(convertToARGB(Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal})));
            }

            invalidate();
            return true;
        }


        return super.onTouchEvent(event);
    }

    private boolean moveTrackersIfNeeded(MotionEvent event) {

        if (mStartTouchPoint == null) return false;

        boolean update = false;

        int startX = mStartTouchPoint.x;
        int startY = mStartTouchPoint.y;


        if (mHueRect.contains(startX, startY)) {
            mLastTouchedPanel = PANEL_HUE;

            mHue = pointToHue(event.getX());

            update = true;
        }


        return update;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        HUE_PANEL_WIDTH = parentWidth;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mDrawingRect = new RectF();
        mDrawingRect.left = mDrawingOffset + getPaddingLeft();
        mDrawingRect.right = w - mDrawingOffset - getPaddingRight();
        mDrawingRect.top = mDrawingOffset + getPaddingTop();
        mDrawingRect.bottom = h - getPaddingBottom();

        setUpHueRect();
    }

    private void setUpHueRect() {
        final RectF dRect = mDrawingRect;

        float left = dRect.left +  BORDER_WIDTH_PX;
        float top = dRect.top + BORDER_WIDTH_PX;
        float bottom = dRect.bottom - BORDER_WIDTH_PX;
        float right = dRect.right - BORDER_WIDTH_PX;

        mHueRect = new RectF(left, top, right, bottom);
    }


    /**
     * Set a OnColorChangedListener to get notified when the color
     * selected by the user has changed.
     *
     * @param listener
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    /**
     * Set the color of the border surrounding all panels.
     *
     * @param color
     */
    public void setBorderColor(int color) {
        mBorderColor = color;
        invalidate();
    }

    /**
     * Get the color of the border surrounding all panels.
     */
    public int getBorderColor() {
        return mBorderColor;
    }

    /**
     * Get the current color this view is showing.
     *
     * @return the current color.
     */
    public int getColor() {
        return Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal});
    }

    /**
     * Set the color the view should show.
     *
     * @param color The color that should be selected.
     */
    public void setColor(int color) {
        setColor(color, false);
    }

    /**
     * Set the color this view should show.
     *
     * @param color    The color that should be selected.
     * @param callback If you want to get a callback to
     *                 your OnColorChangedListener.
     */
    public void setColor(int color, boolean callback) {

        int alpha = Color.alpha(color);

        float[] hsv = new float[3];

        Color.colorToHSV(color, hsv);

        mAlpha = alpha;
        mHue = hsv[0];
        mSat = hsv[1];
        mVal = hsv[2];

        if (callback && mListener != null) {
            mListener.onColorChanged(convertToARGB(Color.HSVToColor(mAlpha, new float[]{mHue, mSat, mVal})));
        }

        invalidate();
    }

    /**
     * Get the drawing offset of the color picker view.
     * The drawing offset is the distance from the side of
     * a panel to the side of the view minus the padding.
     * Useful if you want to have your own panel below showing
     * the currently selected color and want to align it perfectly.
     *
     * @return The offset in pixels.
     */
    public float getDrawingOffset() {
        return mDrawingOffset;
    }


    public void setSliderTrackerColor(int color) {
        mSliderTrackerColor = color;

        mHueTrackerPaint.setColor(mSliderTrackerColor);

        invalidate();
    }

    public int getSliderTrackerColor() {
        return mSliderTrackerColor;
    }

    private static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }
}