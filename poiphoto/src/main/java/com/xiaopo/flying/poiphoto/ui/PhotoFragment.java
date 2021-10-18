package com.xiaopo.flying.poiphoto.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.xiaopo.flying.poiphoto.Configure;
import com.xiaopo.flying.poiphoto.ConnectionDetector;
import com.xiaopo.flying.poiphoto.Define;
import com.xiaopo.flying.poiphoto.PhotoManager;
import com.xiaopo.flying.poiphoto.R;
import com.xiaopo.flying.poiphoto.datatype.Photo;
import com.xiaopo.flying.poiphoto.ui.adapter.PhotoAdapter;

import java.util.List;

/**
 * the fragment to display photo
 */
public class PhotoFragment extends Fragment {

    Boolean isInternetPresent = false;
    private static final String TAG = PhotoFragment.class.getSimpleName();
    RecyclerView mPhotoList;
    String buckedName;
    private PhotoAdapter mAdapter;
    private PhotoManager mPhotoManager;
    private ImageView img_back;
    private TextView tv_done, folderName;
    ConnectionDetector cd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FrameLayout adContainer;
    private AdView adView;
    private MoPubInterstitial mInterstitial;
//    private InterstitialAd interstitialAd;
//    InterstitialAdListener interstitialAdListener;

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    public static PhotoFragment newInstance(String bucketId, String Name) {
        Bundle bundle = new Bundle();
        bundle.putString("bucketId", bucketId);
        bundle.putString("bucketName", Name);
        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(bundle);

        return fragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoManager = new PhotoManager(getContext());
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.poiphoto_fragment_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String buckedId = getArguments().getString("bucketId");
        buckedName = getArguments().getString("bucketName");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        init(view);


        new PhotoTask().execute(buckedId);

    }

    private void init(final View view) {
//        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        final Configure configure = ((PickActivity) getActivity()).getConfigure();
//        if (toolbar != null) {
//            initToolbar(toolbar, configure);
//
//        }
//        AudienceNetworkAds.initialize(getContext());
//
//        interstitialAd = new com.facebook.ads.InterstitialAd(getContext(), getString(R.string.fb_inter_placementID));
//
//
//        // load the ad
//        listner();
//        interstitialAd.loadAd(
//                interstitialAd.buildLoadAdConfig()
//                        .withAdListener(interstitialAdListener)
//                        .build());


        cd = new ConnectionDetector(getContext());

        isInternetPresent = cd.isConnectingToInternet();

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(getResources().getString(R.string.ad_unit_id_interstitial))
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .withLegitimateInterestAllowed(false)
                .build();

        MoPub.initializeSdk(getContext(), sdkConfiguration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                Log.d("Mopub", "SDK initialized");
                mInterstitial = new MoPubInterstitial(PhotoFragment.this.getActivity(), PhotoFragment.this.getResources().getString(R.string.ad_unit_id_interstitial));
                mInterstitial.load();
                listener();
            }
        });

        img_back = view.findViewById(R.id.img_back);
        tv_done = view.findViewById(R.id.tv_done);
        folderName = view.findViewById(R.id.folderName);

        folderName.setText(buckedName);

        mPhotoList = view.findViewById(R.id.photo_list);

        mPhotoList.setLayoutManager(new GridLayoutManager(getContext(), 3));

        mAdapter = new PhotoAdapter();

        mAdapter.setMaxCount(configure.getMaxCount());

        mAdapter.setOnSelectedMaxListener(new PhotoAdapter.OnSelectedMaxListener() {
            @Override
            public void onSelectedMax() {
                Snackbar.make(view, configure.getMaxNotice(), Snackbar.LENGTH_SHORT).show();
            }
        });

        mAdapter.setOnPhotoSelectedListener(new PhotoAdapter.OnPhotoSelectedListener() {
            @Override
            public void onPhotoSelected(Photo photo, int position) {

            }
        });

        mAdapter.setOnPhotoUnSelectedListener(new PhotoAdapter.OnPhotoUnSelectedListener() {
            @Override
            public void onPhotoUnSelected(Photo photo, int position) {

            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();

            }
        });

        tv_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(interstitialAd == null || !interstitialAd.isAdLoaded()) {
//                    return;
//                }
//                // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
//                if(interstitialAd.isAdInvalidated()) {
//                    return;
//                }
//                // Show the ad
//                interstitialAd.show();
                mInterstitial.show();
            }
        });

        mPhotoList.setHasFixedSize(true);
        mPhotoList.setAdapter(mAdapter);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getContext() != null) {
                        adContainer = view.findViewById(R.id.adContainer);
                        adView = new AdView(getContext());
                        fireAnalyticsAds("admob_banner", "loaded");
                        adView.setAdUnitId(getString(R.string.banner_ad));
                        adView.setAdListener(new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                super.onAdLoaded();
                                fireAnalyticsAds("admob_banner", "loaded");
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                if (loadAdError.getMessage() != null)
                                    fireAnalyticsAds("admob_banner", loadAdError.getMessage());
                            }
                        });
                        LoadAdaptiveBanner(getActivity(), adView);
                        adContainer.addView(adView);
                    }
                }
            }, 2000);
        }

    }

    @Override
    public void onDestroy() {
//        if (interstitialAd != null) {
//            interstitialAd.destroy();
//        }
        super.onDestroy();
        if (mInterstitial != null)
            mInterstitial.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        // load the ad
//        interstitialAd.loadAd(
//                interstitialAd.buildLoadAdConfig()
//                        .withAdListener(interstitialAdListener)
//                        .build());
    }

    private void listener() {

        mInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(Define.PATHS, mAdapter.getSelectedPhotoPaths());
                intent.putParcelableArrayListExtra(Define.PHOTOS, mAdapter.getSelectedPhotos());
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                mInterstitial.load();
                Intent intent = new Intent();
                intent.putStringArrayListExtra(Define.PATHS, mAdapter.getSelectedPhotoPaths());
                intent.putParcelableArrayListExtra(Define.PHOTOS, mAdapter.getSelectedPhotos());
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });


//        interstitialAdListener = new InterstitialAdListener() {
//            @Override
//            public void onError(Ad ad, AdError adError) {
//                // load the ad
//                interstitialAd.loadAd(
//                        interstitialAd.buildLoadAdConfig()
//                                .withAdListener(interstitialAdListener)
//                                .build());
//
//                Intent intent = new Intent();
//                intent.putStringArrayListExtra(Define.PATHS, mAdapter.getSelectedPhotoPaths());
//                intent.putParcelableArrayListExtra(Define.PHOTOS, mAdapter.getSelectedPhotos());
//                getActivity().setResult(Activity.RESULT_OK, intent);
//                getActivity().finish();
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//
//            }
//
//            @Override
//            public void onLoggingImpression(Ad ad) {
//
//            }
//
//            @Override
//            public void onInterstitialDisplayed(Ad ad) {
//
//            }
//
//            @Override
//            public void onInterstitialDismissed(Ad ad) {
//
//                // load the ad
//                interstitialAd.loadAd(
//                        interstitialAd.buildLoadAdConfig()
//                                .withAdListener(interstitialAdListener)
//                                .build());
//
//                Intent intent = new Intent();
//                intent.putStringArrayListExtra(Define.PATHS, mAdapter.getSelectedPhotoPaths());
//                intent.putParcelableArrayListExtra(Define.PHOTOS, mAdapter.getSelectedPhotos());
//                getActivity().setResult(Activity.RESULT_OK, intent);
//                getActivity().finish();
//            }
//        };
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


    private void initToolbar(Toolbar toolbar, Configure configure) {

        if (configure != null) {

            toolbar.setTitle(configure.getPhotoTitle());
            toolbar.setBackgroundColor(configure.getToolbarColor());
            toolbar.setTitleTextColor(configure.getToolbarTitleColor());

            toolbar.inflateMenu(R.menu.menu_pick);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_done) {
                        Intent intent = new Intent();
                        intent.putStringArrayListExtra(Define.PATHS, mAdapter.getSelectedPhotoPaths());
                        intent.putParcelableArrayListExtra(Define.PHOTOS, mAdapter.getSelectedPhotos());
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                        return true;
                    }
                    return false;
                }
            });

            toolbar.setNavigationIcon(configure.getNavIcon());
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();

                }
            });
        }
    }


    private void refreshPhotoList(List<Photo> photos) {
        mAdapter.refreshData(photos);
    }

    private class PhotoTask extends AsyncTask<String, Integer, List<Photo>> {

        @Override
        protected List<Photo> doInBackground(String... params) {
            List<Photo> photoLis = mPhotoManager.getPhoto(params[0]);
//            Collections.sort(photoLis, new Comparator<Photo>() {
//                public int compare(Photo o1, Photo o2) {
//                    String str1 = String.valueOf(o1.getDataModified());
//                    String str2 = String.valueOf(o2.getDataModified());
//                    if (str1 == null || str2 == null)
//                        return 0;
//                    return str2.compareTo(str1);
//                }
//            });
            return photoLis;
        }

        @Override
        protected void onPostExecute(List<Photo> photos) {
            super.onPostExecute(photos);
            refreshPhotoList(photos);
        }
    }


}
