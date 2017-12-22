package com.mobitribe.app.qreader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import me.dm7.barcodescanner.zbar.ZBarScannerView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import com.mobitribe.app.qreader.ApplicationConstant.Companion.REQUEST_IMAGES_CAPTURE
import kotlinx.android.synthetic.main.activity_qrcode_reader.*
import me.dm7.barcodescanner.zbar.Result


/**
 * Author: Muhammad Shahab.
 * Organization: Mobitribe
 * Date: 12/14/17
 * Description: Qr code reader activity reads qr code and return to main Activity with url in result
 */

class QRCodeReaderActivity : ParentActivity(), ZBarScannerView.ResultHandler {
    private var mScannerView: ZBarScannerView? = null
    private var permitted: Boolean = true
    val APP_PERMISSIONS = arrayOf( Manifest.permission.CAMERA)


    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        // Programmatically initialize the scanner view
        setContentView(R.layout.activity_qrcode_reader)
        mScannerView = qrdecoderview// Set the scanner view as the content view
        fullscreenContent = mScannerView as ZBarScannerView
    }

    public override fun onResume() {
        super.onResume()

        delayedHide(0)
        openCamera()
    }

    fun openCamera() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(APP_PERMISSIONS, REQUEST_IMAGES_CAPTURE)
                return
            } else {
                permitted = true
                openPreview()
            }
        } else {

            permitted = true
            //Do Your Stuff
            openPreview()
        }
    }
    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()           // Stop camera on pause
    }

    private val TAG: String? = "QRCodeActivity"

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        Log.v(TAG, rawResult.getBarcodeFormat().getName()) // Prints the scan format (qrcode, pdf417 etc.)

        var url = rawResult.getContents();

        Log.v(TAG, url) // Prints scan results

        if (URLUtil.isValidUrl(url))
        {
            setResult(Activity.RESULT_OK, Intent().putExtra(ApplicationConstant.SCANNED_URL,url))
            finish()
        }
        // If you would like to resume scanning, call this method below:
        mScannerView!!.resumeCameraPreview(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGES_CAPTURE) {

            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // Now user should be able to use camera
                    permitted = false
                }
            }

            /*checking whether all permission granted or not*/
            if (permitted) {
                openPreview()
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT)

            }


        }
    }

    private fun openPreview() {
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera()          // Start camera on resume
    }

    fun onBackPressed(view :View) {
        super.onBackPressed()
    }
}