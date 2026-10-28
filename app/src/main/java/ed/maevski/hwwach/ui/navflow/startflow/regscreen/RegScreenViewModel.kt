package ed.maevski.hwwach.ui.navflow.startflow.regscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.hwwach.common.deeplink.PendingInvite
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.models.User
import ed.maevski.hwwach.domain.usecases.user.RegApiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegScreenViewModel @Inject constructor(
    private val regApiUseCase: RegApiUseCase,
    private val orgRepository: IOrgRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RegScreenState())
    val state: StateFlow<RegScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RegScreenEvent>()
    val events: SharedFlow<RegScreenEvent> = _events.asSharedFlow()

    init {
        // открыли по ссылке-приглашению — подставляем код и сразу проверяем
        PendingInvite.consume()?.let { code ->
            _state.update { it.copy(orgMode = OrgMode.INVITE, inviteCode = code) }
            checkInvite()
        }
    }

    fun handleAction(action: RegScreenAction) {
        when (action) {
            is RegScreenAction.InputLastName -> {
                _state.update { it.copy(lastName = action.name) }
            }

            is RegScreenAction.InputFirstName -> {
                _state.update { it.copy(firstName = action.name) }
            }

            is RegScreenAction.InputMiddleName -> {
                _state.update { it.copy(middleName = action.name) }
            }

            is RegScreenAction.InputPhone -> {
                _state.update { it.copy(phone = action.email) }
            }

            is RegScreenAction.InputPosition -> {
                _state.update { it.copy(position = action.email) }
            }

            is RegScreenAction.InputLogin -> {
                _state.update { it.copy(login = action.email.replace(" ", "")) }
            }

            is RegScreenAction.InputPassword -> {
                _state.update { it.copy(password = action.password) }
            }

            is RegScreenAction.InputConfirmPassword -> {
                // TODO: Implement confirm password logic
            }

            RegScreenAction.OnAuthClicked -> {
                viewModelScope.launch {
                    _events.emit(RegScreenEvent.Exit)
                }
            }
            RegScreenAction.OnRegClicked -> onRegClicked()

            RegScreenAction.OnBackClicked -> {
                viewModelScope.launch {
                    _events.emit(RegScreenEvent.Exit)
                }
            }

            is RegScreenAction.OnPrivacyPolicyChecked -> {
                _state.update { it.copy(isPrivacyPolicyAccepted = action.checked) }
            }

            RegScreenAction.OnPrivacyPolicyClicked -> {
                viewModelScope.launch {
                    _events.emit(RegScreenEvent.NavigateToPrivacyPolicy)
                }
            }

            is RegScreenAction.SetOrgMode -> {
                _state.update { it.copy(orgMode = action.mode, orgError = "") }
            }

            is RegScreenAction.InputOrgQuery -> {
                // запрос — только по кнопке, без подсказок при вводе (9.1)
                _state.update { it.copy(orgQuery = action.query, orgError = "") }
            }

            RegScreenAction.OnSearchOrgClicked -> searchOrg()

            is RegScreenAction.SelectOrg -> {
                _state.update { it.copy(selectedOrg = action.org, orgError = "") }
            }

            is RegScreenAction.InputInviteCode -> {
                _state.update { it.copy(inviteCode = action.code, invite = null, orgError = "") }
            }

            RegScreenAction.OnCheckInviteClicked -> checkInvite()
        }
    }

    private fun searchOrg() {
        val query = _state.value.orgQuery.trim()
        if (query.length < RegScreenState.MIN_ORG_QUERY) return
        viewModelScope.launch {
            _state.update { it.copy(isSearchingOrg = true, orgError = "", selectedOrg = null) }
            when (val result = orgRepository.searchOrganizations(query)) {
                is DomainResult.Success -> _state.update {
                    it.copy(
                        isSearchingOrg = false,
                        orgResults = result.data,
                        orgError = if (result.data.isEmpty()) "Ничего не найдено. Проверьте название или попросите приглашение" else ""
                    )
                }
                is DomainResult.ValidationError -> _state.update { it.copy(isSearchingOrg = false, orgError = result.message) }
                else -> _state.update { it.copy(isSearchingOrg = false, orgError = "Нет связи с сервером. Попробуйте ещё раз") }
            }
        }
    }

    private fun checkInvite() {
        val code = _state.value.inviteCode.trim()
        if (code.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isCheckingInvite = true, orgError = "") }
            when (val result = orgRepository.resolveInvite(code)) {
                is DomainResult.Success -> {
                    val invite = result.data
                    _state.update {
                        it.copy(
                            isCheckingInvite = false,
                            invite = invite,
                            // приглашение из карточки — ФИО уже заведены админом
                            lastName = it.lastName.ifBlank { invite.lastName.orEmpty() },
                            firstName = it.firstName.ifBlank { invite.firstName.orEmpty() },
                            middleName = it.middleName.ifBlank { invite.middleName.orEmpty() },
                            position = it.position.ifBlank { invite.position.orEmpty() },
                        )
                    }
                }
                is DomainResult.ValidationError -> _state.update { it.copy(isCheckingInvite = false, invite = null, orgError = result.message) }
                else -> _state.update { it.copy(isCheckingInvite = false, invite = null, orgError = "Нет связи с сервером. Попробуйте ещё раз") }
            }
        }
    }

    private fun onRegClicked() {
        viewModelScope.launch {
            val currentState = _state.value

            if (!currentState.organizationChosen) {
                _state.update {
                    it.copy(errorMessage = if (currentState.orgMode == OrgMode.INVITE) "Проверьте код приглашения" else "Найдите и выберите организацию")
                }
                return@launch
            }

            // Check if required fields are filled
            if (currentState.lastName.isBlank() ||
                currentState.firstName.isBlank() ||
                currentState.login.isBlank() ||
                currentState.password.isBlank()) {
                _state.update { it.copy(errorMessage = "Заполните все обязательные поля") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = "") }

            // Create user object from state
            val user = User(
                username = currentState.login,
                password = currentState.password,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                middleName = currentState.middleName,
                phone = currentState.phone,
                position = currentState.position,
                organizationId = if (currentState.orgMode == OrgMode.SEARCH) currentState.selectedOrg?.id else null,
                inviteCode = if (currentState.orgMode == OrgMode.INVITE) currentState.invite?.code else null,
            )

            when (val result = regApiUseCase(user)) {
                is DomainResult.Success -> {
                    _events.emit(RegScreenEvent.NavigateToAuth)
                }
                // Чаще всего — сервер на Render ещё просыпается после простоя
                is DomainResult.NetworkError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Нет связи с сервером. Подождите минуту и попробуйте снова"
                        )
                    }
                }
                is DomainResult.ValidationError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ошибка при регистрации"
                        )
                    }
                }
            }
        }
    }
}
