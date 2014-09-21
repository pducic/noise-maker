package com.example.pducic.noisemaker;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;


public class MainActivity extends Activity implements SensorEventListener {

    private static final float POSITIVE_COUNTER_THRESHOLD = (float) 7.0;
    private static final float NEGATIVE_COUNTER_THRESHOLD = (float) -7.0;
    private static final int IGNORE_EVENTS_AFTER_SHAKE = 200;

    private MediaPlayer zSound;
    private MediaPlayer xSound;
    private MediaPlayer ySound;
    private MediaPlayer tempoSound;
    private long lastShake = 0;
    private Task playerThread;
    private Button tempoButton;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        xSound = MediaPlayer.create(this, R.raw.c);
        ySound = MediaPlayer.create(this, R.raw.f);
        zSound = MediaPlayer.create(this, R.raw.g);
        tempoSound = MediaPlayer.create(this, R.raw.pop);
        setContentView(R.layout.activity_my);
        tempoButton = (Button) findViewById(R.id.playButton);
        seekBar = (SeekBar)findViewById(R.id.tempoSeekbar);

        playerThread = new Task();
        seekBar.setProgress(50);

    }

    private long statusBarProgressToTempo(int progress) {
        return 100 + progress*20;
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SensorManager sensorMgr = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        Sensor sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SensorManager sensorMgr = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorMgr.unregisterListener(this);
        stopTempo();
    }

    public void onClick(View view) {
        if (!playerThread.isRunning())
            startTempo();
        else {
            stopTempo();
        }
    }

    private void stopTempo() {
        Log.i("Tempo", "Stopping");
        tempoButton.setText("Start Tempo");
        seekBar.setEnabled(true);
        playerThread.stop();
    }

    private void startTempo() {
        Log.i("Tempo", "Starting");
        tempoButton.setText("Stop Tempo");
        long tempo = statusBarProgressToTempo(seekBar.getProgress());
        Log.i("Tempo", "Changing " + tempo);
        seekBar.setEnabled(false);
        playerThread.setTempo(tempo);
        playerThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            long curTime = System.currentTimeMillis();
            if (lastShake != 0 && (curTime - lastShake) < IGNORE_EVENTS_AFTER_SHAKE) return;

            float x = event.values[SensorManager.DATA_X];
            float y = event.values[SensorManager.DATA_Y];
            float z = event.values[SensorManager.DATA_Z];

            float[] floats = {x, y, z};
            Direction direction = Direction.fromValue(indexOfMax(floats));

            if(floats[direction.getIndex()] > POSITIVE_COUNTER_THRESHOLD){// || floats[direction.getIndex()] < NEGATIVE_COUNTER_THRESHOLD){
                Log.v("XYZ", Float.toString(x) + "   " + Float.toString(y) + "   " + Float.toString(z) + "   ");
                playSound(direction, curTime);
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

    private void playSound(Direction direction, Long currentTime) {
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
        if(currentTime != null)
            lastShake = currentTime;
    }

    //TODO use android intent service
    private class Task implements Runnable {

        private boolean running = false;
        private long tempo = 1000L;
        private volatile Thread thread = null;

        public void setTempo(long tempo) {
            if(!isRunning())
                this.tempo = tempo;
        }

        void start(){
            synchronized (this) {
                if (running)
                    return;

                thread = new Thread(this);
                thread.setPriority(Thread.MAX_PRIORITY);

                running = true;
                thread.start();
            }
        }

        void stop() {
            thread = null;
        }

        boolean isRunning() {
            return thread != null;
        }

        @Override
        public void run() {
            try {
                while (thread == Thread.currentThread()) {
                    Thread.sleep(tempo);
                    Log.v("Tempo", "Playing sound");
                    tempoSound.start();
                }
            } catch (Exception e) {
                Log.e("Tempo", "Interrupted", e);
            }
            synchronized (this) {
                running = false;
            }
        }
    }
}
