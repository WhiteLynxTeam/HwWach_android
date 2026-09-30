package ed.maevski.hwwach.ui.navflow.mainflow.addasset

import ed.maevski.hwwach.domain.models.Category
import ed.maevski.hwwach.domain.models.Photo

sealed class AddAssetScreenAction {
    data class InputName(val value: String) : AddAssetScreenAction()
    data class InputCategoryText(val value: String) : AddAssetScreenAction()
    data class SelectCategory(val category: Category) : AddAssetScreenAction()
    data object CreateCustomCategory : AddAssetScreenAction()
    data class SetCategoryDropdownVisible(val visible: Boolean) : AddAssetScreenAction()
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
    val categoryText: String = "",
    val selectedCategory: Category? = null,
    val categorySuggestions: List<Category> = emptyList(),
    val isCategoryDropdownVisible: Boolean = false,
    val isSearchingCategories: Boolean = false,
    val inventoryNumber: String = "",
    val address: String = "",
    val comment: String = "",
    val photos: List<Photo> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String = "",
) {
    val canAddCustomCategory: Boolean
        get() {
            val trimmed = categoryText.trim()
            if (trimmed.length < 2) return false
            return categorySuggestions.none { it.name.equals(trimmed, ignoreCase = true) }
        }
}