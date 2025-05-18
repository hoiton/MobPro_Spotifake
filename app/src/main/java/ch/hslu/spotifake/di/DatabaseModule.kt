package ch.hslu.spotifake.di

import android.content.Context
import androidx.room.Room
import ch.hslu.spotifake.db.MusicDatabase
import ch.hslu.spotifake.db.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music-database"
        ).build()
    }

    @Provides
    fun provideMyDao(database: MusicDatabase): PlaylistDao {
        return database.playlistDao()
    }
}