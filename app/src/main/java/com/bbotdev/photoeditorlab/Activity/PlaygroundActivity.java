package com.bbotdev.photoeditorlab.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbotdev.photoeditorlab.Adapter.PuzzleAdapter;
import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.PuzzleUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.xiaopo.flying.puzzle.PuzzleLayout;
import com.xiaopo.flying.puzzle.slant.SlantPuzzleLayout;

public class PlaygroundActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initView();
        prefetchResPhoto();
    }

    private void prefetchResPhoto() {
        final int[] resIds = new int[] {
                R.drawable.add, R.drawable.add, R.drawable.add, R.drawable.add, R.drawable.add,
                R.drawable.add, R.drawable.add, R.drawable.add, R.drawable.add,
        };

        for (int resId : resIds) {
            Picasso.with(this)
                    .load(resId)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .config(Bitmap.Config.RGB_565)
                    .fetch();
        }
    }

    private void initView() {
        final RecyclerView puzzleList = findViewById(R.id.puzzle_list);
        puzzleList.setLayoutManager(new GridLayoutManager(this, 2));

        PuzzleAdapter puzzleAdapter = new PuzzleAdapter();

        puzzleList.setAdapter(puzzleAdapter);

        puzzleAdapter.refreshData(PuzzleUtils.getAllPuzzleLayouts(), null);

        puzzleAdapter.setOnItemClickListener(new PuzzleAdapter.OnItemClickListener() {
            @Override public void onItemClick(PuzzleLayout puzzleLayout, int themeId) {

                fireAnalytics("PlayGroundActivity", "ThemeID", String.valueOf(themeId));
                fireDetailAnalytics(String.valueOf(puzzleLayout.getAreaCount()), "FrameSelect");


                Intent intent = new Intent(PlaygroundActivity.this, ProcessActivity.class);
                if (puzzleLayout instanceof SlantPuzzleLayout) {
                    intent.putExtra("type", 0);
                } else {
                    intent.putExtra("type", 1);
                }
                intent.putExtra("piece_size", puzzleLayout.getAreaCount());
                intent.putExtra("theme_id", themeId);

                startActivity(intent);
            }
        });

        ImageView btnClose = findViewById(R.id.btn_cancel);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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
        params.putString("Select_frame", arg0);
        params.putString("select_from", arg1);
        mFirebaseAnalytics.logEvent("select_image", params);
    }
}