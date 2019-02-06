package net.capellari.julien.ho11oscope.youtube

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchResult
import net.capellari.julien.ho11oscope.RequestManager
import net.capellari.julien.utils.getSHA1Cert

class YoutubeViewModel(app: Application): AndroidViewModel(app) {
    // Companion
    companion object {
        const val YOUTUBE_API_KEY = "AIzaSyBtAtm2vpdYXrHxhoS3i8zc0N9VJuO2GxI"
    }

    // Attributs
    private val dataSourceFactory = YoutubeDataSource.Factory(this)

    val requestManager = RequestManager.getInstance(app)
    val videos: LiveData<PagedList<SearchResult>> = LivePagedListBuilder(dataSourceFactory, 20).build()

    var query: String? = null
    val isLoading = MutableLiveData<Boolean>()

    // Propriétés
    val youtubeApi by lazy {
        YouTube.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                HttpRequestInitializer {
                    it.headers.set("X-Android-Package", getApplication<Application>().packageName)
                    it.headers.set("X-Android-Cert",    getApplication<Application>().getSHA1Cert())
                }
        ).setApplicationName("Ho11oscope").build()
    }

    // Méthodes
    fun invalidate() {
        videos.value?.dataSource?.invalidate()
    }
}