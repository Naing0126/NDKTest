package com.example.user.ndktest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Naing on 2015-07-07.
 * Draw face box in Custom View
 * Faces is passed by CameraPreview
 */
public class FaceDetectorView extends View {

    private static final String TAG = "VIEW_CONTROL";
    private Paint mPaint;
   private Camera.Face[] mFaces;
    private Rect mRect; // default face box
    //private Rect mCurrRect; // current face box

    private Bitmap mFaceline;
    private TextView text;
    private ImageButton shutterButton;
    private int ShutterFlag =0;

    private ImageButton Back;
    private ImageButton Check;

    private Camera mCamera;

    private int ShutterOn;

    private int moffsetX;
    private int moffsetY;

    private int topsizing;
    private int bottomsizing;

    private RelativeLayout mShutterChange;
    private RelativeLayout mBottomView;
    private RelativeLayout mDoneView;
    private RelativeLayout mCheckView;
    private RelativeLayout mBackView;
    private int backflag=0;

    public int mState = 1; // 0 is Drag Mode, 1 is Detecting Mode
    public boolean mTracking = false;
    private CameraPreview mPreview;

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

        moffsetX = moffsetY = 0; // initialize

        Resources res = getResources();
        BitmapDrawable bd = (BitmapDrawable)res.getDrawable(R.drawable.coordinate_face_line);
        mFaceline = bd.getBitmap();
    }

    public void setCamera(Camera camera){
        mCamera = camera;

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mState == 0) { // Camera release in Drag Mode
                    mCamera.stopPreview();
                    //mCamera.release();
                    Log.d("test1234","camera"+mCamera);
                    //mCamera = null;
                }
                // back button click
             /*   if(backflag ==1){
                    Log.d("test1234", "startpreview");
                    mCamera.startPreview();
                    mPreview.startFaceDetection();
                }*/
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
        topsizing=((1000-before.right)*(getWidth()*4/3)/2000 - ((1000 - before.left) * (getWidth() * 4 / 3) / 2000))*1/5;
        bottomsizing=((1000-before.right)*(getWidth()*4/3)/2000 - ((1000 - before.left) * (getWidth() * 4 / 3) / 2000))*1/8;
        // set(left,top,right,bottom)
        after.set((1000 - before.bottom) * getWidth() / 2000, ((1000 - before.right) * (getWidth() * 4 / 3) / 2000) + topsizing,
                (1000 - before.top) * getWidth() / 2000, ((1000 - before.left) * (getWidth() * 4 / 3) / 2000) - bottomsizing);
        return after;
    }

    public Bitmap resizeBitmapImage(Bitmap source,Rect destination)
    {
        int width=destination.width();
        int height=destination.height();
        return Bitmap.createScaledBitmap(source,width,height,true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mState == 1) {
            Log.d("test123","backon");
            // back button click
            if(backflag==1){
                // canvas clear
                Log.d("test123", "clear on");
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            }
            // Detect Mode
            if (mFaces != null && mFaces.length > 0) {
                mRect = new Rect();
                for (Camera.Face face : mFaces) {
                    Log.d(TAG, "face info (" + face.rect.centerX() + "," + face.rect.centerY() + " / " + face.rect.top + " , " + face.rect.bottom + "," + face.rect.left + "," + face.rect.right + ")");
                    Log.d(TAG, "face info (" + face.rect.width() + "," + face.rect.height() + ")");

                    mRect = nomalizing(face.rect);

                    //canvas.drawRect(mRect, mPaint);
 }

                mFaces = null; // remove faces before new detect
            } else {
                // canvas clear
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            }
        }else if (mState == 0){
            // Drag Mode
            mFaceline = resizeBitmapImage(mFaceline, mRect);

            Log.d("touchEvent", "Draw from " + mRect.width() + "," + mRect.height());
            Log.d("touchEvent", "Draw to " + (mRect.width() + moffsetX) + "," + (mRect.height()+moffsetY));

            canvas.drawBitmap(mFaceline,mRect.left+moffsetX,mRect.top+moffsetY,null);
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
                if (mRect.contains((int)x,(int)y)==true) {
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

    //shutter click handling
    public void setShutter(TextView textview, ImageButton shutter) {
        text=textview;
        shutterButton=shutter;

        shutterButton.setOnClickListener(new OnClickListener() {
            //클릭시 text change, string으로 해결
            @Override
            public void onClick(View v) {
                text.setText(getResources().getString(R.string.shutter));
                // mBottomView.setVisibility(View.GONE);
                mCamera.stopPreview();
                mShutterChange.setVisibility(View.VISIBLE);
                mState = 0;
                backflag = 0;
            }
        });
    }

    //Back button handling
    public void setBack(ImageButton BackButton, RelativeLayout BackView){
        Back = BackButton;
        mBackView = BackView;

        Back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomView.setVisibility(View.VISIBLE);
                mShutterChange.setVisibility(View.GONE);
                text.setText(getResources().getString(R.string.back));
                mState=1;
                backflag=1;
                invalidate();
                // restartpreview
                Log.d("startpreview","start");
                mCamera.startPreview();
                Log.d("startpreview","finish ");

            }
        });

        /*
        Back.setOnClickListener(new () {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBackView.setBackgroundColor(Color.rgb(150, 91, 190));
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBackView.setBackgroundColor(Color.rgb(123, 63, 163));
                        mBottomView.setVisibility(View.VISIBLE);
                        mShutterChange.setVisibility(View.GONE);
                        text.setText(getResources().getString(R.string.back));
                        return true;
                }
                return false;
            }
        });*/
    }

    //Check button handling
    public void setCheck(ImageButton CheckButton, RelativeLayout CheckView){
        Check = CheckButton;
        mCheckView=CheckView;

        Check.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mState=1;
                backflag=1;
                invalidate();
                mBottomView.setVisibility(View.VISIBLE);
                mShutterChange.setVisibility(View.GONE);
                mDoneView.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);

                mCamera.stopPreview();
                mCamera.release();
                mCamera=null;
                
            }
        });
        /*Check.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mCheckView.setBackgroundColor(Color.rgb(150,91,190));
                        return true;
                    case MotionEvent.ACTION_UP:
                        mCheckView.setBackgroundColor(Color.rgb(91,28,133));
                        mBottomView.setVisibility(View.VISIBLE);
                        mShutterChange.setVisibility(View.GONE);
                        mDoneView.setVisibility(View.VISIBLE);
                        text.setVisibility(View.GONE);
                        return true;
                }
                return false;
            }
        });*/
    }

    public void setDoneView(RelativeLayout view){
        mDoneView = view;
    }
    public void setBottomView(RelativeLayout view){
        mBottomView = view;
    }

    public void setShutterChange(RelativeLayout view){
        mShutterChange=view;
    }

    public void setPreView(CameraPreview preView) {
        mPreview = preView;
    }
}