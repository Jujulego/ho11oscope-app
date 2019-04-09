package net.capellari.julien.ho11oscope.youtube

import com.google.api.services.youtube.model.SearchResult
import net.capellari.julien.utils.DiffItem
import java.util.*

class VideoResult(val result: SearchResult): DiffItem<VideoResult> {
    // Attributs
    val id:          String get() = result.id.videoId
    val title:       String get() = result.snippet.title
    val publishedAt: Date   get() = Date(result.snippet.publishedAt.value)
    val description: String get() = result.snippet.description
    val imageUrl:    String get() = result.snippet.thumbnails.high.url

    // Méthodes
    override fun isSameItem(other: VideoResult)
            = (id == other.id)

    override fun hasSameContent(other: VideoResult)
            = (title == other.title)
            && (publishedAt == other.publishedAt)
            && (description == other.title)
            && (imageUrl == other.imageUrl)
}