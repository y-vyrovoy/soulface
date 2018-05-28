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

public class EditImageActivity extends BasicBanneredActivity {

    private ScalableImageView mImageMain = null;
    private ImageView mHandImageView0;
    private ImageView mHandImageView1;
    private boolean mAnimateIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugLogger.d(null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        mImageMain = findViewById(R.id.imageSelection);
        mHandImageView0 = findViewById(R.id.image_hand_0);
        mHandImageView1 = findViewById(R.id.image_hand_1);

        InitializeBanner();

        mAnimateIcon = true;
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

    private void animateIcon() {
        DebugLogger.d(null);

        if (mAnimateIcon == true) {
            mAnimateIcon = false;

            List<Integer> lstFrames = new ArrayList<>();
            lstFrames.add(R.drawable.ic_hand_0);
            lstFrames.add(R.drawable.ic_hand_1);
            lstFrames.add(R.drawable.ic_hand_0);
            lstFrames.add(R.drawable.ic_hand_2);
            lstFrames.add(R.drawable.ic_hand_0);

            Runnable task = () -> animationTask(lstFrames);
            Thread thread = new Thread(task);
            thread.start();
        }
    }

    private void animationTask(List<Integer> lstFrames) {
        DebugLogger.d(null);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {}

        for (int i = 0; i < lstFrames.size() - 1; i++) {
            Drawable dr1 = getResources().getDrawable(lstFrames.get(i), null);
            runOnUiThread(() -> {
                mHandImageView1.setImageDrawable(dr1);
                mHandImageView1.setImageAlpha(0);
            });


            Drawable dr0 = getResources().getDrawable(lstFrames.get(i + 1), null);
            runOnUiThread(() -> {
                mHandImageView0.setImageDrawable(dr0);
                mHandImageView0.setImageAlpha(255);
            });


            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {}


            runOnUiThread(() -> {
                for (int iFrame = 1; iFrame < 8; iFrame++) {
                    mHandImageView1.setImageAlpha((iFrame + 1) * 32);
                    mHandImageView0.setImageAlpha(255 - (iFrame + 1) * 32);

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {}
                }
            });
        }
    }
}
