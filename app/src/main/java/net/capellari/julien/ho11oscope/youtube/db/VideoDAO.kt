package net.capellari.julien.ho11oscope.youtube.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface VideoDAO {
    // Requêtes
    @Query("select * from video")
    fun getAll(): List<Video>
}