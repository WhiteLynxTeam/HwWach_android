package ed.maevski.hwwach.ui.navflow.mainflow.assetdetail

import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetServerInfo
import ed.maevski.hwwach.domain.models.PersonBrief
import ed.maevski.hwwach.domain.models.Photo

// Диалоги карточки: передать МОЛ, назначить работающего, сообщить о неисправности
enum class AssetDialog { TRANSFER_MOL, ADD_USER, REPORT_FAULT }

sealed class AssetDetailScreenAction {
    data object OnBackPressed : AssetDetailScreenAction()
    data object EditClicked : AssetDetailScreenAction()
    data object DeleteClicked : AssetDetailScreenAction()
    data class PhotoClicked(val clientId: String) : AssetDetailScreenAction()

    data class OpenDialog(val dialog: AssetDialog) : AssetDetailScreenAction()
    data object CloseDialog : AssetDetailScreenAction()
    data class SelectPerson(val personId: String) : AssetDetailScreenAction()
    data class InputText(val text: String) : AssetDetailScreenAction()
    data object ConfirmDialog : AssetDetailScreenAction()
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
    val errorMessage: String = "",

    // с сервера: учётный статус, состояние, за кем числится, кто работает
    val serverInfo: AssetServerInfo? = null,
    val persons: Map<String, PersonBrief> = emptyMap(),
    val myPersonId: String? = null,
    // админ (asset_roles.assign) или МОЛ этого актива
    val canManageRoles: Boolean = false,

    val dialog: AssetDialog? = null,
    val selectedPersonId: String? = null,
    val dialogText: String = "",
    val isSaving: Boolean = false,
    val infoMessage: String = "",
) {
    fun personName(id: String?): String = id?.let { persons[it]?.fullName } ?: "—"
}
