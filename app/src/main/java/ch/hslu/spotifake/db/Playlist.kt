package ch.hslu.spotifake.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist (
    @PrimaryKey(autoGenerate = true) val playlistId: Int,
    @ColumnInfo(name = "playlist_name") val playlistName: String
)