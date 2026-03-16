package com.unixi.authapp.data.repository

import com.unixi.authapp.data.model.AppConfig
import com.unixi.authapp.data.source.remote.AuthRemoteDataSource

class AuthRepository(
    private val remoteDataSource: AuthRemoteDataSource
) {

    suspend fun resolveQr(appConfig: AppConfig): QrResolveResult {
        return try {
            val response = remoteDataSource.postQrToken(
                endpoint = appConfig.endpointUrl,
                qrToken = appConfig.qrToken
            )

            when (response.code()) {
                HTTP_OK -> {
                    val userData = response.body()
                        ?: return QrResolveResult.Failure(response.code(), response.message())
                    QrResolveResult.Success(userData)
                }
                HTTP_NOT_FOUND -> QrResolveResult.NotFound
                else -> QrResolveResult.Failure(response.code(), response.message())
            }
        } catch (exception: Exception) {
            QrResolveResult.Failure(0,"failed to get response")
        }
    }

    private companion object {
        const val HTTP_OK = 200
        const val HTTP_NOT_FOUND = 404
    }
}