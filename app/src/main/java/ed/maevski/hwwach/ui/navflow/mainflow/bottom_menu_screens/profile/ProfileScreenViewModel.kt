package ed.maevski.hwwach.ui.navflow.mainflow.bottom_menu_screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val userProfileRepository: IUserProfileRepository,
    private val tokensRepository: ITokensRepository,
    private val orgRepository: IOrgRepository,
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
        // роль, признак МОЛ и права могли поменяться — перечитываем профиль с сервера
        viewModelScope.launch { orgRepository.refreshProfile() }
    }

    fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnChangePasswordClicked -> {
                viewModelScope.launch {
                    _events.emit(ProfileEvent.NavigateToChangePassword)
                }
            }
            is ProfileAction.OnAdminClicked -> {
                viewModelScope.launch {
                    _events.emit(ProfileEvent.NavigateToAdmin)
                }
            }
            is ProfileAction.OnAssignmentsClicked -> {
                viewModelScope.launch {
                    _events.emit(ProfileEvent.NavigateToAssignments)
                }
            }
            // просьба «я МОЛ» — признак ставит админ, когда одобрит
            is ProfileAction.OnRequestMolClicked -> {
                viewModelScope.launch {
                    val message = when (val result = orgRepository.requestMol()) {
                        is DomainResult.Success -> "Просьба отправлена администратору"
                        is DomainResult.ValidationError -> result.message
                        else -> "Не удалось отправить, попробуйте позже"
                    }
                    _state.update { it.copy(molRequestMessage = message) }
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
