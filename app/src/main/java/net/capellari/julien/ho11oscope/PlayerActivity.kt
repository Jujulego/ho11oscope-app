package net.capellari.julien.ho11oscope

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

class PlayerActivity : AppCompatActivity() {
    // Companion (equiv to static)
    companion object {
        const val TAG = "PlayerActivity"
    }

    // Propriétés
    private val youtubeApiKey
        get() = resources.getString(R.string.youtube_data_api_key)

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setContentView(R.layout.player_activity)
        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemUI()
        }
    }

    // Méthodes
    private fun setupYoutube() {
    }

    private fun hideSystemUI() {
        // Max brightness
        window.attributes.screenBrightness = BRIGHTNESS_OVERRIDE_FULL

        // Immersive mode
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Move layout behind system bars
                or View.SYSTEM_UI_LAYOUT_FLAGS
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                // Hide system bars
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        // Default brightness
        window.attributes.screenBrightness = BRIGHTNESS_OVERRIDE_NONE

        // Normal mode
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}