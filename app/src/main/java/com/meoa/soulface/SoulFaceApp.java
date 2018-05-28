package com.meoa.soulface;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.meoa.soulface.R;

import java.io.File;

/**
 * Created by Yura Vyrovoy on 10/13/2017.
 */

public class SoulFaceApp extends Application {

    private static SoulFaceApp mInstance = null;

    private Bitmap mBitmapToEdit = null;
    private Bitmap mBitmapLeft = null;
    private Bitmap mBitmapRight = null;

    private String mPathPhotos;
    private FullScreenAd mFullScreenAd;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        mFullScreenAd = new FullScreenAd(this);
        mFullScreenAd.loadAd();

        createFolder();
    }

    public static SoulFaceApp getInstance() {
        DebugLogger.d(null);
        return mInstance;
    }

    public static void setBitmapToEdit(Bitmap bitmapToEdit) {
        DebugLogger.d(null);

        if (mInstance == null) {
            return;
        }
        mInstance.mBitmapToEdit = bitmapToEdit;
    }

    public static Bitmap getBitmapToEdit() {
        DebugLogger.d(null);

        if (mInstance == null) {
            return null;
        }
        return mInstance.mBitmapToEdit;
    }

    public static void setBitmapLeft(Bitmap bitmapToEdit) {
        DebugLogger.d(null);

        if (mInstance == null) {
            return;
        }
        mInstance.mBitmapLeft = bitmapToEdit;
    }

    public static Bitmap getBitmapLeft() {
        DebugLogger.d(null);

        if (mInstance == null) {
            return null;
        }
        return mInstance.mBitmapLeft;
    }

    public static void setBitmapRight(Bitmap bitmapToEdit) {
        DebugLogger.d(null);

        if (mInstance == null) {
            return;
        }
        mInstance.mBitmapRight = bitmapToEdit;
    }

    public static Bitmap getBitmapRight() {
        DebugLogger.d(null);

        if (mInstance == null) {
            return null;
        }
        return mInstance.mBitmapRight;
    }

    public static Bitmap getVrModeBitmap(boolean bAddCaptures){
        DebugLogger.d(null);

        if (mInstance == null || mInstance.mBitmapLeft == null || mInstance.mBitmapRight == null) {
            return null;
        }

        if (!bAddCaptures) {
            return BitmapUtils.compileVrModeBitmap(mInstance.mBitmapLeft, mInstance.mBitmapRight,
                                                            null, null);
        } else {

            Bitmap bmpCaptionLeft = BitmapFactory.decodeResource(mInstance.getResources(), R.drawable.ic_soul_vr_mode);
            Bitmap bmpCaptionRight = BitmapFactory.decodeResource(mInstance.getResources(), R.drawable.ic_face_vr_mode);
            return BitmapUtils.compileVrModeBitmap(mInstance.mBitmapLeft, mInstance.mBitmapRight,
                    bmpCaptionLeft, bmpCaptionRight);
        }
    }

    public static Bitmap getSingleResultBitmap() {
        DebugLogger.d(null);

        if (mInstance == null || mInstance.mBitmapLeft == null || mInstance.mBitmapRight == null) {
            return null;
        }
        return BitmapUtils.compileOverlayedImage(mInstance.mBitmapLeft, mInstance.mBitmapRight);
    }

    private void createFolder() {
        DebugLogger.d(null);

        mPathPhotos = "";
        try {
            File folder = new File(Environment.getExternalStorageDirectory(), getApplicationContext().getString(R.string.app_name));
            folder.mkdir();
            mPathPhotos = folder.getAbsolutePath();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String getPhotosPath() {
        DebugLogger.d(null);

        return mPathPhotos;
    }

    public FullScreenAd getPreloadedAd() {
        return mFullScreenAd;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
