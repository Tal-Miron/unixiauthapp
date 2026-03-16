package com.unixi.authapp.data.repository

import com.unixi.authapp.data.model.UserData

sealed class QrResolveResult {

    data class Success(
        val userData: UserData
    ) : QrResolveResult()

    object NotFound : QrResolveResult()

    data class Failure(
        val code: Int,
        val message: String? = null
    ) : QrResolveResult()

    companion object {
        const val INVALID_URL_CODE = -100
        const val NETWORK_ERROR_CODE = -101
        const val UNKNOWN_ERROR_CODE = -102
    }
}