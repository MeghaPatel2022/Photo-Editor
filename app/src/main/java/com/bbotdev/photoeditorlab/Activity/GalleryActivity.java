package com.bbotdev.photoeditorlab.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbotdev.photoeditorlab.Adapter.pictureFolderAdapter;
import com.bbotdev.photoeditorlab.Holder.PicHolder;
import com.bbotdev.photoeditorlab.Interfaces.itemClickListener;
import com.bbotdev.photoeditorlab.Model.imageFolder;
import com.bbotdev.photoeditorlab.Model.pictureFacer;
import com.bbotdev.photoeditorlab.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryActivity extends AppCompatActivity implements itemClickListener {

    @BindView(R.id.rv_folder)
    RecyclerView rv_folder;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.img_back)
    ImageView img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ButterKnife.bind(GalleryActivity.this);

        img_back.setOnClickListener(v -> onBackPressed());

        rv_folder.setLayoutManager(new LinearLayoutManager(GalleryActivity.this,RecyclerView.VERTICAL,false));

        ArrayList<imageFolder> folds = getPicturePaths();

        if (folds.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
        } else {
            pictureFolderAdapter folderAdapter = new pictureFolderAdapter(folds, GalleryActivity.this, this);
            rv_folder.setAdapter(folderAdapter);
        }

    }

    private ArrayList<imageFolder> getPicturePaths() {
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do {
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder + "/"));
                folderpaths = folderpaths + folder + "/";
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();
                    picFolders.add(folds);
                } else {
                    for (int i = 0; i < picFolders.size(); i++) {
                        if (picFolders.get(i).getPath().equals(folderpaths)) {
                            picFolders.get(i).setFirstPic(datapath);
                            picFolders.get(i).addpics();
                        }
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < picFolders.size(); i++) {
            Log.d("picture folders", picFolders.get(i).getFolderName() + " and path = " + picFolders.get(i).getPath() + " " + picFolders.get(i).getNumberOfPics());
        }

        return picFolders;
    }


    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<pictureFacer> pics) {

    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {
        Intent move = new Intent(GalleryActivity.this, ImageActivity.class);
        move.putExtra("folderPath", pictureFolderPath);
        move.putExtra("folderName", folderName);
        startActivity(move);
        overridePendingTransition(R.anim.fade_in, R.anim.stay);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.fade_out);
    }
}