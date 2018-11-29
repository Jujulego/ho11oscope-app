package net.capellari.julien.ho11oscope.youtube

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.SearchResult
import net.capellari.julien.ho11oscope.RequestManager
import org.jetbrains.anko.doAsync

class YoutubeViewModel(app: Application): AndroidViewModel(app) {
    // Companion
    companion object {
        const val YOUTUBE_API_KEY = "AIzaSyByzfP9EwXQj4heQc2xu8iw_mAnnw6yFQk"
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
                HttpRequestInitializer { }
        ).setApplicationName("Ho11oscope").build()
    }

    // Méthodes
    fun invalidate() {
        videos.value?.dataSource?.invalidate()
    }
}