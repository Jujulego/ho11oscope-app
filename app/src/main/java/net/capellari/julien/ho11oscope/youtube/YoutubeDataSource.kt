package net.capellari.julien.ho11oscope.youtube

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.google.api.services.youtube.model.SearchResult

class YoutubeDataSource(val ytModel: YoutubeViewModel) : PageKeyedDataSource<String, VideoResult>() {
    // Classes
    class Factory(val ytModel: YoutubeViewModel) : DataSource.Factory<String, VideoResult>() {
        // Méthodes
        override fun create(): DataSource<String, VideoResult> {
            return YoutubeDataSource(ytModel)
        }
    }

    // Méthodes
    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, VideoResult>) {
        // Préparation de la requête
        val req = ytModel.youtubeApi.search().list("snippet")

        req.key  = YoutubeViewModel.YOUTUBE_API_KEY

        req.type = "video"
        ytModel.query?.let { req.q = it }
        req.maxResults = minOf(params.requestedLoadSize, 50).toLong()

        // Recherche
        ytModel.isLoading.postValue(true)
        val results = req.execute()

        callback.onResult(results.items.map { VideoResult(it) }, results.prevPageToken, results.nextPageToken)
        ytModel.isLoading.postValue(false)
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, VideoResult>) {
        // Préparation de la requête
        val req = ytModel.youtubeApi.search().list("snippet")

        req.key = YoutubeViewModel.YOUTUBE_API_KEY
        req.pageToken  = params.key
        req.maxResults = minOf(params.requestedLoadSize, 50).toLong()

        // Recherche
        ytModel.isLoading.postValue(true)
        val results = req.execute()

        callback.onResult(results.items.map { VideoResult(it) }, results.nextPageToken)
        ytModel.isLoading.postValue(true)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, VideoResult>) {
        // Préparation de la requête
        val req = ytModel.youtubeApi.search().list("snippet")

        req.key = YoutubeViewModel.YOUTUBE_API_KEY
        req.pageToken  = params.key
        req.maxResults = minOf(params.requestedLoadSize, 50).toLong()

        // Recherche
        ytModel.isLoading.postValue(true)
        val results = req.execute()

        callback.onResult(results.items.map { VideoResult(it) }, results.prevPageToken)
        ytModel.isLoading.postValue(false)
    }
}