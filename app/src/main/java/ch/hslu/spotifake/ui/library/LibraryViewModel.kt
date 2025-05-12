package ch.hslu.spotifake.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ch.hslu.spotifake.db.Track
import ch.hslu.spotifake.db.TrackDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.asLiveData

@HiltViewModel
class LibraryViewModel @Inject constructor(
    trackDao: TrackDao
) : ViewModel() {
    val allItems: LiveData<List<Track>> = trackDao.getAll().asLiveData()
}