package com.example.pducic.noisemaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
        Intent intent = new Intent(this, LyricsActivity.class);
        startActivity(intent);
    }
}
