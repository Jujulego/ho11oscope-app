package net.capellari.julien.ho11oscope

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class YoutubeViewModel: ViewModel() {
    // Companion
    companion object {
        const val YOUTUBE_API_KEY = "AIzaSyByzfP9EwXQj4heQc2xu8iw_mAnnw6yFQk"
    }

    // Attributs
    val searchResults = MutableLiveData<SearchListResponse>()

    // Propriétés
    private val youtubeApi
        get() = YouTube.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                HttpRequestInitializer { }
        ).setApplicationName("Ho11oscope").build()

    // Méthodes
    fun search(query: String) : LiveData<SearchListResponse> {
        doAsync {
            // Search !
            val req = youtubeApi.search().list("snippet")
            req.apply {
                key = YOUTUBE_API_KEY

                q = query
                maxResults = 25
                type = "video"
            }

            searchResults.postValue(req.execute())
        }

        return searchResults
    }
}