package ed.maevski.hwwach.ui.navflow.mainflow.camera

import androidx.camera.core.CameraSelector

sealed class CameraAction {
    data class OnPhotoCaptured(val uri: String) : CameraAction()
    data object OnBackPressed : CameraAction()
    data object ToggleLensFacing : CameraAction()
}

sealed class CameraEvent {
    // PhotoCaptured удален: URI передается напрямую через CameraResultViewModel (Activity-scope)
    data object NavigateBack : CameraEvent()
}

data class CameraState(
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK
)
