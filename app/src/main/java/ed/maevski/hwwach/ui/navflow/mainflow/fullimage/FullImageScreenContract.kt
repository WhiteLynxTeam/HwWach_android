package ed.maevski.hwwach.ui.navflow.mainflow.fullimage

import ed.maevski.hwwach.domain.models.Photo

sealed class FullImageScreenAction {
    data object OnBackPressed : FullImageScreenAction()
}

sealed class FullImageScreenEvent {
    data object NavigateBack : FullImageScreenEvent()
}

data class FullImageScreenState(
    val photo: Photo? = null
)
