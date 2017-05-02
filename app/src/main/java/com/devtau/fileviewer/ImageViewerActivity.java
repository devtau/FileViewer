package com.devtau.fileviewer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;

public class ImageViewerActivity extends FileViewerActivity {

    private static final String LOG_TAG = "ImageViewerActivity";
    private ImageView mContentImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_image_viewer);
        super.onCreate(savedInstanceState);
        initViews();
        setUpUiState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_viewer_menu, menu);
        MenuItem menuItemShare = menu.findItem(R.id.action_share);
        ShareUtils.prepareFileShare(menuItemShare, "image/jpeg", filePath, this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    void initViews() {
        super.initViews();
        mContentImageView = (ImageView) findViewById(R.id.fullscreen_image_view);
    }

    @Override
    void setUpUiState(Bundle savedInstanceState) {
        super.setUpUiState(savedInstanceState);
        mContentImageView.setVisibility(View.VISIBLE);
        if (!BitmapUtils.isImagePathValid(this, filePath)) return;
        Glide.with(this).load(new File(filePath)).into(mContentImageView);
    }
}
