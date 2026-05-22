package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.assets

sealed class AssetsScreenAction {
    data object AddAssetClicked : AssetsScreenAction()
    data object LoadAssets : AssetsScreenAction()
    data class AssetClicked(val clientId: String) : AssetsScreenAction()
}

sealed class AssetsScreenEvent {
    data object ShowSuccessMessage : AssetsScreenEvent()
    data class ShowErrorMessage(val message: String) : AssetsScreenEvent()
    data object NavigateToAddAsset : AssetsScreenEvent()
    data class NavigateToAssetDetail(val clientId: String) : AssetsScreenEvent()
}

data class AssetsScreenState(
    val assets: List<com.whitelynxteam.hwwach.domain.models.Asset> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
)