package net.capellari.julien.ho11oscope.youtube

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerListener

class YoutubePlayerFragment : Fragment(), YouTubePlayerListener {
    // Companion
    companion object {
        // Attributs
        const val TAG = "YoutubePlayerFragment"
        const val ARG_VIDEO_ID = "video"
    }

    // Attributs
    var player: YouTubePlayer? = null

    // Propriétés
    val videoId : String? get() = arguments?.getString(ARG_VIDEO_ID)

    // Events
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return YouTubePlayerView(context)
                .apply {
                    initialize( {
                        player = it
                        it.addListener(this@YoutubePlayerFragment)
                    }, true)
                }
    }

    override fun onReady() {
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