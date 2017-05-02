package com.devtau.fileviewer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PermissionHelperImpl implements PermissionHelper {

    private static final String LOG_TAG = "PermissionHelperImpl";

    @Override
    public void requestPermissionsIfNeeded(Activity activity, PermissionRequester requester, String... permissions) {
        if (!isPermissionDynamic()) return;
        String[] permissionsNotGranted = arePermissionsGranted(activity, permissions);
        if (permissionsNotGranted.length == 0) return;

        String[] permissionsNeedExplanation = areExplanationsNeeded(activity, permissionsNotGranted);
        if (permissionsNeedExplanation.length != 0) {
            String explanationText = composeExplanation(activity, permissionsNeedExplanation);
            String declinedText = activity.getString(R.string.permissions_not_granted);
            showExplanationDialog(activity, requester, MULTIPLE_PERMISSIONS_REQUEST_CODE, explanationText, declinedText, permissionsNotGranted);
        } else {
            requester.requestPermissions(MULTIPLE_PERMISSIONS_REQUEST_CODE, permissions);
        }
    }

    @Override
    public boolean checkWriteStoragePermission(Context context) {
        if (!isPermissionDynamic()) return true;
        return isPermissionGranted(context, WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public boolean checkReadPhoneStatePermission(Context context) {
        if (!isPermissionDynamic()) return true;
        return isPermissionGranted(context, READ_PHONE_STATE);
    }

    @Override
    public boolean checkReadContactsPermission(Context context) {
        if (!isPermissionDynamic()) return true;
        return isPermissionGranted(context, READ_CONTACTS);
    }

    @Override
    public boolean checkRecordAudioPermission(Context context) {
        if (!isPermissionDynamic()) return true;
        return isPermissionGranted(context, RECORD_AUDIO);
    }

    @Override
    public void requestWriteStoragePermission(Activity activity, PermissionRequester requester) {
        String explanationText = activity.getString(R.string.permission_explanation_external_storage);
        String declinedText = activity.getString(R.string.permission_not_granted_external_storage);
        requestPermission(activity, requester, WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_REQUEST_CODE, explanationText, declinedText);
    }

    @Override
    public void requestWriteStoragePermissionForFile(Activity activity, PermissionRequester requester) {
        String explanationText = activity.getString(R.string.permission_explanation_external_storage_for_file);
        String declinedText = activity.getString(R.string.permission_not_granted_external_storage);
        requestPermission(activity, requester, WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_REQUEST_CODE, explanationText, declinedText);
    }

    @Override
    public void requestReadPhoneStatePermission(Activity activity, PermissionRequester requester) {
        String explanationText = activity.getString(R.string.permission_explanation_phone_state);
        String declinedText = activity.getString(R.string.permission_not_granted_phone_state);
        requestPermission(activity, requester, READ_PHONE_STATE, READ_PHONE_STATE_REQUEST_CODE, explanationText, declinedText);
    }

    @Override
    public void requestReadContactsPermission(Activity activity, PermissionRequester requester) {
        String explanationText = activity.getString(R.string.permission_explanation_read_contacts);
        String declinedText = activity.getString(R.string.permission_not_granted_read_contacts);
        requestPermission(activity, requester, READ_CONTACTS, READ_CONTACTS_REQUEST_CODE, explanationText, declinedText);
    }

    @Override
    public void requestRecordAudioPermission(Activity activity, PermissionRequester requester) {
        String explanationText = activity.getString(R.string.permission_explanation_record_audio);
        String declinedText = activity.getString(R.string.permission_not_granted_record_audio);
        requestPermission(activity, requester, RECORD_AUDIO, RECORD_AUDIO_REQUEST_CODE, explanationText, declinedText);
    }


    private boolean isPermissionDynamic() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean isPermissionGranted(Context context, String permission) {
        try {
            int selfPermission = PermissionChecker.checkSelfPermission(context, permission);
            if (selfPermission == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "Permission: " + permission + " is granted");
                return true;
            } else {
                Log.d(LOG_TAG, "Permission: " + permission + " is denied");
                return false;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to check permission", e);
            return false;
        }
    }

    private String[] arePermissionsGranted(Context context, String[] permissions) {
        List<String> permissionsNotGranted = new ArrayList<>();
        for (String permission: permissions) {
            if (!isPermissionGranted(context, permission)) {
                permissionsNotGranted.add(permission);
            }
        }
        Log.d(LOG_TAG, "Permissions are not granted: " + permissionsNotGranted);
        return permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]);
    }

    //запрос на эти разрешения уже показывался пользователю раньше
    private String[] areExplanationsNeeded(Activity activity, String[] permissions) {
        List<String> permissionsNeedExplanation = new ArrayList<>();
        for (String permission: permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                permissionsNeedExplanation.add(permission);
            }
        }
        Log.d(LOG_TAG, "Permissions need explanation: " + permissionsNeedExplanation);
        return permissionsNeedExplanation.toArray(new String[permissionsNeedExplanation.size()]);
    }

    private String composeExplanation(Context context, String... permissions) {
        String explanationText = "";
        for (String permission: permissions) {
            switch (permission) {
                case WRITE_EXTERNAL_STORAGE:
                    explanationText += context.getString(R.string.permission_explanation_external_storage);
                    break;
                case READ_PHONE_STATE:
                    if (!TextUtils.isEmpty(explanationText)) {
                        explanationText += ",\n";
                    }
                    explanationText += context.getString(R.string.permission_explanation_phone_state);
                    break;
                case READ_CONTACTS:
                    if (!TextUtils.isEmpty(explanationText)) {
                        explanationText += ",\n";
                    }
                    explanationText += context.getString(R.string.permission_explanation_read_contacts);
                    break;
                case RECORD_AUDIO:
                    if (!TextUtils.isEmpty(explanationText)) {
                        explanationText += ",\n";
                    }
                    explanationText += context.getString(R.string.permission_explanation_record_audio);
                    break;
            }
        }
        return explanationText;
    }

    private void requestPermission(Activity activity, PermissionRequester requester, String permission, int requestCode, String explanationText, String declinedText) {
        boolean isExplanationNeeded = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        Log.d(LOG_TAG, "Permission: " + permission + " needs explanation: " + isExplanationNeeded);
        if (isExplanationNeeded) {
            showExplanationDialog(activity, requester, requestCode, explanationText, declinedText, permission);
        } else {
            requester.requestPermissions(requestCode, permission);
        }
    }

    //permissions - это список всех необходимых разрешений, а не только тех, что требуют пояснений
    private void showExplanationDialog(Context context, PermissionRequester requester, int requestCode, String explanationText, String declinedText, String... permissions) {
        Log.d(LOG_TAG, "Showing explanation dialog for permissions: " + Arrays.toString(permissions));
        new AlertDialog.Builder(context)
                .setMessage(explanationText)
                .setPositiveButton(android.R.string.yes, ((dialog, which) ->
                        requester.requestPermissions(requestCode, permissions)))
                .setNegativeButton(android.R.string.no, ((dialog, which) ->
                        Toast.makeText(context, declinedText, Toast.LENGTH_SHORT).show()))
                .show();
    }
}
