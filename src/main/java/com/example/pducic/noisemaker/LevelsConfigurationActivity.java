package com.example.pducic.noisemaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class LevelsConfigurationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels_configuration);

        ListView levelsListView = (ListView) findViewById(R.id.levelsListView);
        final List<Level> levels = MainConfiguration.getLevels();
        ArrayAdapter<Level> adapter = new ArrayAdapter<Level>(this, R.layout.list_view_level_item, R.id.textViewLevelItem, levels);
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
