package com.unixi.authapp.data.model

sealed class PasswordValidationResult {

    data class Success(
        val message: String
    ) : PasswordValidationResult()

    object WrongPassword : PasswordValidationResult()

    data class Failure(
        val code: Int,
        val message: String? = null
    ) : PasswordValidationResult()

    companion object {
        const val INVALID_URL_CODE = -100
        const val NO_ACTIVE_SESSION_CODE = -101
        const val NETWORK_ERROR_CODE = -102
        const val UNKNOWN_ERROR_CODE = -103
    }
}