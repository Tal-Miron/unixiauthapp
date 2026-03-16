package com.unixi.authapp.data.repository

import com.unixi.authapp.data.model.PasswordValidationResult
import com.unixi.authapp.data.session.SessionStore
import com.unixi.authapp.data.source.remote.AuthRemoteDataSource
import com.unixi.authapp.data.source.remote.RemoteResult

class PasswordRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val sessionStore: SessionStore
) {

    suspend fun validate(password: String): PasswordValidationResult {
        val endpoint = sessionStore.endpoint
        val userData = sessionStore.userData

        if (endpoint.isNullOrBlank() || userData == null) {
            return PasswordValidationResult.Failure(
                code = PasswordValidationResult.NO_ACTIVE_SESSION_CODE,
                message = "No active session found."
            )
        }

        return when (
            val result = authRemoteDataSource.validatePassword(
                endpoint = endpoint,
                email = userData.email,
                password = password
            )
        ) {
            is RemoteResult.Success -> {
                PasswordValidationResult.Success(
                    message = result.body.message
                )
            }

            is RemoteResult.Error -> {
                when (result.code) {
                    404 -> PasswordValidationResult.WrongPassword

                    RemoteResult.NETWORK_ERROR_CODE -> {
                        PasswordValidationResult.Failure(
                            code = PasswordValidationResult.NETWORK_ERROR_CODE,
                            message = result.message
                        )
                    }

                    RemoteResult.UNKNOWN_ERROR_CODE -> {
                        PasswordValidationResult.Failure(
                            code = PasswordValidationResult.UNKNOWN_ERROR_CODE,
                            message = result.message
                        )
                    }

                    else -> {
                        PasswordValidationResult.Failure(
                            code = result.code,
                            message = result.message
                        )
                    }
                }
            }
        }
    }
}