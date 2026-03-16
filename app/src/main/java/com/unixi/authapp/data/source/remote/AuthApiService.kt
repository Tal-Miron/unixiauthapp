package com.unixi.authapp.data.source.remote

import com.unixi.authapp.data.model.UserData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

// ── Request Bodies ────────────────────────────────────────────

@Serializable
data class LoginRequestBody(
    @SerialName("user_id")
    val userId: String
)

@Serializable
data class ValidateRequestBody(
    @SerialName("password")
    val password: String
)

// ── Response Bodies ───────────────────────────────────────────

@Serializable
data class ValidateResponse(
    @SerialName("message")
    val message: String
)

// ── API Service ───────────────────────────────────────────────

interface AuthApiService {

    @POST
    suspend fun login(
        @Url endpoint: String,
        @Body body: LoginRequestBody
    ): Response<UserData>

    @POST
    suspend fun validate(
        @Url endpoint: String,
        @Body body: ValidateRequestBody
    ): Response<ValidateResponse>
}