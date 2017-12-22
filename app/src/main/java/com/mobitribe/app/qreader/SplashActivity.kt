package com.mobitribe.app.qreader

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Author: Muhammad Shahab.
 * Organization: Mobitribe
 * Date: 12/14/17
 * Description: Splash activity from where the app start to launch
 */
class SplashActivity : ParentActivity() {

    private val SPLASH_TIMEOUT: Long = 3000

    private lateinit var context: SplashActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        fullscreenContent = fullscreen_content;
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Handler().postDelayed(Runnable { callNextScreen() }, SPLASH_TIMEOUT)


    }

    private fun callNextScreen() {
        startActivity( Intent(this,MainActivity::class.java))
        finish()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
//        // are available.
        delayedHide(0)
    }

}
