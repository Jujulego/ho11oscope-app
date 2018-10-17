package net.capellari.julien.ho11oscope.youtube

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import net.capellari.julien.ho11oscope.R

class YoutubeFragment : Fragment(), MenuItem.OnActionExpandListener {
    // Companion (equiv to static)
    companion object {
        const val TAG = "YoutubeFragment"

        // Fragment tags
        const val SEARCH_TAG  = "YoutubeSearchFragment"
    }

    // Enumeration
    enum class State {
        NONE,
        SEARCH
    }

    // Attributs
    private var searchMenuItem: MenuItem? = null
    private var searchExpandListener: MenuItem.OnActionExpandListener? = null

    private var state = State.NONE
    private var youtubeViewModel: YoutubeViewModel? = null

    // Propriétés
    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(context, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    private val searchView: SearchView?
        get() = searchMenuItem?.actionView as? SearchView

    private val searchFragment: YoutubeSearchFragment?
        get() = fragmentManager?.findFragmentByTag(SEARCH_TAG) as? YoutubeSearchFragment

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setHasOptionsMenu(true)

        // Get ViewModel
        youtubeViewModel = activity?.run {
            ViewModelProviders.of(this).get(YoutubeViewModel::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Create linear layout
        return LinearLayout(context).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu
        inflater.inflate(R.menu.toolbar_main, menu)

        // SearchItem
        searchMenuItem = menu.findItem(R.id.tool_search)
                ?.setOnActionExpandListener(this)

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
        youtubeViewModel?.apply {
            query = query ?: ""
        }
        setupSearch(true)

        // Listener
        return searchExpandListener?.onMenuItemActionExpand(item) == true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        // Stop searching ...
        youtubeViewModel?.query = null
        setupNone()

        // Listener
        return searchExpandListener?.onMenuItemActionCollapse(item) == true
    }

    // Methods
    fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener) {
        searchExpandListener = listener
        searchMenuItem?.setOnActionExpandListener(listener)
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
                youtubeViewModel?.query?.also { query ->
                    searchMenuItem?.expandActionView()
                    setQuery(query, false)
                    clearFocus() // with no focus
                }

                // Listeners
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String?): Boolean = false
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        searchFragment?.search(query)

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
                            searchFragment?.search(query)
                            this@apply.setQuery(query, false)

                            return true
                        }

                        return false
                    }
                })
            }
        }
    }

    private fun setupNone() {
        // Removing all fragments
        fragmentManager?.beginTransaction()
                ?.apply {
                    searchFragment?.let { remove(it) }
                    addToBackStack(null)
                }?.commit()

        // Update state
        state = State.NONE
    }
    private fun setupSearch(fromActionExpandListener: Boolean = false) {
        view?.let { view ->
            searchMenuItem?.apply {
                // Check if the action view is expanded
                if (!fromActionExpandListener and !isActionViewExpanded) {
                    expandActionView()
                    return
                }

                // Create fragment
                val frag = YoutubeSearchFragment()

                // Replace fragment
                fragmentManager?.beginTransaction()
                        ?.apply {
                            replace(view.id, frag, SEARCH_TAG)
                            addToBackStack(null)
                        }?.commit()

                // Update state
                state = State.SEARCH
            }
        }
    }
}