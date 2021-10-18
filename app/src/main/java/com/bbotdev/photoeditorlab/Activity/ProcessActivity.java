package com.bbotdev.photoeditorlab.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bbotdev.photoeditorlab.Interfaces.Callback;
import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.DegreeSeekBar;
import com.bbotdev.photoeditorlab.Utils.FileUtils;
import com.bbotdev.photoeditorlab.Utils.PuzzleUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.PhotoPicker;
import com.xiaopo.flying.puzzle.PuzzleLayout;
import com.xiaopo.flying.puzzle.PuzzleView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.bbotdev.photoeditorlab.Utils.Const.FolderName;

public class ProcessActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int FLAG_CONTROL_LINE_SIZE = 1;
    private static final int FLAG_CONTROL_CORNER = 1 << 1;
    private final List<Target> targets = new ArrayList<>();
    private PuzzleLayout puzzleLayout;
    private List<String> bitmapPaint;
    private PuzzleView puzzleView;
    private DegreeSeekBar degreeSeekBar;
    private int deviceWidth = 0;

    private int controlFlag;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        deviceWidth = getResources().getDisplayMetrics().widthPixels;

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        int type = getIntent().getIntExtra("type", 0);
        int pieceSize = getIntent().getIntExtra("piece_size", 0);
        int themeId = getIntent().getIntExtra("theme_id", 0);
        bitmapPaint = getIntent().getStringArrayListExtra("photo_path");
        puzzleLayout = PuzzleUtils.getPuzzleLayout(type, pieceSize, themeId);

        fireDetailAnalytics(String.valueOf(pieceSize), "piece_size");

        initView();

        puzzleView.post(new Runnable() {
            @Override
            public void run() {
                loadPhoto();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void loadPhoto() {
        if (bitmapPaint == null) {
            loadPhotoFromRes();
            return;
        }

        final List<Bitmap> pieces = new ArrayList<>();

        final int count = Math.min(bitmapPaint.size(), puzzleLayout.getAreaCount());

        for (int i = 0; i < count; i++) {
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    pieces.add(bitmap);
                    if (pieces.size() == count) {
                        if (bitmapPaint.size() < puzzleLayout.getAreaCount()) {
                            for (int i = 0; i < puzzleLayout.getAreaCount(); i++) {
                                puzzleView.addPiece(pieces.get(i % count));
                            }
                        } else {
                            puzzleView.addPieces(pieces);
                        }
                    }
                    targets.remove(this);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            Picasso.with(this)
                    .load("file:///" + bitmapPaint.get(i))
                    .resize(deviceWidth, deviceWidth)
                    .centerInside()
                    .config(Bitmap.Config.RGB_565)
                    .into(target);

            targets.add(target);
        }
    }

    private void loadPhotoFromRes() {
        final List<Bitmap> pieces = new ArrayList<>();

        final int[] resIds = new int[]{
                R.drawable.add, R.drawable.add, R.drawable.add, R.drawable.add, R.drawable.add,
                R.drawable.add, R.drawable.add, R.drawable.add, R.drawable.add,
        };

        final int count =
                resIds.length > puzzleLayout.getAreaCount() ? puzzleLayout.getAreaCount() : resIds.length;

        for (int i = 0; i < count; i++) {
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    pieces.add(bitmap);
                    if (pieces.size() == count) {
                        if (resIds.length < puzzleLayout.getAreaCount()) {
                            for (int i = 0; i < puzzleLayout.getAreaCount(); i++) {
                                puzzleView.addPiece(pieces.get(i % count));
                            }
                        } else {
                            puzzleView.addPieces(pieces);
                        }
                    }
                    targets.remove(this);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            Picasso.with(this).load(resIds[i]).config(Bitmap.Config.RGB_565).into(target);

            targets.add(target);
        }
    }

    private void initView() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> share());

        puzzleView = findViewById(R.id.puzzle_view);
        degreeSeekBar = findViewById(R.id.degree_seek_bar);

        //TODO the method we can use to change the puzzle view's properties
        puzzleView.setPuzzleLayout(puzzleLayout);
        puzzleView.setTouchEnable(true);
        puzzleView.setNeedDrawLine(false);
        puzzleView.setNeedDrawOuterLine(false);
        puzzleView.setLineSize(4);
        puzzleView.setLineColor(Color.WHITE);
        puzzleView.setSelectedLineColor(Color.WHITE);
        puzzleView.setHandleBarColor(Color.WHITE);
        puzzleView.setAnimateDuration(300);
        puzzleView.setOnPieceSelectedListener((piece, position) -> {
            Log.e("LLLLL_PAth: ", "Path: " + piece.getPath() + "       draw: " + piece.getDrawable());
            Snackbar.make(puzzleView, "Piece " + position + " selected", Snackbar.LENGTH_SHORT).show();
        });

        // currently the SlantPuzzleLayout do not support padding
        puzzleView.setPiecePadding(10);

        ImageView btnReplace = findViewById(R.id.btn_replace);
        ImageView btnRotate = findViewById(R.id.btn_rotate);
        ImageView btnFlipHorizontal = findViewById(R.id.btn_flip_horizontal);
        ImageView btnFlipVertical = findViewById(R.id.btn_flip_vertical);
        ImageView btnBorder = findViewById(R.id.btn_border);
        ImageView btnCorner = findViewById(R.id.btn_corner);

        btnReplace.setOnClickListener(this);
        btnRotate.setOnClickListener(this);
        btnFlipHorizontal.setOnClickListener(this);
        btnFlipVertical.setOnClickListener(this);
        btnBorder.setOnClickListener(this);
        btnCorner.setOnClickListener(this);

        TextView btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(view -> {
            String root = Environment.getExternalStorageDirectory().toString();
            File file = FileUtils.getNewFile(ProcessActivity.this, root + "/" + FolderName + "/temp");
            FileUtils.savePuzzle(puzzleView, file, 100, new Callback() {
                @Override
                public void onSuccess() {
                    fireAnalytics("ProcessActivity", "Save Image", "Collage Maker");
                    fireDetailAnalytics(file.getAbsolutePath(), "Collage Save Image");

                    Intent intent = new Intent(ProcessActivity.this, EditActivity.class);
                    intent.putExtra("filePath", file.getAbsolutePath());
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.stay);
                }

                @Override
                public void onFailed() {
                    MDToast mdToast = MDToast.makeText(ProcessActivity.this, "Image saved error!", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR);
                    mdToast.show();
                }
            });
        });

        degreeSeekBar.setCurrentDegrees(puzzleView.getLineSize());
        degreeSeekBar.setDegreeRange(0, 30);
        degreeSeekBar.setScrollingListener(new DegreeSeekBar.ScrollingListener() {
            @Override
            public void onScrollStart() {

            }

            @Override
            public void onScroll(int currentDegrees) {
                switch (controlFlag) {
                    case FLAG_CONTROL_LINE_SIZE:
                        puzzleView.setLineSize(currentDegrees);
                        break;
                    case FLAG_CONTROL_CORNER:
                        puzzleView.setPieceRadian(currentDegrees);
                        break;
                }
            }

            @Override
            public void onScrollEnd() {

            }
        });
    }

    private void share() {
        final File file = FileUtils.getNewFile(this, getResources().getString(R.string.app_name));

        FileUtils.savePuzzle(puzzleView, file, 100, new Callback() {
            @Override
            public void onSuccess() {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                //Uri uri = Uri.fromFile(file);
                Uri uri = FileProvider.getUriForFile(ProcessActivity.this,
                        "com.bbotdev.photoeditorlab.provider", file);

                if (uri != null) {
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.prompt_share)));
                }
            }

            @Override
            public void onFailed() {
                Snackbar.make(puzzleView, R.string.prompt_share_failed, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_replace:
                showSelectedPhotoDialog();
                break;
            case R.id.btn_rotate:
                puzzleView.rotate(90f);
                break;
            case R.id.btn_flip_horizontal:
                puzzleView.flipHorizontally();
                break;
            case R.id.btn_flip_vertical:
                puzzleView.flipVertically();
                break;
            case R.id.btn_border:
                controlFlag = FLAG_CONTROL_LINE_SIZE;
                puzzleView.setNeedDrawLine(!puzzleView.isNeedDrawLine());
                if (puzzleView.isNeedDrawLine()) {
                    degreeSeekBar.setVisibility(View.VISIBLE);
                    degreeSeekBar.setCurrentDegrees(puzzleView.getLineSize());
                    degreeSeekBar.setDegreeRange(0, 30);
                } else {
                    degreeSeekBar.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.btn_corner:
                if (controlFlag == FLAG_CONTROL_CORNER && degreeSeekBar.getVisibility() == View.VISIBLE) {
                    degreeSeekBar.setVisibility(View.INVISIBLE);
                    return;
                }
                degreeSeekBar.setCurrentDegrees((int) puzzleView.getPieceRadian());
                controlFlag = FLAG_CONTROL_CORNER;
                degreeSeekBar.setVisibility(View.VISIBLE);
                degreeSeekBar.setDegreeRange(0, 100);
                break;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.DEFAULT_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> paths = data.getStringArrayListExtra(Define.PATHS);
            String path = paths.get(0);

            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    puzzleView.replace(bitmap, "");
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Snackbar.make(puzzleView, "Replace Failed!", Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            //noinspection SuspiciousNameCombination
            Picasso.with(this)
                    .load("file:///" + path)
                    .resize(deviceWidth, deviceWidth)
                    .centerInside()
                    .config(Bitmap.Config.RGB_565)
                    .into(target);
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
        params.putString("image_Path", arg0);
        params.putString("select_from", arg1);
        mFirebaseAnalytics.logEvent("select_image", params);
    }

}