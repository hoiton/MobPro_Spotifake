package ch.hslu.spotifake.di

import android.content.Context
import androidx.room.Room
import ch.hslu.spotifake.db.TrackDao
import ch.hslu.spotifake.db.TrackDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): TrackDatabase {
        return Room.databaseBuilder(
            context,
            TrackDatabase::class.java,
            "track-database"
        ).build()
    }

    @Provides
    fun provideMyDao(database: TrackDatabase): TrackDao {
        return database.trackDao()
    }
}