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
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class OdRecordActivity extends Activity {
    private AudioRecorder mRecorder = null;
    private Timer mUiTimer = null;
    private long mRecordingStartTime = 0;
    private String mLastFileName = "";
    private MediaPlayer mMediaPlayer = null;

    class UiUpdateTask extends TimerTask {
        public void run() {
            final VuMeterView vuMeter = (VuMeterView) findViewById(R.id.vuMeterView);
            final TextView durationText = (TextView) findViewById(R.id.textRecordDuration);

            vuMeter.setValue(mRecorder.getAmplitude());

            final long duration = System.currentTimeMillis()
                    - mRecordingStartTime;

            runOnUiThread(new Runnable() {
                public void run() {
                    durationText.setText(String.format(
                            "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration)
                                    - TimeUnit.MINUTES
                                            .toSeconds(TimeUnit.MILLISECONDS
                                                    .toMinutes(duration))));
                    vuMeter.invalidate();
                }
            });

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ToggleButton playBtn = (ToggleButton) findViewById(R.id.btnPlayLastRecord);
        playBtn.setEnabled(false);
        playBtn.setAlpha(0.3f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_share:
            if (mLastFileName.equals("")) {
                Toast.makeText(this,
                        getResources().getString(R.string.no_recording_done),
                        Toast.LENGTH_LONG).show();
                return true;
            }

            Intent sharingIntent = new Intent(
                    android.content.Intent.ACTION_SEND);
            sharingIntent.setType("audio/3gpp");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.share_title));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    getResources().getString(R.string.share_body));
            sharingIntent.putExtra(Intent.EXTRA_STREAM,
                    Uri.parse("file:///sdcard/Recording/" + mLastFileName));
            startActivity(Intent.createChooser(sharingIntent, getResources()
                    .getString(R.string.share_dialog_title)));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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

    public void onClickPlay(View v) {
        if (mMediaPlayer == null) {
            String PATH_TO_FILE = "/sdcard/Recording/" + mLastFileName;
            mMediaPlayer = new MediaPlayer();
    
            try {
                mMediaPlayer.setDataSource(PATH_TO_FILE);
                mMediaPlayer.prepare();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
            
            mMediaPlayer.start();
        } else {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
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
            mLastFileName = fileNameDate.format(new Date()) + ".3gp";
            mRecorder = new AudioRecorder(mLastFileName);

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
                
                ToggleButton playBtn = (ToggleButton) findViewById(R.id.btnPlayLastRecord);
                playBtn.setEnabled(true);
                playBtn.setAlpha(1f);
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