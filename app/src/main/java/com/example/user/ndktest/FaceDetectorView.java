package com.example.user.ndktest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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

    private ImageControlView mControlView;

    private Paint mPaint;
    private Paint mTextPaint;
    private Camera.Face[] mFaces;
    private Rect mRect; // default face box
    private Rect mCurrRect; // current face box
    private Rect mSizeRect;
    private Rect mRotateRect;

    private Bitmap mFaceline;
    private Bitmap mSize;
    private Bitmap mRotate;

    private static final int CONTROL_BTN_SIZE = 100;

    private Camera mCamera;

    private int moffsetX;
    private int moffsetY;
    private double mScale;
    private float mDegree;

    private static final int DEFAULT_MODE = 0;
    private static final int DRAG_MODE = 1;
    private static final int RESIZE_MODE = 2;
    private static final int ROTATE_MODE = 3;

    public int mState = DEFAULT_MODE; // 0 is Drag Mode, 1 is Detecting Mode
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
        mScale = 1;
        mDegree = 0;

        Resources res = getResources();
        BitmapDrawable bd = (BitmapDrawable)res.getDrawable(R.drawable.default_image);
        mFaceline = bd.getBitmap();
        bd = (BitmapDrawable)res.getDrawable(R.drawable.coordinate_size);
        mSize = bd.getBitmap();
        bd = (BitmapDrawable)res.getDrawable(R.drawable.coordinate_rotate);
        mRotate = bd.getBitmap();

        Rect initBtn = new Rect();
        initBtn.set(0,0,CONTROL_BTN_SIZE,CONTROL_BTN_SIZE);
        mSize = resizeBitmapImage(mSize,initBtn);
        mRotate = resizeBitmapImage(mRotate, initBtn);

        mCurrRect = new Rect();
        //mCurrRect.set(mRect.left + moffsetX, mRect.top + moffsetY, mRect.right + moffsetX, mRect.bottom + moffsetY);

        mSizeRect = new Rect();

        mRotateRect = new Rect();
    }

    public void setCamera(Camera camera){
        mCamera = camera;

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mState == DRAG_MODE) { // Camera release in Drag Mode
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

    public void setControlView(ImageControlView view){
        mControlView = view;
    }

    public Rect nomalizing(Rect before){
        // Normalizing detected face rect to displayed face rect
        Rect after = new Rect();
        // set(left,top,right,bottom)
        after.set((1000 - before.bottom) * getWidth() / 2000, (1000 - before.right) * (getWidth() * 4 / 3) / 2000,
                (1000 - before.top) * getWidth() / 2000, (1000 - before.left) * (getWidth() * 4 / 3) / 2000);

        return after;
    }
/*
    public Rect moving(Rect before){
        Rect after = new Rect();
        //Log.d("touchEvent", "before offset (" + before.centerX() + "," + before.centerY() + "/ " + before.top + "," + before.bottom + "," + before.left + "," + before.right + ")");
        after.set(before.left + moffsetX, before.top + moffsetY, before.right + moffsetX, before.bottom + moffsetY);
        //Log.d("touchEvent", "after offset (" + after.centerX() + "," + after.centerY() + "/ " + after.top + "," + after.bottom + "," + after.left + "," + after.right + ")");
        return after;
    }
*/

    public Bitmap resizeBitmapImage(Bitmap source,Rect destination)
    {
        int width=destination.width();
        int height=destination.height();
        return Bitmap.createScaledBitmap(source,width,height,true);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(mState == DEFAULT_MODE) {
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
                    canvas.drawRect(mCurrRect, mPaint);

                    canvas.drawText("before : " + face.rect.top + "," + face.rect.bottom + "," + face.rect.left + "," + face.rect.right, 20, getWidth() * 4 / 3 - 80, mTextPaint);
                    canvas.drawText("after : " + mCurrRect.top + "," + mCurrRect.bottom + "," + mCurrRect.left + "," + mCurrRect.right, 20, getWidth() * 4 / 3 - 40, mTextPaint);


                    //mSizeRect = new Rect();
                    mSizeRect.set(mCurrRect.right - CONTROL_BTN_SIZE / 2, mCurrRect.top,
                            mCurrRect.right + CONTROL_BTN_SIZE / 2, mCurrRect.top + CONTROL_BTN_SIZE);

                    //mRotateRect = new Rect();
                    mRotateRect.set(mCurrRect.right - CONTROL_BTN_SIZE / 2, mCurrRect.bottom
                            - CONTROL_BTN_SIZE, mCurrRect.right + CONTROL_BTN_SIZE / 2, mCurrRect.bottom);

                }

                mFaces = null; // remove faces before new detect
            } else {
                // canvas clear
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                canvas.drawText("No Face", 20, 60, mTextPaint);
                //Log.d(TAG,"passed faces is null");
            }
        }else if (mState >= DRAG_MODE){
            switch(mState) {
                case DRAG_MODE:
                    // Drag Mode
                    canvas.drawText("Draging Mode", 20, 60, mTextPaint);
                    break;
                case RESIZE_MODE:
                    canvas.drawText("Resising Mode", 20, 60, mTextPaint);
                    break;
                case ROTATE_MODE:
                    canvas.drawText("Rotating Mode", 20, 60, mTextPaint);
                    break;
            }
/*

            Log.d("touchEvent", "Draw from " + mRect.width() + "," + mRect.height());
            Log.d("touchEvent", "Draw to " + (mRect.width() + moffsetX) + "," + (mRect.height() + moffsetY));

            Log.d("touchEvent", "(center" + (int) (mRect.centerX() + moffsetX - mRect.width() / 2) + "," +
                    (int) (mRect.centerY() + moffsetY - mRect.height() / 2) + "," +
                    (int) (mRect.centerX() + moffsetX + mRect.width() / 2) + "," +
                    (int) (mRect.centerY() + moffsetY + mRect.height() / 2) + ")");
            Log.d("touchEvent", "(origin" + (int) (mRect.left + moffsetX)+","+
                    (int) (mRect.top + moffsetY)+","+
                    (int) (mRect.right + moffsetX)+","+
                    (int) (mRect.bottom + moffsetY)+")");
            */

            canvas.drawText("before offset : " + mRect.centerX() + "," + mRect.centerY() + " / " + mRect.width() + "," + mRect.height(), 20, getWidth() * 4 / 3 - 80, mTextPaint);
            canvas.drawText("after offset : " + mCurrRect.centerX() + "," + mCurrRect.centerY() + " / " + mCurrRect.width() + "," + mCurrRect.height(), 20, getWidth() * 4 / 3 - 40, mTextPaint);
            canvas.drawText("offset : " + moffsetX + "," + moffsetY, 20, getWidth() * 4 / 3 - 120, mTextPaint);
            canvas.drawText("scale : " + mScale, 20, getWidth() * 4 / 3 - 160, mTextPaint);
            canvas.drawText("degree : " + mDegree, 20, getWidth() * 4 / 3 - 200, mTextPaint);

            canvas.drawBitmap(mFaceline, mCurrRect.left, mCurrRect.top, mPaint);

            canvas.drawBitmap(mSize, mSizeRect.left, mSizeRect.top, null);

            canvas.drawBitmap(mRotate,mRotateRect.left,mRotateRect.top,null);

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
                if(mState>DEFAULT_MODE)
                    mState = DRAG_MODE;
                break;
            case MotionEvent.ACTION_DOWN:
                Log.d("touchEvent", "Down");

                if(mSizeRect.contains((int)x,(int)y)==true){
                    mState = RESIZE_MODE;
                    Log.d("touchEvent", "resizing mode");
                }else if(mRotateRect.contains((int)x,(int)y)==true){
                    mState = ROTATE_MODE;
                    Log.d("touchEvent", "rotating mode");

                    Matrix matrix = new Matrix();
                    mDegree = (mDegree + 90) % 360;
                    matrix.setRotate(mDegree,mFaceline.getWidth()/2,mFaceline.getHeight()/2);

                    mFaceline = Bitmap.createBitmap(mFaceline,0,0,mFaceline.getWidth(),mFaceline.getHeight(),matrix,true);
                    mFaceline = resizeBitmapImage(mFaceline, mCurrRect);

                    //mFaceline.recycle();
                    invalidate();

                }else if (mCurrRect.contains((int)x,(int)y)==true) {
                    if(mState == DEFAULT_MODE){
                        Log.d("touchEvent", "Change to drag mode");
                        mState = DRAG_MODE;

                        mFaceline = resizeBitmapImage(mFaceline, mCurrRect);

                        mPaint.setColor(Color.BLUE);
                        mPaint.setAlpha(128);

                        mTextPaint.setColor(Color.BLUE);
                    }else{
                        mState = DRAG_MODE;
                        Log.d("touchEvent", "dragging mode");
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE :
                //Log.d("touchEvent", "Move");
                //Log.d("touchEvent","("+x+","+y+")");
                if (mSizeRect.contains((int) x, (int) y) == true && mState == RESIZE_MODE) {
                    Log.d("touchEvent", "Change scale");

                    int length = event.getHistorySize();

                    if (length != 0) {
                        int tx = (int) (event.getHistoricalX(length - 1) - event.getHistoricalX(0));
                        int ty = (int) (event.getHistoricalY(length - 1) - event.getHistoricalY(0));
                        int abs;
                        Log.d("touchEvent","tx "+tx+" ty "+ty);

                        if(tx==0 && ty==0){
                            break;
                        }
                        if(tx>0){
                            Log.d("touchEvent","to bigger");
                            if (tx < ty * (-1)) {
                                abs = (-1) * ty;
                            } else {
                                abs = tx;
                            }
                        }else{
                            Log.d("touchEvent","to smaller");
                            if (tx > ty * (-1)) {
                                abs = tx;
                            } else {
                                abs = (-1) * ty;
                            }
                        }

                        mScale = (mRect.width() * mScale + abs) / mRect.width();
                        Log.d("touchEvent", "abs " + abs + " mScale " + mScale);

                        mCurrRect.set(mCurrRect.left, (int) (mCurrRect.bottom - mRect.height() * mScale), (int) (mCurrRect.left + mRect.width() * mScale), mCurrRect.bottom);

                        Log.d("touchEvent", "mScale " + mScale + " before / after " + (double)mCurrRect.width()/(double)mRect.width());

                        mFaceline = resizeBitmapImage(mFaceline, mCurrRect);

                        mSizeRect.set(mCurrRect.right - CONTROL_BTN_SIZE / 2, mCurrRect.top,
                                mCurrRect.right + CONTROL_BTN_SIZE / 2, mCurrRect.top + CONTROL_BTN_SIZE);

                        mRotateRect.set(mCurrRect.right - CONTROL_BTN_SIZE / 2, mCurrRect.bottom
                                - CONTROL_BTN_SIZE, mCurrRect.right + CONTROL_BTN_SIZE / 2, mCurrRect.bottom);
                        Log.d("touchEvent", "mScale is " + mScale);

                    }
                    invalidate();

                }
                else if (mCurrRect.contains((int) x, (int) y) == true && mState == DRAG_MODE) {
                    Log.d("touchEvent", "Image Move");

                    int length = event.getHistorySize();

                    if (length != 0) {
                        int tx = (int) (event.getHistoricalX(length - 1) - event.getHistoricalX(0));
                        int ty = (int) (event.getHistoricalY(length - 1) - event.getHistoricalY(0));

                        mCurrRect.set(mCurrRect.left+tx,mCurrRect.top+ty,mCurrRect.right+tx,mCurrRect.bottom+ty);

                        mSizeRect.set(mCurrRect.right - CONTROL_BTN_SIZE / 2, mCurrRect.top,
                                mCurrRect.right + CONTROL_BTN_SIZE / 2, mCurrRect.top + CONTROL_BTN_SIZE);

                        mRotateRect.set(mCurrRect.right - CONTROL_BTN_SIZE / 2, mCurrRect.bottom
                                - CONTROL_BTN_SIZE, mCurrRect.right + CONTROL_BTN_SIZE / 2, mCurrRect.bottom);

                        moffsetX += tx;
                        moffsetY += ty;
                        //Log.d("touchEvent", "offset " + moffsetX + ", " + moffsetY);
                    }
                    invalidate();

                }
                break;
        }
        return true;
    }
}
