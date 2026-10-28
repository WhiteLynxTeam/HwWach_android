package ed.maevski.hwwach.ui.navflow.startflow.regscreen

import ed.maevski.hwwach.domain.models.InviteInfo
import ed.maevski.hwwach.domain.models.OrgSearchItem

// Как человек попадает в организацию (9.1): по коду приглашения или через поиск.
// В обоих случаях заявку утверждает админ организации
enum class OrgMode { INVITE, SEARCH }

sealed class RegScreenAction {
    data class InputLastName(val name: String) : RegScreenAction()
    data class InputFirstName(val name: String) : RegScreenAction()
    data class InputMiddleName(val name: String) : RegScreenAction()
    data class InputPhone(val email: String) : RegScreenAction()
    data class InputPosition(val email: String) : RegScreenAction()
    data class InputLogin(val email: String) : RegScreenAction()
    data class InputPassword(val password: String) : RegScreenAction()
    data class InputConfirmPassword(val confirmPassword: String) : RegScreenAction()
    data object OnRegClicked : RegScreenAction()
    data object OnAuthClicked : RegScreenAction()
    data object OnBackClicked : RegScreenAction()
    data class OnPrivacyPolicyChecked(val checked: Boolean) : RegScreenAction()
    data object OnPrivacyPolicyClicked : RegScreenAction()

    data class SetOrgMode(val mode: OrgMode) : RegScreenAction()
    data class InputOrgQuery(val query: String) : RegScreenAction()
    data object OnSearchOrgClicked : RegScreenAction()
    data class SelectOrg(val org: OrgSearchItem) : RegScreenAction()
    data class InputInviteCode(val code: String) : RegScreenAction()
    data object OnCheckInviteClicked : RegScreenAction()
}

sealed class RegScreenEvent {
    data object NavigateToMain : RegScreenEvent()
    data object NavigateToAuth : RegScreenEvent()
    data object Exit : RegScreenEvent()
    data object NavigateToPrivacyPolicy : RegScreenEvent()
}

data class RegScreenState (
    val lastName: String = "",
    val firstName: String = "",
    val middleName : String = "",
    val phone: String = "",
    val position: String = "",
    val login: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val isPrivacyPolicyAccepted: Boolean = false,

    val orgMode: OrgMode = OrgMode.INVITE,
    val orgQuery: String = "",
    val isSearchingOrg: Boolean = false,
    val orgResults: List<OrgSearchItem>? = null,
    val selectedOrg: OrgSearchItem? = null,
    val inviteCode: String = "",
    val isCheckingInvite: Boolean = false,
    val invite: InviteInfo? = null,
    val orgError: String = "",
) {
    // кнопка «Найти» появляется от 4 символов: «пуща» найдёт «… пуща», 3 дают слишком много совпадений
    val canSearchOrg: Boolean
        get() = orgQuery.trim().length >= MIN_ORG_QUERY

    val organizationChosen: Boolean
        get() = if (orgMode == OrgMode.INVITE) invite != null else selectedOrg != null

    companion object {
        const val MIN_ORG_QUERY = 4
    }
}
