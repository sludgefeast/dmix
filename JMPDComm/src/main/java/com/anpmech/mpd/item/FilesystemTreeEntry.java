/*
 * Copyright (C) 2004 Felipe Gustavo de Almeida
 * Copyright (C) 2010-2016 The MPDroid Project
 *
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice,this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.anpmech.mpd.item;

/**
 * This Interface represents a filesystem entry ({@link Directory}, {@link Entry}, {@link Music} or
 * {@link PlaylistFile}) for a MPD protocol item.
 */
public interface FilesystemTreeEntry {

    /**
     * The full path as given by the MPD protocol.
     *
     * @return The full path for this entry.
     */
    String getFullPath();

    /**
     * This method returns the last modified time for this entry in Unix time.
     * <p>
     * <p>The Last-Modified response value is expected to be given in ISO8601.</p>
     *
     * @return The last modified time for this entry in Unix time.
     */
    long getLastModified();

    /**
     * This returns the size a MPD entry file.
     * <p>
     * <p><b>This is only available with some MPD command responses.</b></p>
     *
     * @return The size of a MPD entry file, {@link Integer#MIN_VALUE} if it doesn't exist in this
     * response.
     */
    long size();
}
