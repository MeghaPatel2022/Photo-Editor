package com.bbotdev.photoeditorlab.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbotdev.photoeditorlab.Adapter.PuzzleAdapter;
import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.PuzzleUtils;
import com.bbotdev.photoeditorlab.layout.straight.StraightLayoutHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.xiaopo.flying.poiphoto.ConnectionDetector;
import com.xiaopo.flying.poiphoto.GetAllPhotoTask;
import com.xiaopo.flying.poiphoto.PhotoManager;
import com.xiaopo.flying.poiphoto.datatype.Photo;
import com.xiaopo.flying.poiphoto.ui.adapter.PhotoAdapter;
import com.xiaopo.flying.puzzle.PuzzleLayout;
import com.xiaopo.flying.puzzle.slant.SlantPuzzleLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CollageActivity extends AppCompatActivity {

    private static final String TAG = "CollageActivity";
    private final ArrayList<String> selectedPath = new ArrayList<>();
    private final List<Target> targets = new ArrayList<>();
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    private RecyclerView photoList;
    private RecyclerView puzzleList;
    private PuzzleAdapter puzzleAdapter;
    private PhotoAdapter photoAdapter;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private ArrayMap<String, Bitmap> arrayBitmaps = new ArrayMap<>();
    private PuzzleHandler puzzleHandler;
    private int deviceWidth;
    private FirebaseAnalytics mFirebaseAnalytics;
//    private AdView adView;
//    private FrameLayout adContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        puzzleHandler = new PuzzleHandler(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        deviceWidth = getResources().getDisplayMetrics().widthPixels;

        initView();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            }, 110);
        } else {
            loadPhoto();
        }

    }

    private void loadPhoto() {

        new GetAllPhotoTask() {
            @Override
            protected void onPostExecute(List<Photo> photos) {
                super.onPostExecute(photos);
                photoAdapter.refreshData(photos);
            }
        }.execute(new PhotoManager(this));
    }

    private void initView() {

        cd = new ConnectionDetector(CollageActivity.this);

        isInternetPresent = cd.isConnectingToInternet();

        photoList = findViewById(R.id.photo_list);
        puzzleList = findViewById(R.id.puzzle_list);

        photoAdapter = new PhotoAdapter();
        photoAdapter.setMaxCount(9);
        photoAdapter.setSelectedResId(R.drawable.photo_selected_shadow, true);

        photoList.setAdapter(photoAdapter);
        photoList.setLayoutManager(new GridLayoutManager(this, 4));

        puzzleAdapter = new PuzzleAdapter();
        puzzleList.setAdapter(puzzleAdapter);
        puzzleList.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        puzzleList.setHasFixedSize(true);

        puzzleAdapter.setOnItemClickListener(new PuzzleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(PuzzleLayout puzzleLayout, int themeId) {

                fireAnalytics("CollageActivity", "ThemeID", String.valueOf(themeId));
                fireDetailAnalytics(String.valueOf(selectedPath.size()), "Gallery");

                Intent intent = new Intent(CollageActivity.this, ProcessActivity.class);
                intent.putStringArrayListExtra("photo_path", selectedPath);
                if (puzzleLayout instanceof SlantPuzzleLayout) {
                    intent.putExtra("type", 0);
                } else {
                    intent.putExtra("type", 1);
                }
                intent.putExtra("piece_size", selectedPath.size());
                intent.putExtra("theme_id", themeId);

                startActivity(intent);
            }
        });

        photoAdapter.setOnPhotoSelectedListener((photo, position) -> {
            Message message = Message.obtain();
            message.what = 120;
            message.obj = photo.getPath();
            puzzleHandler.sendMessage(message);

            //prefetch the photo
            Picasso.with(CollageActivity.this)
                    .load("file:///" + photo.getPath())
                    .resize(deviceWidth, deviceWidth)
                    .centerInside()
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .fetch();
        });

        photoAdapter.setOnPhotoUnSelectedListener(new PhotoAdapter.OnPhotoUnSelectedListener() {
            @Override
            public void onPhotoUnSelected(Photo photo, int position) {
                Bitmap bitmap = arrayBitmaps.remove(photo.getPath());
                bitmaps.remove(bitmap);
                selectedPath.remove(photo.getPath());

                puzzleAdapter.refreshData(StraightLayoutHelper.getAllThemeLayout(bitmaps.size()), bitmaps);
            }
        });

        photoAdapter.setOnSelectedMaxListener(new PhotoAdapter.OnSelectedMaxListener() {
            @Override
            public void onSelectedMax() {
                MDToast mdToast = MDToast.makeText(CollageActivity.this, "You can select only 9 photos", MDToast.LENGTH_LONG, MDToast.TYPE_WARNING);
                mdToast.show();
            }
        });

        ImageView btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmaps == null || bitmaps.size() == 0) {
                    onBackPressed();
                    return;
                }

                arrayBitmaps.clear();
                bitmaps.clear();
                selectedPath.clear();

                photoAdapter.reset();
                puzzleHandler.sendEmptyMessage(119);
            }
        });

        ImageView btnMore = findViewById(R.id.btn_more);
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreDialog(view);
            }
        });

//        isInternetPresent = cd.isConnectingToInternet();
//        if (isInternetPresent) {
//
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    adContainer = findViewById(com.xiaopo.flying.poiphoto.R.id.adContainer);
//                    adView = new AdView(CollageActivity.this);
//                    adView.setAdUnitId(getString(com.xiaopo.flying.poiphoto.R.string.banner_ad));
//                    LoadAdaptiveBanner(CollageActivity.this, adView);
//                    adContainer.addView(adView);
//                }
//            }, 2000);
//        }
    }

    public void LoadAdaptiveBanner(Activity context, final AdView adView) {
        AdRequest adRequest = new AdRequest.Builder().build();


        AdSize adSize = getAdSize(context);
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        adView.loadAd(adRequest);

        adView.setVisibility(View.GONE);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

    }

    private AdSize getAdSize(Activity context) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    private void showMoreDialog(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        popupMenu.inflate(R.menu.menu_main);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_playground:
                        Intent intent = new Intent(CollageActivity.this, PlaygroundActivity.class);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 110
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            loadPhoto();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        arrayBitmaps.clear();
        arrayBitmaps = null;

        bitmaps.clear();
        bitmaps = null;
    }

    private void refreshLayout() {
        puzzleList.post(new Runnable() {
            @Override
            public void run() {
                puzzleAdapter.refreshData(PuzzleUtils.getPuzzleLayouts(bitmaps.size()), bitmaps);
            }
        });
    }

    public void fetchBitmap(final String path) {
        Log.d(TAG, "fetchBitmap: ");
        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Log.d(TAG, "onBitmapLoaded: ");

                arrayBitmaps.put(path, bitmap);
                bitmaps.add(bitmap);
                selectedPath.add(path);

                puzzleHandler.sendEmptyMessage(119);
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
                .load("file:///" + path)
                .resize(300, 300)
                .centerInside()
                .config(Bitmap.Config.RGB_565)
                .into(target);

        targets.add(target);
    }

    @Override
    public void onBackPressed() {
        finish();
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
        params.putString("total_select_image", arg0);
        params.putString("select_from", arg1);
        mFirebaseAnalytics.logEvent("select_image", params);
    }

    private static class PuzzleHandler extends Handler {
        private final WeakReference<CollageActivity> mReference;

        PuzzleHandler(CollageActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 119) {
                mReference.get().refreshLayout();
            } else if (msg.what == 120) {
                mReference.get().fetchBitmap((String) msg.obj);
            }
        }
    }
}