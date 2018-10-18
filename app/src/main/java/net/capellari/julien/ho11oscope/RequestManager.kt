package net.capellari.julien.ho11oscope

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.Request
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

class RequestManager private constructor(context: Context) {
    // Companion
    companion object {
        // Attributs
        @Volatile
        private var instance: RequestManager? = null

        // Méthodes
        fun getInstance(context: Context): RequestManager = instance ?: synchronized(this) {
            instance ?: RequestManager(context).also { instance = it }
        }
    }

    // Attributs
    val requestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    val imageLoader by lazy {
        ImageLoader(requestQueue, object : ImageLoader.ImageCache {
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

    // Méthodes
    fun <T> addRequest(req: Request<T>) {
        requestQueue.add(req)
    }
}