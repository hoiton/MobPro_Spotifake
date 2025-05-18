package ch.hslu.spotifake.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM track")
    fun getAllTracks(): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossReference)

    @Transaction
    @Query("SELECT * FROM Playlist")
    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistId = :playlistId")
    fun getPlaylistWithTracks(playlistId: Int): Flow<PlaylistWithTracks>

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("DELETE FROM PlaylistTrackCrossReference WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Int, trackId: Int)
}