package com.devtau.fileviewer;

import android.app.Activity;
import android.content.Context;

/**
 * Если клиент использует универсальный метод requestPermissionsIfNeeded, то обрабатывать результат в
 * onRequestPermissionsResult следует по общему requestCode MULTIPLE_PERMISSIONS_REQUEST_CODE
 * Специфические запросы порождают специфические requestCode
 */
public interface PermissionHelper {

    int MULTIPLE_PERMISSIONS_REQUEST_CODE = 5700;
    int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 5701;
    int READ_PHONE_STATE_REQUEST_CODE = 5702;
    int READ_CONTACTS_REQUEST_CODE = 5703;
    int RECORD_AUDIO_REQUEST_CODE = 5704;

    void requestPermissionsIfNeeded(Activity activity, PermissionRequester requester, String... permissions);

    boolean checkWriteStoragePermission(Context context);
    boolean checkReadPhoneStatePermission(Context context);
    boolean checkReadContactsPermission(Context context);
    boolean checkRecordAudioPermission(Context context);

    void requestWriteStoragePermission(Activity activity, PermissionRequester requester);
    void requestWriteStoragePermissionForFile(Activity activity, PermissionRequester requester);
    void requestReadPhoneStatePermission(Activity activity, PermissionRequester requester);
    void requestReadContactsPermission(Activity activity, PermissionRequester requester);
    void requestRecordAudioPermission(Activity activity, PermissionRequester requester);

    interface PermissionRequester {
        void requestPermissions(int code, String... permissions);
    }
}
