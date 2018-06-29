package com.meoa.soulface.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.meoa.soulface.BitmapUtils;
import com.meoa.soulface.DebugLogger;
import com.meoa.soulface.SoulFaceApp;
import com.meoa.soulface.ScalableImageView;
import com.meoa.soulface.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditImageActivity extends BasicBanneredActivity {

    private ScalableImageView mImageMain = null;
    private ImageView mHandImageView0;
    private ImageView mHandImageView1;

    private int mAnimationFrame;

    List<Integer> mAnimationFramesList = new ArrayList<>();
    private AtomicBoolean mRunAnimation = new AtomicBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugLogger.d(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        mImageMain = findViewById(R.id.imageSelection);
        mHandImageView0 = findViewById(R.id.image_hand_0);
        mHandImageView1 = findViewById(R.id.image_hand_1);

        InitializeBanner();
        initAnimationList();
    }

    @Override
    public void onStart () {
        DebugLogger.d(null);

        super.onStart();

        if (SoulFaceApp.getBitmapToEdit() != null) {
            mImageMain.setImageBitmap(SoulFaceApp.getBitmapToEdit());
        }

        animateIcon();
    }

    @Override
    public void onPause () {

        mRunAnimation.set(false);
        super.onPause();
    }

    public void onBtnBack(View v) {
        DebugLogger.d(null);
        onBackPressed();
    }

    public void onBtnReady(View v) {
        DebugLogger.d(null);

        Bitmap bmpResult = mImageMain.getResultBitmap();

        SoulFaceApp.setBitmapLeft(BitmapUtils.getDoubledLeftPart(bmpResult));
        SoulFaceApp.setBitmapRight(BitmapUtils.getDoubledRightPart(bmpResult));

        startActivity(new Intent(this, ResultActivity.class));
    }

    private void initAnimationList() {
        DebugLogger.d(null);

        mAnimationFramesList.add(R.drawable.ic_hand_00);
        mAnimationFramesList.add(R.drawable.ic_hand_01);
        mAnimationFramesList.add(R.drawable.ic_hand_02);
        mAnimationFramesList.add(R.drawable.ic_hand_03);
        mAnimationFramesList.add(R.drawable.ic_hand_04);
        mAnimationFramesList.add(R.drawable.ic_hand_05);
        mAnimationFramesList.add(R.drawable.ic_hand_06);
        mAnimationFramesList.add(R.drawable.ic_hand_07);
    }

    private void animateIcon() {
        DebugLogger.d(null);

        mRunAnimation.set(true);

        Runnable task = () -> animationTask(mAnimationFramesList);
        Thread thread = new Thread(task);
        thread.start();
    }

    private void animationTask(List<Integer> lstFrames) {
        DebugLogger.d(null);

        int i = 0;
        while (mRunAnimation.get()) {
            Drawable dr1 = getResources().getDrawable(lstFrames.get(i), null);
            runOnUiThread(() -> {
                mHandImageView1.setImageDrawable(dr1);
                mHandImageView1.setImageAlpha(0);
            });

            int iNext = (i + 1) % lstFrames.size();
            Drawable dr0 = getResources().getDrawable(lstFrames.get(iNext), null);
            runOnUiThread(() -> {
                mHandImageView0.setImageDrawable(dr0);
                mHandImageView0.setImageAlpha(255);
            });


            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {}

            for (mAnimationFrame = 1; mAnimationFrame < 8; mAnimationFrame++) {
                runOnUiThread(() -> {
                    mHandImageView1.setImageAlpha((mAnimationFrame + 1) * 32);
                    mHandImageView0.setImageAlpha(255 - (mAnimationFrame + 1) * 32);
                });
            };

            i = ++i % lstFrames.size();
        }
    }
}
