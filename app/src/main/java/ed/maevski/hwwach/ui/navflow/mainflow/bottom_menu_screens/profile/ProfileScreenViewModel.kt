package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val userProfileRepository: IUserProfileRepository,
    private val tokensRepository: ITokensRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            userProfileRepository.userProfile.collectLatest { user ->
                _state.value = _state.value.copy(
                    user = user,
                    isLoading = false
                )
            }
        }
    }

    fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnChangePasswordClicked -> {
                viewModelScope.launch {
                    _events.emit(ProfileEvent.NavigateToChangePassword)
                }
            }
            is ProfileAction.OnLogoutClicked -> {
                viewModelScope.launch {
                    tokensRepository.clearTokens()
                    userProfileRepository.clearUserProfile()
                    _events.emit(ProfileEvent.NavigateToLogin)
                }
            }
        }
    }
}
