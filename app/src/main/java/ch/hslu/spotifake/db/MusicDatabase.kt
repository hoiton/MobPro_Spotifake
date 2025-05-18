package ch.hslu.spotifake.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

@Database(entities = [Track::class, Playlist::class, PlaylistTrackCrossReference::class],
    version = 1)
abstract class MusicDatabase: RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        private const val DB_NAME = "music-database"
        private var INSTANCE: MusicDatabase? = null
        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: buildDatabase(context).also {
                INSTANCE = it
            }
        }

        private fun buildDatabase(context: Context): MusicDatabase {
            val ioDispatcherExecutor = Dispatchers.IO.asExecutor()
            return Room
                .databaseBuilder(
                    context,
                    MusicDatabase::class.java,
                    DB_NAME
                )
                .setQueryExecutor(ioDispatcherExecutor)
                .setTransactionExecutor(ioDispatcherExecutor)
                .allowMainThreadQueries()
                .build()
        }
    }
}