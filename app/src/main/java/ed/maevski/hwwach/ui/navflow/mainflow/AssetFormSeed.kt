package ed.maevski.hwwach.ui.navflow.mainflow

import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Заготовка новой формы актива из галереи: выбранные снимки и их группа (место, человек).
 * Галерея кладёт перед переходом, форма забирает один раз при открытии
 */
@ActivityRetainedScoped
class AssetFormSeed @Inject constructor() {

    data class Seed(val photoIds: List<String>, val groupId: String?)

    @Volatile
    private var pending: Seed? = null

    fun put(photoIds: List<String>, groupId: String?) {
        pending = Seed(photoIds, groupId)
    }

    fun consume(): Seed? = pending.also { pending = null }
}
