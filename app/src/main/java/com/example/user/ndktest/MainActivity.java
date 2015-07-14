package com.example.user.ndktest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
    private static final String TAG = "naing";

    private CameraPreview mPreview;
    private FaceDetectorView mDetectView;
    private RelativeLayout mRelativeBottom;
    private RelativeLayout mShutterChnage;
    private RelativeLayout mDoneView;
    private RelativeLayout mBackView;
    private RelativeLayout mCheckView;

    private TextView textview;
    private ImageButton shutter;

    private ImageButton Back;
    private ImageButton Check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        //View
        mPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        mDetectView = (FaceDetectorView) findViewById(R.id.detectView);
        mRelativeBottom = (RelativeLayout)findViewById(R.id.bottomlayout);
        mShutterChnage = (RelativeLayout) findViewById(R.id.shutterchange);
        mDoneView=(RelativeLayout)findViewById(R.id.donelayout);
        mBackView=(RelativeLayout)findViewById(R.id.left);
        mCheckView=(RelativeLayout)findViewById(R.id.right);

        //Preview View setting
        mPreview.setRelativeBottom(mRelativeBottom);
        mPreview.setDetectedView(mDetectView); // Previewø° Face Detector View ∞¥√º∏¶ ¿˙¿Â«ÿµ–¥Ÿ
        mPreview.setShutterChange(mShutterChnage);

        //Detector VIew setting
        mDetectView.setShutterChange(mShutterChnage);
        mDetectView.setBottomView(mRelativeBottom);
        mDetectView.setPreView(mPreview);

        //handling Shutter button event
        textview = (TextView)findViewById(R.id.textview);
        shutter = (ImageButton)findViewById(R.id.shutter);
        mDetectView.setShutter(textview, shutter);

        //handling bottom button event
        Back = (ImageButton)findViewById(R.id.back);
        Check =(ImageButton)findViewById(R.id.check);
        mDetectView.setBack(Back,mBackView);
        mDetectView.setCheck(Check,mCheckView);

        //handling done layout
        mDetectView.setDoneView(mDoneView);
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
