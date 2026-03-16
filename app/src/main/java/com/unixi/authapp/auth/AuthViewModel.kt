package com.unixi.authapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unixi.authapp.data.model.PasswordValidationResult
import com.unixi.authapp.data.repository.PasswordRepository
import com.unixi.authapp.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val passwordRepository: PasswordRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            email = sessionStore.userData?.email.orEmpty()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password
        )
    }

    fun submitPassword() {
        val currentPassword = _uiState.value.password.trim()

        if (currentPassword.isBlank() || _uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )

            when (passwordRepository.validate(currentPassword)) {
                is PasswordValidationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        navigateToSuccess = true
                    )
                }

                PasswordValidationResult.WrongPassword -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        password = "",
                        navigateToError = true
                    )
                }

                is PasswordValidationResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        password = "",
                        navigateToError = true
                    )
                }
            }
        }
    }

    fun onSuccessNavigationHandled() {
        _uiState.value = _uiState.value.copy(
            navigateToSuccess = false
        )
    }

    fun onErrorNavigationHandled() {
        _uiState.value = _uiState.value.copy(
            navigateToError = false
        )
    }
}