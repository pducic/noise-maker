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

import java.util.Arrays;

public class LevelsConfigurationActivity extends Activity {

    //TODO move this into configuration...
    public static final String[] LEVELS = new String[]{"Level 1"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels_configuration);

        ListView levelsListView = (ListView) findViewById(R.id.levelsListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_view_level_item, R.id.textViewLevelItem, Arrays.asList(LEVELS));
        levelsListView.setAdapter(adapter);

        levelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //TODO pass information on level...
                Intent intent = new Intent(LevelsConfigurationActivity.this, LevelActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.levels_configuration, menu);
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
