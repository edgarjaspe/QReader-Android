package com.mobitribe.app.qreader

import android.content.Context
import android.preference.PreferenceManager
import com.mobitribe.app.qreader.ApplicationConstant.Companion.DEVICE_UUID
import java.util.*

/**
 * Author: Muhammad Shahab.
 * Organization: Mobitribe
 * Date: 12/18/17
 * Description: class to save or retrieve data from shared preferences
 */

class SharedPreferences(mContext: Context) {

    private val mPrefs: android.content.SharedPreferences


    fun getDeviceUUID(): String? {
            var device_UUID = mPrefs.getString(DEVICE_UUID, "")
            if (device_UUID!!.isEmpty()) {
                device_UUID = UUID.randomUUID().toString()
                setDeviceUUID(device_UUID)
            }
            return device_UUID
        }

    init {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    fun setDeviceUUID(deviceUUID: String?): Boolean {

        val prefsEditor = mPrefs.edit()
        prefsEditor.putString(DEVICE_UUID, deviceUUID)
        return prefsEditor.commit()
    }
}

