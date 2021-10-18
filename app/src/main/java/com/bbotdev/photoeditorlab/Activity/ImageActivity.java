package com.bbotdev.photoeditorlab.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bbotdev.photoeditorlab.Adapter.picture_Adapter;
import com.bbotdev.photoeditorlab.Holder.PicHolder;
import com.bbotdev.photoeditorlab.Interfaces.itemClickListener;
import com.bbotdev.photoeditorlab.Model.pictureFacer;
import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.Const;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageActivity extends AppCompatActivity implements itemClickListener {

    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.folderName)
    TextView folderName;
    @BindView(R.id.loader)
    ProgressBar load;
    @BindView(R.id.img_back)
    ImageView img_back;

    ArrayList<pictureFacer> allpictures;
    String foldePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ButterKnife.bind(ImageActivity.this);

        folderName.setText(getIntent().getStringExtra("folderName"));

        foldePath = getIntent().getStringExtra("folderPath");
        allpictures = new ArrayList<>();

        recycler.hasFixedSize();

        img_back.setOnClickListener(v -> onBackPressed());

        if (allpictures.isEmpty()) {
            load.setVisibility(View.VISIBLE);
            allpictures = getAllImagesByFolder(foldePath);
            recycler.setAdapter(new picture_Adapter(allpictures, ImageActivity.this, this));
            load.setVisibility(View.GONE);
        } else {

        }

    }

    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<pictureFacer> pics) {
//        System.gc();
        Const.FilePath = pics.get(position).getPicturePath();
        Intent move = new Intent(ImageActivity.this, EditActivity.class);
        move.putExtra("filePath", Const.FilePath);
        startActivity(move);
        overridePendingTransition(R.anim.fade_in, R.anim.stay);
    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

    }

    public ArrayList<pictureFacer> getAllImagesByFolder(String path) {
        ArrayList<pictureFacer> images = new ArrayList<>();
        Uri allVideosuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = ImageActivity.this.getContentResolver().query(allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            cursor.moveToFirst();
            do {
                pictureFacer pic = new pictureFacer();

                pic.setPicturName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));

                pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));

                pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));

                images.add(pic);
            } while (cursor.moveToNext());
            cursor.close();
            ArrayList<pictureFacer> reSelection = new ArrayList<>();
            for (int i = images.size() - 1; i > -1; i--) {
                reSelection.add(images.get(i));
            }
            images = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.fade_out);
    }
}