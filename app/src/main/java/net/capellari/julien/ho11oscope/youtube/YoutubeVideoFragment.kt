package net.capellari.julien.ho11oscope.youtube

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.api.services.youtube.model.SearchResult
import kotlinx.android.synthetic.main.youtube_video_fragment.view.*
import net.capellari.julien.ho11oscope.PlayerActivity
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.ho11oscope.RequestManager

class YoutubeVideoFragment : androidx.fragment.app.Fragment() {
    // Companion
    companion object {
        // Attributs
        const val ARGS_VIDEO_ID          = "id"
        const val ARGS_VIDEO_TITLE       = "title"
        const val ARGS_VIDEO_DESCRIPTION = "description"
        const val ARGS_VIDEO_IMAGE_URL   = "imageUrl"

        // Méthodes
        fun newInstance(video: SearchResult): YoutubeVideoFragment {
            val bundle = Bundle()
            bundle.apply {
                putString(ARGS_VIDEO_ID,          video.id.videoId)
                putString(ARGS_VIDEO_TITLE,       video.snippet.title)
                putString(ARGS_VIDEO_DESCRIPTION, video.snippet.description)
                putString(ARGS_VIDEO_IMAGE_URL,   video.snippet.thumbnails.high.url)
            }

            return YoutubeVideoFragment()
                    .apply { arguments = bundle }
        }
    }

    // Attributs
    private lateinit var requestManager: RequestManager
    private var youtubeViewModel: YoutubeViewModel? = null

    // Propriétés
    private val videoId: String?
        get() = arguments?.getString(ARGS_VIDEO_ID)

    private val videoTitle: String?
        get() = arguments?.getString(ARGS_VIDEO_TITLE)

    private val videoDescription: String?
        get() = arguments?.getString(ARGS_VIDEO_DESCRIPTION)

    private val videoImageUrl: String?
        get() = arguments?.getString(ARGS_VIDEO_IMAGE_URL)

    // Events
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Get requestManager
        requestManager = RequestManager.getInstance(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get ViewModel
        youtubeViewModel = activity?.run {
            ViewModelProviders.of(this).get(YoutubeViewModel::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate !
        val view = inflater.inflate(R.layout.youtube_video_fragment, container, false)

        // Events
        view.playButton.setOnClickListener {
            videoId?.let { id ->
                val intent = Intent(context, PlayerActivity::class.java)
                PlayerActivity.fillIntent(intent, PlayerActivity.Type.YOUTUBE, id)

                startActivity(intent)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    // Méthodes
    private fun updateUI() {
        view?.let { view ->
            // Filling views
            view.videoTitle.text  = videoTitle ?: "Titre"
            view.description.text = videoDescription ?: "Description"

            videoImageUrl?.let { view.image.setImageUrl(it, requestManager.imageLoader) }
        }
    }
}