package com.pducic.jammin.jamsession;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.example.pducic.noisemaker.R;
import com.pducic.jammin.common.AbstractJamminActivity;
import com.pducic.jammin.common.RecordingsListAdapter;
import com.pducic.jammin.common.RecordingsListView;
import com.pducic.jammin.common.Task;
import com.pducic.jammin.common.config.MainConfiguration;
import com.pducic.jammin.common.model.PlayingSound;
import com.pducic.jammin.common.model.Recording;
import com.pducic.jammin.common.model.Song;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


public class JamminActivity extends AbstractJamminActivity {

    private MediaPlayer tempoSound;
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
    private RecordingsListAdapter recordingsListAdapter;
    private SeekBar playingSeekbar;
    private boolean normalizeSong;
    private int tempo = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jammin);

        leftConfigButton = (Button) findViewById(R.id.leftConfigButton);
        rightConfigButton = (Button) findViewById(R.id.rightConfigButton);

        tempoSound = MediaPlayer.create(this, R.raw.pop);
        tempoButton = (Button) findViewById(R.id.playTempoButton);
        tempoSlider = (SeekBar)findViewById(R.id.tempoSeekbar);
        playButton = (ImageButton) findViewById(R.id.playButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        tempoSlider.setProgress(tempoToSlider(MainConfiguration.DEFAULT_TEMPO));
        tempoTask = new TempoTask();
        playTask = new PlayTask();
        recordingsListAdapter = new RecordingsListAdapter(this, song, null, soundsConfiguration, 0, 0);
        RecordingsListView recordingsListView = (RecordingsListView) findViewById(R.id.recordingsList);
        recordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(JamminActivity.this)
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
    protected void onPlayingSound(PlayingSound playingSound) {
        if (isRecording) {
            if (currentRecording.size() > MainConfiguration.MAX_RECORDING_SIZE) {
                stopRecording();
            }
            playingSound.setTime(System.currentTimeMillis() - startRecording);
            currentRecording.add(playingSound);
        }
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
        playTask.setSongDuration(0);
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
        playTask.setSongDuration(0);
        playTask.start();
    }

    protected void stopRecording() {
        isRecording = false;
        recordButton.setBackgroundResource(android.R.color.darker_gray);
        playButton.setEnabled(true);
        recordingsListAdapter.add(new Recording(getString(R.string.recording) + getNextEntryIndex(song.getRecordings()), new ArrayList<PlayingSound>(currentRecording)));
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
        public void start() {
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
        public void stop() {
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
