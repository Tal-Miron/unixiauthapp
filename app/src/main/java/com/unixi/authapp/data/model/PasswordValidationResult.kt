package com.unixi.authapp.data.model

sealed class PasswordValidationResult {

    data class Success(
        val message: String
    ) : PasswordValidationResult()

    object WrongPassword : PasswordValidationResult()

    data class Failure(
        val code: Int, val message: String? = null
    ) : PasswordValidationResult()

}