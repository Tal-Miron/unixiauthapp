package com.unixi.authapp.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unixi.authapp.data.model.AppConfig
import com.unixi.authapp.data.model.UserData
import com.unixi.authapp.data.session.SessionStore
import com.unixi.authapp.data.repository.AuthRepository
import com.unixi.authapp.data.repository.QrResolveResult
import com.unixi.authapp.util.QrDecryptor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var lastScannedValue: String? = null
    private var scanLockJob: Job? = null
    private var rateLimitJob: Job? = null
    private var rateLimitedUntilMillis: Long? = null

    fun onQrScanned(rawValue: String) {
        if (_uiState.value.isLoading) {
            return
        }

        if (isRateLimited()) {
            showActiveRateLimitDialog()
            return
        }

        if (rawValue == lastScannedValue) {
            return
        }

        lockScan(rawValue)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                dialogState = null
            )

            val appConfig = runCatching {
                QrDecryptor.decrypt(rawValue)
            }.getOrElse { exception ->
                unlockScan()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dialogState = ScanDialogState.InvalidFormat(
                        message = exception.message ?: "Invalid QR format."
                    )
                )
                return@launch
            }

            resolveQr(appConfig)
        }
    }

    fun dismissDialog() {
        unlockScan()

        _uiState.value = _uiState.value.copy(
            dialogState = null
        )
    }

    fun onNavigationHandled() {
        unlockScan()

        _uiState.value = _uiState.value.copy(
            navigateToAuth = false
        )
    }

    private suspend fun resolveQr(appConfig: AppConfig) {
        when (val result = authRepository.resolveQr(appConfig)) {
            is QrResolveResult.Success -> handleResolveSuccess(
                endpoint = appConfig.endpointUrl,
                userData = result.userData
            )

            QrResolveResult.NotFound -> handleNotFound()

            is QrResolveResult.Failure -> handleFailure(
                code = result.code,
                message = result.message
            )
        }
    }

    private fun handleResolveSuccess(
        endpoint: String,
        userData: UserData
    ) {
        sessionStore.saveSession(
            endpoint = endpoint,
            userData = userData
        )

        resetFailureCounters()

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            dialogState = null,
            navigateToAuth = true
        )
    }

    private fun handleNotFound() {
        unlockScan()

        val updatedCount = _uiState.value.notFoundCount + 1

        if (updatedCount >= MAX_NOT_FOUND_ATTEMPTS) {
            startRateLimit(RATE_LIMIT_NOT_FOUND_SECONDS)
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            notFoundCount = updatedCount,
            dialogState = ScanDialogState.NotFound
        )
    }

    private fun handleFailure(
        code: Int,
        message: String?
    ) {
        unlockScan()

        if (code == QrResolveResult.INVALID_URL_CODE) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dialogState = ScanDialogState.InvalidUrl(
                    message = message ?: "The QR code contains an invalid backend URL."
                )
            )
            return
        }

        val updatedCount = _uiState.value.unknownErrorCount + 1

        if (updatedCount >= MAX_UNKNOWN_ERROR_ATTEMPTS) {
            startRateLimit(RATE_LIMIT_UNKNOWN_ERROR_SECONDS)
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            unknownErrorCount = updatedCount,
            dialogState = ScanDialogState.UnknownError(
                message = message
            )
        )
    }

    private fun lockScan(rawValue: String) {
        lastScannedValue = rawValue

        scanLockJob?.cancel()
        scanLockJob = viewModelScope.launch {
            delay(SCAN_LOCK_DURATION_MILLIS)
            lastScannedValue = null
        }
    }

    private fun unlockScan() {
        scanLockJob?.cancel()
        lastScannedValue = null
    }

    private fun startRateLimit(durationSeconds: Int) {
        rateLimitJob?.cancel()
        unlockScan()

        val expiresAt = System.currentTimeMillis() + durationSeconds * 1000L
        rateLimitedUntilMillis = expiresAt

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            dialogState = ScanDialogState.RateLimited(
                remainingSeconds = durationSeconds
            )
        )

        rateLimitJob = viewModelScope.launch {
            var remainingSeconds = durationSeconds

            while (remainingSeconds > 0) {
                _uiState.value = _uiState.value.copy(
                    dialogState = ScanDialogState.RateLimited(
                        remainingSeconds = remainingSeconds
                    )
                )
                delay(1000L)
                remainingSeconds--
            }

            rateLimitedUntilMillis = null
            resetFailureCounters()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dialogState = null
            )
        }
    }

    private fun isRateLimited(): Boolean {
        val expiresAt = rateLimitedUntilMillis ?: return false
        return System.currentTimeMillis() < expiresAt
    }

    private fun showActiveRateLimitDialog() {
        val expiresAt = rateLimitedUntilMillis ?: return
        val remainingMillis = expiresAt - System.currentTimeMillis()
        val remainingSeconds = (remainingMillis / 1000L).toInt().coerceAtLeast(1)

        _uiState.value = _uiState.value.copy(
            dialogState = ScanDialogState.RateLimited(
                remainingSeconds = remainingSeconds
            )
        )
    }

    private fun resetFailureCounters() {
        _uiState.value = _uiState.value.copy(
            notFoundCount = 0,
            unknownErrorCount = 0
        )
    }

    companion object {
        private const val MAX_NOT_FOUND_ATTEMPTS = 5
        private const val MAX_UNKNOWN_ERROR_ATTEMPTS = 10
        private const val RATE_LIMIT_NOT_FOUND_SECONDS = 120
        private const val RATE_LIMIT_UNKNOWN_ERROR_SECONDS = 60
        private const val SCAN_LOCK_DURATION_MILLIS = 1500L
    }
}