package ed.maevski.hwwach.ui.navflow.startflow.orgregscreen

// Регистрация организации (8.1): почта → код → организация и администратор
enum class OrgRegStep { EMAIL, CODE, DETAILS }

enum class OrgRegField {
    EMAIL, CODE, NAME, SHORT_NAME, ADDRESS, DIRECTOR, OWNER, LAST_NAME, FIRST_NAME, MIDDLE_NAME, POSITION, PASSWORD
}

sealed class OrgRegAction {
    data class Input(val field: OrgRegField, val value: String) : OrgRegAction()
    data object OnNextClicked : OrgRegAction()
    data object OnResendClicked : OrgRegAction()
    data object OnBackClicked : OrgRegAction()
}

sealed class OrgRegEvent {
    data object NavigateToMain : OrgRegEvent()
    data object Exit : OrgRegEvent()
}

data class OrgRegState(
    val step: OrgRegStep = OrgRegStep.EMAIL,
    val email: String = "",
    val code: String = "",
    val registrationToken: String? = null,
    val name: String = "",
    val shortName: String = "",
    val address: String = "",
    val directorName: String = "",
    val ownerName: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val position: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val infoMessage: String = "",
)
