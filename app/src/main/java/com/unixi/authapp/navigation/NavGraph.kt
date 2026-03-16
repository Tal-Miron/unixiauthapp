package com.unixi.authapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.unixi.authapp.scan.ScanScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navigationActions = UnixiNavigationActions(navController)

    NavHost(
        navController = navController,
        startDestination = UnixiDestinations.SCAN_ROUTE,
        modifier = modifier
    ) {
        composable(
            route = UnixiDestinations.SCAN_ROUTE
        ) {
             ScanScreen(
                 onNavigateToAuth = navigationActions::navigateToAuth,
                 onShowError = navigationActions::navigateToError
             )
        }

        composable(
            route = UnixiDestinations.AUTH_ROUTE
        ) {
            // AuthScreen(
            //     onAuthenticationSuccess = navigationActions::navigateToSuccess,
            //     onAuthenticationFailure = {
            //         navigationActions.navigateToError("Wrong password")
            //     }
            // )
        }

        composable(
            route = UnixiDestinations.ERROR_ROUTE,
            arguments = listOf(
                navArgument(UnixiNavigationArgs.ERROR_MESSAGE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val errorMessage = backStackEntry.arguments
                ?.getString(UnixiNavigationArgs.ERROR_MESSAGE)

            // ErrorScreen(
            //     message = errorMessage ?: "Authentication failed",
            //     onTryAgain = navigationActions::popBack
            // )
        }

        composable(
            route = UnixiDestinations.SUCCESS_ROUTE
        ) {
            // SuccessScreen(
            //     onContinue = navigationActions::navigateToHome
            // )
        }

        composable(
            route = UnixiDestinations.HOME_ROUTE
        ) {
            // HomeScreen()
        }
    }
}