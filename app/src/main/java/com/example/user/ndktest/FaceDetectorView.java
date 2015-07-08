package com.example.user.ndktest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Naing on 2015-07-07.
 */
public class FaceDetectorView extends View {

    private static final String TAG = "CAMERA_PREVIEW";
    private Paint mPaint;
    private Paint mTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private Camera.Face[] mFaces;


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

    }

    public void setFaces(Camera.Face[] faces){
        mFaces = faces;
        Log.d(TAG, "# of passed faces is " + mFaces.length);
        invalidate();
    }

    public Rect nomalizing(Rect before){
        Rect after = new Rect();
        after.set((1000-before.bottom)*getWidth()/2000,(1000-before.right)*(getWidth()*4/3)/2000,
                (1000-before.top)*getWidth()/2000,(1000-before.left)*(getWidth()*4/3)/2000);
        Log.d(TAG, "normalized (" + after.centerX() + "," + after.centerY() + "/ " +after.top + "," + after.bottom + "," + after.left + "," + after.right + ")");

        return after;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFaces != null && mFaces.length > 0) {
            Rect rect = new Rect();
            for (Camera.Face face : mFaces) {
                Log.d(TAG, "face info ("+face.rect.centerX() +"," +face.rect.centerY()+"/ "+face.rect.top +"," +face.rect.bottom+","+face.rect.left +"," +face.rect.right+")");
                Log.d(TAG, "face info (" + face.rect.width() + "," + face.rect.height() + ")");

                canvas.drawText("Detect Face!", 20, 40, mTextPaint);
                //canvas.drawText("("+getWidth()/2+","+getHeight()/2+")",getWidth()/2,getHeight()/2,mTextPaint);

                rect = nomalizing(face.rect);

                canvas.drawRect(rect, mPaint);

                canvas.drawText("before : "+face.rect.top +"," +face.rect.bottom+","+face.rect.left +"," +face.rect.right, 20,getWidth() * 4/3 - 80,mTextPaint);
                canvas.drawText("after : "+rect.top +"," +rect.bottom+","+rect.left +"," +rect.right, 20,getWidth() * 4/3 - 40,mTextPaint);

            }

            canvas.restore();
            mFaces = null;
        }else if(mFaces == null){
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawText("No Face", 20, 40, mTextPaint);

            Log.d(TAG,"passed faces is null" );
        }else{
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawText("No Face", 20, 40, mTextPaint);
            Log.d(TAG,"# of mFaces "+mFaces.length );
        }

        super.onDraw(canvas);

    }

}
