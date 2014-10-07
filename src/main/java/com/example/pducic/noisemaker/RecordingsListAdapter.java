package com.example.pducic.noisemaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pducic on 02.10.14
 */
public class RecordingsListAdapter extends ArrayAdapter<Recording> {
    private final Context context;
    private final List<Recording> values;

    public RecordingsListAdapter(Context context, List<Recording> values) {
        super(context, R.layout.list_view_recording_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_view_recording_item, parent, false);

        long songLength = 0;
        for (Recording value : values) {
            long length = value.getRecordingLength();
            if(length > songLength){
                songLength = length;
            }
        }

        RecordingView recordingView = (RecordingView) rowView.findViewById(R.id.recordingViewItem);
        recordingView.setContent(values.get(position).getName(), songLength, values.get(position).getSounds());
        TextView textView = (TextView) rowView.findViewById(R.id.textViewItem);
        textView.setText(values.get(position).getName());

        //here add other content in a recording row. option for delete, volume, edit etc

        return rowView;
    }
}
