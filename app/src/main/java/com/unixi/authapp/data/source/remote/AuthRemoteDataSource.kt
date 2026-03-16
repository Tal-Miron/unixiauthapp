package com.unixi.authapp.data.source.remote

import com.unixi.authapp.data.model.UserData
import retrofit2.Response

class AuthRemoteDataSource(
    private val apiService: AuthApiService
) {

    suspend fun postQrToken(
        endpoint: String,
        qrToken: String
    ): Response<UserData> {
        return apiService.login(
            endpoint = endpoint,
            body = LoginRequestBody(qr_token = qrToken)
        )
    }

    suspend fun postPassword(
        endpoint: String,
        password: String
    ): Response<ValidateResponse> {
        return apiService.validate(
            endpoint = endpoint,
            body = ValidateRequestBody(password = password)
        )
    }
}