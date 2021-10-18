package com.bbotdev.photoeditorlab.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bbotdev.photoeditorlab.Holder.PicHolder;
import com.bbotdev.photoeditorlab.Interfaces.itemClickListener;
import com.bbotdev.photoeditorlab.Model.pictureFacer;
import com.bbotdev.photoeditorlab.R;

import java.util.ArrayList;

import static androidx.core.view.ViewCompat.setTransitionName;

public class picture_Adapter extends RecyclerView.Adapter<PicHolder> {

    private ArrayList<pictureFacer> pictureList;
    private Context pictureContx;
    private final itemClickListener picListerner;

    public picture_Adapter(ArrayList<pictureFacer> pictureList, Context pictureContx,itemClickListener picListerner) {
        this.pictureList = pictureList;
        this.pictureContx = pictureContx;
        this.picListerner = picListerner;
    }

    @NonNull
    @Override
    public PicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.list_images, parent, false);
        return new PicHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull PicHolder holder, int position) {
        final pictureFacer image = pictureList.get(position);

        Bitmap bitmap = BitmapFactory.decodeFile(image.getPicturePath());
//        holder.picture.setImageBitmap(bitmap);

        Glide.with(pictureContx)
                .load(bitmap)
                .apply(new RequestOptions().centerCrop())
                .into(holder.picture);

        setTransitionName(holder.picture, String.valueOf(position) + "_image");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picListerner.onPicClicked(holder,position, pictureList);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }
}
