package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.irepositories.ITokensRepository
import javax.inject.Inject

class CheckAuthTokenUseCase @Inject constructor(
    private val tokensRepository: ITokensRepository
) {
    operator fun invoke(): Boolean {
        return tokensRepository.hasRefreshToken()
    }
}
