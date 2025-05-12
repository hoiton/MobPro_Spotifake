package ch.hslu.spotifake.ui.upload

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.spotifake.db.Track
import ch.hslu.spotifake.db.TrackDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val _trackName = MutableStateFlow("")
    val trackName: StateFlow<String> = _trackName

    private val _artistName = MutableStateFlow("")
    val artistName: StateFlow<String> = _artistName

    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName

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
    }

    fun uploadTrack() {
        viewModelScope.launch {
            val name = _trackName.value
            val artist = _artistName.value
            val uri = _selectedFileUri ?: return@launch

            val application = getApplication<Application>()
            val cr = application.contentResolver

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
                0, _trackName.value, _artistName.value, "https://lastfm.freetls.fastly.net/i/u/770x0/0cc48bdf9e22bf52c4d91b9f66873319.jpg", storageFile.path
            )
            val db = TrackDatabase.getDatabase(application)
            db.trackDao().insertTrack(track)

            _trackName.value = ""
            _artistName.value = ""
            _selectedFileName.value = null
            _selectedFileUri = null
        }
    }
}