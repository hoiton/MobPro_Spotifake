package ch.hslu.spotifake.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class Track (
    @PrimaryKey(autoGenerate = true) val trackId: Int,
    @ColumnInfo(name = "track_name") val trackName: String,
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "cover") val cover: String?,
    @ColumnInfo(name = "file_uri") val fileURI: String
) {
    companion object {
        const val DEFAULT_COVER_URL = "https://cdn.iconscout.com/icon/free/png-256/free-data-not-found-icon-download-in-svg-png-gif-file-formats--drive-full-storage-empty-state-pack-miscellaneous-icons-1662569.png"
    }
}