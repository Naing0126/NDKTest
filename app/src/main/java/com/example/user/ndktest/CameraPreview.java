package com.example.user.ndktest;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by Naing on 2015-07-06.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    String TAG = "CAMERA_PREVIEW";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Activity mActivity;

    public CameraPreview(Context context, Camera camera){
        super(context);
        mActivity = (Activity)context;
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        Log.d(TAG, "before : surface view's width " + width + " height " + height);

        if(width * 4/3 < height) {
            height = (int) ((double) width * (double) 4 / 3);
        }else if(height * 3/4 < width){
            width = (int) ((double) width * (double) 4 / 3);
        }
        Log.d(TAG, "after : surface view's width " + width + " height " + height);

        setMeasuredDimension(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    // preview를 holder로 받은 SurfaceHolder에 뿌려준다
        try {
            Camera.Parameters parameters = mCamera.getParameters();

            setCameraDisplayOrientation(mActivity);

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            mCamera.setFaceDetectionListener(new MyFaceDetectionListener());

            startFaceDetection();

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }

        FaceDetector.Face[] faces = new FaceDetector.Face[10];

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void setCameraDisplayOrientation(Activity activity){

        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        Camera.CameraInfo info = new Camera.CameraInfo();
        // default camera id is 0 (default : 카메라 두개. 후방 0, 전방 1)
        Camera.getCameraInfo(0, info);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { // 전면카메라
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // 후면카메라
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            mCamera.startFaceDetection();
        }
    }
}

class MyFaceDetectionListener implements Camera.FaceDetectionListener{

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0){
            Log.d("FaceDetection", "face detected: "+ faces.length +
                    " Face 1 Location X: " + faces[0].rect.centerX() +
                    "Y: " + faces[0].rect.centerY() );
        }
    }
}