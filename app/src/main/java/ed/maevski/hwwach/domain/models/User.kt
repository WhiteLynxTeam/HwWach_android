package ed.maevski.hwwach.domain.models

data class User(
    val username: String? = null,
    val password: String? = null,
    val lastName: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val position: String? = null,
    val officeName: String? = null,
    val officeLocation: String? = null,
    // код системной роли (admin, member…) — только для показа; права — в perms
    val role: String? = null,
    val roleName: String? = null,
    val perms: List<String> = emptyList(),
    val personId: String? = null,
    val organizationId: String? = null,
    val organizationName: String? = null,
    // материально ответственное лицо; molRequested — просил признак, ждёт админа
    val isMol: Boolean = false,
    val molRequested: Boolean = false,
    // регистрация: организация из поиска (organizationId) или код приглашения
    val inviteCode: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    fun can(perm: String): Boolean = perms.contains(perm)

    /**
     * Validate user data
     */
    fun validate(): Boolean {
        return username?.isNotBlank() == true &&
                password?.isNotBlank() == true
    }

    /**
     * Update the user with new values
     */
    fun copyWithUpdates(
        username: String? = this.username,
        password: String? = this.password,
        lastName: String? = this.lastName,
        firstName: String? = this.firstName,
        middleName: String? = this.middleName,
        fullName: String? = this.fullName,
        phone: String? = this.phone,
        position: String? = this.position,
        officeName: String? = this.officeName,
        officeLocation: String? = this.officeLocation,
        role: String? = this.role,
        isActive: Boolean = this.isActive,
    ): User {
        return this.copy(
            username = username,
            password = password,
            lastName = lastName,
            firstName = firstName,
            middleName = middleName,
            fullName = fullName,
            phone = phone,
            position = position,
            officeName = officeName,
            officeLocation = officeLocation,
            role = role,
            isActive = isActive,
            updatedAt = System.currentTimeMillis()
        )
    }
}
