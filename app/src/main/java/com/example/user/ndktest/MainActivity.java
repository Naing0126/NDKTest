package com.example.user.ndktest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity {
    private static final String TAG = "naing";

    private CameraPreview mPreview;
    private FaceDetectorView mDetectView;

    static{
        try {
            System.loadLibrary("NDKTest");
            System.loadLibrary("opencv_java");
        } catch (UnsatisfiedLinkError e){
        }
    }


    public native int getMatHeight(int w, int h);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

//        TextView view = (TextView) findViewById(R.id.textView);
//        view.setText("result is "+getMatHeight(50,10));

        mPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        mDetectView = (FaceDetectorView) findViewById(R.id.detectView);
        mPreview.setDetectedView(mDetectView); // Previewø° Face Detector View ∞¥√º∏¶ ¿˙¿Â«ÿµ–¥Ÿ
    }

    @Override
    protected void onPause() {
        mPreview.releaseCamera();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mPreview.releaseCamera();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
