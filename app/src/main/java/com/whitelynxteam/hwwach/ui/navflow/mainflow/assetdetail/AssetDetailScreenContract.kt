package com.whitelynxteam.hwwach.ui.navflow.mainflow.assetdetail

import com.whitelynxteam.hwwach.domain.models.Asset
import com.whitelynxteam.hwwach.domain.models.Photo

sealed class AssetDetailScreenAction {
    data object OnBackPressed : AssetDetailScreenAction()
    data object EditClicked : AssetDetailScreenAction()
    data object DeleteClicked : AssetDetailScreenAction()
    data class PhotoClicked(val clientId: String) : AssetDetailScreenAction()
}

sealed class AssetDetailScreenEvent {
    data object NavigateBack : AssetDetailScreenEvent()
    data object NavigateToEdit : AssetDetailScreenEvent() // For future use
    data class NavigateToFullImage(val clientId: String) : AssetDetailScreenEvent()
    data object ShowDeleteConfirmation : AssetDetailScreenEvent() // For future use
    data class ShowErrorMessage(val message: String) : AssetDetailScreenEvent()
}

data class AssetDetailScreenState(
    val asset: Asset? = null,
    val photos: List<Photo> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String = ""
)
