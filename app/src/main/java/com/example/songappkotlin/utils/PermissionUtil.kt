package com.example.songappkotlin.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

object PermissionUtil {
    fun checkReadAudioPermission(context: Context) : Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        else context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowRequestPermissionRationaleForReadAudio(activity: Activity) : Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) ActivityCompat.shouldShowRequestPermissionRationale(
            activity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        else ActivityCompat.shouldShowRequestPermissionRationale(
            activity, android.Manifest.permission.READ_MEDIA_AUDIO)
    }
}