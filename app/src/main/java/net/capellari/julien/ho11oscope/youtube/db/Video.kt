package net.capellari.julien.ho11oscope.youtube.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video")
data class Video(@PrimaryKey var id: String,
        var title: String = "",
        var description: String = "",
        var image: String = "") {

    // Companion
    companion object {
        const val VERSION = 1
    }

    // Constructeur alternatif
    constructor():this("","","","")
}