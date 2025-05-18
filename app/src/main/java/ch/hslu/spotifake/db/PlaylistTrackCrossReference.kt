package ch.hslu.spotifake.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Track::class,
            parentColumns = ["trackId"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ])
data class PlaylistTrackCrossReference(
    val playlistId: Int,
    val trackId: Int
)