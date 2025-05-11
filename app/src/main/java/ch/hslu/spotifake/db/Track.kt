package ch.hslu.spotifake.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class Track (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "track_name") val trackName: String,
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "cover") val cover: String?,
    @ColumnInfo(name = "file_uri") val fileURI: String
)