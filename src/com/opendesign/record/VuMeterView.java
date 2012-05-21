/**
 * Copyright (C) 2012 The OpenDESIGN Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.opendesign.record;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class VuMeterView extends View {
    int mCurrentValue = 0;
    int mPeakValue = 0;

    // Max value returned by Recorder getMaxAmplitude
    final int mMaxValue = 26400;

    public VuMeterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VuMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VuMeterView(Context context) {
        super(context);
    }

    public void setValue(int value) {
        if (value == 0)
            return; // if we poll audiorecorder too fast, we have a zero value,
                    // making meter flickering

        mCurrentValue = value;

        if (value > mPeakValue)
            mPeakValue = value;
    }

    public void resetPeak() {
        mPeakValue = mCurrentValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(30);

        // draw meter line
        paint.setColor(0xFF99CC00);
        canvas.drawLine(0, 0, mCurrentValue * getWidth() / mMaxValue, 0, paint);

        // draw peak
        paint.setColor(0xFFCC0000);
        canvas.drawLine(mPeakValue * getWidth() / mMaxValue - 10, 0, mPeakValue
                * getWidth() / mMaxValue, 0, paint);
    }
}