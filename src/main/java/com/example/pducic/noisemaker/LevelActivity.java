package com.example.pducic.noisemaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * TODO refactor to fragments!!!!
 */
public class LevelActivity extends Activity implements SensorEventListener {

    private SoundPool soundPool;
    private SoundsConfiguration soundsConfiguration;
    private int mistakeMillis;
    private Recording goalRecording;

    private static final int sensorType = Sensor.TYPE_GYROSCOPE;
    private int pauseThreshold = MainConfiguration.IGNORE_EVENTS_AFTER_SOUND;
    private float sensorThreshold = MainConfiguration.POSITIVE_COUNTER_THRESHOLD;
    private long lastShake = 0;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private ImageButton playButton;
    private ImageButton recordButton;
    private long startRecording;
    private List<PlayingSound> currentRecording = new LinkedList<PlayingSound>();

    private Song song = new Song();
    private PlayTask playTask;
    private RecordingsListAdapter recordingsListAdapter;
    private SeekBar playingSeekbar;
    private Button leftConfigButton;
    private Button rightConfigButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        Level level = (Level) intent.getSerializableExtra(IntentConstants.LEVEL_CONFIGURATION);
        soundPool = new SoundPool(MainConfiguration.MAX_STREAMS, AudioManager.STREAM_MUSIC, MainConfiguration.SRC_QUALITY);
        soundsConfiguration = level.getSoundsConfiguration();
        soundsConfiguration.init(this, soundPool);
        goalRecording = level.getGoal();
        mistakeMillis = level.getMistakeMillis();

        setContentView(R.layout.activity_level);
        RecordingView goalRecordingView = (RecordingView) findViewById(R.id.levelGoalRecordingView);


        goalRecordingView.setContent(goalRecording.getRecordingLength(), goalRecording.getPlayingSounds(), soundsConfiguration);

        leftConfigButton = (Button) findViewById(R.id.leftConfigButton);
        rightConfigButton = (Button) findViewById(R.id.rightConfigButton);

        playButton = (ImageButton) findViewById(R.id.levelPlayButton);
        recordButton = (ImageButton) findViewById(R.id.levelRecordButton);
        playTask = new PlayTask();
        recordingsListAdapter = new RecordingsListAdapter(this, song, soundsConfiguration, goalRecording.getRecordingLength());
        RecordingsListView recordingsListView = (RecordingsListView) findViewById(R.id.levelRecordingsList);
        recordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(LevelActivity.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                recordingsListAdapter.remove(song.getRecordings().get(position));
                                validate();
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

        playingSeekbar = (SeekBar)findViewById(R.id.levelPlayingSeekBar);
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
    }

    private void validate() {
        Log.i("Validation", "Start...");
        LinkedList<PlayingSound> recordingPlayingSounds = new LinkedList<PlayingSound>();
        for (Recording recording : song.getRecordings()) {
            recordingPlayingSounds.addAll(recording.getPlayingSounds());
        }
        List<PlayingSound> goalPlayingSounds = goalRecording.getPlayingSounds();
        if(recordingPlayingSounds.size() != goalPlayingSounds.size()){
            return;
        }
        Log.i("Validation", "Same size...");
        outer: for (PlayingSound goalSound : goalPlayingSounds) {
            for (PlayingSound recordingSound : recordingPlayingSounds) {
                if(recordingSound.getSoundId().equals(goalSound.getSoundId()) && (Math.abs(recordingSound.getTime() - goalSound.getTime()) < mistakeMillis)){
                    continue outer;
                }
            }
            return;
        }
        Log.i("Validation", "Success!");

        Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show();
        finish();
//        Intent intent = new Intent(this, LevelsConfigurationActivity.class);
//        startActivity(intent);

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
            if(playingSound == null){
                return;
            }

            Log.d("PLAYING", Float.toString(x) + "   " + Float.toString(z) + "   ");
            if (isRecording) {
                if (currentRecording.size() > MainConfiguration.MAX_RECORDING_SIZE) {
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
        playTask.setSongDuration(goalRecording.getRecordingLength());
        playTask.start();
    }

    private void stopPlaying() {
        Log.i("Play", "Stopping...");
        isPlaying = false;
        playButton.setImageResource(R.drawable.ic_action_play);
        playTask.stop();
    }

    protected void startRecording() {
        currentRecording.clear();
        isRecording = true;
        startRecording = System.currentTimeMillis();
        recordButton.setBackgroundResource(android.R.color.holo_red_dark);
        playButton.setEnabled(false);
        playTask.setSongDuration(goalRecording.getRecordingLength());
        playTask.start();
    }

    protected void stopRecording() {
        isRecording = false;
        recordButton.setBackgroundResource(android.R.color.darker_gray);
        playButton.setEnabled(true);
        recordingsListAdapter.add(new Recording(getString(R.string.recording) + getNextEntryIndex(song.getRecordings()), new ArrayList<PlayingSound>(currentRecording)));
        playTask.stop();
        validate();
    }

    private int getNextEntryIndex(List<Recording> recordings) {
        if(recordings.isEmpty()) return 1;
        String recordingString = getString(R.string.recording);
        String lastEntryName = recordings.get(recordings.size() - 1).getName();
        return Integer.valueOf(lastEntryName.substring(lastEntryName.indexOf(recordingString) + recordingString.length())) + 1;
    }

    private PlayingSound resolveSound(float x, float z){
        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? mapSound(Direction.UP, x) : mapSound(Direction.DOWN, x);
        } else {
            return z > 0 ? mapSound(Direction.LEFT, z) : mapSound(Direction.RIGHT, z);
        }
    }

    private PlayingSound mapSound(Direction direction, float amplitude) {
        SoundGesture.ConfigurationButtonId buttonId;
        if(leftConfigButton.isPressed()){
            buttonId = SoundGesture.ConfigurationButtonId.LEFT;
        }
        else if(rightConfigButton.isPressed()){
            buttonId = SoundGesture.ConfigurationButtonId.RIGHT;
        }
        else{
            return null;
        }
        String soundId = soundsConfiguration.getSoundId(direction, buttonId);
        if(soundId == null){
            return null;
        }
        return new PlayingSound(soundId, amplitude);
    }

    private void playSound(PlayingSound[] playingSound, Long currentTime) {
        for (PlayingSound sound : playingSound) {
            Log.i("Playing", sound.toString());
            Integer soundPoolId = soundsConfiguration.getSoundPoolId(sound.getSoundId());
            soundPool.play(soundPoolId, 1, 1, 1, 0, 1);
        }

        if (currentTime != null)
            lastShake = currentTime;
    }

    /**
     * TODO extract and pass events through broadcast
     */
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
            songDuration = Math.max(song.getDuration(), songDuration);
            startTime = System.currentTimeMillis();

            if (song.getRecordings().isEmpty()) return;

            for (Recording recording : song.getRecordings()) {
                size += recording.getPlayingSounds().size();
            }
            if(size == 0){
                return;
            }

            soundsPriorityQueue = new PriorityQueue<PlayingSound>(size, new Comparator<PlayingSound>() {
                @Override
                public int compare(PlayingSound lhs, PlayingSound rhs) {
                    return lhs.getTime() < rhs.getTime() ? -1 : (lhs.getTime() == rhs.getTime() ? 0 : 1);
                }
            });
            for (Recording recording : song.getRecordings()) {
                for (PlayingSound playingSound : recording.getPlayingSounds()) {
                    soundsPriorityQueue.add(playingSound);
                }
            }
            next = soundsPriorityQueue.isEmpty() ? null : soundsPriorityQueue.poll();
        }

        public void setSongDuration(long songDuration) {
            this.songDuration = songDuration;
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
            long currentTimeMillis = System.currentTimeMillis();
            if (next == null && songDuration < (currentTimeMillis - startTime)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopPlaying();
                    }
                });

                stop();
                return;
            }
            final long currentSongTime = currentTimeMillis - startTime;
            final int progress = (int) (100 * (1.0 * currentSongTime / songDuration));
            if (progress > percent + MainConfiguration.SEEKBAR_GRANULARITY) {
                percent = progress;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playingSeekbar.setProgress(progress);
                    }
                });
                if(next == null){
                    return;
                }
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
