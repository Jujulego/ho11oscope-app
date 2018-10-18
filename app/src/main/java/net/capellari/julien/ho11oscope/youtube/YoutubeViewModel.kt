package net.capellari.julien.ho11oscope.youtube

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import org.jetbrains.anko.doAsync

class YoutubeViewModel(application: Application): AndroidViewModel(application) {
    // Companion
    companion object {
        const val YOUTUBE_API_KEY = "AIzaSyByzfP9EwXQj4heQc2xu8iw_mAnnw6yFQk"
    }

    // Attributs
    var query: String? = null
    val searchResults = MutableLiveData<SearchListResponse>()

    // Propriétés
    private val youtubeApi
        get() = YouTube.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                HttpRequestInitializer { }
        ).setApplicationName("Ho11oscope").build()

    // Méthodes
    fun search(query: String? = null) : LiveData<SearchListResponse> {
        // Sauvegarde
        query?.let {
            this.query = it
        }

        // Search !
        doAsync {
            val req = youtubeApi.search().list("snippet")
                    .apply {
                        key = YOUTUBE_API_KEY

                        q = (query ?: this@YoutubeViewModel.query)
                        maxResults = 25
                        type = "video"
                    }

            searchResults.postValue(req.execute())
        }

        return searchResults
    }
}