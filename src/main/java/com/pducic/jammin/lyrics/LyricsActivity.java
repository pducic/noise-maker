package com.pducic.jammin.lyrics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import com.example.pducic.noisemaker.R;
import com.pducic.jammin.common.AbstractJamminActivity;
import com.pducic.jammin.common.Task;
import com.pducic.jammin.common.config.IntentConstants;
import com.pducic.jammin.common.model.PlayingSound;

public class LyricsActivity extends AbstractJamminActivity {

    private Task playTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        LyricsContent content = (LyricsContent) intent.getSerializableExtra(IntentConstants.LYRICS_CONFIGURATION);

        setContentView(R.layout.activity_lyrics);

        final HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        final LyricsView contentView = (LyricsView) findViewById(R.id.contentView);
        leftConfigButton = (Button) findViewById(R.id.leftConfigButton);
        rightConfigButton = (Button) findViewById(R.id.rightConfigButton);

        soundsConfiguration = content.getSoundsConfiguration();
        soundsConfiguration.init(this, soundPool);
        contentView.setContent(getResources().getString(content.getLyricResourceId()), content.getTakt(), soundsConfiguration);
        final int bpm = content.getBpm();
        playTask = new Task() {
            private long startMillis =0;

            @Override
            public void start() {
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

    public void onStart(View view) {
        if(playTask.isRunning()){
            playTask.stop();
        }
        else {
            playTask.start();
        }
    }
}
