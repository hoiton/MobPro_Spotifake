package ch.hslu.spotifake.ui.upload

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri

    fun onTrackNameChange(newName: String) {
        _trackName.value = newName
    }

    fun onArtistNameChange(newName: String) {
        _artistName.value = newName
    }

    fun onFileSelected(uri: Uri?) {
        _selectedFileUri.value = uri
    }

    fun uploadTrack() {
        viewModelScope.launch {
            val name = _trackName.value
            val artist = _artistName.value
            val uri = _selectedFileUri.value ?: return@launch

            val application = getApplication<Application>()
            val cr = application.contentResolver

            val filesDir = application.filesDir

            val rawName = cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    } else null
                }

            val safeName = rawName
                ?.replace(Regex("""[\\/]+"""), "_")       // no slashes
                ?.takeIf { it.contains('.') }            // must have an extension
                ?: "upload_${System.currentTimeMillis()}.bin"
            val storageFile = File(filesDir, safeName)

            // Copy URI content into local file
            application.contentResolver.openInputStream(uri)?.use { inStream ->
                storageFile.outputStream().use { outStream ->
                    inStream.copyTo(outStream)
                }
            }

            _trackName.value = ""
            _artistName.value = ""
            _selectedFileUri.value = null
        }
    }
}