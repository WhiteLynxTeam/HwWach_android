package com.whitelynxteam.hwwach.ui.navflow.mainflow.fullimage

import com.whitelynxteam.hwwach.domain.models.Photo

sealed class FullImageScreenAction {
    data object OnBackPressed : FullImageScreenAction()
}

sealed class FullImageScreenEvent {
    data object NavigateBack : FullImageScreenEvent()
}

data class FullImageScreenState(
    val photo: Photo? = null
)
