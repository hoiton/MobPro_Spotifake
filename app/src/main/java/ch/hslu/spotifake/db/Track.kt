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
//        const val DEFAULT_COVER_URL = "https://f4.bcbits.com/img/a0768625472_16.jpg"
        const val DEFAULT_COVER_URL = "https://lastfm.freetls.fastly.net/i/u/770x0/0cc48bdf9e22bf52c4d91b9f66873319.jpg"
    }
}