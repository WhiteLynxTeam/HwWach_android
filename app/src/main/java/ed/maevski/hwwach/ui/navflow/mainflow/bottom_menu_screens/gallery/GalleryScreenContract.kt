package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.gallery

import ed.maevski.hwwach.domain.models.Photo
import ed.maevski.hwwach.domain.models.UploadStatusEnum

sealed class GalleryScreenAction {
    data class AddImage(val uri: String) : GalleryScreenAction()
    data class RemovePhoto(val photo: Photo) : GalleryScreenAction()

    data object ShowSourceSelector : GalleryScreenAction()
    data object OpenGallery : GalleryScreenAction()
    data object OpenCamera : GalleryScreenAction()
    data object SyncPendingPhotos : GalleryScreenAction()
    data object CancelSync : GalleryScreenAction()

    data class FilterByStatus(val status: UploadStatusEnum?) : GalleryScreenAction()

    data class OpenFullImage(val clientId: String) : GalleryScreenAction()

    data object OnSubmitClicked : GalleryScreenAction()
}

sealed class GalleryScreenEvent {
    data object ShowSuccessMessage : GalleryScreenEvent()
    data class ShowErrorMessage(val message: String) : GalleryScreenEvent()
    data class NavigateToFullImage(val clientId: String) : GalleryScreenEvent()
}

data class GalleryScreenState(
    val photos: List<Photo> = emptyList(),

    val errorMessage: String = "",

    val isServerSyncing: Boolean = false, // Синхронизация с сервером при старте (reset/resume/retry)

    val isUploading: Boolean = false, // Загрузка фото на сервер (upload)
    val uploadError: String = "",

    val statusFilter: UploadStatusEnum? = null,

    val canUpload: Boolean = false, // Есть PENDING фото — показать кнопку загрузки
)