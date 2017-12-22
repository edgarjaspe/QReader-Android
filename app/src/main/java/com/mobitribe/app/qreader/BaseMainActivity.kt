package com.mobitribe.app.qreader

import android.Manifest
import android.content.Intent
import android.os.Build
import android.annotation.TargetApi
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.webkit.WebView
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import com.mobitribe.app.qreader.ApplicationConstant.Companion.FILE_CHOOSER_RESULT_CODE
import com.mobitribe.app.qreader.ApplicationConstant.Companion.REQUEST_CONTACTS_READ
import com.mobitribe.app.qreader.ApplicationConstant.Companion.REQUEST_IMAGES_CHOOSER
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.design.widget.Snackbar
import android.view.View
import android.webkit.WebResourceResponse
import android.webkit.WebResourceRequest




/**
 * Author: Muhammad Shahab.
 * Organization: Mobitribe
 * Date: 12/14/17
 * Description: Base activity for main activity contains operational work
 */
open class BaseMainActivity : ParentActivity(){

    protected var mUploadMessage: ValueCallback<Uri>? = null
    protected var mUploadMessages: ValueCallback<Array<Uri>>? = null
    protected var mCapturedImageURI: Uri? = null
    private val TAG: String? = "BaseMainActivity"
    private var permitted: Boolean = true
    var device_UUID: String? = null

    val APP_STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val APP_CONTACTS_PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        device_UUID = SharedPreferences(this).getDeviceUUID()

    }

    protected fun handleUploadMessage(requestCode: Int, resultCode: Int, intent: Intent?) {
        var result: Uri? = null
        try {
            if (resultCode != RESULT_OK) {
                result = null
            } else {
                // retrieve from the private variable if the intent is null

                result = if (intent == null) mCapturedImageURI else intent.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mUploadMessage?.onReceiveValue(result)
        mUploadMessage = null
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun handleUploadMessages(requestCode: Int, resultCode: Int, intent: Intent?) {
        var results: Array<Uri>? = null
        try {
            if (resultCode != RESULT_OK) {
                results = null
            } else {
                if (intent != null) {

                    val dataString = intent.dataString
                    val clipData = intent.clipData

                    if (clipData != null || dataString !=null) {
                        if (clipData != null) {
                            results = Array<Uri>(clipData.itemCount, { i -> Uri.EMPTY })
                            for (i in 0 until clipData.itemCount) {
                                val item = clipData.getItemAt(i)
                                results[i] = item.uri
                            }
                        }
                        if (dataString != null) {
                            results = arrayOf<Uri>(Uri.parse(dataString))
                        }
                    }
                    else
                    {
                        results = mCapturedImageURI?.let { arrayOf<Uri>(it) }
                    }
                } else {
                    results = mCapturedImageURI?.let { arrayOf<Uri>(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mUploadMessages?.onReceiveValue(results)
        mUploadMessages = null
    }

    /**
     * @purpose It opens Image chooser from External Storage
     */
    protected fun openImageChooser() {

        Log.d(TAG,"Calling image chooser")

        try {
            val imageStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FolderName")
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs()
            }
            val file = File(imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis().toString() + ".jpg")
            mCapturedImageURI = Uri.fromFile(file)
            Log.d(TAG,"Image Uri" + mCapturedImageURI.toString());

            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI)

            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"

            val chooserIntent = Intent.createChooser(i, "Item Chooser")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent))

            startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private var firstTime: Boolean = true;

    /**
     * @purpose It initialize webview with all necessary options
     */
    protected fun initWebView() {

        webview.settings.javaScriptEnabled = true
        webview.settings.setLoadWithOverviewMode(true)


        //Other webview settings
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY)
        webview.setScrollbarFadingEnabled(false)
        webview.settings.setAppCacheEnabled(true);
        webview.settings.setPluginState(WebSettings.PluginState.ON)
        webview.settings.setAllowFileAccess(true)

        webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                setProgress(progress * 1000)
            }


            // openFileChooser for Android 3.0+
            fun openFileChooser(uploadMsg:ValueCallback<Uri>, acceptType:String) {

                // Update message
                mUploadMessage = uploadMsg
                if(checkPermission(APP_STORAGE_PERMISSIONS, REQUEST_IMAGES_CHOOSER))
                {
                    openImageChooser()
                }

            }
            // For Lollipop 5.0+ Devices

            override fun onShowFileChooser(mWebView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                mUploadMessages = filePathCallback
                if(checkPermission(APP_STORAGE_PERMISSIONS, REQUEST_IMAGES_CHOOSER))
                {
                    openImageChooser()
                }
                return true
            }

            // openFileChooser for Android < 3.0

            fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
                openFileChooser(uploadMsg, "")
            }

            //openFileChooser for other Android versions

            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
                openFileChooser(uploadMsg, acceptType)
            }


            // The webPage has 2 filechoosers and will send a
            // console message informing what action to perform,
            // taking a photo or updating the file

            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {

                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId())
                return true
            }

            override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
                Log.d("androidruntime", "Show console messages, Used for debugging: " + message);
            }


        }

        webview.webViewClient = object : WebViewClient() {

            //If you will not use this method url links are open in new brower not in webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                // Check if Url contains ExternalLinks string in url
                // then open url in new browser
                // else all webview links will open in webview browser
                 if (url.contains("whatsapp")) {

                    // Could be cleverer and use a regex
                    //Open links in new browser
                    view.context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url)))

                }
                else if(url.contains("geo:")||url.contains("maps")) {
                    val gmmIntentUri = Uri.parse(url)
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.`package` = "com.google.android.apps.maps"
                    if (mapIntent.resolveActivity(packageManager) != null) {
                        startActivity(mapIntent)
                    }
                }
                else {

                    // Stay within this webview and load url
                    view.loadUrl(url)
                }

                return true
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                error_layout.visibility = View.VISIBLE
                Log.d(TAG,"onReceivedError")
                super.onReceivedError(view, request, error)
            }

            /*
                Added in API level 23
            */
            @TargetApi(android.os.Build.VERSION_CODES.M)
            override fun onReceivedHttpError(view: WebView,
                                    request: WebResourceRequest, errorResponse: WebResourceResponse) {

                /*First time this method called whether app return error or not
                  so this logic is to prevent the first call*/
                if(!firstTime) error_layout.visibility = View.VISIBLE
                else firstTime = false;

                Log.d(TAG,"onReceivedHttpError")
                super.onReceivedHttpError(view, request, errorResponse)
            }

        }
        loadUrlWithCookies(getString(R.string.url))
    }


    protected fun loadUrlWithCookies(webUrl: String) {
        Log.d(TAG,"Web Url${webUrl}Device UUID$device_UUID")
        CookieManager.getInstance().setCookie(webUrl, "device_uuid=" + device_UUID)
        webview.loadUrl(webUrl)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGES_CHOOSER) {

            permitted = checkPermissionDenied(grantResults)

            /*checking whether all permission granted or not*/
            if (permitted) {
                openImageChooser()
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
                Toast.makeText(this, "Storage Read Permission denied.", Toast.LENGTH_SHORT)

            }


        }else if(requestCode ==  REQUEST_CONTACTS_READ)
        {
            /*checking whether all permission granted or not*/
            if (checkPermissionDenied(grantResults)) {
                updateContacts()
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
                Toast.makeText(this, "Contacts Permission denied.", Toast.LENGTH_SHORT)

            }
        }
    }


    /**
     * @purpose check if used denied any permission
     */
    private fun checkPermissionDenied(grantResults: IntArray): Boolean {
        for (i in grantResults.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                // Now user should be able to use camera
                return false
            }
        }
        return true;
    }

    /**
     * @purpose call to sync contacts with server
     */
    protected fun updateContacts() {
        ContactSyncUp(this).execute()
    }


    /**
     * @purpose checking contact read permissions
     */
    protected fun checkContactReadPermission() {
        if(checkPermission(APP_CONTACTS_PERMISSIONS, REQUEST_CONTACTS_READ))
        {
            updateContacts()
        }
    }


    /**
     * @purpose It checks the permission granted or not on requests given as parameters
     * @param permissions, requestCode
     * @return Boolean
     */
    private fun checkPermission(permissions: Array<String>, requestCode: Int): Boolean
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permissions.get(0)) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, requestCode)
                return false;
            } else {
                return true
            }
        } else {
            return true
        }
    }
}