package com.unixi.authapp.scan

data class ScanUiState(
    val isLoading: Boolean = false,
    val dialogState: ScanDialogState? = null,
    val notFoundCount: Int = 0,
    val unknownErrorCount: Int = 0,
    val navigateToAuth: Boolean = false
)

sealed class ScanDialogState {

    data object NotFound : ScanDialogState()

    data class UnknownError(
        val message: String? = null
    ) : ScanDialogState()

    data class RateLimited(
        val remainingSeconds: Int
    ) : ScanDialogState()
}