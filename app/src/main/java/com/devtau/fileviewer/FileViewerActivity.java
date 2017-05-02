package com.devtau.fileviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import java.io.File;

public abstract class FileViewerActivity extends AppCompatActivity {

    public static final String FILE_PATH_EXTRA_TAG = "FILE_PATH_EXTRA_TAG";
    public static final String FILE_NAME_EXTRA_TAG = "FILE_NAME_EXTRA_TAG";
    static final int AUTO_HIDE_DELAY_MILLIS = 3_000;
    static final int UI_ANIMATION_DURATION = 700;
    private static final boolean AUTO_HIDE = false;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int UI_ANIMATION_DELAY_INITIAL = 800;
    private static final String LOG_TAG = "FileViewerActivity";

    String filePath;
    String fileName;
    PermissionHelper mPermissionHelper;
    boolean isDeviceInPortrait;
    private boolean mVisible = true;
    private final Handler mHideHandler = new Handler();
    private Runnable mHideRunnable;
    private Runnable mShowRunnable;

    View mTapMask;
    private View mRootView;
    private Toolbar mToolbar;


    public static void startForImage(Context context, String imagePath, String imageName) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(FileViewerActivity.FILE_PATH_EXTRA_TAG, imagePath);
        intent.putExtra(FileViewerActivity.FILE_NAME_EXTRA_TAG, imageName);
        context.startActivity(intent);
    }

    public static void startForVideo(Context context, String videoPath, String videoName) {
        Intent intent = new Intent(context, VideoViewerActivity.class);
        intent.putExtra(FileViewerActivity.FILE_PATH_EXTRA_TAG, videoPath);
        intent.putExtra(FileViewerActivity.FILE_NAME_EXTRA_TAG, videoName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filePath = getIntent().getStringExtra(FILE_PATH_EXTRA_TAG);
        fileName = getIntent().getStringExtra(FILE_NAME_EXTRA_TAG);
        isDeviceInPortrait = Utils.isDeviceInPortrait(this);
        mPermissionHelper = new PermissionHelperImpl();
        initViews();
        setUpUiState(savedInstanceState);
        prepareToolbars();
        prepareRunnables();
        toggleNavigationAndStatusBar(true);//нужно для установки корректных флагов отображения
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (AUTO_HIDE) {
            delayedHideToolbars(UI_ANIMATION_DELAY_INITIAL);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                if (mPermissionHelper.checkWriteStoragePermission(this)) {
                    BitmapUtils.saveFile(this, filePath, fileName);
                } else {
                    mPermissionHelper.requestWriteStoragePermissionForFile(this, (code, permissions) ->
                            FileViewerActivity.this.requestPermissions(permissions, code));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isDeviceInPortrait = Utils.isDeviceInPortrait(this);
        prepareToolbars();
    }

    //    если останется время, запилить обработку выданного разрешения
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == PermissionHelper.WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                savePhoto();
//            } else {
//                Toast.makeText(getApplicationContext(), R.string.permission_explanation_external_storage_for_file, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    void initViews() {
        mRootView = findViewById(android.R.id.content);
        mTapMask = findViewById(R.id.video_view_tap_mask);
        mToolbar = (Toolbar) findViewById(R.id.file_viewer_toolbar);
    }

    void setUpUiState(Bundle savedInstanceState) {
        mTapMask.setOnClickListener(view -> toggleToolbars());
    }

    void toggleToolbars() {
        if (mVisible) {
            delayedHideToolbars(UI_ANIMATION_DELAY);
        } else {
            delayedShowToolbars();
        }
    }

    private void delayedHideToolbars(int delayMillis) {
        mHideHandler.removeCallbacks(mShowRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void delayedShowToolbars() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mShowRunnable, UI_ANIMATION_DELAY);
    }

    void prepareToolbars() {
        int statusBarHeight = Utils.getStatusBarHeight(this);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams();
        params.topMargin = statusBarHeight;
        mToolbar.setTitle(new File(filePath).getName());
        setSupportActionBar(mToolbar);

        int toolbarHorizontalPadding = isDeviceInPortrait ? 0 : Utils.getNavigationBarHeight(this);
        mToolbar.setPadding(toolbarHorizontalPadding, 0, toolbarHorizontalPadding, 0);
    }

    private void prepareRunnables() {
        mShowRunnable = this::showToolbars;
        mHideRunnable = this::hideToolbars;
    }

    void showToolbars() {
        if (mToolbar == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toggleNavigationAndStatusBar(true);
            AnimatorListenerAdapter animationListener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    if (AUTO_HIDE) {
                        delayedHideToolbars(AUTO_HIDE_DELAY_MILLIS);
                    }
                }
            };
            mToolbar.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(UI_ANIMATION_DURATION)
                    .setListener(animationListener)
                    .start();
        } else {
            //low-end devices freeze toolbar animation if picture is greater than 500kb
            mToolbar.setVisibility(View.VISIBLE);
            toggleNavigationAndStatusBar(true);
            if (AUTO_HIDE) {
                delayedHideToolbars(AUTO_HIDE_DELAY_MILLIS);
            }
        }
    }

    void hideToolbars() {
        if (mToolbar == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatorListenerAdapter animationListener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    toggleNavigationAndStatusBar(false);
                }
            };
            mToolbar.animate()
                    .translationY(- mToolbar.getBottom())
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(UI_ANIMATION_DURATION)
                    .setListener(animationListener)
                    .start();
        } else {
            mToolbar.setVisibility(View.GONE);
            toggleNavigationAndStatusBar(false);
        }
    }

    @SuppressLint("InlinedApi")
    private void toggleNavigationAndStatusBar(boolean show) {
        if (mRootView == null) return;
        mVisible = show;

        if (show) {
            mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else {
            mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}
