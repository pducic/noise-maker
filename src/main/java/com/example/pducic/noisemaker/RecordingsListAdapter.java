package com.example.pducic.noisemaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by pducic on 02.10.14
 */
public class RecordingsListAdapter extends ArrayAdapter<Recording> {
    private final Context context;
    private final Song song;
    private final SoundsConfiguration soundConfiguration;
    private final long songDuration;

    public RecordingsListAdapter(Context context, Song song, SoundsConfiguration soundsConfiguration, long songDuration) {
        super(context, R.layout.list_view_recording_item, song.getRecordings());
        this.context = context;
        this.song = song;
        this.soundConfiguration = soundsConfiguration;
        this.songDuration = songDuration;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_view_recording_item, parent, false);

        long songLength = Math.max(song.getDuration(), songDuration);

        RecordingView recordingView = (RecordingView) rowView.findViewById(R.id.recordingViewItem);
        Recording recording = song.getRecordings().get(position);
        recordingView.setContent(songLength, recording.getPlayingSounds(), soundConfiguration);
        TextView textView = (TextView) rowView.findViewById(R.id.textViewItem);
        textView.setText(recording.getName());

        //here add other content in a recording row. option for delete, volume, edit etc

        return rowView;
    }
}
