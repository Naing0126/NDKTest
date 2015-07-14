package com.example.user.ndktest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Naing on 2015-07-13.
 */
public class ImageControlView extends View{

    private static final String TAG = "IMAGE_CONTROL";

    private Bitmap mFaceline;
    private Bitmap mSize;
    private Bitmap mRotate;

    public ImageControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFaceline(Bitmap faceline){
        mFaceline = faceline;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
