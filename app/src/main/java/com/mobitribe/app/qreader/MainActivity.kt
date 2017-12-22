package com.mobitribe.app.qreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import com.mobitribe.app.qreader.ApplicationConstant.Companion.FILE_CHOOSER_RESULT_CODE
import com.mobitribe.app.qreader.ApplicationConstant.Companion.QR_CODE_SCANNER_CODE
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Author: Muhammad Shahab.
 * Organization: Mobitribe
 * Date: 12/14/17
 * Description: Home page of application where the webview exists
 */
class MainActivity : BaseMainActivity() {

    private val TAG: String? = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.requestFeature(Window.FEATURE_PROGRESS)
        setContentView(R.layout.activity_main)
        fullscreenContent = main_content

        /*Intializing webview*/
        initWebView()

        /*QR code button listener*/
        fab.setOnClickListener { view ->

            forwardTransition = false
            startActivityForResult(Intent(this, QRCodeReaderActivity::class.java),
                    QR_CODE_SCANNER_CODE)
        }

        refresh.setOnClickListener { view ->
            webview.reload()
            error_layout.visibility = View.GONE
        }

        checkContactReadPermission();
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == QR_CODE_SCANNER_CODE && data != null) {
                val text: String = data.getStringExtra(ApplicationConstant.SCANNED_URL)
                error_layout.visibility = View.GONE
                loadUrlWithCookies(text)
            }
            if(requestCode== FILE_CHOOSER_RESULT_CODE) {

                Log.d(TAG,"onActivityResult")
                if (null == mUploadMessage && null == mUploadMessages) {
                    return
                }

                if (null != mUploadMessage) {
                    handleUploadMessage(requestCode, resultCode, data)

                } else if (mUploadMessages != null) {
                    handleUploadMessages(requestCode, resultCode, data)
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
//        // are available.
        delayedHide(0)
    }

    // Detect when the back button is pressed
    override fun onBackPressed() {

        if (webview.canGoBack()) {

            webview.goBack()

        } else {
            // stop the system handle the back button
        }
    }
}
