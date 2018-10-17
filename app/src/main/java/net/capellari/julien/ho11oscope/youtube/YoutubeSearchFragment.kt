package net.capellari.julien.ho11oscope.youtube

import android.app.ActivityOptions
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.LruCache
import android.util.Pair as UtilPair
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.SearchResult
import kotlinx.android.synthetic.main.youtube_search_fragment.*
import kotlinx.android.synthetic.main.youtube_search_result.view.*
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.inflate
import java.text.SimpleDateFormat
import java.util.*

class YoutubeSearchFragment : Fragment() {
    // Companion
    companion object {
        const val TAG = "YoutubeSearchFragment"
    }

    // Attributs
    private val videoAdapter = VideoAdapter()
    private var youtubeViewModel: YoutubeViewModel? = null

    private lateinit var requestQueue: RequestQueue
    private lateinit var imageLoader: ImageLoader

    // Propriétés
    private val searchRecentSuggestions
        get() = SearchRecentSuggestions(context, YoutubeSearchProvider.AUTHORITY, YoutubeSearchProvider.MODE)

    // Events
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get ViewModel
        youtubeViewModel = activity?.run {
            ViewModelProviders.of(this).get(YoutubeViewModel::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.youtube_search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Setup recycler view
        results.apply {
            layoutManager = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> GridLayoutManager(context, 2)
                else -> LinearLayoutManager(context)
            }
            adapter = videoAdapter
        }

        youtubeViewModel?.run {
            // Observe search results
            searchResults.observe(this@YoutubeSearchFragment,
                    androidx.lifecycle.Observer { results ->
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

    override fun onDetach() {
        super.onDetach()

        // Stop RequestQueue
        requestQueue.stop()
    }

    // Fonctions
    fun search(query: String? = null) {
        // Sauvegarde
        searchRecentSuggestions.saveRecentQuery(query, null)

        // Start searching
        youtubeViewModel?.run {
            this.search(query)
            swipeRefresh.isRefreshing = true
        }
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
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
            return VideoHolder(parent.inflate(R.layout.youtube_search_result, false))
        }

        override fun getItemCount(): Int = videos?.items?.size ?: 0

        override fun onBindViewHolder(holder: VideoHolder, position: Int) {
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
                with(view) {
                    videoTitle.text = video.snippet.title
                    description.text = video.snippet.description
                    pubDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)

                    image.setImageUrl(video.snippet.thumbnails.high.url, imageLoader)
                }
            }

            override fun onClick(v: View?) {
                // Gardien
                video?.also {
                    // Start new activity ;)
                    val intent = Intent(context, YoutubeVideoActivity::class.java).apply {
                        putExtra(YoutubeVideoActivity.EXTRA_VIDEO_ID,          it.id.videoId)
                        putExtra(YoutubeVideoActivity.EXTRA_VIDEO_TITLE,       it.snippet.title)
                        putExtra(YoutubeVideoActivity.EXTRA_VIDEO_DESCRIPTION, it.snippet.description)
                        putExtra(YoutubeVideoActivity.EXTRA_VIDEO_IMAGE,       it.snippet.thumbnails.high.url)
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
}