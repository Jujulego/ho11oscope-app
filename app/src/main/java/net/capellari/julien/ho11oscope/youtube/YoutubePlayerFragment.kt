package net.capellari.julien.ho11oscope.youtube

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerListener

class YoutubePlayerFragment : Fragment(), YouTubePlayerListener {
    // Companion
    companion object {
        // Attributs
        const val TAG = "YoutubePlayerFragment"

        const val ARG_VIDEO_ID = "id"

        // Méthodes
        fun newInstance(videoId: String) : YoutubePlayerFragment {
            val bundle = Bundle()
                    .apply {
                        putString(ARG_VIDEO_ID, videoId)
                    }

            return YoutubePlayerFragment()
                    .apply {
                        arguments = bundle
                    }
        }
    }

    // Attributs
    var player: YouTubePlayer? = null

    // Propriétés
    val videoId : String? get() = arguments?.getString(ARG_VIDEO_ID)

    // Events
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        return YouTubePlayerView(context)
                .apply {
                    initialize( {
                        Log.d(TAG, "initialized")
                        player = it
                        it.addListener(this@YoutubePlayerFragment)
                    }, true)
                }
    }

    override fun onReady() {
        Log.d(TAG, "onReady")
        player?.let { player ->
            videoId?.let {
                Log.d(TAG, it)
                player.loadVideo(it, 0.0F)
            }
        }
    }

    override fun onPlaybackQualityChange(playbackQuality: PlayerConstants.PlaybackQuality) { }
    override fun onVideoDuration(duration: Float) { }
    override fun onCurrentSecond(second: Float) { }
    override fun onVideoLoadedFraction(loadedFraction: Float) { }
    override fun onPlaybackRateChange(playbackRate: PlayerConstants.PlaybackRate) { }
    override fun onVideoId(videoId: String) { }
    override fun onApiChange() { }
    override fun onError(error: PlayerConstants.PlayerError) { }
    override fun onStateChange(state: PlayerConstants.PlayerState) { }
}