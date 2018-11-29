package net.capellari.julien.ho11oscope.youtube

import android.app.SearchManager
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import androidx.appcompat.widget.*
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import net.capellari.julien.ho11oscope.R

class YoutubeFragment : Fragment(), MenuItem.OnActionExpandListener {
    // Companion (equiv to static)
    companion object {
        const val TAG = "YoutubeFragment"
    }

    // Attributs
    private var searchMenuItem: MenuItem? = null

    private lateinit var ytModel: YoutubeViewModel

    // Propriétés
    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(context, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    private val searchView: SearchView?
        get() = searchMenuItem?.actionView as? SearchView

    private val navController by lazy { Navigation.findNavController(this.requireActivity(), R.id.navHostFragment) }

    // Events
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Get ViewModels
        ytModel = ViewModelProviders.of(activity!!)[YoutubeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.youtube_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu
        inflater.inflate(R.menu.toolbar_recherche, menu)

        // SearchItem
        searchMenuItem = menu.findItem(R.id.tool_search)?.setOnActionExpandListener(this)

        setupSearchView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.tool_clearhistory -> {
                searchRecentSuggestions.clearHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        // Searching ...
        ytModel.apply {
            query = query ?: ""
        }

        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        // Stop searching ...
        ytModel.query = null
        ytModel.invalidate()

        return true
    }

    // Methods
    fun search(query: String? = null) {
        // Sauvegarde
        searchRecentSuggestions.saveRecentQuery(query, null)

        // Start searching
        query?.let { ytModel.query = it }
        ytModel.invalidate()
    }

    private fun setupSearchView() {
        // Check activity and searchView
        activity?.also { activity ->
            searchView?.apply {
                // Setup
                val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
                setIconifiedByDefault(true)

                // Restore state
                ytModel.query?.also { query ->
                    searchMenuItem?.expandActionView()
                    setQuery(query, false)
                    clearFocus() // with no focus
                }

                // Listeners
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String?): Boolean = false
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        search(query)

                        return true
                    }
                })

                setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                    override fun onSuggestionSelect(position: Int): Boolean = false
                    override fun onSuggestionClick(position: Int): Boolean {
                        val cursor = suggestionsAdapter.cursor

                        // Get suggestion selected
                        if (cursor.moveToPosition(position)) {
                            val query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))

                            // Start searching + update UI
                            search(query)
                            this@apply.setQuery(query, false)

                            return true
                        }

                        return false
                    }
                })
            }
        }
    }
}