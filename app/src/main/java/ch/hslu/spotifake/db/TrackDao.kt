package ch.hslu.spotifake.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAll(): List<Track>

    @Query("SELECT * FROM track WHERE uid IN (:trackIds)")
    fun loadAllByIds(trackIds: IntArray): List<Track>

    @Insert
    fun insertTrack(track: Track)

    @Insert
    fun insertAll(vararg tracks: Track)

    @Delete
    fun delete(track: Track)
}