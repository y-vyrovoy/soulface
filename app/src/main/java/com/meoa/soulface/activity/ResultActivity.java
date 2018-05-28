package com.meoa.soulface.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.meoa.soulface.BitmapUtils;
import com.meoa.soulface.DebugLogger;
import com.meoa.soulface.FullScreenAd;
import com.meoa.soulface.SoulFaceApp;
import com.meoa.soulface.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResultActivity extends BasicBanneredActivity {
    private static final String TAG = ResultActivity.class.getSimpleName();
    private static final double RESULT_VIEW_PHOTO_RATIO = 0.6;
    private static final int ROUND_RADIUS = 40;

    private Handler mHandler = new Handler();

    private View mLeftViewTop;
    private View mLeftViewBottom;
    private View mRightViewTop;
    private View mRightViewBottom;
    private ProgressBar mProgressBar;
    private ImageView mImageSaved;
    private FullScreenAd mFullScreenAd;

    private int mScreenWidth;
    private boolean mLeftOnTop;

    private LinearLayout mLayoutButtons0;
    private LinearLayout mLayoutButtons1;

    private AtomicBoolean mRunAnimation = new AtomicBoolean();
    boolean mOneOrTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugLogger.d(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        InitializeBanner();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        mLeftViewTop = layoutInflater.inflate(R.layout.layout_left_photo_top, null);
        setSizedDrawable (mLeftViewTop, BitmapUtils.getRoundedCornerBitmap( SoulFaceApp.getBitmapLeft(), ROUND_RADIUS, true, true, this ));

        mLeftViewBottom = layoutInflater.inflate(R.layout.layout_left_photo_bottom, null);
        setSizedDrawable (mLeftViewBottom, BitmapUtils.getRoundedCornerBitmap( SoulFaceApp.getBitmapLeft(), ROUND_RADIUS, true, false, this ));

        mRightViewTop = layoutInflater.inflate(R.layout.layout_right_photo_top, null);
        setSizedDrawable (mRightViewTop, BitmapUtils.getRoundedCornerBitmap( SoulFaceApp.getBitmapRight(), ROUND_RADIUS, true, true, this ));

        mRightViewBottom = layoutInflater.inflate(R.layout.layout_right_photo_bottom, null);
        setSizedDrawable (mRightViewBottom, BitmapUtils.getRoundedCornerBitmap( SoulFaceApp.getBitmapRight(), ROUND_RADIUS, true, false, this ));

        mProgressBar = findViewById(R.id.progressBar);
        mImageSaved = findViewById(R.id.image_saved);

        mFullScreenAd = SoulFaceApp.getInstance().getPreloadedAd();
    }

    @Override
    public void onStart() {
        DebugLogger.d(null);

        super.onStart();

        doLayout(true);
        mProgressBar.setVisibility(View.INVISIBLE);
        mImageSaved.setVisibility(View.INVISIBLE);

        animateIcon();
    }

    @Override
    public void onPause() {
        mRunAnimation.set(false);
        super.onPause();
    }

    private void doLayout(boolean leftOnTop){
        DebugLogger.d(null);

        if (mLeftOnTop == leftOnTop) {
            return;
        }
        mLeftOnTop = leftOnTop;

        RelativeLayout layoutRoot = findViewById(R.id.layout_root);
        if (layoutRoot == null) {
            Log.e(TAG, "Can't find root layout. doLayout() terminated");
            return;
        }
        layoutRoot.removeAllViews();

        if (leftOnTop) {
            layoutRoot.addView(mRightViewBottom);
            layoutRoot.addView(mLeftViewTop);

            mLayoutButtons0 = mRightViewBottom.findViewById(R.id.layout_buttons_right_0);
            mLayoutButtons1 = mRightViewBottom.findViewById(R.id.layout_buttons_right_1);
        } else {
            layoutRoot.addView(mLeftViewBottom);
            layoutRoot.addView(mRightViewTop);

            mLayoutButtons0 = mRightViewTop.findViewById(R.id.layout_buttons_right_0);
            mLayoutButtons1 = mRightViewTop.findViewById(R.id.layout_buttons_right_1);
        }

        mLayoutButtons0.setVisibility(View.VISIBLE);
        mLayoutButtons1.setVisibility(View.INVISIBLE);
    }

    private void setSizedDrawable(View view, Bitmap bitmapSrc) {
        DebugLogger.d(null);

        ImageView imageView = view.findViewById(R.id.photo);

        if (imageView != null) {
            try {
                double desiredWidth = mScreenWidth * RESULT_VIEW_PHOTO_RATIO;
                int bmpWidth = SoulFaceApp.getBitmapLeft().getWidth();
                double ratio = desiredWidth / bmpWidth;

                int imageWidth = (int) (bitmapSrc.getWidth() * ratio);
                int imageHeight = (int) (bitmapSrc.getHeight() * ratio);

                Bitmap bitmapDest = Bitmap.createScaledBitmap(bitmapSrc, imageWidth, imageHeight, false);
                imageView.setImageBitmap(bitmapDest);
            } catch (Exception ex) {}

        }
    }

    public void onBtnSave(View v) {
        DebugLogger.d(null);

        Bitmap bmpToSave = null;
        if (v == mLeftViewTop.findViewById(R.id.btn_save_left)) {
            bmpToSave = SoulFaceApp.getBitmapLeft();
        } else if (v == mRightViewTop.findViewById(R.id.btn_save_right)) {
            bmpToSave = SoulFaceApp.getBitmapRight();
        }

        if (bmpToSave != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            BitmapUtils.saveBitmapGallery(bmpToSave, this);
            mProgressBar.setVisibility(View.INVISIBLE);
            v.setVisibility(View.INVISIBLE);

            mImageSaved.setVisibility(View.VISIBLE);
            mHandler.postDelayed(() -> mImageSaved.setVisibility(View.INVISIBLE), 1000);
        }
    }

    public void onBtnShare(View v) {
        DebugLogger.d(null);

        mProgressBar.setVisibility(View.VISIBLE);
        Bitmap bmpToShare = null;
        if (v == mLeftViewTop.findViewById(R.id.btn_share_left)) {
            bmpToShare = SoulFaceApp.getBitmapLeft();
        } else if (v == mRightViewTop.findViewById(R.id.btn_share_right)) {
            bmpToShare = SoulFaceApp.getBitmapRight();
        }

        if (bmpToShare != null) {
            BitmapUtils.shareImage(bmpToShare,
                                    this,
                                    ()-> {
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                        mImageSaved.setVisibility(View.VISIBLE);
                                        mHandler.postDelayed(() -> mImageSaved.setVisibility(View.INVISIBLE), 1000);
                                    },
                                    null);
        }
    }

    public void onBtnVrMode(View v) {
        DebugLogger.d(null);

        mFullScreenAd.showAd(()->{
            Intent intent = new Intent(this, VrModeActivity.class);
            startActivity(intent);
        });
    }

    public void onBtnSingleMode(View v) {
        DebugLogger.d(null);

        mFullScreenAd.showAd(()->{
            Intent intent = new Intent(this, SingleResultActivity.class);
            startActivity(intent);
        });
    }

    public void onBtnBack(View v) {
        DebugLogger.d(null);
        onBackPressed();
    }

    public void onImageClick(View v) {
        DebugLogger.d(null);

        if (v == mLeftViewBottom.findViewById(R.id.photo)) {
            doLayout(true);
        } else if (v == mRightViewBottom.findViewById(R.id.photo)) {
            doLayout(false);
        }
    }

    private void animateIcon() {
        DebugLogger.d(null);

        mRunAnimation.set(true);

        Runnable task = () -> animationTask();
        Thread thread = new Thread(task);
        thread.start();
    }

    private void animationTask() {
        DebugLogger.d(null);

        mOneOrTwo = true;

        while (mRunAnimation.get()) {

            runOnUiThread(() -> {
                mLayoutButtons0.setVisibility(mOneOrTwo ? View.VISIBLE : View.INVISIBLE);
                mLayoutButtons1.setVisibility(mOneOrTwo ? View.INVISIBLE : View.VISIBLE);
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {}

            mOneOrTwo = !mOneOrTwo;
        }
    }


}
