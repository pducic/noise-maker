package com.example.pducic.noisemaker;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements SensorEventListener {

    private static final int IGNORE_EVENTS_AFTER_SOUND = 500;
    private static final float POSITIVE_COUNTER_THRESHOLD = (float) 5.0;
    private static final long DEFAULT_TEMPO = 1000;
    private static final int sensorType = Sensor.TYPE_GYROSCOPE;
    private static final int MAX_RECORDING_SIZE = 1000;

    // SoundPool constants
    private static final int MAX_STREAMS = 3; // 3 sounds for now, TODO - make it configurable
    private static final int SRC_QUALITY = 0; // Android Docs: "The sample-rate converter quality. Currently has no effect. Use 0 for the default"

    private int pauseThreshold = IGNORE_EVENTS_AFTER_SOUND;
    private float sensorThreshold = POSITIVE_COUNTER_THRESHOLD;
    private MediaPlayer tempoSound;
    private long lastShake = 0;
    private TempoTask tempoTask;
    private Button tempoButton;
    private SeekBar tempoSlider;
    private boolean recording = false;
    private boolean playing = false;
    private ImageButton playButton;
    private ImageButton recordButton;
    private long startRecording;
    //TODO use linked list or some kind
    private Map<Long, Sound> recordingMap = new HashMap<Long, Sound>();
    private PlayTask playTask;
    private SoundPool soundPool;
    private Map<Direction, Integer> soundDirections = new HashMap<Direction, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SRC_QUALITY);

        soundDirections.put(Direction.UP, soundPool.load(this, R.raw.g, 1));
        soundDirections.put(Direction.DOWN, soundPool.load(this, R.raw.drums, 1));
        soundDirections.put(Direction.LEFT, soundPool.load(this, R.raw.c, 1));
        soundDirections.put(Direction.RIGHT, soundPool.load(this, R.raw.f, 1));

        tempoSound = MediaPlayer.create(this, R.raw.pop);
        setContentView(R.layout.activity_my);
        tempoButton = (Button) findViewById(R.id.playTempoButton);
        tempoSlider = (SeekBar)findViewById(R.id.tempoSeekbar);
        playButton = (ImageButton) findViewById(R.id.playButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        tempoSlider.setProgress(tempoToSlider(DEFAULT_TEMPO));
        tempoTask = new TempoTask();
        playTask = new PlayTask();

        SeekBar sensorSlider = (SeekBar)findViewById(R.id.sensorThresholdSlider);
        sensorSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensorThreshold = sliderToAccelerometerThreshold(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sensorSlider.setProgress(thresholdToAccelerometerSlider(sensorThreshold));

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
        Sensor sensor = sensorMgr.getDefaultSensor(sensorType);
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
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensorType) {
            Log.v("XYZ", Float.toString(event.values[0]) + "   " + Float.toString(event.values[2]) + "   ");

            long curTime = System.currentTimeMillis();
            if (lastShake != 0 && (curTime - lastShake) < pauseThreshold) return;

            float x = event.values[0];
            float z = event.values[2];

            Sound sound = resolveSound(x, z);

            if(sound.getAmplitude() > sensorThreshold){
                Log.d("PLAYING", Float.toString(x) + "   " + Float.toString(z) + "   ");
                if(recording){
                    if (recordingMap.size() > MAX_RECORDING_SIZE) {
                        stopRecording();
                    }
                    recordingMap.put(curTime-startRecording, sound);
                }
                playSound(sound, curTime);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onPlayTempoClick(View view) {
        if (!tempoTask.isRunning())
            startTempo();
        else {
            stopTempo();
        }
    }

    public void onPlayClick(View view){
        if(playing){
            stopPlaying();
        }
        else{
            startPlaying();
        }
    }

    public void onRecordClick(View view){
        if(recording){
            stopRecording();
        }
        else{
            startRecording();
        }
    }

    private void startPlaying() {
        Log.i("Play", "Starting...");
        playing = true;
        playButton.setImageResource(R.drawable.ic_action_stop);
        playTask.start();

    }

    private void stopPlaying() {
        Log.i("Play", "Stopping...");
        playing = false;
        playButton.setImageResource(R.drawable.ic_action_play);
        playTask.stop();
    }

    private void startRecording() {
        recordingMap.clear();
        recording = true;
        startRecording = System.currentTimeMillis();
        recordButton.setBackgroundResource(android.R.color.darker_gray);
        playButton.setEnabled(false);
    }

    private void stopRecording() {
        recording = false;
        recordButton.setBackgroundResource(android.R.color.holo_red_dark);
        playButton.setEnabled(true);
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
        tempoButton.setText(R.string.start_tempo);
        tempoSlider.setEnabled(true);
        tempoTask.stop();
    }

    private void startTempo() {
        Log.i("Tempo", "Starting");
        tempoButton.setText(R.string.stop_tempo);
        long tempo = sliderToTempo(tempoSlider.getProgress());
        Log.i("Tempo", "Changing " + tempo);
        tempoSlider.setEnabled(false);
        tempoTask.setTempo(tempo);
        tempoTask.start();
    }

    private Sound resolveSound(float x, float z){
        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? mapSound(Direction.LEFT, x) : mapSound(Direction.RIGHT, x);
        } else {
            return z > 0 ? mapSound(Direction.UP, z) : mapSound(Direction.DOWN, z);
        }
    }

    private Sound mapSound(Direction direction, float amplitude) {
        return new Sound(soundDirections.get(direction), Math.abs(amplitude));
    }

    private void playSound(Sound sound, Long currentTime) {
        soundPool.play(sound.getSoundId(), 1, 1, 1, 0, 1);

        if (currentTime != null)
            lastShake = currentTime;
    }

    private class TempoTask extends Task {

        private long tempo = 1000L;
        @Override
        protected void process() {
            try {
                Thread.sleep(tempo);
            } catch (InterruptedException e) {
                Log.e("Tempo", "Interrupt");
            }
            Log.v("Tempo", "Playing sound");
            tempoSound.start();
        }

        public void setTempo(long tempo) {
            if(!isRunning())
                this.tempo = tempo;
        }
    }

    private class PlayTask extends Task {

        private int playedDirections;
        private long startTime;

        @Override
        void start() {
            super.start();
            playedDirections = 0;
            startTime = System.currentTimeMillis();
        }

        @Override
        protected void process() {
            if (playedDirections >= recordingMap.size()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopPlaying();
                    }
                });
            }
            Sound sound = recordingMap.get(System.currentTimeMillis() - startTime);
            if(sound != null){
                playSound(sound, null);
                playedDirections++;
            }
        }
    }
}
