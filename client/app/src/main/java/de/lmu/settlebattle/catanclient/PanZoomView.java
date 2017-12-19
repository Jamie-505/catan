package de.lmu.settlebattle.catanclient;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import android.view.ViewConfiguration;


public class PanZoomView extends View {

    static protected final boolean ScaleAtFocusPoint = false;
    static protected final int DefaultDrawableId = R.drawable.desert_default;

    protected Drawable mSampleImage;
    protected Context mContext;
    protected float mPosX;
    protected float mPosY;
    protected float mPosX0 = 0;
    protected float mPosY0 = 0;
    protected float mFocusX;
    protected float mFocusY;
    
    protected float mLastTouchX;
    protected float mLastTouchY;
    protected float mInitialTouchX;
    protected float mInitialTouchY;
    protected boolean mDoTouchUp = false;

    protected boolean mHandlingTouchUp = false;

    protected boolean mTouchable = true;
    
protected static final int INVALID_POINTER_ID = -1;


protected int mActivePointerId = INVALID_POINTER_ID;

protected ScaleGestureDetector mScaleDetector;
protected float mScaleFactor = 1.f;
protected float mMinScaleFactor = 0.1f;
protected float mMaxScaleFactor = 5.0f;


protected boolean mSupportsPan = true;
protected boolean mSupportsZoom = true;
protected boolean mSupportsScaleAtFocus = true;
protected boolean mSupportsOnTouchDown = false;
protected boolean mSupportsOnTouchUp = false;


    protected long mLongPressTimeOut;
    protected long mDownTime;

    protected boolean mIsMove;
    protected boolean mIsLongPress;

    static private final float SCROLL_THRESHOLD = 20;
    
/**
 */
public PanZoomView (Context context) {
    this(context, null, 0);
}

public PanZoomView (Context context, AttributeSet attrs) {
    this(context, attrs, 0);
}

public PanZoomView (Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
    setupToDraw (context, attrs, defStyle);
    setupScaleDetector (context, attrs, defStyle);
}



public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        } else {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
    }
    return inSampleSize;
}



public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
        int reqWidth, int reqHeight) {


    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);


    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);


    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
}


 
public void disableTouch (boolean disable) {
    mTouchable = disable;
}

public void disableTouch () {
    disableTouch (true);
}
 

public float getScaleFactor () {
    return mScaleFactor;
}

public void drawOnCanvas (Canvas canvas) {
    mSampleImage.draw(canvas);
}

@Override public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    zeichnen (canvas);
}



protected void zeichnen (Canvas canvas) {
    canvas.save();

    float x = 0, y = 0;
    x = mPosX + mPosX0;
    y = mPosY + mPosY0;
    if (mSupportsZoom || mSupportsPan) {
       if (mSupportsPan && !mSupportsZoom) {
          canvas.translate(x, y);
          canvas.scale(mScaleFactor, mScaleFactor);

       } else if (mSupportsPan && mSupportsZoom) {
         if (mScaleDetector.isInProgress()) {


           mFocusX = mScaleDetector.getFocusX ();
           mFocusY = mScaleDetector.getFocusY ();
           canvas.scale(mScaleFactor, mScaleFactor, mFocusX, mFocusY);

         } else {

           canvas.translate(x, y);
           canvas.scale(mScaleFactor, mScaleFactor);

         }
       } else if (mSupportsZoom) {

         canvas.translate(mPosX0, mPosY0);
         mFocusX = mScaleDetector.getFocusX ();
         mFocusY = mScaleDetector.getFocusY ();
         canvas.scale(mScaleFactor, mScaleFactor, mFocusX -mPosX0, mFocusY -mPosY0);
   

       }
    }


    drawOnCanvas (canvas);

    canvas.restore();
}



public boolean onTouchEvent (MotionEvent ev) {


    if (!mTouchable) return false;


    if (!mSupportsZoom && !mSupportsPan) return false;




    mScaleDetector.onTouchEvent(ev);
    
    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
    case MotionEvent.ACTION_DOWN: {
        final float x = ev.getX();
        final float y = ev.getY();

        mIsMove = false;
        
        mLastTouchX = x;
        mLastTouchY = y;
        mActivePointerId = ev.getPointerId(0);
        if (mSupportsOnTouchDown) {
           onTouchDown (x, y);
        }
        if (mSupportsOnTouchUp) {

           mInitialTouchX = x;
           mInitialTouchY = y;
           if (AppConfig.DEBUG) Log.d (de.lmu.settlebattle.catanclient.utils.Constants.LOG_UI, "initial x, y : " + mInitialTouchX + ", " + mInitialTouchY);
           mDoTouchUp = true;
        }
        break;
    }
        
    case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        if (!mIsMove && (Math.abs(mInitialTouchX - x) > SCROLL_THRESHOLD
                         || Math.abs (mInitialTouchY - y) > SCROLL_THRESHOLD)) {
                if (AppConfig.DEBUG) Log.d (de.lmu.settlebattle.catanclient.utils.Constants.LOG_UI, "movement detected");
                mIsMove = true;
        }



        boolean scalingInProgress = mScaleDetector.isInProgress ();
        if (mSupportsPan && !scalingInProgress) {
           if (mIsMove) {
              final float dx = x - mLastTouchX;
              final float dy = y - mLastTouchY;

              mPosX += dx;
              mPosY += dy;



              invalidate();

           }
        } else if (scalingInProgress) mDoTouchUp = false;

        mLastTouchX = x;
        mLastTouchY = y;

        break;
    }
        
    case MotionEvent.ACTION_UP: {
        if (mIsMove) {
           mHandlingTouchUp = false;
           mDoTouchUp = false;
        } else {
          mActivePointerId = INVALID_POINTER_ID;
          if (mSupportsOnTouchUp && mDoTouchUp) {
             final float x = ev.getX();
             final float y = ev.getY();
             try {
                 mHandlingTouchUp = true;
                 onTouchUp (mInitialTouchX, mInitialTouchY, x, y);
             } finally {
               mHandlingTouchUp = false;
             }
             mDoTouchUp = false;
          }
        }
        break;
    }
        
    case MotionEvent.ACTION_CANCEL: {
        mActivePointerId = INVALID_POINTER_ID;
        break;
    }
    
    case MotionEvent.ACTION_POINTER_UP: {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {


            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastTouchX = ev.getX(newPointerIndex);
            mLastTouchY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
        break;
    }
    }

    this.performClick ();

    return true;
}


public void onTouchDown (float downX, float downY) {
    mDownTime = System.nanoTime ();

}



public void onTouchUp (float downX, float downY, float upX, float upY) {

}



@Override public boolean performClick() {
   super.performClick();
   return true;
}



public int sampleDrawableId () {
    return DefaultDrawableId;
}


public void setScaleMinMax (float minScale, float maxScale) {
    mMinScaleFactor = minScale;
    mMaxScaleFactor = maxScale;
}



protected void setupScaleDetector (Context context, AttributeSet attrs, int defStyle) {

    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
}



protected void setupToDraw (Context context, AttributeSet attrs, int defStyle) {
    mIsMove = false;
    mIsLongPress = false;

    mTouchable = true;
    mSupportsOnTouchDown = supportsOnTouchDown ();
    mSupportsOnTouchUp  = supportsOnTouchUp ();
    mSupportsPan = supportsPan ();
    mSupportsZoom = supportsZoom ();
    mSupportsScaleAtFocus = supportsScaleAtFocusPoint ();


    mLongPressTimeOut = ViewConfiguration.getLongPressTimeout () * 1000000;

    int resourceId = sampleDrawableId ();
    if (resourceId == 0) return;
    Resources res = context.getResources ();
    Theme theme = context.getTheme ();
    mSampleImage = res.getDrawable(resourceId, theme);

    if (mSampleImage != null) {
    	mSampleImage.setBounds(0, 0, mSampleImage.getIntrinsicWidth(), 
    			                     mSampleImage.getIntrinsicHeight());
    }
}



public boolean supportsOnTouchDown () {
    return false;
}



public boolean supportsOnTouchUp () {
    return false;
}



public boolean supportsPan () {
    return true;
}



public boolean supportsScaleAtFocusPoint () {
    return ScaleAtFocusPoint;
}


public boolean supportsZoom () {
    return true;
}



public boolean unzoomed () {
    return (mScaleFactor == 1.0f);
}


protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!mSupportsZoom) return true;
        mScaleFactor *= detector.getScaleFactor();
        

        mScaleFactor = Math.max(mMinScaleFactor, Math.min(mScaleFactor, mMaxScaleFactor));
        mFocusX = detector.getFocusX ();
        mFocusY = detector.getFocusY ();

        invalidate();
        return true;
    }
}

}
