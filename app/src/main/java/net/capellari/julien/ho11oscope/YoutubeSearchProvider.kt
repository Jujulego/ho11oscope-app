package net.capellari.julien.ho11oscope

import android.content.SearchRecentSuggestionsProvider

class YoutubeSearchProvider : SearchRecentSuggestionsProvider() {
    // Companion
    companion object {
        const val AUTHORITY = "net.capellari.julien.ho11oscope.YoutubeSearchProvider"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }

    // Constructeur
    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}