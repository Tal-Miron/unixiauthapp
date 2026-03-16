package com.unixi.authapp.data.source.remote

import android.util.Log
import com.unixi.authapp.data.model.UserData
import com.unixi.authapp.util.BackendUrlProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException

class AuthRemoteDataSource(
    private val httpClient: HttpClient,
    private val backendUrlProvider: BackendUrlProvider
) {

    suspend fun resolveQr(
        endpoint: String,
        qrToken: String
    ): RemoteResult<UserData> {
        val url = backendUrlProvider.buildResolveQrUrl(endpoint)
        Log.d("AuthRemoteDataSource", "resolveQr url = $url")
        return executeRequest<UserData>(url) {
            setBody(
                QrResolveRequest(
                    qrToken = qrToken
                )
            )
        }
    }

    suspend fun validatePassword(
        endpoint: String,
        email: String,
        password: String
    ): RemoteResult<AuthMessageResponse> {
        val url = backendUrlProvider.buildValidatePasswordUrl(endpoint)

        return executeRequest<AuthMessageResponse>(url) {
            setBody(
                PasswordValidationRequest(
                    email = email,
                    password = password
                )
            )
        }
    }

    private suspend inline fun <reified T> executeRequest(
        url: String,
        crossinline configureBody: suspend io.ktor.client.request.HttpRequestBuilder.() -> Unit
    ): RemoteResult<T> {
        return try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                configureBody()
            }

            RemoteResult.Success(
                body = response.body(),
                code = response.status.value
            )
        } catch (exception: ClientRequestException) {
            RemoteResult.Error(
                code = exception.response.status.value,
                message = exception.message
            )
        } catch (exception: ServerResponseException) {
            RemoteResult.Error(
                code = exception.response.status.value,
                message = exception.message
            )
        } catch (exception: IOException) {
            RemoteResult.Error(
                code = RemoteResult.NETWORK_ERROR_CODE,
                message = exception.message
            )
        } catch (exception: Exception) {
            RemoteResult.Error(
                code = RemoteResult.UNKNOWN_ERROR_CODE,
                message = exception.message
            )
        }
    }
}

sealed class RemoteResult<out T> {

    data class Success<T>(
        val body: T,
        val code: Int
    ) : RemoteResult<T>()

    data class Error(
        val code: Int,
        val message: String? = null
    ) : RemoteResult<Nothing>()

    companion object {
        const val NETWORK_ERROR_CODE = -1
        const val UNKNOWN_ERROR_CODE = -2
    }
}

@Serializable
data class QrResolveRequest(
    @SerialName("qr_token")
    val qrToken: String
)

@Serializable
data class PasswordValidationRequest(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)

@Serializable
data class AuthMessageResponse(
    @SerialName("message")
    val message: String
)