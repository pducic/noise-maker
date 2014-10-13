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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


public class MainActivity extends Activity implements SensorEventListener {

    private static final int IGNORE_EVENTS_AFTER_SOUND = 300;
    private static final float POSITIVE_COUNTER_THRESHOLD = (float) 5.0;
    private static final long DEFAULT_TEMPO = 1000;
    private static final int sensorType = Sensor.TYPE_GYROSCOPE;
    private static final int MAX_RECORDING_SIZE = 1000;
    /**
     * [1-100] greater -> rougher
     */
    private static final int SEEKBAR_GRANULARITY = 1;

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
    private List<PlayingSound> currentRecording = new LinkedList<PlayingSound>();
    private Song song = new Song();

    private PlayTask playTask;
    private SoundPool soundPool;
    private RecordingsListAdapter recordingsListAdapter;
    private SoundsConfiguration soundConfiguration;
    private SeekBar playingSeekbar;
    private boolean normalizeSong;
    private int tempo = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SRC_QUALITY);
        soundConfiguration = new SoundsConfiguration(this, soundPool);

        tempoSound = MediaPlayer.create(this, R.raw.pop);
        setContentView(R.layout.main_activity);
        tempoButton = (Button) findViewById(R.id.playTempoButton);
        tempoSlider = (SeekBar)findViewById(R.id.tempoSeekbar);
        playButton = (ImageButton) findViewById(R.id.playButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        tempoSlider.setProgress(tempoToSlider(DEFAULT_TEMPO));
        tempoTask = new TempoTask();
        playTask = new PlayTask();
        recordingsListAdapter = new RecordingsListAdapter(this, song, soundConfiguration);
        RecordingsListView recordingsListView = (RecordingsListView) findViewById(R.id.recordingsList);
        recordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                recordingsListAdapter.remove(song.getRecordings().get(position));
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

        tempoSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempo = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        tempoSlider.setProgress(tempo);

        playingSeekbar = (SeekBar)findViewById(R.id.playingSeekBar);
        playingSeekbar.setEnabled(false);
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

            if(Math.abs(x) < sensorThreshold && Math.abs(z) < sensorThreshold){
                return;
            }

            PlayingSound playingSound = resolveSound(x, z);

            Log.d("PLAYING", Float.toString(x) + "   " + Float.toString(z) + "   ");
            if (isRecording) {
                if (currentRecording.size() > MAX_RECORDING_SIZE) {
                    stopRecording();
                }
                playingSound.setTime(curTime - startRecording);
                currentRecording.add(playingSound);
            }
            playSound(new PlayingSound[]{playingSound}, curTime);
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
        playTask.setMute(true);
        playTask.start();
    }

    private void stopRecording() {
        isRecording = false;
        recordButton.setBackgroundResource(android.R.color.holo_red_dark);
        playButton.setEnabled(true);
        recordingsListAdapter.add(new Recording(getString(R.string.recording) + getNextEntryIndex(song.getRecordings()), new ArrayList<PlayingSound>(currentRecording)));
        playTask.setMute(false);
        playTask.stop();
    }

    private int getNextEntryIndex(List<Recording> recordings) {
        if(recordings.isEmpty()) return 1;
        String recordingString = getString(R.string.recording);
        String lastEntryName = recordings.get(recordings.size() - 1).getName();
        return Integer.valueOf(lastEntryName.substring(lastEntryName.indexOf(recordingString) + recordingString.length())) + 1;
    }

    private int thresholdToPauseSlider(int pauseThreshold) {
        return (pauseThreshold - 100) / 20;
    }

    private int sliderToPauseThreshold(int progress) {
        return 10 + progress * 20;
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

    private PlayingSound resolveSound(float x, float z){
        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? mapSound(Direction.LEFT, x) : mapSound(Direction.RIGHT, x);
        } else {
            return z > 0 ? mapSound(Direction.UP, z) : mapSound(Direction.DOWN, z);
        }
    }

    private PlayingSound mapSound(Direction direction, float amplitude) {
        return new PlayingSound(soundConfiguration.getSoundId(direction), amplitude);
    }

    private void playSound(PlayingSound[] playingSound, Long currentTime) {
        for (PlayingSound sound : playingSound) {
            Log.i("Playing", sound.toString());
            soundPool.play(soundConfiguration.getSoundPoolId(sound.getSoundId()), 1, 1, 1, 0, 1);
        }

        if (currentTime != null)
            lastShake = currentTime;
    }

    public void onNormalizeSongCheckboxToggle(View view) {
        normalizeSong = ((CheckBox) view).isChecked();
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
        private PlayingSound next;
        private PriorityQueue<PlayingSound> soundsPriorityQueue;
        private long songDuration;
        private int percent = 0;

        private boolean mute = false;

        @Override
        void start() {
            super.start();
            int size = 0;
            if (song.getRecordings().isEmpty()) return;

            for (Recording recording : song.getRecordings()) {
                size += recording.getPlayingSounds().size();
            }
            soundsPriorityQueue = new PriorityQueue<PlayingSound>(size, new Comparator<PlayingSound>() {
                @Override
                public int compare(PlayingSound lhs, PlayingSound rhs) {
                    return lhs.getTime() < rhs.getTime() ? -1 : (lhs.getTime() == rhs.getTime() ? 0 : 1);
                }
            });
            for (Recording recording : song.getRecordings()) {
                for (PlayingSound playingSound : recording.getPlayingSounds()) {
                    if (normalizeSong) {
                        int factor = tempo / 5;
                        soundsPriorityQueue.add(new PlayingSound(playingSound.getSoundId(), playingSound.getAmplitude(), (playingSound.getTime() >> factor  << factor)));
                    } else {
                        soundsPriorityQueue.add(playingSound);
                    }
                }
            }
            next = soundsPriorityQueue.isEmpty() ? null : soundsPriorityQueue.poll();
            songDuration = song.getDuration();
            startTime = System.currentTimeMillis();
        }

        public void setMute(boolean mute) {
            this.mute = mute;
        }

        @Override
        void stop() {
            super.stop();
            reset();
        }

        private synchronized void reset() {
            percent = 0;
            playingSeekbar.setProgress(0);
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
                stop();
                return;
            }
            final long currentSongTime = System.currentTimeMillis() - startTime;
            final int progress = (int) (100 * (1.0 * currentSongTime / songDuration));
            if (progress > percent + SEEKBAR_GRANULARITY) {
                percent = progress;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playingSeekbar.setProgress(progress);
                    }
                });
                if (currentSongTime > next.getTime()) {
                    List<PlayingSound> sameMilliSounds = new LinkedList<PlayingSound>();
                    sameMilliSounds.add(next);
                    PlayingSound temp = soundsPriorityQueue.isEmpty() ? null : soundsPriorityQueue.poll();
                    while (temp != null && temp.getTime() == next.getTime()){
                        sameMilliSounds.add(temp);
                        temp = soundsPriorityQueue.isEmpty() ? null : soundsPriorityQueue.poll();
                    }
                    if(!mute) {
                        playSound(sameMilliSounds.toArray(new PlayingSound[sameMilliSounds.size()]), null);
                    }
                    next = temp;
                }
            }
        }
    }
}
