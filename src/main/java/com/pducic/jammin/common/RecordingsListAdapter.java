package com.pducic.jammin.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.pducic.noisemaker.R;
import com.pducic.jammin.common.model.PlayingSound;
import com.pducic.jammin.common.model.Recording;
import com.pducic.jammin.common.model.Song;
import com.pducic.jammin.common.model.SoundsConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by pducic on 02.10.14
 */
public class RecordingsListAdapter extends ArrayAdapter<Recording> {
    private final Context context;
    private final Song song;
    private final SoundsConfiguration soundConfiguration;
    private final long songDuration;
    private final Song referentSong;
    private final long mistakeMillis;

    public RecordingsListAdapter(Context context, Song song, Song referentSong, SoundsConfiguration soundsConfiguration, long songDuration, long mistakeMillis) {
        super(context, R.layout.list_view_recording_item, song.getRecordings());
        this.context = context;
        this.song = song;
        this.soundConfiguration = soundsConfiguration;
        this.songDuration = songDuration;
        this.referentSong = referentSong;
        this.mistakeMillis = mistakeMillis;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_view_recording_item, parent, false);

        long songLength = Math.max(song.getDuration(), songDuration);

        RecordingView recordingView = (RecordingView) rowView.findViewById(R.id.recordingViewItem);
        Recording recording = song.getRecordings().get(position);
        Set<String> correctlyPlayed = null;
        if(referentSong!=null) {
            correctlyPlayed = new HashSet<String>();
            for (PlayingSound playingSound : recording.getPlayingSounds()) {
                if (contains(referentSong, playingSound)) {
                    correctlyPlayed.add(playingSound.getSoundId());
                }
            }
        }
        recordingView.setContent(songLength, recording.getPlayingSounds(), correctlyPlayed, soundConfiguration);
        TextView textView = (TextView) rowView.findViewById(R.id.textViewItem);
        textView.setText(recording.getName());

        //here add other content in a recording row. option for delete, volume, edit etc

        return rowView;
    }

    private boolean contains(Song referentSong, PlayingSound playingSound) {
        for (Recording recording : referentSong.getRecordings()) {
            for (PlayingSound sound : recording.getPlayingSounds()) {
                if(Math.abs(sound.getTime() - playingSound.getTime()) < mistakeMillis && playingSound.getSoundId().equals(sound.getSoundId())) return true;
            }
        }
        return false;
    }
}
