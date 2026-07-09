package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import javax.inject.Inject

class CheckAuthTokenUseCase @Inject constructor(
    private val tokensRepository: ITokensRepository
) {
    operator fun invoke(): Boolean {
        return tokensRepository.hasRefreshToken()
    }
}
