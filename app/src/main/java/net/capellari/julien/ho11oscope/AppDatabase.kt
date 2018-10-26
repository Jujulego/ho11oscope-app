package net.capellari.julien.ho11oscope

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import net.capellari.julien.ho11oscope.youtube.db.Video
import net.capellari.julien.ho11oscope.youtube.db.VideoDAO

@Database(
        entities = [Video::class],
        version = Video.VERSION
)
abstract class AppDatabase : RoomDatabase() {
    // Companion
    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java, "database.db").build().also { instance = it }
        }
    }

    // DAOs
    abstract fun videoDato(): VideoDAO
}