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

package com.namelessdev.mpdroid.helpers;

import android.util.Log;

import com.anpmech.mpd.Tools;
import com.anpmech.mpd.item.Album;
import com.anpmech.mpd.item.Artist;
import com.anpmech.mpd.item.Music;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store basic information about an album.
 * <p>
 * <p>This class should be thread-safe and was designed for extension.</p>
 */
public class AlbumInfo {

    /**
     * This is a cache of MD5 checksums generated by {@link #getHashFromString(String)}.
     */
    private static final Map<CharSequence, String> CHECKSUM_CACHE;

    /**
     * A hex byte representation in int.
     */
    private static final int HEX_BYTE = 0x0F;

    /**
     * Returned upon invalid checksum.
     */
    private static final String INVALID_ALBUM_CHECKSUM = "INVALID_ALBUM_CHECKSUM";

    /**
     * The class log identifier.
     */
    private static final String TAG = "AlbumInfo";

    /**
     * The album name in relation to this album.
     */
    private final String mAlbumName;

    /**
     * The artist name in relation to this album.
     */
    private final String mArtistName;

    /**
     * The file name in relation to this album.
     */
    private final String mFilename;

    /**
     * The path in relation to this album.
     */
    private final String mParentDirectory;

    static {
        /**
         * It is unlikely that more than one thread would write to the map at one time.
         */
        final int concurrencyLevel = 1;

        /**
         * The HashMap default capacity.
         */
        final int defaultCapacity = 16;

        /**
         * The ConcurrencyMap default.
         */
        final float loadFactor = 0.75f;

        CHECKSUM_CACHE = new ConcurrentHashMap<>(defaultCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * Generates a full AlbumInfo object from a {@link Music} item.
     *
     * @param music The {@link Music} item used to construct this AlbumInfo.
     */
    public AlbumInfo(final Music music) {


        String artist = music.getAlbumArtistName();

        if (artist == null) {
            artist = music.getArtistName();
        }

        mArtistName = artist;
        mAlbumName = music.getAlbumName();
        mParentDirectory = music.getParentDirectory();
        mFilename = music.getFullPath();
    }

    /**
     * Generates a partial AlbumInfo object from a {@link Album} item. The
     * filename will not be available upon construction of this item.
     *
     * @param album The {@link Album} item used to construct this AlbumInfo.
     */
    public AlbumInfo(final Album album) {


        final Artist artist = album.getArtist();
        if (artist != null) {
            mArtistName = artist.getName();
        } else {
            mArtistName = null;
        }

        mAlbumName = album.getName();
        mParentDirectory = album.getPath();
        mFilename = null;
    }

    /**
     * Initializes a newly created AlbumInfo object so that it represents the same fields as the
     * AlbumInfo object in the parameter.
     *
     * @param albumInfo The AlbumInfo object to copy.
     */
    public AlbumInfo(final AlbumInfo albumInfo) {
        this(albumInfo.mArtistName, albumInfo.mAlbumName, albumInfo.mParentDirectory,
                albumInfo.mFilename);
    }

    /**
     * Generates a partial AlbumInfo object from a Artist and Album name. The path and filename
     * will not be available upon generation.
     *
     * @param artistName The artist name used with relation to this album.
     * @param albumName  The album name used with relation to this album.
     */
    public AlbumInfo(final String artistName, final String albumName) {
        this(artistName, albumName, null, null);
    }

    /**
     * This constructor initializes an AlbumInfo object from strings, this is discouraged.
     *
     * @param artistName      The artist name used with relation to this album.
     * @param albumName       The album name used with relation to this album.
     * @param parentDirectory The parent directory to the filename.
     * @param filename        The filename with relation to this album.
     */
    private AlbumInfo(final String artistName, final String albumName, final String parentDirectory,
                      final String filename) {


        mArtistName = artistName;
        mAlbumName = albumName;
        mParentDirectory = parentDirectory;
        mFilename = filename;
    }

    /**
     * Convert byte array to hex string.
     *
     * @param data Target data array.
     * @return Hex string.
     */
    private static String convertToHex(final byte[] data) {
        String hex = null;

        if (data != null && data.length != 0) {
            final char[] charBuffer = new char[data.length << 1];

            for (int byteIndex = 0; byteIndex < data.length; byteIndex++) {

                /** Store the upper and lower nibble with padding, respectively. */
                charBuffer[byteIndex << 1] = getPaddedNibble(data[byteIndex] >>> 4 & HEX_BYTE);
                charBuffer[(byteIndex << 1) + 1] = getPaddedNibble(data[byteIndex] & HEX_BYTE);
            }

            hex = String.copyValueOf(charBuffer);
        }

        return hex;
    }

    /**
     * Gets the hash value from the specified string.
     *
     * @param value Target string value to get hash from.
     * @return the hash from string.
     */
    private static String getHashFromString(final String value) {
        String hash = null;

        if (value != null && !value.isEmpty()) {
            try {
                final MessageDigest hashEngine = MessageDigest.getInstance("MD5");
                hashEngine.update(value.getBytes("iso-8859-1"));
                hash = convertToHex(hashEngine.digest());
            } catch (final NoSuchAlgorithmException | UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to get hash.", e);
            }
        }

        return hash;
    }

    /**
     * This method takes a data byte, pads it and returns it as a char.
     *
     * @param dataByte The data byte to convert to a char.
     * @return The data byte padded and returned as a char.
     */
    private static char getPaddedNibble(final int dataByte) {
        final char hex;

        if (dataByte >= 0 && dataByte <= 9) {
            hex = (char) ('0' + dataByte);
        } else {
            hex = (char) ('a' + dataByte - 10);
        }

        return hex;
    }

    /**
     * Compares this AlbumInfo object to another.
     *
     * @param o the object to compare this instance with.
     * @return True if fields of both Objects are equal, and are of the same class.
     */
    @Override
    public boolean equals(final Object o) {
        Boolean isEqual = null;

        if (this == o) {
            isEqual = Boolean.TRUE;
        } else if (o == null || getClass() != o.getClass()) {
            isEqual = Boolean.FALSE;
        }

        if (isEqual == null || isEqual.equals(Boolean.TRUE)) {
            final AlbumInfo albumInfo = (AlbumInfo) o;

            if (Tools.isNotEqual(mAlbumName, albumInfo.mAlbumName)) {
                isEqual = Boolean.FALSE;
            }

            if (Tools.isNotEqual(mArtistName, albumInfo.mArtistName)) {
                isEqual = Boolean.FALSE;
            }
        }

        if (isEqual == null) {
            isEqual = Boolean.TRUE;
        }

        return isEqual.booleanValue();
    }

    /**
     * This method returns the album name for this object.
     *
     * @return The album name for this object.
     */
    public String getAlbumName() {
        return mAlbumName;
    }

    /**
     * This method returns the artist name relating to this album.
     *
     * @return The artist name relating to this album.
     */
    public String getArtistName() {
        return mArtistName;
    }

    /**
     * This method returns the filename relating to this album.
     *
     * @return The filename relating to this album.
     */
    public String getFilename() {
        return mFilename;
    }

    /**
     * This method checks the cache for a MD5 entry for the album and artist and if it doesn't
     * exist, it generates and stores it; afterwards returning the result.
     *
     * @return An MD5 entry related to this album.
     */
    public String getKey() {
        final String value;

        if (isValid()) {
            final CharSequence key = mAlbumName + mArtistName;

            if (CHECKSUM_CACHE.containsKey(key)) {
                value = CHECKSUM_CACHE.get(key);
            } else {
                value = getHashFromString(key.toString());
                CHECKSUM_CACHE.put(key, value);
            }
        } else {
            value = INVALID_ALBUM_CHECKSUM;
        }

        return value;
    }

    /**
     * Gets the path in relation to this album.
     *
     * @return The path in relation to this album.
     */
    public String getParentDirectory() {
        return mParentDirectory;
    }

    /**
     * Returns an integer hash code for this object.
     * <p>
     * <p>By contract, any two objects for which {@link #equals} returns {@code true} must return
     * the same hash code value. This means that subclasses of {@code Object} usually override
     * both methods or neither method.</p>
     * <p>
     * <p>Note that hash values must not change over time unless information used in equals
     * comparisons also changes.</p>
     *
     * @return this object's hash code.
     * @see #equals
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{mArtistName, mAlbumName});
    }

    /**
     * Checks to make sure that this object for validity.
     *
     * @return True if album and artist exist, false otherwise.
     */
    public boolean isValid() {
        final boolean isArtistNameEmpty = mArtistName == null || mArtistName.isEmpty();
        final boolean isAlbumNameEmpty = mAlbumName == null || mAlbumName.isEmpty();
        return !isAlbumNameEmpty && !isArtistNameEmpty;
    }

    /**
     * Returns information about this object in String form.
     *
     * @return Information about fields stored with relation to this album.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AlbumInfo{");
        sb.append("mAlbumName='").append(mAlbumName).append('\'');
        sb.append(", mArtistName='").append(mArtistName).append('\'');
        sb.append(", mFilename='").append(mFilename).append('\'');
        sb.append(", mParentDirectory='").append(mParentDirectory).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
