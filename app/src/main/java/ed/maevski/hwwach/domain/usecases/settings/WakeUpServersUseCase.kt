package ed.maevski.hwwach.domain.usecases.settings

import ed.maevski.hwwach.domain.irepositories.IServerWarmupRepository
import javax.inject.Inject

class WakeUpServersUseCase @Inject constructor(
    private val serverWarmupRepository: IServerWarmupRepository
) {
    suspend operator fun invoke() {
        serverWarmupRepository.wakeUp()
    }
}
