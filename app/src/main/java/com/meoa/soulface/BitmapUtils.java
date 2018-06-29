package com.meoa.soulface;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.meoa.soulface.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Yura Vyrovoy on 10/15/2017.
 */

public class BitmapUtils {

    public static Bitmap getDoubledBitmap(Bitmap bmpSource, boolean bLeft) {
        DebugLogger.d(null);

        Bitmap bmpFlipped = getFlippedBitmap(bmpSource);

        Bitmap bmpResult = Bitmap.createBitmap(bmpSource.getWidth() * 2,
                                                bmpSource.getHeight(),
                                                bmpSource.getConfig());

        Canvas canvas = new Canvas(bmpResult);
        if(bLeft == true) {
            canvas.drawBitmap(bmpSource, 0f, 0f, null);
            canvas.drawBitmap(bmpFlipped, bmpSource.getWidth(), 0, null);
        } else {
            canvas.drawBitmap(bmpFlipped, 0f, 0f, null);
            canvas.drawBitmap(bmpSource, bmpSource.getWidth(), 0, null);
        }
        return  bmpResult;
    }

    private static Bitmap getFlippedBitmap(Bitmap bmp) {
        DebugLogger.d(null);

        Matrix matrix = new Matrix();

        matrix.preScale(-1, 1);

        Bitmap bmpResult = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
        return bmpResult;
    }

    public static Bitmap getLeftSideBitmap(Bitmap bmpSource) {
        DebugLogger.d(null);

        return Bitmap.createBitmap(bmpSource,
                0, 0,
                bmpSource.getWidth()/2,
                bmpSource.getHeight());

    }

    public static Bitmap getRightSideBitmap(Bitmap bmpSource) {
        DebugLogger.d(null);

        return Bitmap.createBitmap(bmpSource,
                bmpSource.getWidth()/2, 0,
                bmpSource.getWidth()/2,
                bmpSource.getHeight());

    }

    public static Bitmap getDoubledLeftPart(Bitmap source) {
        DebugLogger.d(null);

        return getDoubledBitmap(getLeftSideBitmap(source), true);
    }

    public static Bitmap getDoubledRightPart(Bitmap source) {
        DebugLogger.d(null);

        return getDoubledBitmap(getRightSideBitmap(source), false);
    }

    public static Bitmap compileVrModeBitmap(Bitmap bmpLeft, Bitmap bmpRight,
                                             Bitmap bmpCaptionLeft, Bitmap bmpCaptionRight) {
        DebugLogger.d(null);

        int nIndent = 10;

        int nLeftBitmapWidth = bmpLeft.getWidth();
        int nLeftBitmapHeight = bmpLeft.getHeight();

        int nRightBitmapWidth = bmpRight.getWidth();
        int nRightBitmapHeight = bmpRight.getHeight();

        int nNewBitmapWidth = nLeftBitmapWidth + nRightBitmapWidth;
        int nNewBitmapHeight = Math.max(nLeftBitmapHeight, nRightBitmapHeight);

        Bitmap bmpCanvas = Bitmap.createBitmap(nNewBitmapWidth, nNewBitmapHeight, bmpLeft.getConfig());

        Canvas canvasResult = new Canvas(bmpCanvas);

        canvasResult.drawBitmap(bmpLeft, 0, 0, null);
        canvasResult.drawBitmap(bmpRight, nLeftBitmapWidth + nIndent, 0, null);

        int nCaptionTop = 20;

        if (bmpCaptionLeft != null) {
            int nCaptionLeftLeft = nLeftBitmapWidth/2 - bmpCaptionLeft.getWidth();
            canvasResult.drawBitmap(bmpCaptionLeft ,nCaptionLeftLeft, nCaptionTop, null);
        }

        if (bmpCaptionRight != null) {
            int nCaptionRightLeft = nLeftBitmapWidth + nIndent + nRightBitmapWidth / 2;
            canvasResult.drawBitmap(bmpCaptionRight, nCaptionRightLeft, nCaptionTop, null);
        }


        // ----------------------------------
        // Adding watermark

//        Bitmap bmpWatermark = BitmapFactory.decodeResource(SoulFaceApp.getInstance().getResources(), R.drawable.ic_watermark);
//
//        double dblRatio = (nNewBitmapWidth * 0.11) / bmpWatermark.getWidth();
//        bmpWatermark.reconfigure((int)(nNewBitmapWidth * 0.11),
//                                    (int)(bmpWatermark.getHeight() * dblRatio),
//                                    bmpWatermark.getConfig());
//
//        canvasResult.drawBitmap(bmpWatermark,
//                                bmpCanvas.getWidth() - bmpWatermark.getWidth(),
//                                bmpCanvas.getHeight() - bmpWatermark.getHeight(),
//                                null);

        return bmpCanvas;
    }

    public static Bitmap compileOverlayedImage(Bitmap bmpLeft, Bitmap bmpRight) {
        DebugLogger.d(null);

        int nNewImageWidth = Math.max(bmpLeft.getWidth(), bmpRight.getWidth());
        int nNewImageHeight = Math.max(bmpLeft.getHeight(), bmpRight.getHeight());

        Bitmap bmpResult = Bitmap.createBitmap(nNewImageWidth, nNewImageHeight, bmpLeft.getConfig());

        Canvas canvasResult = new Canvas(bmpResult);

        Paint paint = new Paint();
        paint.setAlpha(100);

        canvasResult.drawBitmap(bmpLeft, 0, 0, null);
        canvasResult.drawBitmap(bmpRight, 0, 0, paint);

        return bmpResult;
    }

    public static Bitmap addWatermark(Bitmap bmpSource) {

        int nBitmapWidth = bmpSource.getWidth();
        int nBitmapHeight = bmpSource.getHeight();

        Bitmap bmpResult = Bitmap.createBitmap(nBitmapWidth, nBitmapHeight, bmpSource.getConfig());
        Canvas canvasResult = new Canvas(bmpResult);

        canvasResult.drawBitmap(bmpSource, 0,0, null);

        Bitmap bmpWatermarkSource = BitmapFactory.decodeResource(SoulFaceApp.getInstance().getResources(), R.drawable.ic_watermark);

        double dblRatio = (nBitmapHeight * 0.08) / bmpWatermarkSource.getHeight();

        Bitmap bmpWatermark = Bitmap.createScaledBitmap(bmpWatermarkSource,
                                                        (int)(bmpWatermarkSource.getWidth() * dblRatio),
                                                        (int)(nBitmapHeight * 0.08), true);

        canvasResult.drawBitmap(bmpWatermark,
                            bmpResult.getWidth() - bmpWatermark.getWidth() - 2,
                            bmpResult.getHeight() - bmpWatermark.getHeight() - 2,
                            null);

        return bmpResult;
    }

    @Nullable
    public static String saveBitmapToAppFolder(Bitmap bmpSource, Context context, boolean bAddWatermark) {
        DebugLogger.d(null);

        Bitmap bmpToSave;

        if (bAddWatermark) {
            bmpToSave = BitmapUtils.addWatermark(bmpSource);
        } else {
            bmpToSave = bmpSource;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        String fileName = sdf.format(new Date(System.currentTimeMillis()));
        File fNew = new File(SoulFaceApp.getInstance().getPhotosPath(), fileName + ".png");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fNew);
            bmpToSave.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        //Toast.makeText(context, context.getResources().getString(R.string.photos_saved), Toast.LENGTH_SHORT).show();
        return fNew.getAbsolutePath();
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        DebugLogger.d(null);

        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            if (cursor == null) {
                return null;
            }
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Nullable
    public static String saveBitmapGallery(Bitmap bmpSource, Context context, boolean bAddWatermark) {
        DebugLogger.d(null);

        Bitmap bmpToSave;

        if (bAddWatermark) {
            bmpToSave = BitmapUtils.addWatermark(bmpSource);
        } else {
            bmpToSave = bmpSource;
        }

        String sImageUrl = MediaStore.Images.Media.insertImage(context.getContentResolver(), bmpToSave, "title" , "description");
        Uri savedImageURI = Uri.parse(sImageUrl);
        String sPath = getRealPathFromURI(context, savedImageURI);
        return sPath;
    }

    public static void shareImage(Bitmap bmpSource, Context context,
                                    OnActionDoneCallback callbackOnSave,
                                    OnActionDoneCallback callbackOnShare,
                                    boolean bAddWatermark) {
        DebugLogger.d(null);

        Bitmap bmpToSave;

        if (bAddWatermark) {
            bmpToSave = BitmapUtils.addWatermark(bmpSource);
        } else {
            bmpToSave = bmpSource;
        }

        String sImageUrl = MediaStore.Images.Media.insertImage(context.getContentResolver(), bmpToSave, "title" , "description");
        Uri savedImageURI = Uri.parse(sImageUrl);
        if (callbackOnSave != null) {
            callbackOnSave.doAction();
        }

        if (savedImageURI != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, savedImageURI);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share"));
        }

        if (callbackOnShare != null) {
            callbackOnShare.doAction();
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {
        return getRoundedCornerBitmap(bitmap, radius, false, true, null);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius, boolean bDrawFrame, boolean bActive, Context context) {
        DebugLogger.d(null);

        Bitmap imageRounded = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(imageRounded);

        Paint paintBitmap = new Paint();
        paintBitmap.setAntiAlias(true);
        paintBitmap.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect((new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight())), radius, radius, paintBitmap);// Round Image Corner 100 100 100 100

        if (bDrawFrame == true) {
            Paint paintFrame = new Paint();
            paintFrame.setAntiAlias(true);
            paintFrame.setStyle(Paint.Style.STROKE);
            paintFrame.setStrokeWidth(5);
            if (bActive) {
                paintFrame.setColor(context.getResources().getColor(R.color.colorPink));
            }
            else {
                paintFrame.setColor(context.getResources().getColor(android.R.color.white));
            }

            canvas.drawRoundRect((new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight())), radius, radius, paintFrame);
        }

        return imageRounded;
    }

    public interface OnActionDoneCallback {
        void doAction();
    }

}
