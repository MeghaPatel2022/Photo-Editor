package stickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import androidx.core.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatImageView;

import com.bbotdev.photoeditorlab.R;

public class StickerView extends AppCompatImageView {
    private static final String TAG = "StickerView";

    private Bitmap deleteBitmap;
    private Bitmap flipVBitmap;
    private Bitmap topBitmap;
    private Bitmap resizeBitmap;
    private Bitmap mBitmap;
    private Rect dst_delete;
    private Rect dst_resize;
    private Rect dst_flipV;
    private Rect dst_top;
    private int deleteBitmapWidth;
    private int deleteBitmapHeight;
    private int resizeBitmapWidth;
    private int resizeBitmapHeight;
    //Horizontal mirroring
    private int flipVBitmapWidth;
    private int flipVBitmapHeight;
    //Top
    private int topBitmapWidth;
    private int topBitmapHeight;
    private Paint localPaint;
    private int mScreenwidth, mScreenHeight;
    private static final float BITMAP_SCALE = 0.7f;
    private PointF mid = new PointF();
    private OperationListener operationListener;
    private float lastRotateDegree;

    //Is the second finger down
    private boolean isPointerDown = false;
    //The finger movement distance must exceed this value
    private final float pointerLimitDis = 20f;
    private final float pointerZoomCoeff = 0.09f;
    /**
     * The length of the diagonal
     */
    private float lastLength;
    private boolean isInResize = false;

    private Matrix matrix = new Matrix();
    /**
     * Is it inside the four lines
     */
    private boolean isInSide;

    private float lastX, lastY;
    /**
     * Is in edit mode
     */
    private boolean isInEdit = true;

    private float MIN_SCALE = 0.5f;

    private float MAX_SCALE = 1.2f;

    private double halfDiagonalLength;

    private float oringinWidth = 0;

    //Initial distance when pinch zoom
    private float oldDis;

    private final long stickerId;

    private DisplayMetrics dm;

    //Horizontal mirroring
    private boolean isHorizonMirror = false;

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        stickerId = 0;
        init();
    }

    public StickerView(Context context) {
        super(context);
        stickerId = 0;
        init();
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        stickerId = 0;
        init();
    }

    private void init() {

        dst_delete = new Rect();
        dst_resize = new Rect();
        dst_flipV = new Rect();
        dst_top = new Rect();
        localPaint = new Paint();
        localPaint.setColor(getResources().getColor(R.color.white));
        localPaint.setAntiAlias(true);
        localPaint.setDither(true);
        localPaint.setStyle(Style.STROKE);
        localPaint.setStrokeWidth(2.0f);
        dm = getResources().getDisplayMetrics();
        mScreenwidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {


            float[] arrayOfFloat = new float[9];
            matrix.getValues(arrayOfFloat);
            float f1 = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
            float f2 = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
            float f3 = arrayOfFloat[0] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
            float f4 = arrayOfFloat[3] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
            float f5 = 0.0F * arrayOfFloat[0] + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
            float f6 = 0.0F * arrayOfFloat[3] + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];
            float f7 = arrayOfFloat[0] * this.mBitmap.getWidth() + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
            float f8 = arrayOfFloat[3] * this.mBitmap.getWidth() + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];

            canvas.save();
            canvas.drawBitmap(mBitmap, matrix, null);

            dst_delete.left = (int) (f3 - deleteBitmapWidth / 2);
            dst_delete.right = (int) (f3 + deleteBitmapWidth / 2);
            dst_delete.top = (int) (f4 - deleteBitmapHeight / 2);
            dst_delete.bottom = (int) (f4 + deleteBitmapHeight / 2);

            dst_resize.left = (int) (f7 - resizeBitmapWidth / 2);
            dst_resize.right = (int) (f7 + resizeBitmapWidth / 2);
            dst_resize.top = (int) (f8 - resizeBitmapHeight / 2);
            dst_resize.bottom = (int) (f8 + resizeBitmapHeight / 2);

            dst_top.left = (int) (f1 - flipVBitmapWidth / 2);
            dst_top.right = (int) (f1 + flipVBitmapWidth / 2);
            dst_top.top = (int) (f2 - flipVBitmapHeight / 2);
            dst_top.bottom = (int) (f2 + flipVBitmapHeight / 2);

            dst_flipV.left = (int) (f5 - topBitmapWidth / 2);
            dst_flipV.right = (int) (f5 + topBitmapWidth / 2);
            dst_flipV.top = (int) (f6 - topBitmapHeight / 2);
            dst_flipV.bottom = (int) (f6 + topBitmapHeight / 2);
            if (isInEdit) {

                canvas.drawLine(f1, f2, f3, f4, localPaint);
                canvas.drawLine(f3, f4, f7, f8, localPaint);
                canvas.drawLine(f5, f6, f7, f8, localPaint);
                canvas.drawLine(f5, f6, f1, f2, localPaint);

                canvas.drawBitmap(deleteBitmap, null, dst_delete, null);
                canvas.drawBitmap(resizeBitmap, null, dst_resize, null);
                canvas.drawBitmap(flipVBitmap, null, dst_flipV, null);
                canvas.drawBitmap(topBitmap, null, dst_top, null);
            }

            canvas.restore();
        }
    }

    @Override
    public void setImageResource(int resId) {
        setBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }

    public void setBitmap(Bitmap bitmap) {
        matrix.reset();
        mBitmap = bitmap;
        setDiagonalLength();
        initBitmaps();
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        oringinWidth = w;
        float initScale = (MIN_SCALE + MAX_SCALE) / 2;
        matrix.postScale(initScale, initScale, w / 2, h / 2);

        matrix.postTranslate(mScreenwidth / 2 - w / 2, (mScreenwidth) / 2 - h / 2);
        invalidate();
    }


    private void setDiagonalLength() {
        halfDiagonalLength = Math.hypot(mBitmap.getWidth(), mBitmap.getHeight()) / 2;
    }

    private void initBitmaps() {
        //When the aspect ratio of the picture is larger than the height, it is calculated according to the width. The zoom size changes according to the size of the picture. The minimum is 1/8 of the picture and the maximum is the screen width.
        if (mBitmap.getWidth() >= mBitmap.getHeight()) {
            float minWidth = mScreenwidth / 8;
            if (mBitmap.getWidth() < minWidth) {
                MIN_SCALE = 1f;
            } else {
                MIN_SCALE = 1.0f * minWidth / mBitmap.getWidth();
            }

            if (mBitmap.getWidth() > mScreenwidth) {
                MAX_SCALE = 1;
            } else {
                MAX_SCALE = 1.0f * mScreenwidth / mBitmap.getWidth();
            }
        } else {
            //When the picture height is larger than the width, it is calculated according to the picture height
            float minHeight = mScreenwidth / 8;
            if (mBitmap.getHeight() < minHeight) {
                MIN_SCALE = 1f;
            } else {
                MIN_SCALE = 1.0f * minHeight / mBitmap.getHeight();
            }

            if (mBitmap.getHeight() > mScreenwidth) {
                MAX_SCALE = 1;
            } else {
                MAX_SCALE = 1.0f * mScreenwidth / mBitmap.getHeight();
            }
        }

        topBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.duplicate);
        deleteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.delete);
        flipVBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flip);
        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.zoom);

        deleteBitmapWidth = (int) (deleteBitmap.getWidth() * BITMAP_SCALE / 1.5);
        deleteBitmapHeight = (int) (deleteBitmap.getHeight() * BITMAP_SCALE / 1.5);

        resizeBitmapWidth = (int) (resizeBitmap.getWidth() * BITMAP_SCALE / 1.5);
        resizeBitmapHeight = (int) (resizeBitmap.getHeight() * BITMAP_SCALE / 1.5);

        flipVBitmapWidth = (int) (flipVBitmap.getWidth() * BITMAP_SCALE / 1.5);
        flipVBitmapHeight = (int) (flipVBitmap.getHeight() * BITMAP_SCALE / 1.5);

        topBitmapWidth = (int) (topBitmap.getWidth() * BITMAP_SCALE / 1.5);
        topBitmapHeight = (int) (topBitmap.getHeight() * BITMAP_SCALE / 1.5);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        boolean handled = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isInButton(event, dst_delete)) {
                    if (operationListener != null) {
                        operationListener.onDeleteClick();
                    }
                } else if (isInResize(event)) {
                    isInResize = true;
                    lastRotateDegree = rotationToStartPoint(event);
                    midPointToStartPoint(event);
                    lastLength = diagonalLength(event);
                } else if (isInButton(event, dst_flipV)) {
                    //Horizontal mirroring
                    PointF localPointF = new PointF();
                    midDiagonalPoint(localPointF);
                    matrix.postScale(-1.0F, 1.0F, localPointF.x, localPointF.y);
                    isHorizonMirror = !isHorizonMirror;
                    invalidate();
                } else if (isInButton(event, dst_top)) {
                    //Top
                    bringToFront();
                    if (operationListener != null) {
                        operationListener.onTop(this);
                    }
                } else if (isInBitmap(event)) {
                    isInSide = true;
                    lastX = event.getX(0);
                    lastY = event.getY(0);
                } else {
                    handled = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (spacing(event) > pointerLimitDis) {
                    oldDis = spacing(event);
                    isPointerDown = true;
                    midPointToStartPoint(event);
                } else {
                    isPointerDown = false;
                }
                isInSide = false;
                isInResize = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //Pinch zoom
                if (isPointerDown) {
                    float scale;
                    float disNew = spacing(event);
                    if (disNew == 0 || disNew < pointerLimitDis) {
                        scale = 1;
                    } else {
                        scale = disNew / oldDis;
                        //Slow zoom
                        scale = (scale - 1) * pointerZoomCoeff + 1;
                    }
                    float scaleTemp = (scale * Math.abs(dst_flipV.left - dst_resize.left)) / oringinWidth;
                    if (((scaleTemp <= MIN_SCALE)) && scale < 1 ||
                            (scaleTemp >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                    } else {
                        lastLength = diagonalLength(event);
                    }
                    matrix.postScale(scale, scale, mid.x, mid.y);
                    invalidate();
                } else if (isInResize) {

                    matrix.postRotate((rotationToStartPoint(event) - lastRotateDegree) * 2, mid.x, mid.y);
                    lastRotateDegree = rotationToStartPoint(event);

                    float scale = diagonalLength(event) / lastLength;

                    if (((diagonalLength(event) / halfDiagonalLength <= MIN_SCALE)) && scale < 1 ||
                            (diagonalLength(event) / halfDiagonalLength >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                        if (!isInResize(event)) {
                            isInResize = false;
                        }
                    } else {
                        lastLength = diagonalLength(event);
                    }
                    matrix.postScale(scale, scale, mid.x, mid.y);

                    invalidate();
                } else if (isInSide) {
                    float x = event.getX(0);
                    float y = event.getY(0);
                    //TODO 移动区域判断 不能超出屏幕
                    matrix.postTranslate(x - lastX, y - lastY);
                    lastX = x;
                    lastY = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isInResize = false;
                isInSide = false;
                isPointerDown = false;
                break;

        }
        if (handled && operationListener != null) {
            operationListener.onEdit(this);
        }
        return handled;
    }

    /**
     * Calculate the angle and other attributes of the picture
     *
     * @param model
     * @return
     */
    public StickerPropertyModel calculate(StickerPropertyModel model) {
        float[] v = new float[9];
        matrix.getValues(v);
        // translation is simple
        float tx = v[Matrix.MTRANS_X];
        float ty = v[Matrix.MTRANS_Y];
        Log.d(TAG, "tx : " + tx + " ty : " + ty);
        // calculate real scale
        float scalex = v[Matrix.MSCALE_X];
        float skewy = v[Matrix.MSKEW_Y];
        float rScale = (float) Math.sqrt(scalex * scalex + skewy * skewy);
        Log.d(TAG, "rScale : " + rScale);
        // calculate the degree of rotation
        float rAngle = Math.round(Math.atan2(v[Matrix.MSKEW_X], v[Matrix.MSCALE_X]) * (180 / Math.PI));
        Log.d(TAG, "rAngle : " + rAngle);

        PointF localPointF = new PointF();
        midDiagonalPoint(localPointF);

        Log.d(TAG, " width  : " + (mBitmap.getWidth() * rScale) + " height " + (mBitmap.getHeight() * rScale));

        float minX = localPointF.x;
        float minY = localPointF.y;

        Log.d(TAG, "midX : " + minX + " midY : " + minY);
        model.setDegree((float) Math.toRadians(rAngle));
        //TODO Percentage of screen
        float precentWidth = (mBitmap.getWidth() * rScale) / mScreenwidth;
        model.setScaling(precentWidth);
        model.setxLocation(minX / mScreenwidth);
        model.setyLocation(minY / mScreenwidth);
        model.setStickerId(stickerId);
        if (isHorizonMirror) {
            model.setHorizonMirror(1);
        } else {
            model.setHorizonMirror(2);
        }
        return model;
    }

    /**
     * Is it inside the four lines
     * After the picture is rotated, there may be a diamond-shaped state. The coordinate range of 4 points cannot be used to determine whether the clicked area is in the picture.
     *
     * @return
     */
    private boolean isInBitmap(MotionEvent event) {
        float[] arrayOfFloat1 = new float[9];
        this.matrix.getValues(arrayOfFloat1);
        //Upper left corner
        float f1 = 0.0F * arrayOfFloat1[0] + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f2 = 0.0F * arrayOfFloat1[3] + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];
        //Upper right corner
        float f3 = arrayOfFloat1[0] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f4 = arrayOfFloat1[3] * this.mBitmap.getWidth() + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];
        //Bottom left
        float f5 = 0.0F * arrayOfFloat1[0] + arrayOfFloat1[1] * this.mBitmap.getHeight() + arrayOfFloat1[2];
        float f6 = 0.0F * arrayOfFloat1[3] + arrayOfFloat1[4] * this.mBitmap.getHeight() + arrayOfFloat1[5];
        //Bottom right corner
        float f7 = arrayOfFloat1[0] * this.mBitmap.getWidth() + arrayOfFloat1[1] * this.mBitmap.getHeight() + arrayOfFloat1[2];
        float f8 = arrayOfFloat1[3] * this.mBitmap.getWidth() + arrayOfFloat1[4] * this.mBitmap.getHeight() + arrayOfFloat1[5];

        float[] arrayOfFloat2 = new float[4];
        float[] arrayOfFloat3 = new float[4];
        //Determine the range of the X direction
        arrayOfFloat2[0] = f1;//左上的x
        arrayOfFloat2[1] = f3;//右上的x
        arrayOfFloat2[2] = f7;//右下的x
        arrayOfFloat2[3] = f5;//左下的x
        //Determine the range in the Y direction
        arrayOfFloat3[0] = f2;//左上的y
        arrayOfFloat3[1] = f4;//右上的y
        arrayOfFloat3[2] = f8;//右下的y
        arrayOfFloat3[3] = f6;//左下的y
        return pointInRect(arrayOfFloat2, arrayOfFloat3, event.getX(0), event.getY(0));
    }

    /**
     * Determine whether the point is inside a rectangle
     *
     * @param xRange
     * @param yRange
     * @param x
     * @param y
     * @return
     */
    private boolean pointInRect(float[] xRange, float[] yRange, float x, float y) {
        //Length of four sides
        double a1 = Math.hypot(xRange[0] - xRange[1], yRange[0] - yRange[1]);
        double a2 = Math.hypot(xRange[1] - xRange[2], yRange[1] - yRange[2]);
        double a3 = Math.hypot(xRange[3] - xRange[2], yRange[3] - yRange[2]);
        double a4 = Math.hypot(xRange[0] - xRange[3], yRange[0] - yRange[3]);
        //The distance from the point to be detected to the four points
        double b1 = Math.hypot(x - xRange[0], y - yRange[0]);
        double b2 = Math.hypot(x - xRange[1], y - yRange[1]);
        double b3 = Math.hypot(x - xRange[2], y - yRange[2]);
        double b4 = Math.hypot(x - xRange[3], y - yRange[3]);

        double u1 = (a1 + b1 + b2) / 2;
        double u2 = (a2 + b2 + b3) / 2;
        double u3 = (a3 + b3 + b4) / 2;
        double u4 = (a4 + b4 + b1) / 2;

        //The area of ​​the rectangle
        double s = a1 * a2;
        //Helen formula calculate the area of ​​4 triangles
        double ss = Math.sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2))
                + Math.sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3))
                + Math.sqrt(u3 * (u3 - a3) * (u3 - b3) * (u3 - b4))
                + Math.sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1));
        return Math.abs(s - ss) < 0.5;


    }


    /**
     * Whether the touch is in a certain button range
     *
     * @param event
     * @param rect
     * @return
     */
    private boolean isInButton(MotionEvent event, Rect rect) {
        int left = rect.left;
        int right = rect.right;
        int top = rect.top;
        int bottom = rect.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    /**
     * Whether the touch is in the stretched area
     *
     * @param event
     * @return
     */
    private boolean isInResize(MotionEvent event) {
        int left = -20 + this.dst_resize.left;
        int top = -20 + this.dst_resize.top;
        int right = 20 + this.dst_resize.right;
        int bottom = 20 + this.dst_resize.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    /**
     * The midpoint between the touched position and the upper left corner of the picture
     *
     * @param event
     */
    private void midPointToStartPoint(MotionEvent event) {
        float[] arrayOfFloat = new float[9];
        matrix.getValues(arrayOfFloat);
        float f1 = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
        float f2 = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = f1 + event.getX(0);
        float f4 = f2 + event.getY(0);
        mid.set(f3 / 2, f4 / 2);
    }

    /**
     * Calculate where the diagonals cross
     *
     * @param paramPointF
     */
    private void midDiagonalPoint(PointF paramPointF) {
        float[] arrayOfFloat = new float[9];
        this.matrix.getValues(arrayOfFloat);
        float f1 = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
        float f2 = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = arrayOfFloat[0] * this.mBitmap.getWidth() + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
        float f4 = arrayOfFloat[3] * this.mBitmap.getWidth() + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];
        float f5 = f1 + f3;
        float f6 = f2 + f4;
        paramPointF.set(f5 / 2.0F, f6 / 2.0F);
    }


    /**
     * In the process of sliding rotation, the origin of the upper left corner is always used as the absolute coordinates to calculate the deflection angle
     *
     * @param event
     * @return
     */
    private float rotationToStartPoint(MotionEvent event) {

        float[] arrayOfFloat = new float[9];
        matrix.getValues(arrayOfFloat);
        float x = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
        float y = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        double arc = Math.atan2(event.getY(0) - y, event.getX(0) - x);
        return (float) Math.toDegrees(arc);
    }

    /**
     * The distance from the touch point to the midpoint of the rectangle
     *
     * @param event
     * @return
     */
    private float diagonalLength(MotionEvent event) {
        float diagonalLength = (float) Math.hypot(event.getX(0) - mid.x, event.getY(0) - mid.y);
        return diagonalLength;
    }

    /**
     * Calculate the distance between two fingers
     */
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    public interface OperationListener {
        void onDeleteClick();

        void onEdit(StickerView stickerView);

        void onTop(StickerView stickerView);
    }

    public void setOperationListener(OperationListener operationListener) {
        this.operationListener = operationListener;
    }

    public void setInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;
        invalidate();
    }
}
