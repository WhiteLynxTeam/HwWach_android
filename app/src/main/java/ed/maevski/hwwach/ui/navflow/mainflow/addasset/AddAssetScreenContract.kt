package ed.maevski.hwwach.ui.navflow.mainflow.addasset

import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.ui.models.Categories

sealed class AddAssetScreenAction {
    data class InputName(val value: String) : AddAssetScreenAction()
    data class InputCategory(val value: Categories) : AddAssetScreenAction()
    data class InputInventoryNumber(val value: String) : AddAssetScreenAction()
    data class InputAddress(val value: String) : AddAssetScreenAction()
    data class InputComment(val value: String) : AddAssetScreenAction()

    data class AddImage(val uri: String) : AddAssetScreenAction()
    data class RemovePhoto(val photo: Photo) : AddAssetScreenAction()

    data object ShowSourceSelector : AddAssetScreenAction()
    data object OpenGallery : AddAssetScreenAction()
    data object OpenCamera : AddAssetScreenAction()

    data class OpenFullImage(val clientId: String) : AddAssetScreenAction()
    data object NavigateBack : AddAssetScreenAction()
    data object Submit : AddAssetScreenAction()
}

sealed class AddAssetScreenEvent {
    data object ShowSuccessMessage : AddAssetScreenEvent()
    data class ShowErrorMessage(val message: String) : AddAssetScreenEvent()
    data object NavigateBack : AddAssetScreenEvent()
    data class NavigateToFullImage(val clientId: String) : AddAssetScreenEvent()
}

data class AddAssetScreenState(
    val name: String = "",
    val category: Categories? = null,
    val inventoryNumber: String = "",
    val address: String = "",
    val comment: String = "",
    val photos: List<Photo> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String = "",
)