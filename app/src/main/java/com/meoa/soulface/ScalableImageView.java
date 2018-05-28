package com.meoa.soulface;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.meoa.soulface.R;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

/**
 * Created by Yura Vyrovoy on 10/17/2017.
 */

public class ScalableImageView extends View {
    private static final String TAG = ScalableImageView.class.getSimpleName();

    public static float RATIO = (float)4/3;
    private static final int SPLITTER_HALF_WIDTH = 3;

    private Rect mRectVisible = new Rect();
    private Rect mRectView = new Rect();

    private int mBitmapWidth = 0;
    private int mBitmapHeight = 0;

    private float mScaleCurrent = 1;
    private float mScaleMinimum = 1;
    private float mUnscaledOffsetX = 0;
    private float mUnscaledOffsetY = 0;

    private boolean mIsScaled;

    private Point mPointFingerOne;
    private Point mPointFingerTwo;
    private Point mLastPointFingerOne;
    private Point mLastPointFingerTwo;

    private float mRotationAngle;

    // items that should be instantiated once, not in every onDraw
    private Bitmap mBitmapSource;
    private Paint mPaintBackground;
    private Paint mPaintTransparent;
    private Paint mPaintTransformed;
    private Paint mPaintDashed;

    private Path mPathSeparator;


    private RectF mRectBitmapTransformed = new RectF();

    // Sets up interactions
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mGestureDetector;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Constructors and initializers
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public ScalableImageView(Context context) {
        super(context);
        DebugLogger.d(null);

        init(context, null);
    }

    public ScalableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        DebugLogger.d(null);

        init(context, attrs);
    }

    public ScalableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DebugLogger.d(null);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        DebugLogger.d(null);

        mScaleGestureDetector = new ScaleGestureDetector(context, _scaleGestureListener);
        mGestureDetector = new GestureDetectorCompat(context, _gestureListener);

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ScalableImageView);
            Drawable drawableSrc = a.getDrawable(R.styleable.ScalableImageView_src);
            if (drawableSrc != null) {
                setImageBitmap(((BitmapDrawable) drawableSrc).getBitmap());
            }

            //Don't forget this
            a.recycle();
        }

        mRotationAngle = 0;

        mPaintBackground = new Paint();
        mPaintBackground.setStyle(Paint.Style.FILL);
        mPaintBackground.setColor(Color.BLACK);

        mPaintTransparent = new Paint();
        mPaintTransparent.setStyle(Paint.Style.STROKE);
        mPaintTransparent.setColor(Color.GREEN);
        mPaintTransparent.setStrokeWidth(1);

        mPaintDashed = new Paint();
        mPaintDashed.setColor(getResources().getColor(R.color.colorPink));
        mPaintDashed.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintDashed.setStrokeWidth(3);
        mPaintDashed.setPathEffect(new DashPathEffect(new float[]{30, 20}, 0));

        mPathSeparator = new Path();

        mPaintTransformed = new Paint();
        mPaintTransformed.setStyle(Paint.Style.FILL);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Other stuff
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setImageBitmap(Bitmap bitmap) {
        DebugLogger.d(null);

        DebugLogger.d("Bitmap w: " + bitmap.getWidth() + ", h: " + bitmap.getHeight());

        mBitmapSource = bitmap;

        initScaleOffset();
        calcTransition();

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        DebugLogger.d(null);

        canvas.clipRect(mRectVisible);

        // Scaled bitmap sizes
        canvas.drawRect(mRectVisible, mPaintBackground);

        if(mBitmapSource != null) {

            canvas.drawRect(mRectBitmapTransformed, mPaintTransformed);

            drawBitmapRotated(canvas,
                    mRectVisible.centerX() + mUnscaledOffsetX * mScaleCurrent,
                    mRectVisible.centerY() + mUnscaledOffsetY * mScaleCurrent,
                    mScaleCurrent, mRotationAngle, mBitmapSource);
        }


        Bitmap bmpDivider = getDividerBitmap();
        canvas.drawBitmap(bmpDivider, mRectVisible.centerX() - bmpDivider.getWidth()/2, mRectVisible.top, null);
    }

    public Bitmap getDividerBitmap() {
        Bitmap bmpDivider = BitmapFactory.decodeResource(getResources(), R.drawable.vertical_divider);
        int height = bmpDivider.getHeight();
        int width = bmpDivider.getWidth();

        float flHeightRatio = ((float) mRectVisible.height()) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(1, flHeightRatio);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bmpDivider, 0, 0, width, height, matrix, false);
        bmpDivider.recycle();
        return resizedBitmap;
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        DebugLogger.d(null);

        // TODO padding

        if( w * RATIO >= h ) {
            mBitmapHeight = h;
            mBitmapWidth = (int)(h / RATIO);
        } else {
            mBitmapHeight = (int)(w * RATIO);
            mBitmapWidth = w;
        }
        mRectVisible.set( (w - mBitmapWidth)/2 , (h - mBitmapHeight)/2, (w + mBitmapWidth)/2 , (h + mBitmapHeight)/2);
        mRectView.set(0, 0, w - 1, h - 1);

        DebugLogger.d("Bitmap w:" + mBitmapWidth + ", h: " + mBitmapHeight);
        DebugLogger.d("mRectVisible w:" + mRectVisible.width() + ", h: " + mRectVisible.height());

        initScaleOffset();
        calcTransition();

        mPathSeparator.reset();

        mPathSeparator.moveTo(w /2, 0);
        mPathSeparator.lineTo(w /2 , h);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Initializes scale and offset basing on sizes of source bitmap and the view size.
     * Scale is initialized to fit bitmap by height. So vertical offset equals 0.
     * Horizontal offset is set to place the bitmap that will be drawn in the middle of the view.
     * Offsets are negative if bitmap size is less than view's size.
     */
    private void initScaleOffset() {
        DebugLogger.d(null);

        mUnscaledOffsetX = 0;
        mUnscaledOffsetY = 0;

        if( (mBitmapSource == null) || mRectVisible.isEmpty()) {
            mScaleCurrent = 1;
            mScaleMinimum = 1;
            return;
        }

        int visibleHeight = mRectVisible.height();
        int visibleWidth = mRectVisible.width();

        mScaleCurrent = (float) visibleHeight / mBitmapSource.getHeight();
        mScaleMinimum = mScaleCurrent;
    }

    /**
     * Prepares _btimapToDraw basing on source bitmap, current scale and offset
     */
    private void calcTransition() {
        DebugLogger.d(null);

        if(mBitmapSource == null) {
            DebugLogger.e("mBitmapSource == null");
            return;
        }

        RectF rectBounds = getMappedRect(mRectVisible.centerX() + mUnscaledOffsetX * mScaleCurrent,
                                            mRectVisible.centerY() + mUnscaledOffsetY * mScaleCurrent,
                mScaleCurrent, mRotationAngle, mBitmapSource);

        float scaledBitmapWidth = rectBounds.width();
        float scaledBitmapHeight = rectBounds.height();

        float scaledOffsetX = mUnscaledOffsetX * mScaleCurrent;
        float scaledOffsetY = mUnscaledOffsetY * mScaleCurrent;

        // View's visible area sizes
        int visibleWidth = mRectVisible.width();
        int visibleHeight = mRectVisible.height();

        // calculating left and width of source bitmap to cut

        if( mBitmapWidth > scaledBitmapWidth ) {
            // SCALED bitmap width is less than view width
            mUnscaledOffsetX = 0;
        } else if(scaledOffsetX < visibleWidth/2 - scaledBitmapWidth/2) {
            // SCALED bitmap left is greater than view's left -> move bitmap left to view's left
            mUnscaledOffsetX = (visibleWidth/2 - scaledBitmapWidth/2) / mScaleCurrent;
        } else if(scaledOffsetX > (scaledBitmapWidth - visibleWidth) / 2) {
            mUnscaledOffsetX = -1 * (visibleWidth/2 - scaledBitmapWidth/2) / mScaleCurrent;
        }


        if( mBitmapHeight > scaledBitmapHeight ) {
            // SCALED bitmap height is less than view height
            mUnscaledOffsetY = 0;
        } else if(scaledOffsetY < visibleHeight/2 - scaledBitmapHeight/2) {
            // SCALED bitmap top is greater than view's top -> move bitmap top to view's top
            mUnscaledOffsetY = (visibleHeight/2 - scaledBitmapHeight/2) / mScaleCurrent;
        } else if(scaledOffsetY > (scaledBitmapHeight - visibleHeight) / 2) {
            // SCALED bitmap bottom is less than view's bottom -> move bitmap bottom to view's bottom
            mUnscaledOffsetY = -1 * (visibleHeight/2 - scaledBitmapHeight/2) / mScaleCurrent;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        DebugLogger.d(null);

        int action = event.getAction();
        int pointerCount = event.getPointerCount();

        switch (action & MotionEvent.ACTION_MASK) {
            case ACTION_DOWN:
                break;

            case ACTION_POINTER_DOWN:
                if (pointerCount > 1) {
                    mPointFingerOne = new Point((int)event.getX(0), (int)event.getY(0));
                    mPointFingerTwo = new Point((int)event.getX(1), (int)event.getY(1));
                    onRotationStart();
                }
                break;

            case ACTION_MOVE:
                if(mIsScaled && (pointerCount > 1)) {
                    mPointFingerOne = new Point((int)event.getX(0), (int)event.getY(0));
                    mPointFingerTwo = new Point((int)event.getX(1), (int)event.getY(1));
                    onRotate();
                }
                break;

            case ACTION_POINTER_UP:
                if (pointerCount <= 1) {
                    onRotationEnds();
                }
                break;

            case ACTION_UP:
                break;

        }

        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        retVal = mGestureDetector.onTouchEvent(event) || retVal;
        return retVal || super.onTouchEvent(event);
    }

    private static Matrix getMatrix(float centerX, float centerY, float scale, float rotationAngleDegrees, Bitmap bitmap) {
        DebugLogger.d(null);

        Matrix matrix = new Matrix();
        matrix.preRotate(rotationAngleDegrees, bitmap.getWidth()/2 * scale, bitmap.getHeight()/2 * scale);
        matrix.preScale(scale, scale);
        matrix.postTranslate(centerX - bitmap.getWidth()/2 * scale,
                centerY - bitmap.getHeight()/2 * scale);

        return matrix;
    }

    private static RectF getMappedRect(float centerX, float centerY, float scale, float rotationAngleDegrees, Bitmap bitmap) {
        DebugLogger.d(null);

        Matrix matrixCalc = new Matrix();
        RectF rectBitmapTransformed = new RectF();

        rectBitmapTransformed.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        matrixCalc.preRotate(rotationAngleDegrees, bitmap.getWidth()/2 * scale, bitmap.getHeight()/2 * scale);
        matrixCalc.preScale(scale, scale);
        matrixCalc.postTranslate(centerX - bitmap.getWidth()/2 * scale,
                centerY - bitmap.getHeight()/2 * scale);

        matrixCalc.mapRect(rectBitmapTransformed);

        return rectBitmapTransformed;
    }

    private static void drawBitmapRotated(Canvas canvas, float centerX, float centerY, float scale, float rotationAngleDegrees, Bitmap bitmap) {
        DebugLogger.d(null);

        Matrix matrix = new Matrix();
        matrix.preRotate(rotationAngleDegrees, bitmap.getWidth()/2 * scale, bitmap.getHeight()/2 * scale);
        matrix.preScale(scale, scale);
        matrix.postTranslate(centerX - bitmap.getWidth()/2 * scale,
                centerY - bitmap.getHeight()/2 * scale);

        canvas.drawBitmap(bitmap, matrix, null);
    }

    public Bitmap getResultBitmap() {
        DebugLogger.d(null);

        Matrix matrix = getMatrix(mRectVisible.centerX() + mUnscaledOffsetX * mScaleCurrent,
                                    mRectVisible.centerY() + mUnscaledOffsetY * mScaleCurrent,
                mScaleCurrent, mRotationAngle, mBitmapSource);

        Bitmap bmpTransformed = Bitmap.createBitmap(mBitmapSource,
                0, 0,
                mBitmapSource.getWidth(),
                mBitmapSource.getHeight(),
                matrix, true);

        RectF rectTransformed = getMappedRect(mRectVisible.centerX() + mUnscaledOffsetX * mScaleCurrent,
                                                mRectVisible.centerY() + mUnscaledOffsetY * mScaleCurrent,
                mScaleCurrent, mRotationAngle, mBitmapSource);

        float leftBorder = Math.max(0, mRectVisible.left - rectTransformed.left);
        float topBorder = Math.max(0, mRectVisible.top - rectTransformed.top);
        float rightBorder = Math.min(rectTransformed.right - rectTransformed.left, mRectVisible.right - rectTransformed.left);
        float bottomBorder = Math.min(rectTransformed.bottom - rectTransformed.top, mRectVisible.bottom - rectTransformed.top);

        if(rightBorder - leftBorder > rectTransformed.width()) {
            leftBorder = 0;
            rightBorder = rectTransformed.width();
        }

        if(bottomBorder - topBorder > rectTransformed.height()) {
            topBorder = 0;
            bottomBorder = rectTransformed.height();
        }

        RectF rectResult = new RectF();
        rectResult.set(leftBorder, topBorder, rightBorder, bottomBorder);

        Bitmap bmpUnCropped = Bitmap.createBitmap(bmpTransformed,
                                                    (int)leftBorder,
                                                    (int)topBorder,
                                                    (int)(rightBorder - leftBorder),
                                                    (int)(bottomBorder - topBorder));

        int croppedHeight;
        int croppedWidth;

        if (bmpUnCropped.getHeight() / bmpUnCropped.getWidth() > RATIO) {
            croppedWidth = bmpUnCropped.getWidth();
            croppedHeight = (int)(croppedWidth * RATIO);
        } else {
            croppedHeight = bmpUnCropped.getHeight();
            croppedWidth = (int)(croppedHeight / RATIO);
        }

        Bitmap bmpReturn = Bitmap.createBitmap(croppedWidth, croppedHeight, bmpUnCropped.getConfig());
        Canvas canvasReturn = new Canvas(bmpReturn);
        canvasReturn.drawRect(0, 0, croppedWidth, croppedHeight, mPaintBackground   );

        canvasReturn.drawBitmap(bmpUnCropped,
                                (croppedWidth - bmpUnCropped.getWidth()) / 2,
                                (croppedHeight - bmpUnCropped.getHeight()) / 2,
                                null);

        return bmpReturn;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Getters and setters
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setScale(float scale) {
        DebugLogger.d(null);

        if(mBitmapSource == null) {return;}

        if(scale >= mScaleMinimum) {
            mScaleCurrent = scale;
        }
    }

    public float getScale() {
        DebugLogger.d(null);

        return mScaleCurrent;
    }

    public float getMinScale() {
        DebugLogger.d(null);

        return mScaleMinimum;
    }

    public void setRotationAngle(float radAngle) {
        DebugLogger.d(null);

        mRotationAngle = (float)(radAngle % (2 * Math.PI));
        if (mRotationAngle < 0) {
            mRotationAngle += 2 * Math.PI;
        }
        Log.d(TAG, "setRotationAngle(): " + mRotationAngle * 180 / Math.PI);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // The scroll listener, used for scrolling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final GestureDetector.SimpleOnGestureListener _gestureListener
            = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            DebugLogger.d(null);

            mUnscaledOffsetX -= distanceX / mScaleCurrent;
            mUnscaledOffsetY -= distanceY / mScaleCurrent;
            calcTransition();

            ViewCompat.postInvalidateOnAnimation(ScalableImageView.this);
            return true;
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // The scale listener, used for handling multi-finger scale gestures.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private final ScaleGestureDetector.OnScaleGestureListener _scaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private double lastSpan;


        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            DebugLogger.d(null);

            float lastSpanX = scaleGestureDetector.getCurrentSpanX();
            float lastSpanY = scaleGestureDetector.getCurrentSpanY();
            lastSpan = (float)Math.sqrt(Math.pow(lastSpanX, 2) + Math.pow(lastSpanY, 2));

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            DebugLogger.d(null);

            float spanX = scaleGestureDetector.getCurrentSpanX();
            float spanY = scaleGestureDetector.getCurrentSpanY();
            double currentSpan = Math.sqrt(Math.pow(spanX, 2) + Math.pow(spanY, 2));

            //TODO try scaleGestureDetector.getScale
            setScale((float)Math.max( (double) (mScaleMinimum), mScaleCurrent * currentSpan / lastSpan));
            calcTransition();

            lastSpan = currentSpan;
            return true;
        }

    };

    private void onRotationStart() {
        DebugLogger.d(null);

        mIsScaled = true;

        if ( (mPointFingerOne != null) && (mPointFingerTwo != null)) {
            mLastPointFingerOne = mPointFingerOne;
            mLastPointFingerTwo = mPointFingerTwo;
        }

        ViewCompat.postInvalidateOnAnimation(ScalableImageView.this);
    }

    private void onRotate() {
        DebugLogger.d(null);

        if ( (mPointFingerOne != null) && (mPointFingerTwo != null) &&
                (mLastPointFingerOne != null) && (mLastPointFingerTwo != null)) {

            mRotationAngle += Vector2D.
                    getSignedAngleBetween(new Vector2D(mLastPointFingerOne, mLastPointFingerTwo),
                            new Vector2D(mPointFingerOne, mPointFingerTwo));
            mRotationAngle %= 360;

            mLastPointFingerOne = mPointFingerOne;
            mLastPointFingerTwo = mPointFingerTwo;
        }
        ViewCompat.postInvalidateOnAnimation(ScalableImageView.this);
    }

    private void onRotationEnds() {
        DebugLogger.d(null);

        mIsScaled = false;
        ViewCompat.postInvalidateOnAnimation(ScalableImageView.this);
    }

    public static  class Vector2D {

        private float _x;
        private float _y;

        public Vector2D() {
            _x = 0;
            _y = 0;
        }

        public Vector2D(float x, float y) {
            _x = x;
            _y = y;
        }

        public Vector2D(Point point) {
            _x = point.x;
            _y = point.y;
        }

        public Vector2D(Point pointOne, Point pointTwo) {
            _x = pointOne.x - pointTwo.x;
            _y = pointOne.y - pointTwo.y;
        }

        public float getLength() {
            return (float)Math.sqrt(Math.pow(_x, 2) + Math.pow(_y, 2));
        }

        public static Vector2D getNormalized(Vector2D v) {
            float l = v.getLength();
            if (l == 0)
                return new Vector2D();
            else
                return new Vector2D(v._x / l, v._y / l);
        }

        public static float getSignedAngleBetween(Vector2D a, Vector2D b) {
            Vector2D na = getNormalized(a);
            Vector2D nb = getNormalized(b);

            double radAngle = Math.atan2(nb._y, nb._x) - Math.atan2(na._y, na._x);

            if ( (na._x * nb._x * na._y * nb._y < 0) ) {
                radAngle *= -1;
            }

            double gradAngle = radAngle * 180 / Math.PI;
            gradAngle %= 360;

            if (gradAngle < -180.f) gradAngle += 360.0f;
            if (gradAngle > 180.f) gradAngle -= 360.0f;

            return (float)gradAngle;

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DebugLogger.d(null);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightSize > widthSize * RATIO) {
            heightSize = (int)(widthSize * RATIO);
        } else {
            widthSize = (int)(heightSize / RATIO);
        }

        setMeasuredDimension(widthSize, heightSize);
    }
}