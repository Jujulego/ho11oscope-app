package net.capellari.julien.ho11oscope.youtube.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface VideoDAO {
    // Requêtes
    @Query("select * from video")
    fun getAll(): List<Video>
}