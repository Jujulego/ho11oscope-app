package net.capellari.julien.ho11oscope.youtube

import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.yt_video_activity.*
import net.capellari.julien.ho11oscope.R

class YoutubeVideoActivity : AppCompatActivity() {
    // Companion
    companion object {
        const val EXTRA_VIDEO_ID          = "net.capellari.julien.ho11oscope.EXTRA_VIDEO_ID"
        const val EXTRA_VIDEO_TITLE       = "net.capellari.julien.ho11oscope.EXTRA_VIDEO_TITLE"
        const val EXTRA_VIDEO_DESCRIPTION = "net.capellari.julien.ho11oscope.EXTRA_VIDEO_DESCRIPTION"
        const val EXTRA_VIDEO_IMAGE       = "net.capellari.julien.ho11oscope.EXTRA_VIDEO_IMAGE"
    }

    // Attributs
    private lateinit var requestQueue: RequestQueue
    private lateinit var imageLoader: ImageLoader

    // Event
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setupVolley()

        // Inflate layout
        setContentView(R.layout.yt_video_activity)

        // Parse intent
        videoTitle.text  = intent.getStringExtra(EXTRA_VIDEO_TITLE) ?: "Title"
        description.text = intent.getStringExtra(EXTRA_VIDEO_DESCRIPTION) ?: "Description"

        val url = intent.getStringExtra(EXTRA_VIDEO_IMAGE)
        if (url != null) {
            image.setImageUrl(url, imageLoader)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop Volley
        requestQueue.stop()
    }

    // Méthodes
    private fun setupVolley() {
        // Prepare requestQueue
        requestQueue = Volley.newRequestQueue(this)

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
}
