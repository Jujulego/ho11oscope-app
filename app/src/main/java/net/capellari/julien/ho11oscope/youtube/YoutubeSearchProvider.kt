package net.capellari.julien.ho11oscope.youtube

import android.content.SearchRecentSuggestionsProvider

class YoutubeSearchProvider : SearchRecentSuggestionsProvider() {
    // Companion
    companion object {
        const val AUTHORITY = "net.capellari.julien.ho11oscope.youtube.YoutubeSearchProvider"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }

    // Constructeur
    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}