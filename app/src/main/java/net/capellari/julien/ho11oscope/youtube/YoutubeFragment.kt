package net.capellari.julien.ho11oscope.youtube

import android.app.SearchManager
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import androidx.appcompat.widget.*
import android.util.Pair as UtilPair
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.SearchResult
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.RequestManager
import net.capellari.julien.ho11oscope.ResultsViewModel
import org.jetbrains.anko.bundleOf
import java.text.SimpleDateFormat
import java.util.*

class YoutubeFragment : Fragment(), MenuItem.OnActionExpandListener, ResultsViewModel.OnResultsListener {
    // Companion (equiv to static)
    companion object {
        const val TAG = "YoutubeFragment"
    }

    // Attributs
    private var searchMenuItem: MenuItem? = null

    private lateinit var videos: YoutubeViewModel
    private lateinit var results: ResultsViewModel
    private lateinit var requestManager: RequestManager

    // Propriétés
    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(context, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    private val searchView: SearchView?
        get() = searchMenuItem?.actionView as? SearchView

    private val navController by lazy { Navigation.findNavController(this.requireActivity(), R.id.navHostFragment) }

    // Events
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Get requestManager
        requestManager = RequestManager.getInstance(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setHasOptionsMenu(true)

        // Get ViewModels
        results = ViewModelProviders.of(activity!!)[ResultsViewModel::class.java]
        results.addOnResultsListener(this)

        videos  = ViewModelProviders.of(activity!!)[YoutubeViewModel::class.java]
        videos.searchResults.observe(this@YoutubeFragment,
                Observer<SearchListResponse> { results ->
                    for (video in results.items) {
                        this@YoutubeFragment.results.add(ResultsViewModel.Result(
                                video.snippet.title,
                                video.snippet.description,
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                        Date(video.snippet.publishedAt.value)
                                ),
                                video.snippet.thumbnails.high.url,
                                video
                        ))
                    }

                    this@YoutubeFragment.results.setRefreshing(false)
                }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.youtube_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu
        inflater.inflate(R.menu.toolbar_recherche, menu)

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
        videos.apply {
            query = query ?: ""
        }

        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        // Stop searching ...
        videos.query = null
        results.clear()

        return true
    }

    override fun onRefresh() {
        search()
    }

    override fun onItemClick(res: ResultsViewModel.Result) {
        Log.d(TAG, "itemclick !")

        (res.obj as? SearchResult)?.also { video ->
            navController.navigate(
                    R.id.action_video_details,
                    bundleOf(
                            YoutubeVideoFragment.ARGS_VIDEO_ID          to video.id.videoId,
                            YoutubeVideoFragment.ARGS_VIDEO_TITLE       to video.snippet.title,
                            YoutubeVideoFragment.ARGS_VIDEO_DESCRIPTION to video.snippet.description,
                            YoutubeVideoFragment.ARGS_VIDEO_IMAGE_URL   to video.snippet.thumbnails.high.url
                    )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        results.removeOnResultsListener(this)
    }

    // Methods
    fun search(query: String? = null) {
        // Sauvegarde
        searchRecentSuggestions.saveRecentQuery(query, null)

        // Start searching
        videos.run {
            this.search(query)

            results.clear()
            results.setRefreshing(true)
        }
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
                videos.query?.also { query ->
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