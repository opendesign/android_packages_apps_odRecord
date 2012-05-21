/**
 * Copyright (C) 2012 The OpenDESIGN Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.opendesign.record;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class OdRecordActivity extends Activity {
    private AudioRecorder mRecorder = null;
    private Timer mUiTimer = null;
    private long mRecordingStartTime = 0;

    class UiUpdateTask extends TimerTask {
        public void run() {
            final VuMeterView vuMeter = (VuMeterView) findViewById(R.id.vuMeterView);
            final TextView durationText = (TextView) findViewById(R.id.textRecordDuration);

            vuMeter.setValue(mRecorder.getAmplitude());

            final long duration = System.currentTimeMillis() - mRecordingStartTime;
            
            runOnUiThread(new Runnable() {
                public void run() {
                    durationText.setText(String.format("%02d:%02d", 
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) - 
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                        ));
                    vuMeter.invalidate();
                }
            });

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    public void makeAlert(final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onClickRecord(View v) {
        final ToggleButton tb = (ToggleButton) findViewById(R.id.btnRecord);

        if (tb.isChecked()) {
            // force disabled before recording
            tb.setChecked(false);
            tb.setEnabled(false);

            // prepare recording
            SimpleDateFormat fileNameDate = new SimpleDateFormat();
            fileNameDate.applyPattern("yyyy-MM-dd HH.mm.ss");
            mRecorder = new AudioRecorder(fileNameDate.format(new Date())
                    + ".3gp");

            // start recording
            try {
                mRecorder.start();
            } catch (IOException e) {
                makeAlert("Error while starting recording:\n" + e.getMessage());
                tb.setChecked(false);
                tb.setEnabled(true);
            } finally {
                tb.setChecked(true);
                tb.setEnabled(true);

                mUiTimer = new Timer();
                mUiTimer.schedule(new UiUpdateTask(), 10, 50);

                mRecordingStartTime = System.currentTimeMillis();
            }
        } else {
            tb.setChecked(true);
            tb.setEnabled(false);

            try {
                mUiTimer.cancel();
                mRecorder.stop();
            } catch (IOException e) {
                makeAlert("Error while stopping recording:\n" + e.getMessage());
                tb.setChecked(true);
                tb.setEnabled(true);
            } finally {
                tb.setChecked(false);
                tb.setEnabled(true);
            }
        }
    }
}