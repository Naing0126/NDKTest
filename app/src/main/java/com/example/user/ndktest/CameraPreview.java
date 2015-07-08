package com.example.user.ndktest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.params.Face;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naing on 2015-07-06.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    String TAG = "CAMERA_PREVIEW";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Activity mActivity;
    private FaceDetectorView mDetectedView;

    public CameraPreview(Context context){
        super(context);

    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        mActivity = (Activity)context;
        if(checkCameraHardware(context)){
            mCamera = getCameraInstance();
        }


        //mDetectedView = new FaceDetectorView(context);

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

    public void setDetectedView(FaceDetectorView view){
        mDetectedView = view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    // preview를 holder로 받은 SurfaceHolder에 뿌려준다
        try {
            Camera.Parameters parameters = mCamera.getParameters();

            setCameraDisplayOrientation(mActivity);

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

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
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Log.i(TAG, "Number of available camera : "+Camera.getNumberOfCameras());
            return true;
        } else {
            Toast.makeText(context, "No camera found!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static int findFrontFacingCamera(){
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for(int i = 0;i<numberOfCameras;i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i,info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(findFrontFacingCamera());
        }
        catch (Exception e){
            // using or disable
        }
        return c;
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
        Log.d(TAG,"# of Faces is " +params.getMaxNumDetectedFaces());
        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            mCamera.setFaceDetectionListener(faceDetectionListener);
            mCamera.startFaceDetection();
        }
    }

    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0){
                //Log.d(TAG, "face detected: " + faces.length);
                mDetectedView.setFaces(faces);
                mDetectedView.invalidate();
            }else {
                Log.d(TAG, "No faces detected");
                //faces = null;
                //mDetectedView.setFaces(faces);
                mDetectedView.invalidate();
            }
        }
    };
}

