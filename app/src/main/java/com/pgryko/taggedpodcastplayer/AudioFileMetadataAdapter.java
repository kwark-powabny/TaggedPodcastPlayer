package com.pgryko.taggedpodcastplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AudioFileMetadataAdapter extends ArrayAdapter<AudioFileMetadata> {
    public AudioFileMetadataAdapter(Context context, ArrayList<AudioFileMetadata> afad) {

        super(context, 0, afad);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position

        AudioFileMetadata afad = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_list_item_text_view, parent, false);
        }

        // Lookup view for data population
        TextView tvDescription = (TextView) convertView.findViewById(R.id.description);
        //TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);

        // Populate the data into the template view using the data object
        tvDescription.setText(afad.description);
        //tvHome.setText(user.hometown);

        // Return the completed view to render on screen
        return convertView;

    }
}
