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

    private int moffsetX,moffsetY;


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
        Log.d("touchEvent", "normalising : offset " + moffsetX + ", " + moffsetY);
        Log.d("touchEvent", "before offset (" + after.centerX() + "," + after.centerY() + "/ " + after.top + "," + after.bottom + "," + after.left + "," + after.right + ")");
        after.set(after.left + moffsetX, after.top + moffsetY, after.right + moffsetX, after.bottom + moffsetY);
        Log.d("touchEvent", "after offset (" + after.centerX() + "," + after.centerY() + "/ " + after.top + "," + after.bottom + "," + after.left + "," + after.right + ")");

        return after;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFaces != null && mFaces.length > 0) {
            mRect = new Rect();
            for (Camera.Face face : mFaces) {
                Log.d(TAG, "face info (" + face.rect.centerX() + "," + face.rect.centerY() + " / " + face.rect.top + " , " + face.rect.bottom + "," + face.rect.left + "," + face.rect.right + ")");
                Log.d(TAG, "face info (" + face.rect.width() + "," + face.rect.height() + ")");

                canvas.drawText("Detect Face!", 20, 40, mTextPaint);
                //canvas.drawText("("+getWidth()/2+","+getHeight()/2+")",getWidth()/2,getHeight()/2,mTextPaint);

                mRect = nomalizing(face.rect);

                canvas.drawRect(mRect, mPaint);

                canvas.drawText("before : "+face.rect.top +"," +face.rect.bottom+","+face.rect.left +"," +face.rect.right, 20,getWidth() * 4/3 - 80,mTextPaint);
                canvas.drawText("after : "+mRect.top +"," +mRect.bottom+","+mRect.left +"," +mRect.right, 20,getWidth() * 4/3 - 40,mTextPaint);


            }

            canvas.restore();
            mFaces = null;
        }else{
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawText("No Face", 20, 40, mTextPaint);
            //Log.d(TAG,"passed faces is null");
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
                    mFaces=null;
                    Log.d("touchEvent","Hit!!!!!!!!1");

                }else{
                    Log.d("touchEvent","MISS...");
                }
                break;
            case MotionEvent.ACTION_MOVE :
                if (mRect.contains((int)x,(int)y)==true) {
                    int length = event.getHistorySize();

                    if(length != 0){
                        moffsetX = (int)(event.getHistoricalX(length-1)-event.getHistoricalX(0));
                        moffsetY = (int)(event.getHistoricalY(length-1)-event.getHistoricalY(0));
                        Log.d("touchEvent","offset "+moffsetX+", "+moffsetY);
                    }

                    mPaint.setColor(Color.BLUE);
                    mPaint.setAlpha(128);
                }
                Log.d("touchEvent", "Move");
                break;
        }
        return true;
    }



}
