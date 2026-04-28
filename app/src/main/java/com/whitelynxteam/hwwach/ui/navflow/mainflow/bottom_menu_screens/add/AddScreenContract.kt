package com.whitelynxteam.hwwach.ui.navflow.mainflow.bottom_menu_screens.add

import com.whitelynxteam.hwwach.domain.models.Photo
import com.whitelynxteam.hwwach.domain.models.PhotoUploadStatusEnum

sealed class AddScreenAction {
    data class SwitchMode(val mode: AddScreenTab) : AddScreenAction()

    data class InputName(val value: String) : AddScreenAction()
    data class InputCategory(val value: Categories) : AddScreenAction()
    data class InputInventoryNumber(val value: String) : AddScreenAction()
    data class InputAddress(val value: String) : AddScreenAction()
    data class InputComment(val value: String) : AddScreenAction()

    data class AddImage(val uri: String) : AddScreenAction()
    data class RemovePhoto(val photo: Photo) : AddScreenAction()

    data object OpenImagePicker : AddScreenAction()
    data object OpenCamera : AddScreenAction()
    data object SyncPendingPhotos : AddScreenAction()
    data object CancelSync : AddScreenAction()

    data class FilterByStatus(val status: PhotoUploadStatusEnum?) : AddScreenAction()

    data object OnSubmitClicked : AddScreenAction()
}

sealed class AddScreenEvent {
    data object ShowSuccessMessage : AddScreenEvent()
    data class ShowErrorMessage(val message: String) : AddScreenEvent()
    data object NavigateBack : AddScreenEvent()
    data object OpenImagePicker : AddScreenEvent()
    data object OpenCamera : AddScreenEvent()
}

data class AddScreenState(
    val currentMode: AddScreenTab = AddScreenTab.Gallery,
    val name: String = "",
    val category: Categories? = null,
    val inventoryNumber: String = "",
    val address: String = "",
    val comment: String = "",
    val photos: List<Photo> = emptyList(),
    val errorMessage: String = "",
    val isInitializing: Boolean = true, // Сервисные операции при старте (reset/resume/retry)
    val isSyncing: Boolean = false,
    val syncError: String = "",
    val statusFilter: PhotoUploadStatusEnum? = null,
)