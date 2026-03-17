package com.unixi.authapp.home.tab1

import androidx.lifecycle.ViewModel
import com.unixi.authapp.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Tab1ViewModel(
    sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        sessionStore.userData?.let { userData ->
            Tab1UiState(
                fullName = userData.fullName,
                email = userData.email,
                company = userData.company,
                department = userData.department,
                userId = userData.userId,
                accountCreationDate = userData.accountCreationDate
            )
        } ?: Tab1UiState()
    )

    val uiState: StateFlow<Tab1UiState> = _uiState.asStateFlow()
}