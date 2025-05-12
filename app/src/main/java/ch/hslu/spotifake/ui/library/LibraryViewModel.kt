package ch.hslu.spotifake.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ch.hslu.spotifake.db.Track
import ch.hslu.spotifake.db.TrackDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.asLiveData

@HiltViewModel
class LibraryViewModel @Inject constructor(
    application: Application,
    private val trackDao: TrackDao
) : AndroidViewModel(application) {

    val allItems: LiveData<List<Track>> = trackDao.getAll().asLiveData()
}