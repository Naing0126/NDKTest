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
 */
public class FaceDetectorView extends View {

    private static final String TAG = "CAMERA_PREVIEW";
    private Paint mPaint;
    private Paint mTextPaint;
    private Camera.Face[] mFaces;
    private Rect mRect;

    public Camera mCamera;

    private int moffsetX,moffsetY;

    public int mState = 1; // 0 is down


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

        moffsetX = moffsetY = 0;

    }

    public void setCamera(Camera camera){
        mCamera = camera;

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters params = mCamera.getParameters();
                if (mState == 0) {
                    mCamera.stopFaceDetection();
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
        invalidate();
    }

    public Rect nomalizing(Rect before){
        Rect after = new Rect();
        // set(left,top,right,bottom)
        after.set((1000 - before.bottom) * getWidth() / 2000, (1000 - before.right) * (getWidth() * 4 / 3) / 2000,
                (1000 - before.top) * getWidth() / 2000, (1000 - before.left) * (getWidth() * 4 / 3) / 2000);

        return after;
    }

    public Rect moving(Rect before){
        Rect after = new Rect();
        Log.d("touchEvent", "before offset (" + before.centerX() + "," + before.centerY() + "/ " + before.top + "," + before.bottom + "," + before.left + "," + before.right + ")");
        after.set(before.left + moffsetX, before.top + moffsetY, before.right + moffsetX, before.bottom + moffsetY);
        Log.d("touchEvent", "after offset (" + after.centerX() + "," + after.centerY() + "/ " + after.top + "," + after.bottom + "," + after.left + "," + after.right + ")");
        return after;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mState == 1) {
            if (mFaces != null && mFaces.length > 0) {
                mRect = new Rect();
                for (Camera.Face face : mFaces) {
                    Log.d(TAG, "face info (" + face.rect.centerX() + "," + face.rect.centerY() + " / " + face.rect.top + " , " + face.rect.bottom + "," + face.rect.left + "," + face.rect.right + ")");
                    Log.d(TAG, "face info (" + face.rect.width() + "," + face.rect.height() + ")");

                    canvas.drawText("Detect Face!", 20, 40, mTextPaint);
                    //canvas.drawText("("+getWidth()/2+","+getHeight()/2+")",getWidth()/2,getHeight()/2,mTextPaint);

                    mRect = nomalizing(face.rect);

                    canvas.drawRect(mRect, mPaint);

                    canvas.drawText("before : " + face.rect.top + "," + face.rect.bottom + "," + face.rect.left + "," + face.rect.right, 20, getWidth() * 4 / 3 - 80, mTextPaint);
                    canvas.drawText("after : " + mRect.top + "," + mRect.bottom + "," + mRect.left + "," + mRect.right, 20, getWidth() * 4 / 3 - 40, mTextPaint);
                }

                canvas.restore();
                mFaces = null;
            } else {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                canvas.drawText("No Face", 20, 40, mTextPaint);
                //Log.d(TAG,"passed faces is null");
            }
        }else if (mState == 0){
            Log.d("touchEvent","onDraw! "+mRect.centerX()+","+mRect.centerY());
            canvas.drawText("Draging Mode", 20, 40, mTextPaint);

            Rect afterRect = moving(mRect);

            mRect = afterRect;
            canvas.drawRect(afterRect, mPaint);
            canvas.restore();
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
                mPaint.setColor(Color.GREEN);
                mPaint.setAlpha(128);
                Log.d("touchEvent", "Up");
                break;
            case MotionEvent.ACTION_DOWN:
                if (mRect.contains((int)x,(int)y)==true) {
                    mState = 0;
                    mFaces=null;
                    Log.d("touchEvent","Hit!!!!!!!!1");

                }else{
                    Log.d("touchEvent","MISS...");
                }
                break;
            case MotionEvent.ACTION_MOVE :
                if (mRect.contains((int)x,(int)y)==true) {
                    mState = 0;
                    int length = event.getHistorySize();

                    if(length != 0){
                        moffsetX = (int)(event.getHistoricalX(length-1)-event.getHistoricalX(0));
                        moffsetY = (int)(event.getHistoricalY(length-1)-event.getHistoricalY(0));
                        Log.d("touchEvent","offset "+moffsetX+", "+moffsetY);
                    }
                    invalidate();
                }
                Log.d("touchEvent", "Move");
                break;
        }
        return true;
    }



}
