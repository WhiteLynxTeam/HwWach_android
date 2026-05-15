package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.assets

sealed class AssetsScreenAction {
    data object AddAssetClicked : AssetsScreenAction()
}

sealed class AssetsScreenEvent {
    data object ShowSuccessMessage : AssetsScreenEvent()
    data class ShowErrorMessage(val message: String) : AssetsScreenEvent()
    data object NavigateToAddAsset : AssetsScreenEvent()

}

data class AssetsScreenState(
    val errorMessage: String = "",
)