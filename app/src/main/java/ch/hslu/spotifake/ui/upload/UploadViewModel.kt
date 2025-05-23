package ch.hslu.spotifake.ui.upload

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.spotifake.business.LastFmService
import ch.hslu.spotifake.db.Track
import ch.hslu.spotifake.db.MusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    application: Application,
    private val lastFm: LastFmService
) : AndroidViewModel(application) {
    private val _trackName = MutableStateFlow("")
    val trackName: StateFlow<String> = _trackName

    private val _artistName = MutableStateFlow("")
    val artistName: StateFlow<String> = _artistName

    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName

    private val _selectedAlbumArtUrl = MutableStateFlow<String?>(null)
    val selectedAlbumArtUrl: StateFlow<String> = _selectedAlbumArtUrl
        .map { it ?: Track.DEFAULT_COVER_URL }
        .stateIn(
            scope       = viewModelScope,
            started     = SharingStarted.WhileSubscribed(5_000),
            initialValue = Track.DEFAULT_COVER_URL
        )

    private var _selectedFileUri: Uri? = null

    fun onTrackNameChange(newName: String) {
        _trackName.value = newName
    }

    fun onArtistNameChange(newName: String) {
        _artistName.value = newName
    }

    fun onFileSelected(uri: Uri?) {
        _selectedFileUri = uri
        if (uri == null) {
            _selectedFileName.value = null
            return
        }
        val context = getApplication<Application>()

        _selectedFileName.value = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
        } catch (e: IllegalArgumentException) {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
            }
        }

        _trackName.value =  retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty()
        _artistName.value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty()

        if (_trackName.value.isNotEmpty() && _artistName.value.isNotEmpty()) {
            viewModelScope.launch {
                val imageUrl = lastFm.getLargestAlbumArtUrl(_artistName.value, _trackName.value)
                _selectedAlbumArtUrl.value = imageUrl
            }
        }
    }

    fun saveTrack(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val uri = _selectedFileUri ?: return@launch

            val application = getApplication<Application>()
            val filesDir = application.filesDir

            val safeName = _selectedFileName.value
                ?.replace(Regex("""[\\/]+"""), "_")
                ?.takeIf { it.contains('.') }
                ?: "upload_${System.currentTimeMillis()}.bin"
            val storageFile = File(filesDir, safeName)

            // Copy URI content into local file
            application.contentResolver.openInputStream(uri)?.use { inStream ->
                storageFile.outputStream().use { outStream ->
                    inStream.copyTo(outStream)
                }
            }

            val track = Track(
                0, _trackName.value, _artistName.value, _selectedAlbumArtUrl.value, storageFile.path
            )
            val db = MusicDatabase.getDatabase(application)
            db.playlistDao().insertTrack(track)

            _trackName.value = ""
            _artistName.value = ""
            _selectedFileName.value = null
            _selectedFileUri = null
            onSuccess()
        }
    }
}