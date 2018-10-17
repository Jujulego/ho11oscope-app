package net.capellari.julien.ho11oscope

import android.app.ActivityOptions
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.LruCache
import android.util.Pair as UtilPair
import android.view.*
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.SearchResult
import kotlinx.android.synthetic.main.yt_search_fragment.*
import kotlinx.android.synthetic.main.yt_search_result.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

class YoutubeSearchFragment : Fragment() {
    // Companion
    companion object {
        const val TAG = "YoutubeSearchFragment"
    }

    // Attributs
    private var query: String? = null
    private val videoAdapter = VideoAdapter()

    private lateinit var requestQueue: RequestQueue
    private lateinit var imageLoader: ImageLoader

    private var searchMenuItem: MenuItem? = null
    private var searchExpandListener: MenuItem.OnActionExpandListener? = null

    // Propriétés
    private val youtubeApi
        get() = YouTube.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                HttpRequestInitializer { }
        ).setApplicationName(resources.getString(R.string.app_name)).build()

    private val youtubeApiKey by lazy {
        resources.getString(R.string.youtube_data_api_key)
    }

    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(context, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // Prepare requestQueue
        requestQueue = Volley.newRequestQueue(context)

        // Setup imageLoader
        imageLoader = ImageLoader(requestQueue, object : ImageLoader.ImageCache {
            // Attributs
            val cache = LruCache<String, Bitmap>(50)

            // Méthodes
            override fun getBitmap(url: String?): Bitmap? {
                return cache.get(url ?: return null)
            }

            override fun putBitmap(url: String?, bitmap: Bitmap?) {
                cache.put(url, bitmap)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.yt_search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Menu refresh
        activity?.invalidateOptionsMenu()

        // Setup recycler view
        results.apply {
            layoutManager = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> GridLayoutManager(context, 2)
                else -> LinearLayoutManager(context)
            }
            adapter = videoAdapter
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

        // SearchView
        val searchManager = context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchMenuItem = menu.findItem(R.id.tool_search)
        (searchMenuItem!!.actionView as SearchView).apply {
            // Setup
            setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

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

                    if (cursor.moveToPosition(position)) {
                        val query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                        search(query)

                        this@apply.setQuery(query, false)

                        return true
                    }

                    return false
                }
            })
        }

        searchMenuItem!!.setOnActionExpandListener(searchExpandListener ?: return)
    }

    override fun onDetach() {
        super.onDetach()

        // Stop RequestQueue
        requestQueue.stop()
    }

    // Fonctions
    fun search(query: String? = null) {
        // Update query attr
        query ?: run {
            this.query = query
        }

        // Sauvegarde
        searchRecentSuggestions.saveRecentQuery(query, null)
        swipeRefresh.isRefreshing = true

        doAsync {
            // Search !
            val req = youtubeApi.search().list("snippet")
            req.apply {
                key = youtubeApiKey

                q = query
                maxResults = 25
                type = "video"
            }

            val rep = req.execute()

            uiThread {
                videoAdapter.videos = rep
                swipeRefresh.isRefreshing = false
            }
        }
    }

    fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener) {
        searchExpandListener = listener
        searchMenuItem?.setOnActionExpandListener(listener)
    }

    // Sous-classes
    inner class VideoAdapter(v: SearchListResponse? = null) : RecyclerView.Adapter<VideoAdapter.VideoHolder>() {
        // Propriétés
        var videos: SearchListResponse? = v
            set(v) {
                field = v
                notifyDataSetChanged()
            }

        // Méthodes
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoAdapter.VideoHolder {
            return VideoHolder(parent.inflate(R.layout.yt_search_result, false))
        }

        override fun getItemCount(): Int = videos?.items?.size ?: 0

        override fun onBindViewHolder(holder: VideoAdapter.VideoHolder, position: Int) {
            holder.bindVideo(videos!!.items[position])
        }

        // Sous-classes
        inner class VideoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
            // Attributs
            private var view: View = v
            private var video: SearchResult? = null

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
                view.videoTitle.text = video.snippet.title
                view.description.text = video.snippet.description
                view.pubDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)

                view.image.setImageUrl(video.snippet.thumbnails.high.url, imageLoader)
            }

            override fun onClick(v: View?) {
                // Gardien
                video ?: return

                // Start new activity ;)
                val intent = Intent(context, YoutubeVideoActivity::class.java).apply {
                    putExtra(YoutubeVideoActivity.EXTRA_VIDEO_ID,          video!!.id.videoId)
                    putExtra(YoutubeVideoActivity.EXTRA_VIDEO_TITLE,       video!!.snippet.title)
                    putExtra(YoutubeVideoActivity.EXTRA_VIDEO_DESCRIPTION, video!!.snippet.description)
                    putExtra(YoutubeVideoActivity.EXTRA_VIDEO_IMAGE,       video!!.snippet.thumbnails.high.url)
                }

                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity,
                        UtilPair(view.image,       "video_image"),
                        UtilPair(view.videoTitle,  "video_title"),
                        UtilPair(view.description, "video_description")
                ).toBundle())
            }
        }
    }
}