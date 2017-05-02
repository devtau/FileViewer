package com.devtau.fileviewer;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.util.Locale;

public class VideoViewerActivity extends FileViewerActivity {

    private static final String LOG_TAG = "VideoViewerActivity";
    private static final String CURRENT_PLAYBACK_TIME_MS = "SEEK_BAR_PROGRESS";
    private CustomVideoView mContentVideoView;
    private Handler mVideoProgressHandler;
    private Runnable mUpdateVideoProgressTask;

    private ViewGroup mBottomActionBar;
    private ImageView mPlayPause;
    private ImageView mMute;
    private TextView mTVProgress;
    private SeekBar mSeekBar;
    private TextView mTVFileDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_video_viewer);
        super.onCreate(savedInstanceState);
        mUpdateVideoProgressTask = new UpdateVideoProgressTask();
        initViews();
        setUpUiState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mVideoProgressHandler = new Handler();
        mVideoProgressHandler.post(mUpdateVideoProgressTask);
    }

    @Override
    protected void onStop() {
        if (mVideoProgressHandler != null) {
            mVideoProgressHandler.removeCallbacks(mUpdateVideoProgressTask);
            mVideoProgressHandler = null;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_viewer_menu, menu);
        MenuItem menuItemShare = menu.findItem(R.id.action_share);
        ShareUtils.prepareFileShare(menuItemShare, "video/mp4", filePath, this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_PLAYBACK_TIME_MS, mContentVideoView.getCurrentPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mContentVideoView != null && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            mContentVideoView.updateVolume();
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    void initViews() {
        super.initViews();
        mContentVideoView = (CustomVideoView) findViewById(R.id.video_view);
        mBottomActionBar = (ViewGroup) findViewById(R.id.bottom_action_bar);
        mPlayPause = (ImageView) findViewById(R.id.play_pause);
        mMute = (ImageView) findViewById(R.id.mute);
        mTVProgress = (TextView) findViewById(R.id.text_view_progress);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mTVFileDuration = (TextView) findViewById(R.id.text_view_file_duration);

        mPlayPause.setOnClickListener(view -> {
            if (!mContentVideoView.isPlaying()) toggleToolbars();
            mContentVideoView.playPause(mPlayPause);
        });
        mMute.setOnClickListener(view -> mContentVideoView.mute(mMute));
    }

    @Override
    void setUpUiState(Bundle savedInstanceState) {
        super.setUpUiState(savedInstanceState);
        mContentVideoView.setVisibility(View.VISIBLE);

        if (!BitmapUtils.isVideoPathValid(this, filePath)) return;
        mContentVideoView.setVideoPath(filePath);
        if (mPermissionHelper.checkWriteStoragePermission(this)) {
            setUpFileDuration(savedInstanceState);
            setUpSeekBar();
        } else {
            mPermissionHelper.requestWriteStoragePermissionForFile(this, (code, permissions) ->
                    VideoViewerActivity.this.requestPermissions(permissions, code));
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        boolean isMuted = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
        if (isMuted) mMute.setImageResource(R.drawable.ic_volume_off_white_24dp);
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    private void setUpFileDuration(Bundle savedInstanceState) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.fromFile(new File(filePath)));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int fileDurationMs = Integer.parseInt(time);
        mTVFileDuration.setText(formatTime(fileDurationMs));
        mSeekBar.setMax(fileDurationMs);//sets scale of SeekBar to milliseconds

        if (savedInstanceState != null) {
            int currentPlaybackTimeMs = savedInstanceState.getInt(CURRENT_PLAYBACK_TIME_MS);
            mContentVideoView.seekTo(currentPlaybackTimeMs);
            mTVProgress.setText(formatTime(currentPlaybackTimeMs));
            Log.d(LOG_TAG, "progress: " + currentPlaybackTimeMs);
            mSeekBar.post(() -> mSeekBar.setProgress(currentPlaybackTimeMs));
            if (currentPlaybackTimeMs != 0) mContentVideoView.playPause(mPlayPause);
        }
    }

    private void setUpSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentPlaybackTimeMs = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentPlaybackTimeMs = i;
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(LOG_TAG, "SeekBar onStopTrackingTouch progress: " + currentPlaybackTimeMs);
                mContentVideoView.seekTo(currentPlaybackTimeMs);
                mTVProgress.setText(formatTime(currentPlaybackTimeMs));
            }
        });
    }

    private String formatTime(long timeMs) {
        int minutes = (int) timeMs / (60 * 1000);
        int seconds = (int) (timeMs / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    void prepareToolbars() {
        super.prepareToolbars();
        int navigationBarHeight = isDeviceInPortrait ? Utils.getNavigationBarHeight(this) : 0;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mBottomActionBar.getLayoutParams();
        params.bottomMargin = navigationBarHeight;

        int toolbarHorizontalPadding = isDeviceInPortrait ? 0 : Utils.getNavigationBarHeight(this);
        mBottomActionBar.setPadding(toolbarHorizontalPadding, 0, toolbarHorizontalPadding, 0);
    }

    @Override
    void showToolbars() {
        super.showToolbars();
        if (mBottomActionBar == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomActionBar.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(UI_ANIMATION_DURATION)
                    .start();
        } else {
            //low-end devices freeze toolbar animation if picture is greater than 500kb
            mBottomActionBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    void hideToolbars() {
        super.hideToolbars();
        if (mBottomActionBar == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int bottomActionBarTranslationY = mBottomActionBar.getHeight();
            if (isDeviceInPortrait) {
                bottomActionBarTranslationY += Utils.getNavigationBarHeight(this);
            }
            mBottomActionBar.animate()
                    .translationY(bottomActionBarTranslationY)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(UI_ANIMATION_DURATION)
                    .start();
        } else {
            mBottomActionBar.setVisibility(View.GONE);
        }
    }



    private class UpdateVideoProgressTask implements Runnable {
        @Override
        public void run() {
            if (mVideoProgressHandler == null) return;
            int fileDuration = mContentVideoView.getDuration();
            if (fileDuration != -1 && mContentVideoView != null && mTVProgress != null && mSeekBar != null) {
                int currentPlaybackTimeMs = mContentVideoView.getCurrentPosition();
                mSeekBar.post(() -> {
                    if (fileDuration > currentPlaybackTimeMs) {
                        mSeekBar.setProgress(currentPlaybackTimeMs);
                        mTVProgress.setText(formatTime(currentPlaybackTimeMs));
                    } else {
                        mSeekBar.setProgress(0);
                        mTVProgress.setText(formatTime(0));
                        mPlayPause.setImageResource(android.R.drawable.ic_media_play);
                    }
                });
            }
            mVideoProgressHandler.postDelayed(this, 40);
        }
    }
}
