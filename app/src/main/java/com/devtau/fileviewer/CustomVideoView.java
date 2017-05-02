/*
 *
 * Copyright (C) 2016, Telco Cloud Trading & Logistic Ltd
 *
 * This file is part of dodicall.
 * dodicall is free software : you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * dodicall is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dodicall.If not, see <http://www.gnu.org/licenses/>.
 */

package com.devtau.fileviewer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.VideoView;

public class CustomVideoView extends VideoView implements MediaPlayerController {

    private int mMuteSavedVolume;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    public CustomVideoView(Context context) {
        this(context, null);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        setOnCompletionListener(mediaPlayer -> mMediaPlayer = mediaPlayer);
    }


    @Override
    public void playPause(ImageView control) {
        if (isPlaying()) {
            pause();
            control.setImageResource(android.R.drawable.ic_media_play);
        } else {
            start();
            control.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void mute(ImageView control) {
        if (mMuteSavedVolume != 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMuteSavedVolume, 0);
            mMuteSavedVolume = 0;
            control.setImageResource(R.drawable.ic_volume_on_white_24dp);
        } else {
            updateVolume();
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
            control.setImageResource(R.drawable.ic_volume_off_white_24dp);
        }
    }

    @Override
    public void updateVolume() {
        mMuteSavedVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void setFullscreen(boolean fullscreen) {

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mMuteSavedVolume);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        //TODO: simplify
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mMuteSavedVolume = savedState.mMuteSavedVolume;
    }



    private static class SavedState extends BaseSavedState {

        private final int mMuteSavedVolume;

        private SavedState(Parcelable superState, int muteSavedVolume) {
            super(superState);
            this.mMuteSavedVolume = muteSavedVolume;
        }

        private SavedState(Parcel in) {
            super(in);
            mMuteSavedVolume = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(mMuteSavedVolume);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
