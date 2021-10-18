package com.bbotdev.photoeditorlab.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bbotdev.photoeditorlab.Interfaces.itemClickListener;
import com.bbotdev.photoeditorlab.Model.imageFolder;
import com.bbotdev.photoeditorlab.R;

import java.util.ArrayList;

public class pictureFolderAdapter extends RecyclerView.Adapter<pictureFolderAdapter.MyClassView> {

    private ArrayList<imageFolder> folders;
    private Context folderContx;
    private itemClickListener listenToClick;

    public pictureFolderAdapter(ArrayList<imageFolder> folders, Context folderContx, itemClickListener listen) {
        this.folders = folders;
        this.folderContx = folderContx;
        this.listenToClick = listen;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.list_folder, parent, false);
        return new MyClassView(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        final imageFolder folder = folders.get(position);

        Bitmap bitmap = BitmapFactory.decodeFile(folder.getFirstPic());
        holder.folderPic.setImageBitmap(bitmap);

//        Glide.with(folderContx)
//                .load(folder.getFirstPic())
//                .apply(new RequestOptions().centerCrop())
//                .into(holder.folderPic);

        //setting the number of images
        String text = ""+folder.getFolderName();
        String folderSizeString=""+folder.getNumberOfPics()+" Media";
        holder.folderSize.setText(folderSizeString);
        holder.folderName.setText(text);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenToClick.onPicClicked(folder.getPath(),folder.getFolderName());
            }
        });

    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        ImageView folderPic;
        TextView folderName;
        TextView folderSize;

        public MyClassView(@NonNull View itemView) {
            super(itemView);

            folderPic = itemView.findViewById(R.id.folderPic);
            folderName = itemView.findViewById(R.id.folderName);
            folderSize=itemView.findViewById(R.id.folderSize);
        }
    }
}
