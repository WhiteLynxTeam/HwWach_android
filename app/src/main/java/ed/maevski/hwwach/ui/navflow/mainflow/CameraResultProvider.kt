package ed.maevski.hwwach.ui.navflow.mainflow

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Снимки камеры — тому экрану, который её открыл. Раньше снимок получали все живые экраны сразу
 * (вкладка «Галерея» и форма актива), и он сохранялся дважды
 */
@ActivityRetainedScoped
class CameraResultProvider @Inject constructor() {

    private data class Shot(val requester: String, val uri: String)

    @Volatile
    private var requester: String = ""

    private val shots = MutableSharedFlow<Shot>(extraBufferCapacity = 8)

    /** Экран открывает камеру: снимки, сделанные до следующего request, получит только он */
    fun request(requester: String) {
        this.requester = requester
    }

    fun resultsFor(requester: String): Flow<String> =
        shots.filter { it.requester == requester }.map { it.uri }

    fun onPhotoCaptured(uri: String) {
        shots.tryEmit(Shot(requester, uri))
    }
}
