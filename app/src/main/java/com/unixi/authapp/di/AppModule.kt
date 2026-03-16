package com.unixi.authapp.di

import com.unixi.authapp.data.session.SessionStore
import com.unixi.authapp.util.BackendUrlProvider
import com.unixi.authapp.util.DeviceInfoProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single {
        SessionStore()
    }

    single {
        DeviceInfoProvider(
            context = androidContext()
        )
    }

    single {
        BackendUrlProvider()
    }
}