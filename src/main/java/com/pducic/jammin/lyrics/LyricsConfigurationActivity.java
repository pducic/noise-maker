package com.pducic.jammin.lyrics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pducic.noisemaker.R;
import com.pducic.jammin.common.config.IntentConstants;
import com.pducic.jammin.common.config.MainConfiguration;

import java.util.List;

public class LyricsConfigurationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics_configuration);

        ListView levelsListView = (ListView) findViewById(R.id.lyricsListView);
        final List<LyricsContent> lyrics = MainConfiguration.getLyrics();
        ArrayAdapter<LyricsContent> adapter = new ArrayAdapter<LyricsContent>(this, R.layout.list_view_level_item, R.id.textViewLevelItem, lyrics);
        levelsListView.setAdapter(adapter);

        levelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                Intent intent = new Intent(LyricsConfigurationActivity.this, LyricsActivity.class);
                intent.putExtra(IntentConstants.LYRICS_CONFIGURATION, lyrics.get(position));
                startActivity(intent);
            }
        });
    }
}
