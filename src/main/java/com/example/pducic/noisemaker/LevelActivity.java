package com.example.pducic.noisemaker;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;

public class LevelActivity extends Activity {

    protected SoundPool soundPool;
    protected SoundsConfiguration soundsConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Level level = (Level) intent.getSerializableExtra(IntentConstants.LEVEL_CONFIGURATION);

        setContentView(R.layout.activity_level);
        RecordingView goalRecordingView = (RecordingView) findViewById(R.id.levelGoalRecordingView);

        soundPool = new SoundPool(MainConfiguration.MAX_STREAMS, AudioManager.STREAM_MUSIC, MainConfiguration.SRC_QUALITY);
        soundsConfiguration = level.getSoundsConfiguration();
        soundsConfiguration.init(this, soundPool);
        goalRecordingView.setContent(level.getGoal().getRecordingLength(), level.getGoal().getPlayingSounds(), soundsConfiguration);
    }

    private void validate() {
        Log.i("Validating", "...");
    }
}
