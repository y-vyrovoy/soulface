package com.meoa.soulface.activity;


import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.meoa.soulface.BitmapUtils;
import com.meoa.soulface.DebugLogger;
import com.meoa.soulface.SoulFaceApp;
import com.meoa.soulface.R;

public class VrModeActivity extends BasicBanneredActivity {
    private final static String TAG = VrModeActivity.class.getSimpleName();

    private Handler mHandler = new Handler();

    private ProgressBar mProgressBar;
    private ImageView mImageSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugLogger.d(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_mode);

        InitializeBanner();

        ImageView imageGeneral = findViewById(R.id.image_general);
        mProgressBar = findViewById(R.id.progress_bar);
        mImageSaved = findViewById(R.id.image_saved);

        Bitmap bmpVrModeImage = SoulFaceApp.getVrModeBitmap(true);

        if (imageGeneral != null && bmpVrModeImage != null) {
            imageGeneral.setImageBitmap(bmpVrModeImage);
        } else {
            Log.e(TAG, "Can't find left ImageView");
        }
    }

    @Override
    protected void onStart() {
        DebugLogger.d(null);

        super.onStart();

        if (SoulFaceApp.isTablet(this) == false) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        mProgressBar.setVisibility(View.INVISIBLE);
        mImageSaved.setVisibility(View.INVISIBLE);
    }

    public void onBtnShare(View viewShareButton) {
        DebugLogger.d(null);

        mProgressBar.setVisibility(View.VISIBLE);
        Bitmap bmpVrModeImage = SoulFaceApp.getVrModeBitmap(false);
        BitmapUtils.shareImage(bmpVrModeImage,
                this,
                ()-> {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mImageSaved.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(() -> mImageSaved.setVisibility(View.INVISIBLE), 1000);
                },
                null, true);
        viewShareButton.setVisibility(View.INVISIBLE);
    }

    public void onBtnBack(View v) {
        DebugLogger.d(null);
        onBackPressed();
    }

    public void onBtnSave(View viewSaveButton) {
        DebugLogger.d(null);

        mProgressBar.setVisibility(View.VISIBLE);
        Bitmap bmpVrModeImage = SoulFaceApp.getVrModeBitmap(false);
        BitmapUtils.saveBitmapGallery(bmpVrModeImage, this, true);
        mProgressBar.setVisibility(View.INVISIBLE);
        viewSaveButton.setVisibility(View.INVISIBLE);
        mImageSaved.setVisibility(View.VISIBLE);
        mHandler.postDelayed(() -> mImageSaved.setVisibility(View.INVISIBLE), 1000);
    }

}
