package com.devtau.fileviewer;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class ShareUtils {

    private static final String LOG_TAG = "ShareUtils";

    public static void prepareFileShare(MenuItem menuItemShare, String fileType, String filePath, Context context) {
        Intent shareFileIntent = createFileShareIntent(fileType, filePath, context);
        if (shareFileIntent == null) return;
        ShareActionProvider providerShareImage = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItemShare);
        providerShareImage.setShareIntent(shareFileIntent);
    }

    @Nullable
    public static Intent createFileShareIntent(String fileType, String filePath, Context context) {
        Uri fileUri = buildUriFromPath(filePath, context);
        Intent shareFileIntent = new Intent(Intent.ACTION_SEND);
        shareFileIntent.setType(fileType);
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(shareFileIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resInfoList.size() > 0) {
            //блок, не влияющий ни на что
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                shareFileIntent.setTypeAndNormalize(fileType);
                shareFileIntent.setClipData(new ClipData(null, new String[]{fileType}, new ClipData.Item(fileUri)));
            }

            shareFileIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //костыльный блок для шаринга в вк
            for (ResolveInfo resolveInfo: resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            return shareFileIntent;
        } else {
            Toast.makeText(context, R.string.no_app_can_receive_this_file, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "createFileShareIntent no activity for share");
        }
        return null;
    }

    private static Uri buildUriFromPath(String validPath, Context context) {
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(
                    context, BuildConfig.APPLICATION_ID + ".provider", new File(validPath));
        } else {
            fileUri = Uri.fromFile(new File(validPath));
        }
        Log.d(LOG_TAG, "buildUriFromPath fileUri is: " + fileUri);
        return fileUri;
    }
}
