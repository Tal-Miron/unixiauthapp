package com.unixi.authapp.data.repository

import com.unixi.authapp.data.model.UserData

sealed class QrResolveResult {

    data class Success(
        val userData: UserData
    ) : QrResolveResult()

    data object NotFound : QrResolveResult()

    data class Failure(
        val code: Int,
        val message: String? = null
    ) : QrResolveResult()
}