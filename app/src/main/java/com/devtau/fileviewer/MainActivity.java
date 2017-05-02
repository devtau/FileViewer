package com.devtau.fileviewer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openImageViewerActivity(View view) {
        String imagePath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/kitty.jpg";
        FileViewerActivity.startForImage(this, imagePath, "kitty.jpg");
    }

    public void openVideoViewerActivity(View view) {
        String videoPath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/panda.mp4";
        FileViewerActivity.startForVideo(this, videoPath, "panda.mp4");
    }
}
