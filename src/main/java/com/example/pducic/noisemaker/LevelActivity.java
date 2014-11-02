package com.example.pducic.noisemaker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class LevelActivity extends AbstractJamminActivity {

    private int mistakeMillis;
    private Song goalSong;

    private boolean isRecording = false;
    private ImageButton recordButton;
    private long startRecording;
    private List<PlayingSound> currentRecording = new LinkedList<PlayingSound>();

    private Song song = new Song();
    private PlayTask playTask;
    private RecordingsListAdapter recordingsListAdapter;
    private SeekBar playingSeekbar;
    private RecordingsListAdapter goalRecordingsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Level level = (Level) intent.getSerializableExtra(IntentConstants.LEVEL_CONFIGURATION);
        soundPool = new SoundPool(MainConfiguration.MAX_STREAMS, AudioManager.STREAM_MUSIC, MainConfiguration.SRC_QUALITY);
        soundsConfiguration = level.getSoundsConfiguration();
        soundsConfiguration.init(this, soundPool);
        goalSong = level.getGoal();
        mistakeMillis = level.getMistakeMillis();

        setContentView(R.layout.activity_level);
        RecordingsListView goalRecordingView = (RecordingsListView) findViewById(R.id.levelGoalRecordingView);
        goalRecordingsListAdapter = new RecordingsListAdapter(this, goalSong, null, soundsConfiguration, goalSong.getDuration(), 0);
        goalRecordingView.setAdapter(goalRecordingsListAdapter);

        leftConfigButton = (Button) findViewById(R.id.leftConfigButton);
        rightConfigButton = (Button) findViewById(R.id.rightConfigButton);

        recordButton = (ImageButton) findViewById(R.id.levelRecordButton);
        playTask = new PlayTask();
        recordingsListAdapter = new RecordingsListAdapter(this, song, goalSong, soundsConfiguration, goalSong.getDuration(), level.getMistakeMillis());
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

    private void validate() {
        Log.i("Validation", "Start...");
        LinkedList<PlayingSound> recordingPlayingSounds = new LinkedList<PlayingSound>();
        for (Recording recording : song.getRecordings()) {
            recordingPlayingSounds.addAll(recording.getPlayingSounds());
        }
        List<PlayingSound> goalPlayingSounds = new LinkedList<PlayingSound>();
        for (Recording recording : goalSong.getRecordings()) {
            goalPlayingSounds.addAll(recording.getPlayingSounds());
        }
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

    }

    @Override
    protected void onPlayingSound(PlayingSound playingSound) {
        if (isRecording) {
            if (currentRecording.size() > MainConfiguration.MAX_RECORDING_SIZE) {
                stopRecording();
            }
            playingSound.setTime(System.currentTimeMillis() - startRecording);
            currentRecording.add(playingSound);
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

    private void stopPlaying() {
        Log.i("Play", "Stopping...");
        playTask.stop();
    }

    protected void startRecording() {
        currentRecording.clear();
        isRecording = true;
        startRecording = System.currentTimeMillis();
        recordButton.setBackgroundResource(android.R.color.holo_red_dark);
        playTask.setSongDuration(goalSong.getDuration());
        playTask.start();
    }

    protected void stopRecording() {
        isRecording = false;
        recordButton.setBackgroundResource(android.R.color.darker_gray);
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
