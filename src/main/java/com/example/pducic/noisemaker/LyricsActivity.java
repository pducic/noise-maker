package com.example.pducic.noisemaker;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;

public class LyricsActivity extends AbstractJamminActivity {

    private Task playTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        final HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        final LyricsView contentView = (LyricsView) findViewById(R.id.contentView);
        leftConfigButton = (Button) findViewById(R.id.leftConfigButton);
        rightConfigButton = (Button) findViewById(R.id.rightConfigButton);

        SoundsConfiguration defaultSoundConfiguration = MainConfiguration.getDefaultSoundConfiguration(true);
        defaultSoundConfiguration.init(this, soundPool);
        contentView.setContent(getResources().getString(R.string.lyrics_sretan_rodendan), getResources().getInteger(R.integer.lyrics_sretan_rodendan_takt), defaultSoundConfiguration);
        final int bpm = getResources().getInteger(R.integer.lyrics_sretan_rodendan_bpm);
        playTask = new Task() {
            private long startMillis =0;

            @Override
            void start() {
                super.start();
                startMillis = System.currentTimeMillis();
            }

            @Override
            protected void process() {
                int pixelsPerBeat = contentView.getPixelsPerTakt();
                long millisPlaying = System.currentTimeMillis() - startMillis;
                float beatsPerMillisecond = bpm / 60f /  1000;

                int pixels = (int) (millisPlaying * beatsPerMillisecond * pixelsPerBeat);
                scrollView.scrollTo(pixels % contentView.getRight(), 0);
            }
        };
    }

    @Override
    protected void onPlayingSound(PlayingSound playingSound) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lyrics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStart(View view) {
        if(playTask.isRunning()){
            playTask.stop();
        }
        else {
            playTask.start();
        }
    }
}
