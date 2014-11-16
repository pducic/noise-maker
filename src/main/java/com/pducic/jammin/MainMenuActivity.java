package com.pducic.jammin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pducic.noisemaker.R;
import com.pducic.jammin.jamsession.JamminActivity;
import com.pducic.jammin.lyrics.LyricsConfigurationActivity;
import com.pducic.jammin.unlock.LevelsConfigurationActivity;

public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void startJamminMode(View view){
        Intent intent = new Intent(this, JamminActivity.class);
        startActivity(intent);
    }

    public void startUnlockMode(View view){
        Intent intent = new Intent(this, LevelsConfigurationActivity.class);
        startActivity(intent);
    }

    public void startLyricsMode(View view){
        Intent intent = new Intent(this, LyricsConfigurationActivity.class);
        startActivity(intent);
    }
}
