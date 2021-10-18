package com.xiaopo.flying.poiphoto.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.R;
import com.xiaopo.flying.poiphoto.datatype.Photo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * adapter for Photo Adapter
 * Created by Flying SnowBean on 16-4-4.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private final String TAG = PhotoAdapter.class.getSimpleName();

    private List<Photo> mData;
    private final ArrayList<Photo> mSelectedPhotos;
    private final Set<Integer> mSelectedPhotoPositions;

    private OnPhotoSelectedListener mOnPhotoSelectedListener;
    private OnPhotoUnSelectedListener mOnPhotoUnSelectedListener;
    private OnSelectedMaxListener mOnSelectedMaxListener;

    private int mMaxCount = Define.DEFAULT_MAX_COUNT;

    private int mSelectedResId = R.drawable.photo_select;
    private boolean isSelect = false;

    public PhotoAdapter() {
        mSelectedPhotos = new ArrayList<>();
        mSelectedPhotoPositions = new HashSet<>();
    }

    public List<Photo> getData() {
        return mData;
    }

    public void setData(List<Photo> data) {
        mData = data;
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
    }

    public void setOnSelectedMaxListener(OnSelectedMaxListener onSelectedMaxListener) {
        mOnSelectedMaxListener = onSelectedMaxListener;
    }

    public void setOnPhotoSelectedListener(OnPhotoSelectedListener onPhotoSelectedListener) {
        mOnPhotoSelectedListener = onPhotoSelectedListener;
    }

    public void setOnPhotoUnSelectedListener(OnPhotoUnSelectedListener onPhotoUnSelectedListener) {
        mOnPhotoUnSelectedListener = onPhotoUnSelectedListener;
    }

    public ArrayList<Photo> getSelectedPhotos() {
        return mSelectedPhotos;
    }

    public ArrayList<String> getSelectedPhotoPaths() {
        ArrayList<String> paths = new ArrayList<>();
        for (Photo photo : mSelectedPhotos) {
            paths.add(photo.getPath());
        }

        return paths;
    }

    public void refreshData(List<Photo> dataNew) {
        mData = dataNew;
        mSelectedPhotos.clear();
        notifyDataSetChanged();
    }

    public void reset() {
        mSelectedPhotos.clear();
        mSelectedPhotoPositions.clear();

        notifyDataSetChanged();
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.poiphoto_item_photo, parent, false);
        return new PhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        holder.mShadow.setBackgroundResource(mSelectedResId);

        //解决View复用时的问题

        if (!mSelectedPhotoPositions.contains(position) && holder.mShadow.getVisibility() == View.VISIBLE && holder.img_selcted.getVisibility() == View.VISIBLE) {
            holder.mShadow.setVisibility(View.GONE);
            holder.img_selcted.setVisibility(View.GONE);
        } else if (mSelectedPhotoPositions.contains(position) && holder.mShadow.getVisibility() != View.VISIBLE && holder.img_selcted.getVisibility() == View.VISIBLE) {
            holder.mShadow.setVisibility(View.VISIBLE);
            holder.img_selcted.setVisibility(View.VISIBLE);
        }

        final String path = mData.get(position).getPath();
        Glide
                .with(holder.itemView.getContext())
                .load("file:///" + path)
                .centerInside()
                .into(holder.mIvPhoto);

//        Picasso.with(holder.itemView.getContext())
//                .load("file:///" + path)
//                .into(holder.mIvPhoto, new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//
//                    @Override
//                    public void onError() {
//                        Log.e(TAG, "Picasso failed load photo -> " + path);
//                    }
//                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (mSelectedPhotoPositions.contains(pos)) {

                    mSelectedPhotoPositions.remove(pos);
                    mSelectedPhotos.remove(mData.get(pos));
                    if (mOnPhotoUnSelectedListener != null) {
                        mOnPhotoUnSelectedListener.onPhotoUnSelected(mData.get(pos), pos);
                    }

                    holder.mShadow.setVisibility(View.GONE);
                    holder.img_selcted.setVisibility(View.GONE);

                } else {
                    if (mSelectedPhotoPositions.size() >= mMaxCount) {
                        if (mOnSelectedMaxListener != null) mOnSelectedMaxListener.onSelectedMax();
                    } else {
                        mSelectedPhotoPositions.add(pos);
                        mSelectedPhotos.add(mData.get(pos));
                        if (mOnPhotoSelectedListener != null) {
                            mOnPhotoSelectedListener.onPhotoSelected(mData.get(pos), pos);
                        }
                        holder.mShadow.setVisibility(View.VISIBLE);
                        holder.img_selcted.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setSelectedResId(int selectedResId, boolean isSelect) {
        mSelectedResId = selectedResId;
        this.isSelect = isSelect;
    }

    public interface OnPhotoSelectedListener {
        void onPhotoSelected(Photo photo, int position);
    }

    public interface OnPhotoUnSelectedListener {
        void onPhotoUnSelected(Photo photo, int position);
    }

    public interface OnSelectedMaxListener {
        void onSelectedMax();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView mIvPhoto, img_selcted;
        View mShadow;

        public PhotoViewHolder(View itemView) {
            super(itemView);

            mIvPhoto = itemView.findViewById(R.id.iv_photo);
            img_selcted = itemView.findViewById(R.id.img_selcted);
            mShadow = itemView.findViewById(R.id.shadow);
        }
    }
}
