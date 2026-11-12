package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.assets

import ed.maevski.hwwach.domain.models.Asset

sealed class AssetsScreenAction {
    data object AddAssetClicked : AssetsScreenAction()
    data object LoadAssets : AssetsScreenAction()
    data class AssetClicked(val clientId: String) : AssetsScreenAction()
    data class Search(val query: String) : AssetsScreenAction()
}

sealed class AssetsScreenEvent {
    data object ShowSuccessMessage : AssetsScreenEvent()
    data class ShowErrorMessage(val message: String) : AssetsScreenEvent()
    data object NavigateToAddAsset : AssetsScreenEvent()
    data class NavigateToAssetDetail(val clientId: String) : AssetsScreenEvent()
}

data class AssetsScreenState(
    // все активы с телефона: отправленные и ещё ждущие отправки
    val allAssets: List<Asset> = emptyList(),
    // поиск: название, категория, инв. номер, комментарий, место («ферма 700», «каленковичи»)
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
) {
    val assets: List<Asset>
        get() {
            val words = query.trim().lowercase().replace('ё', 'е').split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.isEmpty()) return allAssets
            return allAssets.filter { a ->
                val text = listOfNotNull(a.name, a.category, a.inventoryNum, a.description, a.locationPath)
                    .joinToString(" ").lowercase().replace('ё', 'е')
                words.all { text.contains(it) }
            }
        }
}
