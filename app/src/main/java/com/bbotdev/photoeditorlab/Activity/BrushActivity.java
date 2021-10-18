package com.bbotdev.photoeditorlab.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.Const;
import com.bumptech.glide.Glide;
import com.github.veritas1.HorizontalSlideColorPicker;
import com.xw.repo.BubbleSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bbotdev.photoeditorlab.Utils.Const.FolderName;

public class BrushActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.roundedImageView)
    ImageView roundedImageView;
    @BindView(R.id.ll_myView)
    RelativeLayout ll_myView;
    @BindView(R.id.color_hpicker)
    HorizontalSlideColorPicker color_hpicker;
    @BindView(R.id.img_erase)
    ImageView img_erase;
    @BindView(R.id.ll_framView)
    RelativeLayout ll_framView;
    @BindView(R.id.textSizeSeekbar)
    BubbleSeekBar textSizeSeekbar;
    @BindView(R.id.img_undo)
    ImageView img_undo;
    @BindView(R.id.img_redo)
    ImageView img_redo;
    @BindView(R.id.rl_mainView)
    RelativeLayout rl_mainView;
    @BindView(R.id.img_save)
    ImageView img_save;

    @BindView(R.id.ll_brush1)
    LinearLayout ll_brush1;
    @BindView(R.id.ll_brush2)
    LinearLayout ll_brush2;
    @BindView(R.id.ll_brush3)
    LinearLayout ll_brush3;
    @BindView(R.id.ll_brush4)
    LinearLayout ll_brush4;


    MyView mv;
    private final ArrayList<Path> paths = new ArrayList<Path>();
    private final ArrayList<Paint> paints = new ArrayList<Paint>();
    private final ArrayList<Path> undonePaths = new ArrayList<Path>();

    private Paint mPaint;
    //    private MaskFilter mEmboss;
//    private MaskFilter mBlur;
    private Handler mHandler;

    Bitmap originalBitmap;
    private boolean isImage = false;
    private boolean isErasemode = false;
    float StrokeWidth = 10f;
    private File file;
    private boolean isShadow = false;
    private final boolean isErase = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mv = new MyView(BrushActivity.this);
        mv.setDrawingCacheEnabled(true);

        setContentView(R.layout.activity_brush);
        ButterKnife.bind(BrushActivity.this);

        Glide
                .with(BrushActivity.this)
                .load(getOriginalBitmap())
                .dontAnimate()
                .into(roundedImageView);

        ll_myView.addView(mv);
        mv.setTextColor();

        ll_brush1.setOnClickListener(this);
        ll_brush2.setOnClickListener(this);
        ll_brush3.setOnClickListener(this);
        ll_brush4.setOnClickListener(this);
        img_erase.setOnClickListener(this);
        img_undo.setOnClickListener(this);
        img_redo.setOnClickListener(this);
        img_save.setOnClickListener(this);

        textSizeSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                try {
                    StrokeWidth = progressFloat;
                    mPaint.setStrokeWidth(StrokeWidth);
                    return;
                } catch (Exception e) {
                    Log.e("LLLLL_Error: ", e.getLocalizedMessage());
                    return;
                }
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        color_hpicker.setSliderTrackerColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_brush1:
                isShadow = false;
                isImage = false;
                mv.changeNormal();
                break;
            case R.id.ll_brush2:
                isShadow = false;
                isImage = false;
                mv.changePaint();
                break;
            case R.id.ll_brush3:
                isImage = false;
                mv.changeShadow();
                break;
            case R.id.ll_brush4:
                isShadow = false;
                isImage = true;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brush_4);
                mv.changeBitmap(bitmap);
                break;
            case R.id.img_erase:
                isErasemode = true;
                mv.onEraser();
                break;
            case R.id.img_undo:
                mv.onClickUndo();
                break;
            case R.id.img_redo:
                mv.onClickRedo();
                break;
            case R.id.img_save:
                file = new File(getFilesDir(), "Brush_1.png");

                getLenseBitmap();
                Const.FilePath = file.getAbsolutePath();
                Intent intent = new Intent(BrushActivity.this, EditActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.stay);
                break;
        }
    }

    private Bitmap getOriginalBitmap() {
        originalBitmap = EditActivity.transform_bitmap;
        EditActivity.bitmapList.add(originalBitmap);

        return originalBitmap;
    }

    public class MyView extends View {

        private static final float TOUCH_TOLERANCE = 4;

        Context context;
        int i = 0;
        private Canvas mCanvas;
        private Path mPath;
        private final Paint mBitmapPaint;
        private float mX, mY;

        private Bitmap mBitmapBrush;
        private Vector2 mBitmapBrushDimensions;
        Bitmap bitmap;
        private final List<Vector2> mPositions = new ArrayList<Vector2>(100);
        private final List<Vector2> mPositions1 = new ArrayList<Vector2>(100);

        private final class Vector2 {
            public Vector2(float x, float y) {
                this.x = x;
                this.y = y;
            }

            public final float x;
            public final float y;
        }


        public MyView(Context c) {
            super(c);
            context = c;
            mPath = new Path();
            paths.add(mPath);
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

            setupPaint();
            setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
        }

        private void setupPaint() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(0xFFFF0000);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(StrokeWidth);
            mCanvas = new Canvas();
//            mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1},
//                    0.4f, 6, 3.5f);
//            mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

            // load your brush here
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.brush3);
            mBitmapBrush = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight() + 300, Bitmap.Config.ARGB_8888);
            mBitmapBrushDimensions = new Vector2(mBitmapBrush.getWidth(), mBitmapBrush.getHeight() + 200);

            if (isErase) {
                mPaint.setXfermode(new PorterDuffXfermode(
                        PorterDuff.Mode.CLEAR));
                mPaint.setStrokeWidth(16);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (isImage) {
                for (Path p : paths) {
                    canvas.drawPath(p, mPaint);
                }
                for (Vector2 pos : mPositions) {
                    canvas.drawBitmap(mBitmapBrush, pos.x - 20, pos.y - 50, null);
                }

            } else {
                for (Vector2 pos : mPositions) {
                    canvas.drawBitmap(mBitmapBrush, pos.x - 20, pos.y - 50, null);
                }
                for (Path p : paths) {
                    canvas.drawPath(p, mPaint);
                }
            }
            canvas.drawPath(mPath, mPaint);

        }


        public void changeBitmap(Bitmap bitmap) {
            // load your brush here
            mBitmapBrush = bitmap;
            mBitmapBrushDimensions = new Vector2(mBitmapBrush.getWidth(), mBitmapBrush.getHeight());
            invalidate();
        }

        public void changePaint() {
            mPaint.setPathEffect(new DashPathEffect(new float[]{30, 40}, 0));
            mPaint.setShadowLayer(0, 0, 0, Color.RED);
            invalidate();
        }

        public void changeShadow() {
            isShadow = true;
            mPaint.setPathEffect(new DashPathEffect(new float[]{0, 0}, 0));
            mPaint.setColor(context.getResources().getColor(R.color.white));
            mPaint.setShadowLayer(15, 5, 5, Color.RED);
            invalidate();
        }

        public void changeNormal() {
            mPaint.setColor(0xFFFF0000);
            mPaint.setPathEffect(new DashPathEffect(new float[]{0, 0}, 0));
            mPaint.setColor(context.getResources().getColor(R.color.white));
            mPaint.setShadowLayer(0, 0, 0, Color.RED);
        }

        public void onClickUndo() {
            if (paths.size() > 0) {
                undonePaths.add(paths.remove(paths.size() - 1));
                invalidate();
            }
            if (mPositions.size() > 0) {
                mPositions1.add(mPositions.remove(mPositions.size() - 1));
                invalidate();
            }
            //toast the user
        }

        public void onClickRedo() {
            if (undonePaths.size() > 0) {
                paths.add(undonePaths.remove(undonePaths.size() - 1));
                invalidate();
            }
            if (mPositions1.size() > 0) {
                mPositions.add(mPositions1.remove(mPositions1.size() - 1));
                invalidate();
            }
        }

        public void onEraser() {
            isErasemode = !isErasemode;
        }

        private void remove(int index) {
            paths.remove(index);
            invalidate();
        }


        private void touch_start(float x, float y) {
            //showDialog();
            undonePaths.clear();
            mPath.reset();
            mPath.moveTo(x, y);

            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            paths.add(mPath);
            mPath = new Path();
        }

        public void setTextColor() {
            try {
                color_hpicker.setOnColorChangedListener(color -> {
                    isImage = false;
                    if (!isShadow)
                        mPaint.setColor(Color.parseColor(color));
                    else
                        mPaint.setShadowLayer(12, 0, 0, Color.parseColor(color));
                    invalidate();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();
            if (isErasemode) {

                for (int i = 0; i < paths.size(); i++) {
                    RectF r = new RectF();
                    Point pComp = new Point((int) (event.getX()), (int) (event.getY()));

                    Path mPath = paths.get(i);
                    mPath.computeBounds(r, true);
                    if (r.contains(pComp.x, pComp.y)) {
                        Log.i("need to remove", "need to remove");
                        remove(i);
                        break;
                    }
                }
                return false;
            } else {
                if (isImage) {

                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_MOVE:
                            final float posX = event.getX();
                            final float posY = event.getY();
                            mPositions.add(new Vector2(posX - mBitmapBrushDimensions.x / 2, posY - mBitmapBrushDimensions.y / 2));
                            invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            invalidate();
                            break;
                    }

                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touch_start(x, y);
                            invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            touch_move(x, y);
                            invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            touch_up();
                            invalidate();
                            break;
                    }

                }
                return true;
            }

        }

    }

    private Bitmap getLenseBitmap() {
        rl_mainView.requestLayout();
        rl_mainView.setDrawingCacheEnabled(true);
        rl_mainView.layout(0, 0, rl_mainView.getMeasuredWidth(), rl_mainView.getMeasuredHeight());
        try {
            rl_mainView.getDrawingCache(true).compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            Log.e("LLLL_Error: ", e.getLocalizedMessage());
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/"+FolderName);
        File mySubDir = new File(myDir.getAbsolutePath() + "/temp");
        String path = "";
        Log.d("Data", "data: " + path);
        myDir.mkdirs();
        mySubDir.mkdirs();

        String currentDateAndTime = getCurrentDateAndTime();

        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        if (!mySubDir.exists()) {
            mySubDir.mkdirs();
        }

        File file = new File(mySubDir, "image_" + currentDateAndTime + ".png");
        path = file.getAbsolutePath();

        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            Const.FilePath = path;

            Intent intent = new Intent(BrushActivity.this,EditActivity.class);
            intent.putExtra("filePath",path);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.stay);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }

}

