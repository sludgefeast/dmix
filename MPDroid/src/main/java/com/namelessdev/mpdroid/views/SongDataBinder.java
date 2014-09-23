/*
 * Copyright (C) 2010-2014 The MPDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namelessdev.mpdroid.views;

import com.namelessdev.mpdroid.R;
import com.namelessdev.mpdroid.adapters.ArrayDataBinder;
import com.namelessdev.mpdroid.views.holders.AbstractViewHolder;
import com.namelessdev.mpdroid.views.holders.SongViewHolder;

import org.a0z.mpd.item.Item;
import org.a0z.mpd.item.Music;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class SongDataBinder implements ArrayDataBinder {

    boolean mShowArtist;

    public SongDataBinder() {
        mShowArtist = false;
    }

    public SongDataBinder(boolean showArtist) {
        this.mShowArtist = showArtist;
    }

    @Override
    public AbstractViewHolder findInnerViews(View targetView) {
        // look up all references to inner views
        SongViewHolder viewHolder = new SongViewHolder();
        viewHolder.mTrackTitle = (TextView) targetView.findViewById(R.id.track_title);
        viewHolder.mTrackNumber = (TextView) targetView.findViewById(R.id.track_number);
        viewHolder.mTrackDuration = (TextView) targetView.findViewById(R.id.track_duration);
        viewHolder.mTrackArtist = (TextView) targetView.findViewById(R.id.track_artist);
        return viewHolder;
    }

    @Override
    public int getLayoutId() {
        return R.layout.song_list_item;
    }

    public boolean isEnabled(int position, List<? extends Item> items, Object item) {
        return true;
    }

    public void onDataBind(final Context context, final View targetView,
            final AbstractViewHolder viewHolder, List<? extends Item> items, Object item,
            int position) {
        SongViewHolder holder = (SongViewHolder) viewHolder;

        final Music song = (Music) item;
        int trackNumber = song.getTrack();
        if (trackNumber < 0) {
            trackNumber = 0;
        }

        holder.mTrackTitle.setText(song.getTitle());
        holder.mTrackNumber.setText(trackNumber < 10 ? "0" + Integer.toString(trackNumber) : Integer
                .toString(trackNumber));
        holder.mTrackDuration.setText(song.getFormattedTime());

        if (mShowArtist) {
            String a = song.getArtist();
            if (a == null || a.isEmpty()) {
                a = context.getString(R.string.unknown_metadata_artist);
            }
            holder.mTrackArtist.setText(a);
        }
    }

    @Override
    public View onLayoutInflation(Context context, View targetView, List<? extends Item> items) {
        targetView.findViewById(R.id.track_artist).setVisibility(
                mShowArtist ? View.VISIBLE : View.GONE);
        return targetView;
    }

}
