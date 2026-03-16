package com.unixi.authapp.di

import com.unixi.authapp.auth.AuthViewModel
import com.unixi.authapp.scan.ScanViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { //depricated, explain why ignored
        ScanViewModel(
            authRepository = get(),
            sessionStore = get()
        )
    }

    viewModel {
        AuthViewModel(
            passwordRepository = get(),
            sessionStore = get()
        )
    }

}

/*
package com.unixi.authapp.di

import com.unixi.authapp.auth.AuthViewModel
import com.unixi.authapp.home.HomeViewModel
import com.unixi.authapp.home.tab1.Tab1ViewModel
import com.unixi.authapp.home.tab2.Tab2ViewModel
import com.unixi.authapp.scan.ScanViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        ScanViewModel(
            authRepository = get(),
            sessionStore = get()
        )
    }

    viewModel {
        AuthViewModel(
            passwordRepository = get(),
            sessionStore = get()
        )
    }

    viewModel {
        HomeViewModel()
    }

    viewModel {
        Tab1ViewModel(
            sessionStore = get()
        )
    }

    viewModel {
        Tab2ViewModel(
            deviceInfoProvider = get()
        )
    }
}*/
