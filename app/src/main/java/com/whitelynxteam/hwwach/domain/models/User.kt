package com.whitelynxteam.hwwach.domain.models

data class User(
//    val id: String = UUID.randomUUID().toString(),
    val username: String? = null,
    val password: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val officeName: String? = null,
    val officeLocation: String? = null,
    val role: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    
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
        fullName: String? = this.fullName,
        phone: String? = this.phone,
        officeName: String? = this.officeName,
        officeLocation: String? = this.officeLocation,
        role: String? = this.role,
        isActive: Boolean = this.isActive
    ): User {
        return this.copy(
            username = username,
            password = password,
            fullName = fullName,
            phone = phone,
            officeName = officeName,
            officeLocation = officeLocation,
            role = role,
            isActive = isActive,
            updatedAt = System.currentTimeMillis()
        )
    }
}