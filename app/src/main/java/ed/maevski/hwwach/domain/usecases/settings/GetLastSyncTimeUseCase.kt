package ed.maevski.hwwach.domain.usecases.settings

import ed.maevski.hwwach.domain.irepositories.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastSyncTimeUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    operator fun invoke(): Flow<Long> = settingsRepository.getLastSyncTime()
}
