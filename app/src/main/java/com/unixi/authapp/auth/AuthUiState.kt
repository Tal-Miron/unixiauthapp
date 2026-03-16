package com.unixi.authapp.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val navigateToSuccess: Boolean = false,
    val navigateToError: Boolean = false
)