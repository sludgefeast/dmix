/*
 * Copyright (C) 2010-2016 The MPDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namelessdev.mpdroid.favorites;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anpmech.mpd.MPD;
import com.anpmech.mpd.exception.MPDException;
import com.anpmech.mpd.item.Album;
import com.anpmech.mpd.item.Music;
import com.namelessdev.mpdroid.MPDApplication;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Favorites {

    private static final String STICKER_ALBUM_FAVORITE = "albumfav";

    private static final String PREFERENCE_FAVORITE_KEY = "favoriteKey";

    private final MPD mMPD;

    public Favorites(final MPD mpd) {
        this.mMPD = mpd;
    }

    public void addAlbum(final Album album) throws IOException, MPDException {
        for (final Music song : mMPD.getSongs(album)) {
            mMPD.getStickerManager().set(song, computeFavoriteStickerKey(), "Y");
        }
    }

    public void removeAlbum(final Album album) throws IOException, MPDException {
        for (final Music song : mMPD.getSongs(album)) {
            mMPD.getStickerManager().delete(song, computeFavoriteStickerKey());
        }
    }

    public Collection<Album> getAlbums() throws IOException, MPDException {
        final Set<Music> songs =
                mMPD.getStickerManager().find("", computeFavoriteStickerKey()).keySet();
        final Set<Album> albums = new HashSet<>();
        for (final Music song : songs) {
            albums.add(song.getAlbum());
        }
        return albums;
    }

    private static String computeFavoriteStickerKey() {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(MPDApplication.getInstance());
        final String personalizationKey = settings.getString(PREFERENCE_FAVORITE_KEY, "").trim();
        return STICKER_ALBUM_FAVORITE +
                (!personalizationKey.isEmpty() ? "-" + personalizationKey : "");
    }

}
