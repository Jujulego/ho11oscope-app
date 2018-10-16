package net.capellari.julien.ho11oscope

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {
    // Companion (equiv to static)
    companion object {
        const val TAG = "MainActivity"
    }

    // Attributs
    private lateinit var drawerControl: DrawerControl

    // Propriétés
    val ytSearchFragment: YoutubeSearchFragment
        get() = supportFragmentManager.findFragmentById(R.id.youtubeSearchFragment) as YoutubeSearchFragment

    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(this, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout
        setContentView(R.layout.main_activity)

        // Setup
        setupToolbar()
        drawerControl = DrawerControl(this, drawerLayout, navView)
        drawerControl.setupDrawer(R.id.nav_main)
    }

    override fun onStart() {
        super.onStart()

        // Sync drawerToggle
        drawerControl.syncState()

        // Start search
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also {
                query -> ytSearchFragment.search(query)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        // Pass to the drawerToggle
        drawerControl.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate menu
        menuInflater.inflate(R.menu.toolbar_main, menu)

        // SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.tool_search).actionView as SearchView).apply {
            // Setup
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            // Listeners
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    ytSearchFragment.search(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })

            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return false
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    val cursor = suggestionsAdapter.cursor

                    if (cursor.moveToPosition(position)) {
                        val query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                        ytSearchFragment.search(query)

                        this@apply.setQuery(query, false)

                        return true
                    }

                    return false
                }
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerControl.onOptionsItemSelected(item) || when(item.itemId) {
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
}
