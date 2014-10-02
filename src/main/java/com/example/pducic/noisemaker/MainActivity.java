package com.example.pducic.noisemaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


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
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private ImageButton playButton;
    private ImageButton recordButton;
    private long startRecording;
    private List<Sound> currentRecording = new LinkedList<Sound>();
    private List<Recording> recordings = new ArrayList<Recording>();
    private PlayTask playTask;
    private SoundPool soundPool;
    private Map<Direction, Integer> soundDirections = new HashMap<Direction, Integer>();
    private RecordingsListAdapter recordingsListAdapter;

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
        setContentView(R.layout.main_activity);
        tempoButton = (Button) findViewById(R.id.playTempoButton);
        tempoSlider = (SeekBar)findViewById(R.id.tempoSeekbar);
        playButton = (ImageButton) findViewById(R.id.playButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        tempoSlider.setProgress(tempoToSlider(DEFAULT_TEMPO));
        tempoTask = new TempoTask();
        playTask = new PlayTask();
        recordingsListAdapter = new RecordingsListAdapter(this, recordings);
        ListView recordingsListView = (ListView) findViewById(R.id.recordingsList);
        recordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                recordingsListAdapter.remove(recordings.get(position));
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        recordingsListView.setAdapter(recordingsListAdapter);
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
                if(isRecording){
                    if (currentRecording.size() > MAX_RECORDING_SIZE) {
                        stopRecording();
                    }
                    sound.setTime(curTime - startRecording);
                    currentRecording.add(sound);
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
        if(isPlaying){
            stopPlaying();
        }
        else{
            startPlaying();
        }
    }

    public void onRecordClick(View view){
        if(isRecording){
            stopRecording();
        }
        else{
            startRecording();
        }
    }

    private void startPlaying() {
        Log.i("Play", "Starting...");
        isPlaying = true;
        playButton.setImageResource(R.drawable.ic_action_stop);
        playTask.start();

    }

    private void stopPlaying() {
        Log.i("Play", "Stopping...");
        isPlaying = false;
        playButton.setImageResource(R.drawable.ic_action_play);
        playTask.stop();
    }

    private void startRecording() {
        currentRecording.clear();
        isRecording = true;
        startRecording = System.currentTimeMillis();
        recordButton.setBackgroundResource(android.R.color.darker_gray);
        playButton.setEnabled(false);
    }

    private void stopRecording() {
        isRecording = false;
        recordButton.setBackgroundResource(android.R.color.holo_red_dark);
        playButton.setEnabled(true);
        recordingsListAdapter.add(new Recording(getString(R.string.recording) + getNextEntryIndex(recordings), new LinkedList<Sound>(currentRecording)));
    }

    private int getNextEntryIndex(List<Recording> recordings) {
        if(recordings.isEmpty()) return 1;
        String recordingString = getString(R.string.recording);
        String lastEntryName = recordings.get(recordings.size() - 1).getName();
        return Integer.valueOf(lastEntryName.substring(lastEntryName.indexOf(recordingString) + recordingString.length())) + 1;
    }

    private int thresholdToAccelerometerSlider(float accelerometerThreshold) {
        return (int) (accelerometerThreshold / 0.3);
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

        private long startTime;
        private Sound next;
        private PriorityQueue<Sound> soundsPriorityQueue;

        @Override
        void start() {
            super.start();
            int size = 0;
            for (Recording recording : recordings) {
                size+=recording.getSounds().size();
            }
            soundsPriorityQueue = new PriorityQueue<Sound>(size, new Comparator<Sound>() {
                @Override
                public int compare(Sound lhs, Sound rhs) {
                    return lhs.getTime() < rhs.getTime() ? -1 : (lhs.getTime() == rhs.getTime() ? 0 : 1);
                }
            });
            for (Recording recording : recordings) {
                soundsPriorityQueue.addAll(recording.getSounds());
            }
            next = soundsPriorityQueue.isEmpty()? null : soundsPriorityQueue.poll();
            startTime = System.currentTimeMillis();
        }

        @Override
        protected void process() {
            if (next == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopPlaying();
                    }
                });
                return;
            }
            if(System.currentTimeMillis() - startTime > next.getTime()){
                playSound(next, null);
                next = soundsPriorityQueue.isEmpty()? null : soundsPriorityQueue.poll();
            }
        }
    }
}
