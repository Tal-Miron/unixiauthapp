package com.unixi.authapp.data.repository

import com.unixi.authapp.data.model.AppConfig
import com.unixi.authapp.data.source.remote.AuthRemoteDataSource
import com.unixi.authapp.data.source.remote.RemoteResult

class AuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource
) {

    suspend fun resolveQr(appConfig: AppConfig): QrResolveResult {
        return when (
            val result = authRemoteDataSource.resolveQr(
                endpoint = appConfig.endpointUrl,
                qrToken = appConfig.qrToken
            )
        ) {
            is RemoteResult.Success -> {
                QrResolveResult.Success(
                    userData = result.body
                )
            }

            is RemoteResult.Error -> {
                when (result.code) {
                    404 -> QrResolveResult.NotFound

                    RemoteResult.NETWORK_ERROR_CODE -> {
                        QrResolveResult.Failure(
                            code = QrResolveResult.NETWORK_ERROR_CODE,
                            message = result.message
                        )
                    }

                    RemoteResult.UNKNOWN_ERROR_CODE -> {
                        QrResolveResult.Failure(
                            code = QrResolveResult.UNKNOWN_ERROR_CODE,
                            message = result.message
                        )
                    }

                    else -> {
                        QrResolveResult.Failure(
                            code = result.code,
                            message = result.message
                        )
                    }
                }
            }
        }
    }
}