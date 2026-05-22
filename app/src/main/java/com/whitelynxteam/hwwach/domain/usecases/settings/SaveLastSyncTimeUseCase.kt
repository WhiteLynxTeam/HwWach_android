package com.whitelynxteam.hwwach.domain.usecases.settings

import com.whitelynxteam.hwwach.domain.irepositories.ISettingsRepository
import javax.inject.Inject

class SaveLastSyncTimeUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    suspend operator fun invoke(timestamp: Long) {
        settingsRepository.saveLastSyncTime(timestamp)
    }
}
