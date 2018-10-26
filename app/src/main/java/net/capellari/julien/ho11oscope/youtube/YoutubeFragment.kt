package net.capellari.julien.ho11oscope.youtube

import android.app.SearchManager
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.*
import android.util.Log
import android.util.Pair as UtilPair
import android.view.*
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.SearchResult
import kotlinx.android.synthetic.main.youtube_fragment.*
import kotlinx.android.synthetic.main.youtube_search_result.view.*
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.RequestManager
import net.capellari.julien.ho11oscope.inflate
import java.text.SimpleDateFormat
import java.util.*

class YoutubeFragment : androidx.fragment.app.Fragment(), MenuItem.OnActionExpandListener {
    // Companion (equiv to static)
    companion object {
        const val TAG = "YoutubeFragment"
    }

    // Enumeration
    enum class State {
        NONE, SEARCH
    }

    // Attributs
    private var searchMenuItem: MenuItem? = null
    private var searchExpandListener: MenuItem.OnActionExpandListener? = null

    private var state = State.NONE
    private val videoAdapter = VideoAdapter()
    private var youtubeViewModel: YoutubeViewModel? = null
    private lateinit var requestManager: RequestManager

    private var listeners = mutableListOf<YoutubeListener>()

    // Propriétés
    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(context, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    private val searchView: SearchView?
        get() = searchMenuItem?.actionView as? SearchView

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

        // Get ViewModel
        youtubeViewModel = activity?.run {
            ViewModelProviders.of(this).get(YoutubeViewModel::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.youtube_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Setup recycler view
        results.apply {
            layoutManager = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> androidx.recyclerview.widget.GridLayoutManager(context, 2)
                else -> androidx.recyclerview.widget.LinearLayoutManager(context)
            }
            adapter = videoAdapter
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        }

        youtubeViewModel?.run {
            // Observe search results
            searchResults.observe(this@YoutubeFragment,
                    androidx.lifecycle.Observer<SearchListResponse> { results ->
                        videoAdapter.videos = results
                        swipeRefresh.isRefreshing = false
                    }
            )
        }

        // Listeners
        swipeRefresh.apply {
            // Setup
            setColorSchemeResources(R.color.colorPrimary)

            // Listeners
            setOnRefreshListener {
                search()
            }
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

        // Listeners
        var res = true
        for (listener in listeners) {
            res = res and listener.onMenuItemActionExpand(item)
        }

        return res
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        // Stop searching ...
        youtubeViewModel?.query = null
        setupNone()

        // Listeners
        var res = true
        for (listener in listeners) {
            res = res and listener.onMenuItemActionCollapse(item)
        }

        return res
    }

    // Methods
    fun search(query: String? = null) {
        // Sauvegarde
        searchRecentSuggestions.saveRecentQuery(query, null)

        // Start searching
        youtubeViewModel?.run {
            this.search(query)
            swipeRefresh.isRefreshing = true
        }
    }
    fun addYoutubeListener(listener: YoutubeListener) {
        listeners.add(listener)
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

    private fun setupNone() {
        // Hide result list
        videoAdapter.videos = null
        search.visibility = View.GONE

        // Update state
        state = State.NONE
    }
    private fun setupSearch(fromActionExpandListener: Boolean = false) {
        // Show result list
        search.visibility = View.VISIBLE

        // Update state
        state = State.SEARCH
    }

    // Sous-classes
    interface YoutubeListener : MenuItem.OnActionExpandListener {
        fun onVideoClick(video: VideoAdapter.VideoHolder)
    }

    inner class VideoAdapter(v: SearchListResponse? = null) : androidx.recyclerview.widget.RecyclerView.Adapter<VideoAdapter.VideoHolder>() {
        // Propriétés
        var videos: SearchListResponse? = v
            set(v) {
                field = v
                notifyDataSetChanged()
            }

        // Méthodes
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
            return VideoHolder(parent.inflate(R.layout.youtube_search_result, false))
        }

        override fun getItemCount(): Int = videos?.items?.size ?: 0

        override fun onBindViewHolder(holder: VideoHolder, position: Int) {
            holder.bindVideo(videos!!.items[position])
        }

        // Sous-classes
        inner class VideoHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), View.OnClickListener {
            // Attributs
            var view: View = v
                private set
            var video: SearchResult? = null
                private set

            // Constructeur
            init {
                view.setOnClickListener(this)
            }

            // Méthodes
            fun bindVideo(video: SearchResult) {
                this.video = video

                // Prepare date
                val date = Date(video.snippet.publishedAt.value)

                // Filling views
                with(view) {
                    videoTitle.text = video.snippet.title
                    description.text = video.snippet.description
                    pubDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)

                    image.setImageUrl(video.snippet.thumbnails.high.url, requestManager.imageLoader)
                }
            }

            override fun onClick(v: View?) {
                // Gardien
                video?.also {
                    // Listeners
                    for (listener in listeners) {
                        listener.onVideoClick(this)
                    }
                }
            }

            fun setTransitionNames() {
                view.image.transitionName       = "video_image"
                view.videoTitle.transitionName  = "video_title"
                view.description.transitionName = "video_description"
            }
        }
    }
}