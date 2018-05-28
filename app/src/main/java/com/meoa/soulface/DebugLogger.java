package com.meoa.soulface;
import android.util.Log;

/**
 * Created by Yura on 3/11/2018.
 */

public class DebugLogger {
    private static final String TAG = DebugLogger.class.getSimpleName();
    private static final int ADDITIONAL_LEVEL = 1;


    public static void i(String message) {
        if (message == null) {
            message = " ";
        }

        try {
            Log.i(getCallingFunction(), message);
        } catch (Exception ex){
            Log.i(TAG, "Error in i(): " + ex.getMessage());
        }
    }

    public static void d(String message) {
        if (message == null) {
            message = " ";
        }
        try {
            Log.d(getCallingFunction(), message);
        } catch (Exception ex){
            Log.d(TAG, "Error in i(): " + ex.getMessage());
        }
    }


    public static void e(String message) {
        if (message == null) {
            message = " ";
        }
        try {
            Log.e(getCallingFunction(), message);
        } catch (Exception ex){
            Log.e(TAG, "Error in i(): " + ex.getMessage());
        }
    }

    public static void v(String message) {
        if (message == null) {
            message = " ";
        }
        try {
            Log.v(getCallingFunction(), message);
        } catch (Exception ex){
            Log.v(TAG, "Error in i(): " + ex.getMessage());
        }
    }

    public static void w(String message) {
        if (message == null) {
        }
        try {
            Log.w(getCallingFunction(), message);
        } catch (Exception ex){
            Log.w(TAG, "Error in i(): " + ex.getMessage());
        }
    }

    private static String getCallingFunction() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4];
        String methodName = e.getClassName() + "::" + e.getMethodName() + "()";
        return methodName;
    }


}
