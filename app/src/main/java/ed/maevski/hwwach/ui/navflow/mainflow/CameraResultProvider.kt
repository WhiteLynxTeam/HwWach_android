package ed.maevski.hwwach.ui.navflow.mainflow

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@ActivityRetainedScoped
class CameraResultProvider @Inject constructor() {

    private val _photoUri = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val photoUri: SharedFlow<String> = _photoUri.asSharedFlow()

    fun onPhotoCaptured(uri: String) {
        _photoUri.tryEmit(uri)
    }
}
