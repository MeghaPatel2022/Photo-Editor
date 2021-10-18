package com.bbotdev.photoeditorlab.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.Const;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.wang.avi.AVLoadingIndicatorView;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.PhotoPicker;
import com.xw.repo.BubbleSeekBar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import stickerview.StickerView;

import static com.bbotdev.photoeditorlab.Utils.Const.FolderName;
import static com.bbotdev.photoeditorlab.Utils.Const.From;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final String TAG = EditActivity.class.getSimpleName();
    public static Bitmap transform_bitmap;
    public static ArrayList<Bitmap> bitmapList = new ArrayList<>();
    public static int udoPosition = bitmapList.size() - 1;
    private static StickerView mCurrentView;
    private final ArrayList mViews = new ArrayList();
    private final ArrayList<String> lensList = new ArrayList<>();
    private final ArrayList<ImageView> imagList = new ArrayList<>();
    private final PointF start = new PointF();
    private final PointF mid = new PointF();
    @BindView(R.id.framimg)
    RelativeLayout framimg;
    @BindView(R.id.ll_lens)
    LinearLayout ll_lens;
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.ll_type)
    LinearLayout ll_type;
    @BindView(R.id.btn_cancel)
    Button btn_cancel;
    @BindView(R.id.btn_save)
    Button btn_save;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.ll_cropView)
    LinearLayout ll_cropView;
    @BindView(R.id.ll_crop)
    LinearLayout ll_crop;
    @BindView(R.id.ll_crop_1)
    LinearLayout ll_crop_1;
    @BindView(R.id.ll_rotate)
    LinearLayout ll_rotate;
    @BindView(R.id.ll_vertical)
    LinearLayout ll_vertical;
    @BindView(R.id.ll_horizontal)
    LinearLayout ll_horizontal;
    @BindView(R.id.ll_text)
    LinearLayout ll_text;
    @BindView(R.id.ll_imgAdd)
    LinearLayout ll_imgAdd;
    @BindView(R.id.ll_imgLans)
    LinearLayout ll_imgLans;
    @BindView(R.id.textOpacitySeekBar)
    BubbleSeekBar textOpacitySeekBar;
    @BindView(R.id.textLansOpacitySeekBar)
    BubbleSeekBar textLansOpacitySeekBar;
    @BindView(R.id.rl_toolbar_add)
    RelativeLayout rl_toolbar_add;
    @BindView(R.id.img_close)
    ImageView img_close;
    @BindView(R.id.img_done)
    ImageView img_done;
    @BindView(R.id.ll_filter)
    LinearLayout ll_filter;
    @BindView(R.id.rlLoading)
    RelativeLayout rlLoading;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.ll_lensView)
    LinearLayout ll_lensView;
    @BindView(R.id.rv_lens)
    RecyclerView rv_lens;
    @BindView(R.id.imgLens)
    ImageView imgLens;
    @BindView(R.id.ll_brush)
    LinearLayout ll_brush;
    @BindView(R.id.rl_add_img)
    RelativeLayout rl_add_img;
    @BindView(R.id.ll_add_img)
    LinearLayout ll_add_img;
    @BindView(R.id.rl_mainImg)
    RelativeLayout rl_mainImg;
    @BindView(R.id.tv_save)
    TextView tv_save;
    @BindView(R.id.rl_lense)
    RelativeLayout rl_lense;
    @BindView(R.id.rl_toolbar_lans)
    RelativeLayout rl_toolbar_lans;
    @BindView(R.id.img_cancel_lans)
    ImageView img_cancel_lans;
    @BindView(R.id.img_lans_done)
    ImageView img_lans_done;
    @BindView(R.id.rl_toolbar)
    RelativeLayout rl_toolbar;
    @BindView(R.id.img_undo)
    ImageView img_undo;
    @BindView(R.id.img_redo)
    ImageView img_redo;

    Bitmap original_bitmap;
    BottomSheetBehavior behavior;
    int windowwidth;
    int windowheight;
    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    float oldDist = 1f;
    String savedItemClicked;
    MediaScannerConnection msConn;
    private String FilePath = "";
    private File file;
    private boolean isZoomAndRotate;
    private boolean isOutSide;
    private int mode = NONE;
    private float xCoOrdinate, yCoOrdinate;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(filePath));
        } catch (IOException e) {
        }
        return bitmap;
    }

    void startAnim() {
        rlLoading.setVisibility(View.VISIBLE);
        avi.smoothToShow();
        // or avi.smoothToShow();
    }

    void stopAnim() {
        rlLoading.setVisibility(View.GONE);
        avi.smoothToHide();
        // or avi.smoothToHide();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ButterKnife.bind(EditActivity.this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FilePath = getIntent().getStringExtra("filePath");
        setBitmap();

        setStickerView();

        ll_crop.setOnClickListener(this);
        ll_crop_1.setOnClickListener(this);
        ll_filter.setOnClickListener(this);
        ll_rotate.setOnClickListener(this);
        ll_vertical.setOnClickListener(this);
        ll_horizontal.setOnClickListener(this);
        ll_text.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        ll_lens.setOnClickListener(this);
        ll_brush.setOnClickListener(this);
        ll_add_img.setOnClickListener(this);
        img_back.setOnClickListener(this);
        img_done.setOnClickListener(this);
        img_close.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        img_cancel_lans.setOnClickListener(this);
        img_lans_done.setOnClickListener(this);
        img_undo.setOnClickListener(this);
        img_redo.setOnClickListener(this);

        behavior = BottomSheetBehavior.from(ll_type);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        framimg.setOnTouchListener((v, event) -> {
            if (!mViews.isEmpty()) {
                file = new File(getFilesDir(), "Lens_1.png");
                mCurrentView.setInEdit(false);
                transform_bitmap = getLenseBitmap();
                EditActivity.bitmapList.add(transform_bitmap);
                if (ll_imgAdd.getVisibility() == View.VISIBLE) {
                    Const.collapse(ll_imgAdd);
                }
                if (ll_imgLans.getVisibility() == View.VISIBLE) {
                    Const.collapse(ll_imgLans);
                }
                if (ll_cropView.getVisibility() == View.VISIBLE) {
                    Const.collapse(ll_cropView);
                }
            }
            return false;
        });

        new LongOperation().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // set FilterAdapter
    }

    private void setStickerView() {
        if (getIntent().getByteArrayExtra("TextBitmap") != null && getIntent().getByteArrayExtra("TextBitmap").length > 0) {
            byte[] byteArray = getIntent().getByteArrayExtra("TextBitmap");
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            initBitmapofStiker(bitmap);
        }
    }

    private void setBitmap() {
        if (From.equals("Camera")) {
            From = "Other";
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];
            original_bitmap = rotate(BitmapFactory.decodeFile(FilePath, options), 90);
        } else {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];
            original_bitmap = BitmapFactory.decodeFile(FilePath, options);
        }

        transform_bitmap = original_bitmap;
        EditActivity.bitmapList.add(transform_bitmap);
        imgMain.setImageBitmap(original_bitmap);
    }

    private void setLensAdapter() {
        rv_lens.setLayoutManager(new LinearLayoutManager(EditActivity.this, RecyclerView.HORIZONTAL, false));
        LensAdapter lensAdapter = new LensAdapter();
        rv_lens.setAdapter(lensAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_crop) {

            if (ll_lensView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_lensView);
            }
            if (ll_imgAdd.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgAdd);
            }
            if (ll_imgLans.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgLans);
            }
            if (ll_cropView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_cropView);
            } else {
                Const.expand(ll_cropView);
            }
        } else if (v.getId() == R.id.img_undo) {

            if (EditActivity.udoPosition < 1) {
                EditActivity.udoPosition = EditActivity.bitmapList.size() - 1;
            }
            EditActivity.udoPosition--;
            transform_bitmap = EditActivity.bitmapList.get(EditActivity.udoPosition);
            EditActivity.bitmapList.add(transform_bitmap);
            setImage();
        } else if (v.getId() == R.id.img_redo) {
            if (EditActivity.udoPosition < EditActivity.bitmapList.size() - 2) {
                EditActivity.udoPosition++;
                transform_bitmap = EditActivity.bitmapList.get(EditActivity.udoPosition);
                EditActivity.bitmapList.add(transform_bitmap);
                setImage();
            }
        } else if (v.getId() == R.id.ll_crop_1) {
            System.gc();
            fireAnalytics("EditActivity", "Open crop", "CropImage");

            Intent intent = new Intent(EditActivity.this, CropImgActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.stay);
        } else if (v.getId() == R.id.ll_vertical) {
            fireAnalytics("EditActivity", "Open crop option", "Vertical Image");
            Bitmap bInput = transform_bitmap;
            Matrix matrix = new Matrix();
            matrix.preScale(1.0f, -1.0f);
            Bitmap bOutput = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);

            transform_bitmap = bOutput;
            EditActivity.bitmapList.add(transform_bitmap);
            setImage();
        } else if (v.getId() == R.id.ll_horizontal) {
            fireAnalytics("EditActivity", "Open crop option", "HorizontalImage");
            Bitmap bInput = transform_bitmap;
            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f);
            Bitmap bOutput = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);

            transform_bitmap = bOutput;
            EditActivity.bitmapList.add(transform_bitmap);
            setImage();
        } else if (v.getId() == R.id.ll_rotate) {
            fireAnalytics("EditActivity", "Open crop option", "RotateImage");
            Bitmap bInput = transform_bitmap;
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);

            System.gc();
            bInput.recycle();
            transform_bitmap = rotatedBitmap;
            EditActivity.bitmapList.add(transform_bitmap);

            setImage();
        } else if (v.getId() == R.id.ll_filter) {
            if (ll_cropView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_cropView);
            }
            fireAnalytics("EditActivity", "FilterApply", "Open");
            Intent intent = new Intent(EditActivity.this, FilterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.stay);
        } else if (v.getId() == R.id.ll_text) {

            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (v.getId() == R.id.btn_save) {
            fireAnalytics("EditActivity", "Add Text", "Open");
            Intent intent = new Intent(EditActivity.this, AddTextActivity.class);
            intent.putExtra("StringText", et_name.getText().toString().trim());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.stay);
        } else if (v.getId() == R.id.btn_cancel) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (v.getId() == R.id.ll_lens) {

            if (ll_cropView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_cropView);
            }
            if (ll_lensView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_lensView);
            } else {
                Const.expand(ll_lensView);
            }
            if (rl_toolbar_lans.getVisibility() == View.VISIBLE) {
                rl_toolbar_lans.setVisibility(View.GONE);
            } else {
                rl_toolbar_lans.setVisibility(View.VISIBLE);
            }
            if (rl_toolbar.getVisibility() == View.VISIBLE) {
                rl_toolbar.setVisibility(View.GONE);
            } else {
                rl_toolbar.setVisibility(View.VISIBLE);
            }
            if (ll_imgLans.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgLans);
            } else {
                Const.expand(ll_imgLans);
            }
        } else if (v.getId() == R.id.img_lans_done) {

            if (ll_cropView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_cropView);
            }
            if (ll_lensView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_lensView);
            } else {
                Const.expand(ll_lensView);
            }
            if (rl_toolbar_lans.getVisibility() == View.VISIBLE) {
                rl_toolbar_lans.setVisibility(View.GONE);
            } else {
                rl_toolbar_lans.setVisibility(View.VISIBLE);
            }
            if (rl_toolbar.getVisibility() == View.VISIBLE) {
                rl_toolbar.setVisibility(View.GONE);
            } else {
                rl_toolbar.setVisibility(View.VISIBLE);
            }
            if (ll_imgLans.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgLans);
            } else {
                Const.expand(ll_imgLans);
            }
            fireAnalytics("EditActivity", "Lens Apply", "Open");
            file = new File(getFilesDir(), "LanseImg_1.png");
            transform_bitmap = getLenseBitmap();
            EditActivity.bitmapList.add(transform_bitmap);
        } else if (v.getId() == R.id.img_cancel_lans) {
            rl_lense.removeAllViews();
            if (ll_cropView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_cropView);
            }
            if (ll_lensView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_lensView);
            } else {
                Const.expand(ll_lensView);
            }
            if (rl_toolbar.getVisibility() == View.VISIBLE) {
                rl_toolbar.setVisibility(View.GONE);
            } else {
                rl_toolbar.setVisibility(View.VISIBLE);
            }
            if (rl_toolbar_lans.getVisibility() == View.VISIBLE) {
                rl_toolbar_lans.setVisibility(View.GONE);
            } else {
                rl_toolbar_lans.setVisibility(View.VISIBLE);
            }
            if (ll_imgLans.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgLans);
            } else {
                Const.expand(ll_imgLans);
            }
        } else if (v.getId() == R.id.ll_brush) {
            fireAnalytics("EditActivity", "Brush Apply", "Open");
            Intent intent = new Intent(EditActivity.this, BrushActivity.class);
            intent.putExtra("StringText", et_name.getText().toString().trim());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.stay);
        } else if (v.getId() == R.id.ll_add_img) {

            showSelectedPhotoDialog();
        } else if (v.getId() == R.id.img_back) {
            onBackPressed();
        } else if (v.getId() == R.id.img_done) {
            fireAnalytics("EditActivity", "Add Extra Image", "Open");
            file = new File(getFilesDir(), "AddImg_1.png");
            transform_bitmap = getLenseBitmap();
            EditActivity.bitmapList.add(transform_bitmap);
            if (rl_toolbar_add.getVisibility() == View.GONE) {
                rl_toolbar_add.setVisibility(View.VISIBLE);
            } else {
                rl_toolbar_add.setVisibility(View.GONE);
            }
            if (ll_imgAdd.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgAdd);
            } else {
                Const.expand(ll_imgAdd);
            }

            imagList.get(0).setOnTouchListener(null);
        } else if (v.getId() == R.id.img_close) {
            rl_add_img.removeAllViews();
            if (ll_imgAdd.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgAdd);
            } else {
                Const.expand(ll_imgAdd);
            }
            if (rl_toolbar_add.getVisibility() == View.GONE) {
                rl_toolbar_add.setVisibility(View.VISIBLE);
            } else {
                rl_toolbar_add.setVisibility(View.GONE);
            }
        } else if (v.getId() == R.id.tv_save) {
            fireAnalytics("EditActivity", "Save Edited Image", "Open");
            file = new File(getFilesDir(), "Save_1.png");
            new SaveOperation().execute(getLenseBitmap());
        }
    }

    private void showSelectedPhotoDialog() {
        PhotoPicker.newInstance()
                .setAlbumTitle("Album")
                .setPhotoTitle("Photo")
                .setMaxCount(1)
                .setMaxNotice("can not select more than 1")
                .pick(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.DEFAULT_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> paths = data.getStringArrayListExtra(Define.PATHS);
            if (rl_toolbar_add.getVisibility() == View.GONE) {
                rl_toolbar_add.setVisibility(View.VISIBLE);
            } else {
                rl_toolbar_add.setVisibility(View.GONE);
            }

            if (ll_lensView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_lensView);
            }
            if (ll_cropView.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_cropView);
            }
            if (ll_imgAdd.getVisibility() == View.VISIBLE) {
                Const.collapse(ll_imgAdd);
            } else {
                Const.expand(ll_imgAdd);
            }


            for (int i = 0; i < paths.size(); i++) {
                ImageView imgView = new ImageView(EditActivity.this);
                imagList.add(imgView);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                imgView.setLayoutParams(lp);

                fireDetailAnalytics(paths.get(i), "Select extra image");

                Glide.with(EditActivity.this)
                        .load("file:///" + paths.get(i))
                        .override(300, 300)
                        .into(imgView);
                rl_add_img.addView(imgView);

                textOpacitySeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                    @Override
                    public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                        try {
                            imgView.setAlpha(((float) progress) / 100.0f);
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

                windowwidth = getWindowManager().getDefaultDisplay().getWidth();
                windowheight = getWindowManager().getDefaultDisplay().getHeight();

                imgView.setScaleType(ImageView.ScaleType.MATRIX);
                imgView.setOnTouchListener((v, event) -> {
                    // Handle touch events here...
                    ImageView view = (ImageView) v;
                    view.bringToFront();
                    viewTransformation(view, event);
                    return true;
                });

            }

        }
    }

    private void viewTransformation(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();

                start.set(event.getX(), event.getY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    mode = ZOOM;
                }

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
                isZoomAndRotate = false;
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false;
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            float scale = newDist1 / oldDist * view.getScaleX();
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            view.setRotation(view.getRotation() + (newRot - d));
                        }
                    }
                }
                break;
        }
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (int) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void setImage() {
        System.gc();

        runOnUiThread(() -> Glide.with(EditActivity.this)
                .asBitmap()
                .load(transform_bitmap)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgMain));

//        runOnUiThread(() -> imgMain.setImageBitmap(transform_bitmap));
    }

    public void initBitmapofStiker(final Bitmap bitmap) {
        if (bitmap != null) {
            try {
//                set_somtthing = true;
                final StickerView stickerView = new StickerView(this);
                stickerView.setBitmap(bitmap);
                stickerView.setOperationListener(new StickerView.OperationListener() {
                    public void onDeleteClick() {
                        mViews.remove(stickerView);
                        framimg.removeView(stickerView);
                    }

                    public void onEdit(StickerView stickerView) {
                        mCurrentView.setInEdit(false);
                        mCurrentView = stickerView;
                        mCurrentView.setInEdit(true);
                    }

                    @Override
                    public void onTop(StickerView stickerView) {
                        int position = mViews.indexOf(stickerView);
                        initBitmapofStiker(bitmap);
                        if (position == mViews.size() - 1) {
                            return;
                        }
                        StickerView stickerTemp = (StickerView) mViews.remove(position);
                        mViews.add(mViews.size(), stickerTemp);

                    }
                });
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-1, -1);
                lp.addRule(13);
                this.framimg.addView(stickerView, lp);
                this.mViews.add(stickerView);
                setCurrentEdit(stickerView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setCurrentEdit(StickerView stickerView) {
        try {
            if (mCurrentView != null) {
                mCurrentView.setInEdit(false);
            }
            mCurrentView = stickerView;
            stickerView.setInEdit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getLenseBitmap() {
        rl_mainImg.requestLayout();
        rl_mainImg.setDrawingCacheEnabled(true);
        rl_mainImg.layout(0, 0, rl_mainImg.getMeasuredWidth(), rl_mainImg.getMeasuredHeight());
        try {
            rl_mainImg.getDrawingCache(true).compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            Log.e("LLLL_Error: ", e.getLocalizedMessage());
        }
        System.out.println("Sagartext_path:" + this.file.getAbsolutePath());

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return bitmap;
    }

    @Override
    public void onBackPressed() {
        final Dialog dial = new Dialog(EditActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.item_album_delete_confirmation);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);
        dial.findViewById(R.id.delete_yes).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fireAnalytics("EditActivity", "Discard Changes", "Image");
                EditActivity.bitmapList.clear();
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.stay);
            }
        });
        dial.findViewById(R.id.delete_no).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dial.dismiss();
            }
        });
        dial.show();
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + FolderName);
        File mySubDir = new File(myDir.getAbsolutePath());
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

        fireDetailAnalytics(file.getAbsolutePath(), "Save ImagePath");

        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            scanPhoto(file.toString());

            File dir = new File(root + "/" + FolderName + "/temp");
            deleteFolder(dir.getAbsolutePath());
            File dir1 = getFilesDir();
            deleteFolder(dir1.getAbsolutePath());

            Const.FilePath = path;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
                Log.i("msClient obj", "connection established");
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
                Log.i("msClient obj", "scan completed");
            }
        });
        this.msConn.connect();
    }


    private String getCurrentDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }

    private void deleteFolder(String path) {
        Log.e("LLLLLL_Path: ", path);
        File dir = new File(path);
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class LensAdapter extends RecyclerView.Adapter<LensAdapter.MyClassView> {

        Bitmap originalBitmap;

        @NonNull
        @Override
        public LensAdapter.MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View cell = inflater.inflate(R.layout.list_lens, parent, false);
            return new MyClassView(cell);
        }

        @Override
        public void onBindViewHolder(@NonNull LensAdapter.MyClassView holder, int position) {
            originalBitmap = transform_bitmap;
            if (position == 0) {
                holder.img_lens.setImageDrawable(getResources().getDrawable(R.drawable.none));
            } else {
                holder.img_lens.setImageBitmap(getBitmapFromAsset(EditActivity.this, "lens/" + (position - 1) + ".png"));
                holder.img_lens.setId(position);
                Glide
                        .with(EditActivity.this)
                        .asBitmap()
                        .load(getBitmapFromAsset(EditActivity.this, "lens/" + (position - 1) + ".png"))
                        .into(holder.img_source);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != 0) {
                        file = new File(getFilesDir(), "Lens_1.png");
//                        imgLens.setImageBitmap(getBitmapFromAsset(EditActivity.this, "lens/" + (position - 1) + ".png"));

                        ImageView imgView = new ImageView(EditActivity.this);
                        imagList.add(imgView);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        imgView.setLayoutParams(lp);
                        Glide.with(EditActivity.this)
                                .asBitmap()
                                .load(getBitmapFromAsset(EditActivity.this, "lens/" + (position - 1) + ".png"))
                                .override(700, 400)
                                .into(imgView);
                        rl_lense.addView(imgView);

                        textLansOpacitySeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                            @Override
                            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                                try {
                                    imgView.setAlpha(((float) progress) / 100.0f);
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

                        windowwidth = getWindowManager().getDefaultDisplay().getWidth();
                        windowheight = getWindowManager().getDefaultDisplay().getHeight();

                        imgView.setScaleType(ImageView.ScaleType.MATRIX);
                        imgView.setOnTouchListener(new View.OnTouchListener() {
                            @SuppressLint("ClickableViewAccessibility")
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                // Handle touch events here...
                                ImageView view = (ImageView) v;
                                view.bringToFront();
                                viewTransformation(view, event);
                                return true;
                            }
                        });


                    } else {
                        rl_lense.removeAllViews();
                        transform_bitmap = originalBitmap;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return 12;
        }

        public class MyClassView extends RecyclerView.ViewHolder {

            CircleImageView img_source;
            ImageView img_lens;

            public MyClassView(@NonNull View itemView) {
                super(itemView);

                img_source = itemView.findViewById(R.id.img_source);
                img_lens = itemView.findViewById(R.id.img_lens);
            }
        }
    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            setLensAdapter();
//            setFilterAdapter();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Executed"))
                stopAnim();
            // You might want to change "executed" for the returned string
            // passed into onPostExecute(), but that is up to you
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }
    }

    private final class SaveOperation extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            SaveImage(params[0]);
            System.gc();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            MDToast mdToast;
            if (result.equals("Executed")) {
                mdToast = MDToast.makeText(EditActivity.this, "Image saved successfully!", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS);
            } else {
                mdToast = MDToast.makeText(EditActivity.this, "Image saved error!", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR);
            }
            stopAnim();
            mdToast.show();
            Intent intent = new Intent(EditActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.stay);

            // txt.setText(result);
            // You might want to change "executed" for the returned string
            // passed into onPostExecute(), but that is up to you
        }
    }

    private void fireAnalytics(String arg0, String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg0);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg1);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void fireDetailAnalytics(String arg0, String arg1) {
        Bundle params = new Bundle();
        params.putString("image_path", arg0);
        params.putString("select_from", arg1);
        mFirebaseAnalytics.logEvent("select_image", params);
    }

}