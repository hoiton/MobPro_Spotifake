package ch.hslu.spotifake.ui.upload

import android.app.Application
import android.net.Uri
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

            val filesDir = application.filesDir
            val ext = uri.path?.substringAfterLast('.')
            val storageFile = File(filesDir, "upload_${System.currentTimeMillis()}.$ext")

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