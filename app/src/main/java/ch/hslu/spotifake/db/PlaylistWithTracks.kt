package ch.hslu.spotifake.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithTracks(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "trackId",
        associateBy = Junction(PlaylistTrackCrossReference::class)
    )
    val tracks: List<Track>
)