package com.example.myfirstapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

class MySurfaceView extends SurfaceView {

    private class Ripple {
        public int iteration, hue;
        public float x, y, ax, ay;

        public Ripple(int hue, float x, float y, float ax, float ay) {
            this.iteration = 0;
            this.hue = hue;
            this.x = x;
            this.y = y;
            this.ax = ax;
            this.ay = ay;
        }
    }

    private int mStep;
    private int mRippleHue;
    private LinkedList<Ripple> mRipples;
    private final int MAX_ITERATION = 100;
    private float[] mSensorValues = new float[3];

    public MySurfaceView(Context context) {
        super(context);
        setWillNotDraw(false);
        mStep = 0;
        mRipples = new LinkedList<Ripple>();
        mRippleHue = 0;
        for (int i = 0; i < mSensorValues.length; i++) {
            mSensorValues[i] = 0;
        }
    }

    public void setSensorValues(float[] values) {
        synchronized (mSensorValues) {
            for (int i = 0; i < mSensorValues.length; i++) {
                mSensorValues[i] = values[i];
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE)
            return false;

        final int pointers = event.getPointerCount();
        synchronized (mRipples) {
            synchronized (mSensorValues) {
                for (int i = 0; i < pointers; i++) {
                    mRippleHue = (mRippleHue + 1) % 360;
                    mRipples.add(new Ripple(mRippleHue, event.getX(i), event.getY(i),
                                            -mSensorValues[0], mSensorValues[1]));
                }
            }
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        Random rand = new Random();
        Paint paint = new Paint();
        float[] hsv = new float[]{0, 1, 1};
        // paint.setARGB(255, 200, 150, 100);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawARGB(255, 0, 0, 0);
        synchronized (mSensorValues) {
            if (mStep % 100 == 0) {
                Log.i("MSVSensor", String.format("x=%f, y=%f, z=%f", mSensorValues[0], mSensorValues[1], mSensorValues[2]));
            }
        }
        synchronized (mRipples) {
            for (Ripple r : mRipples) {
                r.iteration++;
                hsv[0] = r.hue;
                r.x += r.ax * 2;
                r.y += r.ay * 2;
                paint.setColor(Color.HSVToColor(Math.max(0, (MAX_ITERATION - r.iteration) * 2), hsv));
                paint.setStrokeWidth(10 - r.iteration / 10);
                canvas.drawCircle(Math.round(r.x), Math.round(r.y), r.iteration * 5, paint);
            }
            Iterator<Ripple> i = mRipples.iterator();
            while (i.hasNext()) {
                Ripple r = i.next();
                if (r.iteration > 100) {
                    i.remove();
                }
            }
        }
        mStep ++;
        postInvalidate();
    }
}
