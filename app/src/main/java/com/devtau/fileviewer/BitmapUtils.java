package com.devtau.fileviewer;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class BitmapUtils {

    private static final String APP_PUBLIC_DIR_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "devtaufileviewer";
    private static final String JPG = ".jpg";
    private static final String MP4 = ".mp4";
    private static final String LOG_TAG = "BitmapUtils";


    private static File generateDefaultFile(String fileName) throws IOException {
        return generateFile(System.currentTimeMillis() + "_" + fileName);
    }

    private static File generateFile(String filename) throws IOException {
        File dir = new File(APP_PUBLIC_DIR_PATH);
        if (!dir.exists()) {
            if (!dir.mkdir()) throw new IOException("dir " + APP_PUBLIC_DIR_PATH + " not created");
        }
        return new File(dir, filename);
    }

    public static void saveFile(Context context, String sourceFilePath, String sourceFileName) {
        File sourceFile = new File(sourceFilePath);
        if (!BitmapUtils.isFilePathValid(context, sourceFilePath)) return;
        try {
            File destinationFile = BitmapUtils.generateDefaultFile(sourceFileName);
            BitmapUtils.saveFileToFileSystem(sourceFile, destinationFile, context);
        } catch (IOException e) {
            Log.e(LOG_TAG, "while creating file", e);
        }
    }

    private static void saveFileToFileSystem(File sourceFile, File destinationFile, Context context) {
        Observable<String> observable = Observable.fromCallable(() -> {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(sourceFile);
                outputStream = new FileOutputStream(destinationFile);
                byte buffer[] = new byte[8192];

                while (inputStream.read(buffer) != -1) {
                    outputStream.write(buffer);
                }
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
            return destinationFile.getAbsolutePath();
        });

        Action1<String> filePathHandler = filePath -> {
            MediaScannerConnection.scanFile(context, new String[]{filePath}, null,
                    (path, uri) -> Log.d(LOG_TAG, "file: " + path + " was scanned successfully. uri is: " + uri));
            String msg = context.getString(R.string.file_saved_msg, filePath);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, msg);
        };
        Action1<Throwable> errorHandler = throwable -> Log.e(LOG_TAG, "while creating file", throwable);

        observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(filePathHandler, errorHandler);
    }

    public static boolean isImagePathValid(Context context, String imagePath) {
        boolean isJPG = TextUtils.equals(JPG, imagePath.substring(imagePath.length() - JPG.length()));
        boolean isImagePathValid = isJPG && new File(imagePath).exists();
        if (!isImagePathValid) {
            String msg = context.getString(R.string.invalid_file_path_with_type, imagePath, JPG);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
        return isImagePathValid;
    }

    public static boolean isVideoPathValid(Context context, String videoPath) {
        boolean isMP4 = TextUtils.equals(MP4, videoPath.substring(videoPath.length() - MP4.length()));
        boolean isVideoPathValid = isMP4 && new File(videoPath).exists();
        if (!isVideoPathValid) {
            String msg = context.getString(R.string.invalid_file_path_with_type, videoPath, MP4);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
        return isVideoPathValid;
    }

    private static boolean isFilePathValid(Context context, String filePath) {
        boolean isVideoPathValid = new File(filePath).exists();
        if (!isVideoPathValid) {
            String msg = context.getString(R.string.invalid_file_path, filePath);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
        return isVideoPathValid;
    }
}
