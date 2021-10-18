package com.bbotdev.photoeditorlab.Adapter;

import android.app.Activity;
import android.graphics.Typeface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.bbotdev.photoeditorlab.R;

import java.util.ArrayList;

public class FontsAdpater extends RecyclerView.Adapter<FontsAdpater.MyViewHolder> {
    private FontItemClickListener clickListener;
    private final ArrayList<String> fontlist;
    private Activity mContext;

    public interface FontItemClickListener {
        void onItemClick(View view, int i);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        public TextView txt_font_style;

        public MyViewHolder(View view) {
            super(view);
            this.txt_font_style = (TextView) view.findViewById(R.id.txt_font_style);
            view.setTag(view);
            view.setOnClickListener(this);
            this.txt_font_style.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (FontsAdpater.this.clickListener != null) {
                FontsAdpater.this.clickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public void setClickListener(FontItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public FontsAdpater(Activity activity, ArrayList<String> fontlist) {
        this.mContext = activity;
        this.fontlist = fontlist;
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_text, parent, false));
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.txt_font_style.setText("Hello");
        holder.txt_font_style.setTypeface(Typeface.createFromAsset(mContext.getAssets(),(String) fontlist.get(position)));
    }

    public int getItemCount() {
        return this.fontlist.size();
    }
}
