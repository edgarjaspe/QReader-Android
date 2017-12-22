package com.mobitribe.app.qreader

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Author: Muhammad Shahab.
 * Organization: Mobitribe
 * Date: 12/14/17
 * Description: It is a parent Activity for all activities have common functions
 */
open class ParentActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    private val mHideHandler = Handler()
    protected lateinit var fullscreenContent: View
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private val mHideRunnable = Runnable { hide() }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, ParentActivity.UI_ANIMATION_DELAY.toLong())
    }


    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    protected fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 0
    }

    protected var forwardTransition: Boolean = true

    override fun onPause() {
            if (forwardTransition)
                overridePendingTransition(R.anim.slide_in_right_activity, R.anim.slide_out_left_activity)
            else
                overridePendingTransition(R.anim.slide_in_left_activity, R.anim.slide_out_right_activity)
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}