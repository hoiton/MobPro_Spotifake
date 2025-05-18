package ch.hslu.spotifake.db

import androidx.room.Entity

@Entity(primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossReference(
    val playlistId: Int,
    val trackId: Int
)