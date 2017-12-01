package com.googleplacepicker.MediaPermission;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class PermissionsChecker {
    private final Context context;

    public PermissionsChecker(Context context) {
        this.context = context;
    }

    public boolean lacksPermissions(String... permissions) {
        boolean isLack = false;
        try {
            for (String permission : permissions) {
                if (lacksPermission(permission)) {
                    isLack = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isLack;
    }

    private boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

}