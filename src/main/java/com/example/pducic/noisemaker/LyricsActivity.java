package com.example.pducic.noisemaker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;

public class LyricsActivity extends Activity {

    public static final int SCROLL_FACTOR = 1000000;
    private Task playTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        final HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        final LyricsView contentView = (LyricsView) findViewById(R.id.contentView);
        contentView.setContent(getResources().getString(R.string.lyrics_sretan_rodendan), getResources().getInteger(R.integer.lyrics_sretan_rodendan_takt), MainConfiguration.getDefaultSoundConfiguration(true));
        playTask = new Task() {
            private long startMillis =0;

            @Override
            void start() {
                super.start();
                startMillis = System.currentTimeMillis();
            }

            @Override
            protected void process() {
                scrollView.scrollTo((int) (getPercentFromCurrentTime() * contentView.getRight()), 0);
            }
        };
    }

    /**
     *
     * @return
     */
    private static float getPercentFromCurrentTime() {
        return 1f*(System.currentTimeMillis()% SCROLL_FACTOR)/SCROLL_FACTOR;
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
