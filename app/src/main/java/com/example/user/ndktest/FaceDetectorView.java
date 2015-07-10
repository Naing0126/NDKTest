package com.example.user.ndktest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Naing on 2015-07-07.
 * Draw face box in Custom View
 * Faces is passed by CameraPreview
 */
public class FaceDetectorView extends View {

    private static final String TAG = "VIEW_CONTROL";
    private Paint mPaint;
    private Paint mTextPaint;
    private Camera.Face[] mFaces;
    private Rect mRect; // default face box
    private Rect mCurrRect; // current face box

    private Camera mCamera;

    private int moffsetX;
    private int moffsetY;

    public int mState = 1; // 0 is Drag Mode, 1 is Detecting Mode
    public boolean mTracking = false;

    public FaceDetectorView(Context context) {
        super(context);
    }

    public FaceDetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(128);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(40);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStyle(Paint.Style.FILL);

        moffsetX = moffsetY = 0; // initialize

    }

    public void setCamera(Camera camera){
        mCamera = camera;

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mState == 0) { // Camera release in Drag Mode
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
    }
    public void setFaces(Camera.Face[] faces){
        mFaces = faces;
        Log.d(TAG, "# of passed faces is " + mFaces.length);
    }

    public Rect nomalizing(Rect before){
        // Normalizing detected face rect to displayed face rect
        Rect after = new Rect();
        // set(left,top,right,bottom)
        after.set((1000 - before.bottom) * getWidth() / 2000, (1000 - before.right) * (getWidth() * 4 / 3) / 2000,
                (1000 - before.top) * getWidth() / 2000, (1000 - before.left) * (getWidth() * 4 / 3) / 2000);

        return after;
    }

    public Rect moving(Rect before){
        Rect after = new Rect();
        //Log.d("touchEvent", "before offset (" + before.centerX() + "," + before.centerY() + "/ " + before.top + "," + before.bottom + "," + before.left + "," + before.right + ")");
        after.set(before.left + moffsetX, before.top + moffsetY, before.right + moffsetX, before.bottom + moffsetY);
        //Log.d("touchEvent", "after offset (" + after.centerX() + "," + after.centerY() + "/ " + after.top + "," + after.bottom + "," + after.left + "," + after.right + ")");
        return after;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mState == 1) {
            // Detect Mode
            if (mFaces != null && mFaces.length > 0) {
                mRect = new Rect();
                for (Camera.Face face : mFaces) {
                    Log.d(TAG, "face info (" + face.rect.centerX() + "," + face.rect.centerY() + " / " + face.rect.top + " , " + face.rect.bottom + "," + face.rect.left + "," + face.rect.right + ")");
                    Log.d(TAG, "face info (" + face.rect.width() + "," + face.rect.height() + ")");

                    canvas.drawText("Detect Face!", 20, 60, mTextPaint);
                    //canvas.drawText("("+getWidth()/2+","+getHeight()/2+")",getWidth()/2,getHeight()/2,mTextPaint);

                    mRect = nomalizing(face.rect);
                    mCurrRect = mRect;

                    canvas.drawRect(mRect, mPaint);

                    canvas.drawText("before : " + face.rect.top + "," + face.rect.bottom + "," + face.rect.left + "," + face.rect.right, 20, getWidth() * 4 / 3 - 80, mTextPaint);
                    canvas.drawText("after : " + mRect.top + "," + mRect.bottom + "," + mRect.left + "," + mRect.right, 20, getWidth() * 4 / 3 - 40, mTextPaint);
                }

                mFaces = null; // remove faces before new detect
            } else {
                // canvas clear
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                canvas.drawText("No Face", 20, 60, mTextPaint);
                //Log.d(TAG,"passed faces is null");
            }
        }else if (mState == 0){
            // Drag Mode
            canvas.drawText("Draging Mode", 20, 60, mTextPaint);

            mCurrRect = moving(mRect);
            //mRect = mCurrRect;

            Log.d("touchEvent", "Draw from " + mRect.centerX() + "," + mRect.centerY());
            Log.d("touchEvent", "Draw to " + mCurrRect.centerX() + "," + mCurrRect.centerY());

            canvas.drawText("before offset : " + mRect.top + "," + mRect.bottom + "," + mRect.left + "," + mRect.right, 20, getWidth() * 4 / 3 - 80, mTextPaint);
            canvas.drawText("after offset : " + mCurrRect.top + "," + mCurrRect.bottom + "," + mCurrRect.left + "," + mCurrRect.right, 20, getWidth() * 4 / 3 - 40, mTextPaint);

            canvas.drawRect(mCurrRect, mPaint);
        }
        super.onDraw(canvas);

    }

    public boolean onTouchEvent(MotionEvent event){
        // get touch action
        int action = event.getAction();
        // touched x
        float x = event.getX();
        // touched y
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_UP:
                Log.d("touchEvent", "Up");
                break;
            case MotionEvent.ACTION_DOWN:
                Log.d("touchEvent", "Down");
                if (mCurrRect.contains((int)x,(int)y)==true) {
                    mTracking = true;
                }
                break;
            case MotionEvent.ACTION_MOVE :
                Log.d("touchEvent", "Move");
                if (mTracking) {
                    // Touched in rect
                    if(mState == 1){
                        mState = 0;

                        mPaint.setColor(Color.YELLOW);
                        mPaint.setAlpha(128);

                        mTextPaint.setColor(Color.YELLOW);
                    }

                    int length = event.getHistorySize();

                    if(length != 0){
                        moffsetX += (int)(event.getHistoricalX(length - 1) - event.getHistoricalX(0))*2;
                        moffsetY += (int)(event.getHistoricalY(length - 1) - event.getHistoricalY(0))*2;
                        Log.d("touchEvent","offset " + moffsetX + ", " + moffsetY);
                    }
                    invalidate();
                }
                break;
        }
        return true;
    }
}
