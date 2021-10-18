package com.bbotdev.photoeditorlab.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.Const;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bbotdev.photoeditorlab.R.id.img_save;
import static com.bbotdev.photoeditorlab.Utils.Const.FolderName;

public class CropImgActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.cropImageView)
    CropImageView cropImageView;
    @BindView(R.id.ll_free)
    LinearLayout ll_free;
    @BindView(R.id.ll_1_1)
    LinearLayout ll_1_1;
    @BindView(R.id.ll_4_5)
    LinearLayout ll_4_5;
    @BindView(R.id.ll_story)
    LinearLayout ll_story;
    @BindView(R.id.ll_post)
    LinearLayout ll_post;
    @BindView(R.id.ll_cover)
    LinearLayout ll_cover;
    @BindView(R.id.ll_thumb)
    LinearLayout ll_thumb;
    @BindView(R.id.ll_header)
    LinearLayout ll_header;
    @BindView(R.id.img_save)
    ImageView img_save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_img);

        ButterKnife.bind(CropImgActivity.this);

//        FilePath = getIntent().getStringExtra("filePath");
        setBitmap();

        // OnCLick
        ll_free.setOnClickListener(this);
        ll_1_1.setOnClickListener(this);
        ll_4_5.setOnClickListener(this);
        ll_story.setOnClickListener(this);
        ll_post.setOnClickListener(this);
        ll_cover.setOnClickListener(this);
        ll_thumb.setOnClickListener(this);
        ll_header.setOnClickListener(this);
//        img_link.setOnClickListener(this);
        img_save.setOnClickListener(this);

//        et_heigh.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (isLink) {
////                    et_width.setText(String.valueOf(s));
//                }
//            }
//        });

//        et_width.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (isLink) {
////                    et_heigh.setText(String.valueOf(s));
//                }
//            }
//        });

    }

    private void setBitmap() {
        System.gc();

        runOnUiThread(() -> Glide.with(CropImgActivity.this)
                .asBitmap()
                .load(EditActivity.transform_bitmap)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(cropImageView));
//        cropImageView.setImageBitmap(EditActivity.transform_bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_free:
                cropImageView.setCropMode(CropImageView.CropMode.FREE);
                break;
            case R.id.ll_1_1:
                cropImageView.setCustomRatio(1, 1);
                break;
            case R.id.ll_4_5:
                cropImageView.setCustomRatio(4, 5);
                break;
            case R.id.ll_story:
                cropImageView.setCustomRatio(9, 16);
                break;
            case R.id.ll_post:
                cropImageView.setCustomRatio(2, 3);
                break;
            case R.id.ll_cover:
                cropImageView.setCustomRatio(41, 18);
                break;
            case R.id.ll_thumb:
                cropImageView.setCustomRatio(19, 9);
                break;
            case R.id.ll_header:
                cropImageView.setCustomRatio(3, 1);
                break;
//            case R.id.img_link:
//                if (isLink) {
//                    isLink = false;
//                    img_link.setImageDrawable(getResources().getDrawable(R.drawable.un_link));
//                } else {
//                    isLink = true;
//                    img_link.setImageDrawable(getResources().getDrawable(R.drawable.link_icon));
//                }
//                break;
            case R.id.img_save:
                SaveImage(cropImageView.getCroppedBitmap());
                break;
        }
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

            Intent intent = new Intent(CropImgActivity.this,EditActivity.class);
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