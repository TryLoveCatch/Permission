/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiaodu.permission.util2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ESPermission {

    public static final int SETTINGS_REQ_CODE = 12345;


    public interface ESPermissionCallback extends
            ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

        void onPermissionsAllGranted();

    }

    public static boolean hasPermissions(Context context, String... perms) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }

        int targetSdkVersion = getTargetSdkVersion(context);

        for (String perm : perms) {
            boolean hasPerm = checkSelfPermission(context, targetSdkVersion, perm);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }

    public static void requestPermissions(final Activity activity, String rationale,
                                          final int requestCode, final String... perms) {
        requestPermissions(activity, rationale,
                android.R.string.ok,
                android.R.string.cancel,
                requestCode, perms);
    }

    public static void requestPermissions(final Activity activity, String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          final int requestCode, final String... perms) {

        boolean shouldShowRationale = false;
        for (String perm : perms) {
            shouldShowRationale =
                    shouldShowRationale || shouldShowRequestPermissionRationale(activity, perm);
        }

        if (shouldShowRationale) {
            if (null == getActivity(activity)) {
                return;
            }

            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setMessage(rationale)
                    .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executePermissionsRequest(activity, perms, requestCode);
                        }
                    })
                    .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (activity instanceof ESPermissionCallback) {
                                ((ESPermissionCallback) activity).onPermissionsDenied(requestCode, Arrays.asList(perms));
                            }
                        }
                    }).create();
            dialog.show();
        } else {
            executePermissionsRequest(activity, perms, requestCode);
        }
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                  int[] grantResults, Activity activity) {

        ArrayList<String> granted = new ArrayList<>();
        ArrayList<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        if (!granted.isEmpty()) {
            // Notify callbacks
            if (activity instanceof ESPermissionCallback) {
                ((ESPermissionCallback) activity).onPermissionsGranted(requestCode, granted);
            }
        }

        if (!denied.isEmpty()) {
            if (activity instanceof ESPermissionCallback) {
                ((ESPermissionCallback) activity).onPermissionsDenied(requestCode, denied);
            }
        }

         if (!granted.isEmpty() && denied.isEmpty()) {
            if (activity instanceof ESPermissionCallback)
                ((ESPermissionCallback) activity).onPermissionsAllGranted();
        }
    }


    public static boolean checkDeniedPermissionsNeverAskAgain(final Activity activity,
                                                              String rationale,
                                                              @StringRes int positiveButton,
                                                              @StringRes int negativeButton,
                                                              List<String> deniedPerms) {
        boolean shouldShowRationale;
        for (String perm : deniedPerms) {
            shouldShowRationale = shouldShowRequestPermissionRationale(activity, perm);
            if (!shouldShowRationale) {
                if (null == getActivity(activity)) {
                    return true;
                }

                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setMessage(rationale)
                        .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                intent.setData(uri);
                                startAppSettingsScreen(activity, intent);
                            }
                        })
                        .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                dialog.show();

                return true;
            }
        }

        return false;
    }

    private static boolean shouldShowRequestPermissionRationale(Activity activity, String perm) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, perm);
    }

    //targetSdkVersion < 23 不管用
    private static void executePermissionsRequest(Activity activity, String[] perms, int requestCode) {
        ActivityCompat.requestPermissions(activity, perms, requestCode);
    }

    private static Activity getActivity(Activity activity) {
        if(activity.isFinishing()){
            return null;
        }
        return activity;
    }

    private static void startAppSettingsScreen(Activity pActivity, Intent intent) {
        pActivity.startActivityForResult(intent, SETTINGS_REQ_CODE);
    }

    private static int getTargetSdkVersion(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean checkSelfPermission(Context context, int targetSdkVersion, String perm){
        boolean hasPerm = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                hasPerm = context.checkSelfPermission(perm)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                hasPerm = PermissionChecker.checkSelfPermission(context, perm)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return hasPerm;
    }

}
