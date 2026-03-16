package com.unixi.authapp

import android.app.Application
import com.unixi.authapp.di.appModule
import com.unixi.authapp.di.networkModule
import com.unixi.authapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class UnixiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@UnixiApplication)
            modules(appModule, networkModule, viewModelModule)
        }
    }
}