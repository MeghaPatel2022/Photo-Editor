package com.xiaopo.flying.poiphoto.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AudienceNetworkAds;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.xiaopo.flying.poiphoto.ConnectionDetector;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.PhotoManager;
import com.xiaopo.flying.poiphoto.R;
import com.xiaopo.flying.poiphoto.datatype.Album;
import com.xiaopo.flying.poiphoto.ui.adapter.AlbumAdapter;
import com.xiaopo.flying.poiphoto.ui.custom.DividerLine;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.mopub.mobileads.MoPubView.MoPubAdSize.HEIGHT_50;

/**
 * the fragment to display album
 */
public class AlbumFragment extends Fragment {

    RecyclerView mAlbumList;

    private AlbumAdapter mAlbumAdapter;
    private PhotoManager mPhotoManager;
    private ImageView img_back;
    MoPubView moPubView;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.poiphoto_fragment_album, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AudienceNetworkAds.initialize(getContext());

        init(view);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Define.DEFAULT_REQUEST_PERMISSION_CODE);
        } else {
            startLoad();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (moPubView != null)
            moPubView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Define.DEFAULT_REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startLoad();
        }
    }

    private void startLoad() {
        new AlbumTask().execute();
    }

    private void init(final View view) {

        cd = new ConnectionDetector(getContext());

        isInternetPresent = cd.isConnectingToInternet();

        mAlbumList = view.findViewById(R.id.album_list);
        img_back = view.findViewById(R.id.img_back);

        mPhotoManager = new PhotoManager(getContext());

        mAlbumList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAlbumAdapter = new AlbumAdapter();
        mAlbumAdapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("Album")
                        .replace(R.id.container, PhotoFragment.newInstance(mAlbumAdapter.getBuckedId(position), mAlbumAdapter.getBuckedName(position)))
                        .commit();
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        mAlbumList.setAdapter(mAlbumAdapter);
        mAlbumList.addItemDecoration(new DividerLine());

        AudienceNetworkAds.initialize(getActivity());

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(getResources().getString(R.string.ad_unit_id_banner))
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .withLegitimateInterestAllowed(false)
                .build();

        MoPub.initializeSdk(getContext(), sdkConfiguration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                Log.d("Mopub", "SDK initialized");
            }
        });

        moPubView = view.findViewById(R.id.banner_mopubview);
        RelativeLayout.LayoutParams layoutParams =
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


    }


    private void refreshAlbumList(List<Album> alba) {
        mAlbumAdapter.refreshData(alba);
    }


    private class AlbumTask extends AsyncTask<Void, Integer, List<Album>> {

        @Override
        protected List<Album> doInBackground(Void... params) {
            List<Album> albumList = mPhotoManager.getAlbum();
            Collections.sort(albumList, new Comparator<Album>() {

                @Override
                public int compare(Album lhs, Album rhs) {
                    //here getTitle() method return app name...
                    return lhs.getName().compareTo(rhs.getName());
                }
            });

            return albumList;
        }

        @Override
        protected void onPostExecute(List<Album> alba) {
            super.onPostExecute(alba);
            refreshAlbumList(alba);
        }
    }

}
