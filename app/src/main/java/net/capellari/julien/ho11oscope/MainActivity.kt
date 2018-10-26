package net.capellari.julien.ho11oscope

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.transition.*
import android.view.MenuItem
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.youtube_search_result.view.*
import net.capellari.julien.ho11oscope.opengl.OpenGLActivity
import net.capellari.julien.ho11oscope.poly.PolyActivity
import net.capellari.julien.ho11oscope.youtube.YoutubeFragment
import net.capellari.julien.ho11oscope.youtube.YoutubeVideoFragment

class MainActivity : AppCompatActivity() {
    // Companion (equiv to static)
    companion object {
        const val TAG = "MainActivity"

        // Activity State
        const val STATE_STATE = "state"

        // Fragment tags
        const val SETTINGS_TAG      = "SettingsFragment"
        const val YOUTUBE_TAG       = "YoutubeFragment"
        const val YOUTUBE_VIDEO_TAG = "YoutubeVideoFragment"
    }

    // Enumeration
    enum class State {
        NONE,
        SETTINGS,
        YOUTUBE, YOUTUBE_VIDEO
    }

    // Attributs
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // Propriétés
    private var state = State.NONE

    private val settingsFragment: SettingsFragment?
        get() = supportFragmentManager.findFragmentByTag(SETTINGS_TAG) as? SettingsFragment
    private val youtubeFragment: YoutubeFragment?
        get() = supportFragmentManager.findFragmentByTag(YOUTUBE_TAG) as? YoutubeFragment
    private val youtubeVideoFragment: YoutubeVideoFragment?
        get() = supportFragmentManager.findFragmentByTag(YOUTUBE_VIDEO_TAG) as? YoutubeVideoFragment

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout
        setContentView(R.layout.main_activity)

        // Setup
        setupToolbar()
        setupDrawer()
        setupFragments()

        // Init UI
        setupYoutube()
    }

    override fun onStart() {
        super.onStart()

        // Sync drawerToggle
        drawerToggle.syncState()

        // Start search
        /*if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                youtubeFragment?.search(query)
            }
        }*/
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore state
        savedInstanceState?.getString(STATE_STATE)?.also {
            s -> when (State.valueOf(s)) {
                state -> {}
                State.NONE -> setupNone()

                State.SETTINGS -> setupSettings()
                State.YOUTUBE  -> setupYoutube()
                State.YOUTUBE_VIDEO -> setupYoutube()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        // Save state
        outState?.putString(STATE_STATE, state.name)

        super.onSaveInstanceState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        // Pass to the drawerToggle
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerToggle.onOptionsItemSelected(item) or super.onOptionsItemSelected(item)
    }

    // Méthods
    private fun setupToolbar() {
        // Set support toolbar
        setSupportActionBar(toolbar)

        // Show home button
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
    private fun setupDrawer() {
        // Setup actionbar toggle
        drawerToggle = ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.nav_open, R.string.nav_close
        )

        drawerLayout.addDrawerListener(drawerToggle)

        // Events
        navView.setNavigationItemSelectedListener {
            item -> when (item.itemId) {
                R.id.nav_youtube -> {
                    drawerLayout.closeDrawers()
                    setupYoutube()

                    true
                }
                R.id.nav_opengl -> {
                    startActivity(Intent(this, OpenGLActivity::class.java))
                    true
                }
                R.id.nav_poly -> {
                    startActivity(Intent(this, PolyActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    drawerLayout.closeDrawers()
                    setupSettings()

                    true
                }
                R.id.debug_player_activity -> {
                    startActivity(Intent(this, PlayerActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
    private fun setupFragments() {
        supportFragmentManager.addOnBackStackChangedListener {
            // Update internal & drawer state
            loop@ for (fragment in supportFragmentManager.fragments) {
                when(fragment) {
                    is YoutubeFragment -> {
                        state = State.YOUTUBE
                        navView.setCheckedItem(R.id.nav_youtube)

                        break@loop
                    }
                    is SettingsFragment -> {
                        state = State.SETTINGS
                        navView.setCheckedItem(R.id.nav_settings)

                        break@loop
                    }
                }
            }
        }
    }

    private fun setupNone() {
        // Remove all fragments
        supportFragmentManager.beginTransaction()
                .apply {
                    for (fragment in supportFragmentManager.fragments) {
                        remove(fragment ?: continue)
                    }
                }.commit()

        // Update state
        state = State.NONE
    }
    private fun setupSettings() {
        // Create fragment
        val frag = SettingsFragment()

        // Replace fragment
        supportFragmentManager.beginTransaction()
                .apply {
                    replace(R.id.fragmentPlaceholder, frag, SETTINGS_TAG)
                    if (state != State.NONE) addToBackStack(null)
                }.commit()

        // Mark state as active
        navView.setCheckedItem(R.id.nav_settings)

        // Update state
        state = State.SETTINGS
    }
    private fun setupYoutube() {
        // Create fragment
        val frag = YoutubeFragment()
        frag.addYoutubeListener(object : YoutubeFragment.YoutubeListener {
            override fun onVideoClick(video: YoutubeFragment.VideoAdapter.VideoHolder) {
                setupYoutubeVideo(video)
            }

            // Menu item
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                drawerLayout.closeDrawers()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean = true
        })

        // Replace fragment
        supportFragmentManager.beginTransaction()
                .apply {
                    replace(R.id.fragmentPlaceholder, frag, YOUTUBE_TAG)
                    if (state != State.NONE) addToBackStack(null)
                }.commit()

        // Mark state as active
        navView.setCheckedItem(R.id.nav_youtube)

        // Update state
        state = State.YOUTUBE
    }
    private fun setupYoutubeVideo(video: YoutubeFragment.VideoAdapter.VideoHolder) {
        video.video?.let {
            // Create fragment
            val frag = YoutubeVideoFragment.newInstance(it)

            if (state == State.YOUTUBE) {
                youtubeFragment?.exitTransition = Fade()
                        .apply {
                            duration = 300
                        }

                frag.sharedElementEnterTransition = MoveTransition()
                        .apply {
                            duration = 600
                        }

                frag.enterTransition = Fade()
                        .apply {
                            startDelay = 300
                            duration = 300
                        }
            }

            // Replace fragment
            supportFragmentManager.beginTransaction()
                    .apply {
                        if (state == State.YOUTUBE) {
                            video.setTransitionNames()

                            addSharedElement(video.view.image,       "video_image")
                            addSharedElement(video.view.videoTitle,  "video_title")
                            addSharedElement(video.view.description, "video_description")
                        }
                        replace(R.id.fragmentPlaceholder, frag, YOUTUBE_TAG)

                        if (state != State.NONE) addToBackStack(null)
                    }.commit()

            // Mark state as active
            navView.setCheckedItem(R.id.nav_youtube)

            // Update state
            state = State.YOUTUBE_VIDEO
        }
    }

    inner class MoveTransition : TransitionSet() {
        init {
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())
        }
    }
}
