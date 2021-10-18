package com.bbotdev.photoeditorlab.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.ConnectionDetector;
import com.bbotdev.photoeditorlab.Utils.Const;
import com.bumptech.glide.Glide;
import com.facebook.ads.AudienceNetworkAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.PhotoPicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bbotdev.photoeditorlab.Utils.Const.From;
import static com.mopub.mobileads.MoPubView.MoPubAdSize.HEIGHT_50;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.ll_gallery)
    LinearLayout ll_gallery;
    @BindView(R.id.ll_collage)
    LinearLayout ll_collage;
    @BindView(R.id.ll_camera)
    LinearLayout ll_camera;
    @BindView(R.id.img_edit)
    ImageView img_edit;
    @BindView(R.id.img_camera)
    ImageView img_camera;
    @BindView(R.id.img_collage)
    ImageView img_collage;
    @BindView(R.id.banner_mopubview)
    MoPubView moPubView;

    Uri imageUri;
    ContentValues values;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    public static int REQUEST_PERMISSION = 1;

    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(MainActivity.this);
        permission_fn();

        ll_gallery.setOnClickListener(this);
        ll_collage.setOnClickListener(this);
        ll_camera.setOnClickListener(this);

        cd = new ConnectionDetector(getApplicationContext());

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Glide.with(MainActivity.this)
                .load(R.drawable.edit)
                .into(img_edit);
        Glide.with(MainActivity.this)
                .load(R.drawable.camera)
                .into(img_camera);
        Glide.with(MainActivity.this)
                .load(R.drawable.collage)
                .into(img_collage);

        isInternetPresent = cd.isConnectingToInternet();

        // facebbok Ads
        AudienceNetworkAds.initialize(MainActivity.this);

    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                getPackageName(), null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Permission is needed to access files from your device...")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        }
    }

    private void permission_fn() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
            }
        } else {
            requestStoragePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moPubView.loadAd();
                SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(getResources().getString(R.string.ad_unit_id_banner))
                        .withLogLevel(MoPubLog.LogLevel.DEBUG)
                        .withLegitimateInterestAllowed(false)
                        .build();

                MoPub.initializeSdk(this, sdkConfiguration, new SdkInitializationListener() {
                    @Override
                    public void onInitializationFinished() {
                        Log.d("Mopub", "SDK initialized");
                    }
                });

                final RelativeLayout.LayoutParams layoutParams =
                        (RelativeLayout.LayoutParams) moPubView.getLayoutParams();
                moPubView.setLayoutParams(layoutParams);
                moPubView.setAdUnitId(getResources().getString(R.string.ad_unit_id_banner)); // Enter your Ad Unit ID from www.mopub.com
                moPubView.setAdSize(HEIGHT_50);
                moPubView.loadAd();
                moPubView.setBannerAdListener(new MoPubView.BannerAdListener() {
                    @Override
                    public void onBannerLoaded(@NonNull MoPubView banner) {

                    }

                    @Override
                    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                        Log.e("LLLL_Errr: ", errorCode.toString());
                        moPubView.loadAd();
                    }

                    @Override
                    public void onBannerClicked(MoPubView banner) {

                    }

                    @Override
                    public void onBannerExpanded(MoPubView banner) {

                    }

                    @Override
                    public void onBannerCollapsed(MoPubView banner) {

                    }
                });
            } else {
                requestStoragePermission();
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_gallery:
                From = "Gallery";
                showSelectedPhotoDialog();
                break;
            case R.id.ll_collage:
                Intent intent1 = new Intent(MainActivity.this, CollageActivity.class);
                startActivity(intent1);
                fireAnalytics("MainActivity", "Open collage Maker", "Open");
                overridePendingTransition(R.anim.fade_in, R.anim.stay);
                break;
            case R.id.ll_camera:
                From = "Camera";
                values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 101);
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
            Const.FilePath = path;

            fireAnalytics("MainActivity", "Gallery Image", "Image");
            fireDetailAnalytics(path, "Gallery");

            System.gc();
            Intent move = new Intent(MainActivity.this, EditActivity.class);
            move.putExtra("filePath", Const.FilePath);
            startActivity(move);
            overridePendingTransition(R.anim.fade_in, R.anim.stay);

        }
        if (requestCode == 101 && resultCode == RESULT_OK) {
            try {
                String imageurl = getRealPathFromURI(imageUri);

                Const.FilePath = imageurl;

                fireAnalytics("MainActivity", "Camera Capture", "Image");
                fireDetailAnalytics(imageurl, "Gallery");

                System.gc();
                Intent move = new Intent(MainActivity.this, EditActivity.class);
                move.putExtra("filePath", Const.FilePath);
                startActivity(move);
                overridePendingTransition(R.anim.fade_in, R.anim.stay);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    private String getCurrentDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
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
        params.putString("image_path", arg0);
        params.putString("select_from", arg1);
        mFirebaseAnalytics.logEvent("select_image", params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (moPubView != null) {
            moPubView.destroy();
            moPubView = null;
        }
    }
}