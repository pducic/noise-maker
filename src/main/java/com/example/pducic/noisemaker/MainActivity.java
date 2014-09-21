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

    private static final int IGNORE_EVENTS_AFTER_SOUND = 400;
    private static final float POSITIVE_COUNTER_THRESHOLD = (float) 7.0;
    private static final long DEFAULT_TEMPO = 1000;

    private int pauseThreshold = IGNORE_EVENTS_AFTER_SOUND;
    private float accelerometerThreshold = POSITIVE_COUNTER_THRESHOLD;
    private MediaPlayer zSound;
    private MediaPlayer xSound;
    private MediaPlayer ySound;
    private MediaPlayer tempoSound;
    private long lastShake = 0;
    private Task playerThread;
    private Button tempoButton;
    private SeekBar tempoSlider;

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
        tempoSlider = (SeekBar)findViewById(R.id.tempoSeekbar);
        tempoSlider.setProgress(tempoToSlider(DEFAULT_TEMPO));
        playerThread = new Task();

        SeekBar accelerometerSlider = (SeekBar)findViewById(R.id.accelerometerThresholdSlider);
        accelerometerSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                accelerometerThreshold = sliderToAccelerometerThreshold(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        accelerometerSlider.setProgress(thresholdToAccelerometerSlider(accelerometerThreshold));

        SeekBar pauseSlider = (SeekBar)findViewById(R.id.pauseSlider);
        pauseSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pauseThreshold = sliderToPauseThreshold(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        pauseSlider.setProgress(thresholdToPauseSlider(pauseThreshold));

    }

    @Override
    protected void onResume() {
        super.onResume();
        final SensorManager sensorMgr = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        Sensor sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.i("Delay", String.valueOf(sensor.getMinDelay()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SensorManager sensorMgr = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorMgr.unregisterListener(this);
        stopTempo();
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
            if (lastShake != 0 && (curTime - lastShake) < pauseThreshold) return;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float[] floats = {x, y, z};
            Direction direction = Direction.fromValue(indexOfMax(floats));

            if(floats[direction.getIndex()] > accelerometerThreshold){// || floats[direction.getIndex()] < NEGATIVE_COUNTER_THRESHOLD){
                Log.v("XYZ", Float.toString(x) + "   " + Float.toString(y) + "   " + Float.toString(z) + "   ");
                playSound(direction, curTime);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onClick(View view) {
        if (!playerThread.isRunning())
            startTempo();
        else {
            stopTempo();
        }
    }

    private int thresholdToPauseSlider(int pauseThreshold) {
        return (pauseThreshold - 100) / 20;
    }

    private int thresholdToAccelerometerSlider(float accelerometerThreshold) {
        return (int) (accelerometerThreshold / 0.3);
    }

    private int sliderToPauseThreshold(int progress) {
        return 10 + progress * 20;
    }

    private float sliderToAccelerometerThreshold(int progress) {
        return 0.3f * progress;
    }

    private int tempoToSlider(long tempo) {
        return (int) ((tempo - 100) / 20);
    }

    private long sliderToTempo(int progress) {
        return 100 + progress * 20;
    }

    private void stopTempo() {
        Log.i("Tempo", "Stopping");
        tempoButton.setText("Play Tempo");
        tempoSlider.setEnabled(true);
        playerThread.stop();
    }

    private void startTempo() {
        Log.i("Tempo", "Starting");
        tempoButton.setText("Stop Tempo");
        long tempo = sliderToTempo(tempoSlider.getProgress());
        Log.i("Tempo", "Changing " + tempo);
        tempoSlider.setEnabled(false);
        playerThread.setTempo(tempo);
        playerThread.start();
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
