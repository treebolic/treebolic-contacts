/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package treebolic.provider.contacts

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Permission helper
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object Permissions {

    private const val PERMISSIONS_REQUEST_READ_CONTACTS = 1313

    @JvmStatic
    fun check(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun checkAndRequest(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
            return false
        }
        return true
    }
}
