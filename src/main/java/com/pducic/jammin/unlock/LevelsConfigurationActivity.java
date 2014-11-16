package com.pducic.jammin.unlock;

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

public class LevelsConfigurationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels_configuration);

        ListView levelsListView = (ListView) findViewById(R.id.levelsListView);
        final List<LevelContent> levels = MainConfiguration.getLevels();
        ArrayAdapter<LevelContent> adapter = new ArrayAdapter<LevelContent>(this, R.layout.list_view_level_item, R.id.textViewLevelItem, levels);
        levelsListView.setAdapter(adapter);

        levelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                Intent intent = new Intent(LevelsConfigurationActivity.this, LevelActivity.class);
                intent.putExtra(IntentConstants.LEVEL_CONFIGURATION, levels.get(position));
                startActivity(intent);
            }
        });
    }
}
