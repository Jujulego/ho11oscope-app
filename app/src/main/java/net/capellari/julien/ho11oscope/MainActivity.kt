package net.capellari.julien.ho11oscope

import android.app.SearchManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import net.capellari.julien.ho11oscope.youtube.YoutubeSearchFragment
import net.capellari.julien.ho11oscope.youtube.YoutubeSearchProvider
import net.capellari.julien.ho11oscope.youtube.YoutubeVideoActivity

class MainActivity : AppCompatActivity() {
    // Companion (equiv to static)
    companion object {
        const val TAG = "MainActivity"

        // Activity State
        const val STATE_STATE = "state"

        // Fragment tags
        const val SETTINGS_TAG       = "SettingsFragment"
        const val YOUTUBE_SEARCH_TAG = "YoutubeSearchFragment"
    }

    // Enumeration
    enum class State {
        NONE,
        YOUTUBE_SEARCH,
        SETTINGS
    }

    // Attributs
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // Propriétés
    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(this, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    private var _state = State.NONE
    private var state
        get() = _state
        set(state) {
            // Update UI
            when (state) {
                _state -> {}
                State.NONE -> {
                    supportFragmentManager.beginTransaction()
                            .apply {
                                for (fragment in supportFragmentManager.fragments) {
                                    remove(fragment ?: continue)
                                }

                                addToBackStack(null)
                            }.commit()
                }

                State.SETTINGS -> setupSettings(_state)
                State.YOUTUBE_SEARCH -> setupYoutubeSearch(_state)
            }

            // Update value
            _state = state
        }

    private val youtubeSearchFragment: YoutubeSearchFragment?
        get() = supportFragmentManager.findFragmentByTag(YOUTUBE_SEARCH_TAG) as? YoutubeSearchFragment
    private val settingsFragment: SettingsFragment?
        get() = supportFragmentManager.findFragmentByTag(SETTINGS_TAG) as? SettingsFragment

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
        state = State.YOUTUBE_SEARCH
    }

    override fun onStart() {
        super.onStart()

        // Sync drawerToggle
        drawerToggle.syncState()

        // Start search
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also {
                query -> youtubeSearchFragment?.search(query)
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore state
        savedInstanceState?.getString(STATE_STATE)?.also {
            s -> state = State.valueOf(s)
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
        return drawerToggle.onOptionsItemSelected(item) or when(item.itemId) {
            R.id.tool_clearhistory -> {
                searchRecentSuggestions.clearHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                    state = State.YOUTUBE_SEARCH

                    true
                }
                R.id.nav_settings -> {
                    drawerLayout.closeDrawers()
                    state = State.SETTINGS

                    true
                }
                R.id.debug_yt_video_activity -> {
                    startActivity(Intent(this, YoutubeVideoActivity::class.java))
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
                    is YoutubeSearchFragment -> {
                        _state = State.YOUTUBE_SEARCH
                        navView.setCheckedItem(R.id.nav_youtube)

                        break@loop
                    }
                    is SettingsFragment -> {
                        _state = State.SETTINGS
                        navView.setCheckedItem(R.id.nav_settings)

                        break@loop
                    }
                }
            }
        }
    }

    private fun setupYoutubeSearch(previous: State) {
        // Create fragment
        val frag = YoutubeSearchFragment()
        frag.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                drawerLayout.closeDrawers()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean = true
        })

        // Replace fragment
        supportFragmentManager.beginTransaction()
                .apply {
                    replace(R.id.fragmentPlaceholder, frag, YOUTUBE_SEARCH_TAG)
                    addToBackStack(null)
                }.commit()

        // Mark state as active
        navView.setCheckedItem(R.id.nav_youtube)
    }
    private fun setupSettings(previous: State) {
        // Create fragment
        val frag = SettingsFragment()

        // Replace fragment
        supportFragmentManager.beginTransaction()
                .apply {
                    replace(R.id.fragmentPlaceholder, frag, SETTINGS_TAG)
                    addToBackStack(null)
                }.commit()

        // Mark state as active
        navView.setCheckedItem(R.id.nav_settings)
    }
}
