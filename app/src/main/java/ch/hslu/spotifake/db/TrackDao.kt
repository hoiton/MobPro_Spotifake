package ch.hslu.spotifake.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAll(): Flow<List<Track>>

    @Query("SELECT * FROM track WHERE trackId IN (:trackIds)")
    fun loadAllByIds(trackIds: IntArray): List<Track>

    @Insert
    fun insertTrack(track: Track)

    @Insert
    fun insertAll(vararg tracks: Track)

    @Delete
    fun delete(track: Track)
}