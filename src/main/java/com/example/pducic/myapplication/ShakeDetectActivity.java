package com.example.pducic.myapplication;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class ShakeDetectActivity extends Activity implements SensorEventListener {

    private static final float POSITIVE_COUNTER_THRESHOLD = (float) 7.0;
    private static final float NEGATIVE_COUNTER_THRESHOLD = (float) -7.0;
    private static final int IGNORE_EVENTS_AFTER_SHAKE = 200;

    //TODO add calibration
    private static final float DEFAULT_X = 0.23523031f;
    private static final float DEFAULT_Y = 0.12569559f;
    private static final float DEFAULT_Z = 10.666168f;

    private MediaPlayer zSound;
    private MediaPlayer xSound;
    private MediaPlayer ySound;
    private long lastShake = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SensorManager sensorMgr = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        Sensor sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        xSound = MediaPlayer.create(this, R.raw.pew);
        ySound = MediaPlayer.create(this, R.raw.your_mom);
        zSound = MediaPlayer.create(this, R.raw.jump);
        setContentView(R.layout.activity_my);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    sensorMgr.flush(ShakeDetectActivity.this);
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if (lastShake != 0 && (curTime - lastShake) < IGNORE_EVENTS_AFTER_SHAKE) return;

            float x = event.values[SensorManager.DATA_X] - DEFAULT_X;
            float y = event.values[SensorManager.DATA_Y] - DEFAULT_Y;
            float z = event.values[SensorManager.DATA_Z] - DEFAULT_Z;

            float[] floats = {x, y, z};
            Direction direction = Direction.fromValue(indexOfMax(floats));

            if(floats[direction.getIndex()] > POSITIVE_COUNTER_THRESHOLD){// || floats[direction.getIndex()] < NEGATIVE_COUNTER_THRESHOLD){
                Log.v("XYZ", Float.toString(x) + "   " + Float.toString(y) + "   " + Float.toString(z) + "   ");
                Log.i("Before sound", String.valueOf(System.nanoTime()));
                playSound(direction, curTime);
                Log.i("After sound", String.valueOf(System.nanoTime()));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private int indexOfMax(float... values){
        int maxIndex = 0;
        for (int i = 1; i < values.length; i++){
            if ((values[i] > values[maxIndex])){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private void playSound(Direction direction, long currentTime) {
        MediaPlayer player = null;
        switch (direction) {
            case X:
                player = xSound;
                break;
            case Y:
                player = ySound;
                break;
            case Z:
                player = zSound;
                break;
        }
        if(player.isPlaying())
            player.seekTo(0);
        player.start();
        lastShake = currentTime;
    }
}
