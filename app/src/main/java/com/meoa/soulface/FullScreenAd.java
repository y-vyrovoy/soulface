package com.meoa.soulface;


import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.concurrent.atomic.AtomicBoolean;

public class FullScreenAd{

    private static final String APP_AD_ID = "ca-app-pub-3940256099942544~3347511713";
    private static final String SCREEN_AD_ID = "ca-app-pub-3940256099942544/1033173712";

    private long LOAD_AD_TIMEOUT = 10_000;

    private String mAdAppId;
    private String mAdScreenId;
    private InterstitialAd mInterstitialAd;

    private Context mContext;
    private OnAdClosedAction mOnAdCloseAction;
    private OnAdLoadedAction mOnAdLoadedAction;
    private AtomicBoolean mAdIsLoaded = new AtomicBoolean();

    private long mLoadStart;

    public FullScreenAd(Context context) {
        super();
        DebugLogger.d(null);

        mContext = context;

        if (SoulFaceApp.getInstance().isNetworkAvailable()) {
            initAdIDs(null, null);
        }
    }

    public FullScreenAd(Context context, String adAppId, String adScreenId) {
        super();
        DebugLogger.d(null);

        mContext = context;

        if (SoulFaceApp.getInstance().isNetworkAvailable()) {
            initAdIDs(adAppId, adScreenId);
        }
    }

    public FullScreenAd(Context context, int adAppId, int adBannerId) {
        super();
        DebugLogger.d(null);

        mContext = context;

        if (SoulFaceApp.getInstance().isNetworkAvailable()) {
            initAdIDs(context.getResources().getString(adAppId), context.getResources().getString(adBannerId));
        }
    }



    public void setOnAdLoadedAction(OnAdLoadedAction action) {
        mOnAdLoadedAction = action;
    }

    public long getLoadAdTimeout() {
        return LOAD_AD_TIMEOUT;
    }

    public void setLoadAdTimeout(long timeout) {
        LOAD_AD_TIMEOUT = timeout;
    }

    private void initAdIDs(String adAppId, String adScreenId) {
        DebugLogger.d(null);

        if (SoulFaceApp.getInstance().isNetworkAvailable()) {
            mAdAppId = (adAppId != null) ? adAppId : APP_AD_ID;
            mAdScreenId = (adScreenId != null) ? adScreenId : SCREEN_AD_ID;

            mInterstitialAd = new InterstitialAd(mContext);
            mInterstitialAd.setAdUnitId(mAdScreenId);

            setInterstitialAdListener();
            mOnAdCloseAction = null;
            mOnAdLoadedAction = null;
        }
    }

    public void loadAd() {
        DebugLogger.d(null);

        if (SoulFaceApp.getInstance().isNetworkAvailable() == false) {
            DebugLogger.d("No internet connection");
            return;
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(mContext, mAdAppId);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdIsLoaded.set(false);
        mInterstitialAd.loadAd(adRequest);
        mLoadStart = System.currentTimeMillis();
    }

    private void setInterstitialAdListener() {
        DebugLogger.d(null);

//        if (SoulFaceApp.getInstance().isNetworkAvailable() == false) {
//            DebugLogger.d("No internet connection");
//            return;
//        }

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                DebugLogger.d(null);

                DebugLogger.d(String.format("Ad loaded in %d ms", System.currentTimeMillis() - mLoadStart));

                mAdIsLoaded.set(true);
                if (mOnAdLoadedAction != null) {
                    mOnAdLoadedAction.onAdLoadedAction();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                DebugLogger.e("Can't load ad. errorCode" + errorCode);
            }

            @Override
            public void onAdOpened() {
                DebugLogger.d(null);
            }

            @Override
            public void onAdLeftApplication() {
                DebugLogger.d(null);
            }

            @Override
            public void onAdClosed() {
                DebugLogger.d(null);
                if (mOnAdCloseAction != null) {
                    mOnAdCloseAction.onAdCloseAction();
                    loadAd();
                }
            }
        });
    }

   public void showAd(OnAdClosedAction action) {
        DebugLogger.d(null);
        DebugLogger.d("Ad is loaded: " + mAdIsLoaded.get());

       if (SoulFaceApp.getInstance().isNetworkAvailable() == false) {
           DebugLogger.d("No internet connection. Starting onAdClosedAction immediately");
           action.onAdCloseAction();
           return;
       }

        mOnAdCloseAction = action;
        boolean bShow = false;

        long lStartTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - lStartTime < LOAD_AD_TIMEOUT && bShow == false) {
            if (mAdIsLoaded.get()) {
                bShow = true;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {}
        }

        if (bShow) {
            mInterstitialAd.show();
        } else {
            DebugLogger.e("Can't load ad. Starting onAdClosedAction immediately.");
            mOnAdCloseAction.onAdCloseAction();
        }
    }

    public interface OnAdClosedAction {
        void onAdCloseAction();
    }

    public interface OnAdLoadedAction {
        void onAdLoadedAction();
    }
}