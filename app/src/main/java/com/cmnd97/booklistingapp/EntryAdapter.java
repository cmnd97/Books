package com.cmnd97.booklistingapp;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by cristi-mnd on 01.08.17.
 */

class EntryAdapter extends ArrayAdapter<Entry> {

    private final MainActivity activity;

    EntryAdapter(MainActivity context, ArrayList<Entry> entries) {
        super(context, 0, entries);
        this.activity = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;


        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        final Entry currentEntry = getItem(position);
        ((TextView) listItemView.findViewById(R.id.entry_title)).setText(currentEntry.getEntryNumber() + ". " + currentEntry.getEntryTitle());
        ((TextView) listItemView.findViewById(R.id.entry_authors)).setText(currentEntry.getEntryAuthors());
        ((ImageView) listItemView.findViewById(R.id.entry_thumbnail)).setImageBitmap(currentEntry.getEntryThumbnail());
        ((TextView) listItemView.findViewById(R.id.entry_price)).setText(currentEntry.getPrice());
        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.clickOnViewItem(currentEntry.getEntryUrl());
            }
        });

        return listItemView;
    }

}
