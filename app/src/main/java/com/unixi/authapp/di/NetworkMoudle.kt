package com.unixi.authapp.di

import com.unixi.authapp.data.repository.AuthRepository
import com.unixi.authapp.data.repository.PasswordRepository
import com.unixi.authapp.data.source.remote.AuthRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
        }
    }

    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(get())
            }
        }
    }

    single {
        AuthRemoteDataSource(
            httpClient = get(),
            backendUrlProvider = get()
        )
    }

    single {
        AuthRepository(
            authRemoteDataSource = get()
        )
    }

    single {
        PasswordRepository(
            authRemoteDataSource = get(),
            sessionStore = get()
        )
    }
}