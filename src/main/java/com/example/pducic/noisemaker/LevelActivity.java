package com.example.pducic.noisemaker;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.pducic.noisemaker.R;

public class LevelActivity extends Activity {

    private SoundPool soundPool;
    private SoundsConfiguration soundsConfiguration;

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.level, menu);
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
}
