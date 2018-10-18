package net.capellari.julien.ho11oscope

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.api.services.youtube.model.SearchResult
import net.capellari.julien.ho11oscope.youtube.YoutubePlayerFragment
import kotlin.reflect.KFunction

class PlayerActivity : AppCompatActivity() {
    // Companion (equiv to static)
    companion object {
        // Attributs
        const val TAG = "PlayerActivity"

        const val EXTRA_TYPE  = "net.capellari.julien.ho11oscope.PlayerActivity.EXTRA_TYPE"
        const val EXTRA_VALUE = "net.capellari.julien.ho11oscope.PlayerActivity.EXTRA_VALUE"

        // Méthodes
        fun fillIntent(intent: Intent, type: Type, value: String) {
            intent
                .putExtra(EXTRA_TYPE, type.name)
                .putExtra(EXTRA_VALUE, value)
                .putExtra(EXTRA_VALUE, value)
        }
        fun fillIntent(intent: Intent, video: SearchResult) {
            intent
                .putExtra(EXTRA_TYPE,   Type.YOUTUBE.name)
                .putExtra(EXTRA_VALUE, video.id.videoId)
        }
    }

    // Enumération
    interface IType {
        fun fragment(value: String): Fragment
    }

    enum class Type: IType {
        YOUTUBE {
            override fun fragment(value: String): Fragment = YoutubePlayerFragment.newInstance(value)
        }
    }

    // Propriétés
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private var preferenceBrightness: Boolean
        get()  = sharedPreferences.getBoolean("player_brightness", true)
        set(v) = sharedPreferences.edit().putBoolean("player_brightness", v).apply()

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setContentView(R.layout.player_activity)
        hideSystemUI()

        setupPlayer()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemUI()
        }
    }

    // Méthodes
    private fun setupPlayer() {
        intent.getStringExtra(EXTRA_TYPE)?.let {
            val type = Type.valueOf(it)

            intent.getStringExtra(EXTRA_VALUE)?.let { value ->
                val frag = type.fragment(value)

                // Add fragment
                supportFragmentManager.beginTransaction()
                        .apply {
                            replace(R.id.fragment_placeholder, frag)
                        }.commit()
            }
        }
    }

    private fun hideSystemUI() {
        // Max brightness
        if (preferenceBrightness) {
            window.attributes.screenBrightness = BRIGHTNESS_OVERRIDE_FULL
        }

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
        if (preferenceBrightness) {
            window.attributes.screenBrightness = BRIGHTNESS_OVERRIDE_NONE
        }

        // Normal mode
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}