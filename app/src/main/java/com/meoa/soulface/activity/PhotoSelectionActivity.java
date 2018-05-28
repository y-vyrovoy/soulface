package com.meoa.soulface.activity;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.meoa.soulface.DebugLogger;
import com.meoa.soulface.FullScreenAd;
import com.meoa.soulface.SoulFaceApp;
import com.meoa.soulface.R;

import java.io.IOException;

public class PhotoSelectionActivity extends BasicBanneredActivity {

    private static final String TAG = PhotoSelectionActivity.class.getSimpleName();

    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_CAMERA_IMAGE = 2;

    private ImageView mImageMain;
    private ImageView mImageAnimation;

    private ImageButton mButtonLoad;
    private ImageButton mButtonShoot;
    private ProgressBar mProgressBar;

    private int mAlpha0;
    private int mAlpha1;

    private boolean mIsFirstShow;

    private FullScreenAd mFullScreenAd;
    private boolean mShowProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugLogger.d(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selection);

        InitializeBanner();

        mImageAnimation = findViewById(R.id.image_animation);
        mImageMain = findViewById(R.id.image_main);
        mButtonLoad = findViewById(R.id.btn_load);
        mButtonShoot = findViewById(R.id.btn_shoot);
        mProgressBar = findViewById(R.id.progress_bar);

        mFullScreenAd = SoulFaceApp.getInstance().getPreloadedAd();

        mIsFirstShow = true;
        mShowProgressBar = false;
    }

    @Override
    public void onStart () {
        DebugLogger.d(null);
        super.onStart();

        if (mIsFirstShow == true) {
            mAlpha0 = 0xFF;
            mAlpha1 = 0x00;

            setProgressBarState(false);

            mImageAnimation.setImageResource(R.drawable.face_anim_0);
            mImageAnimation.setImageAlpha(mAlpha0);
            mImageMain.setImageResource(R.drawable.face_anim_1);
            mImageMain.setImageAlpha(mAlpha1);

            RedrawThread th = new RedrawThread();
            th.start();
        } else {
            mImageAnimation.setVisibility(View.INVISIBLE);
            mImageMain.setImageResource(R.drawable.face_anim_1);
        }

        mIsFirstShow = false;
    }

    public void onBtlLoadPhoto(View v) {
        DebugLogger.d(null);

        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, RESULT_LOAD_IMAGE);
    }

    private Uri _imageUri;
    public void onBtnShootPhoto(View v) {
        DebugLogger.d(null);

        ContentValues values = new ContentValues();

        _imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);

        startActivityForResult(intent, RESULT_CAMERA_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DebugLogger.d(null);
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bmp = null;

        if (resultCode == RESULT_OK) {

            if(requestCode == RESULT_LOAD_IMAGE) {
                Uri selectedImage = data.getData();
                if (selectedImage == null) {
                    Log.e(TAG, "URI selectedImage == null");
                    return;
                }

                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                } catch (IOException ex) {ex.printStackTrace();}

                if(bmp != null) {
                    SoulFaceApp.setBitmapToEdit(bmp);
                    setBitmapAndWait(bmp);
                }
            } else if(requestCode == RESULT_CAMERA_IMAGE) {
                try {
                    bmp = MediaStore.Images.Media.getBitmap(
                                                    getContentResolver(), _imageUri);

                } catch (Exception e) {e.printStackTrace();}

                setBitmapAndWait (bmp);
                SoulFaceApp.setBitmapToEdit(bmp);
            }

            if (bmp != null) {
                Runnable task = () ->
                        runOnUiThread(() ->
                                mFullScreenAd.showAd(() -> {
                                    setProgressBarState(false);
                                    runOnUiThread(() -> startActivity(new Intent(this, EditImageActivity.class)));
                                }));

                Thread thread = new Thread(task);
                thread.start();
            } else {
                mImageMain.setImageResource(R.drawable.image_load_error);
                setProgressBarState(false);
            }
        }
    }

    private void setBitmapAndWait(Bitmap bmp) {
        mImageMain.setImageBitmap(bmp);
        setProgressBarState(true);
    }

    private void setProgressBarState(boolean showProgressBar) {
        mShowProgressBar = showProgressBar;

        mProgressBar.setVisibility(mShowProgressBar ? View.VISIBLE : View.INVISIBLE);

        mButtonLoad.setVisibility(mShowProgressBar ? View.INVISIBLE : View.VISIBLE);
        mButtonShoot.setVisibility(mShowProgressBar ? View.INVISIBLE : View.VISIBLE);
    }

    public void onBackPressed() {
        DebugLogger.d(null);

        // doing nothing to prevent moving to WelcomeActivity
    }

    private void refreshImages() {

        if (mImageMain != null && mImageAnimation != null)
        {
            mImageAnimation.setImageAlpha(mAlpha0);
            mImageMain.setImageAlpha(mAlpha1);
            mImageMain.postInvalidate();
            mImageAnimation.postInvalidate();
        }
    }

    private class RedrawThread extends Thread {

        private int mFramesCount = 50;
        private int mFrameDuration = 40;
        private int alphaDelta;

        public RedrawThread() {
        }

        public RedrawThread(int framesCount, int animationDuration) {
            mFramesCount = framesCount;
            mFrameDuration = animationDuration / mFramesCount;
        }

        @Override
        public void run() {
            alphaDelta = 255 / mFramesCount;
            if (alphaDelta * mFramesCount < 255) {
                mFramesCount += 1;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ex){ }

            for (int i = 0; i < mFramesCount; i++)
            {
                mAlpha0 -= alphaDelta;
                mAlpha1 += alphaDelta;
                runOnUiThread(()->refreshImages()) ;

                try {
                    Thread.sleep(mFrameDuration);
                } catch (InterruptedException ex){ }
            }
        }
    }
}
