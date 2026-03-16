package com.unixi.authapp.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Raw route names used internally by the navigation system.
 * These are the base paths for each screen.
 */
private object UnixiScreens {
    const val SCAN = "scan"
    const val AUTH = "auth"
    const val ERROR = "error"
    const val SUCCESS = "success"
    const val HOME = "home"
}

/**
 * Route argument keys.
 * Keep every argument key in one place to avoid typos and duplication.
 */
object UnixiNavigationArgs {
    const val ERROR_MESSAGE = "errorMessage"
}

/**
 * Public route definitions used by the NavHost.
 * If a route needs arguments, define the full route pattern here.
 */
object UnixiDestinations {
    const val SCAN_ROUTE = UnixiScreens.SCAN
    const val AUTH_ROUTE = UnixiScreens.AUTH
    const val SUCCESS_ROUTE = UnixiScreens.SUCCESS
    const val HOME_ROUTE = UnixiScreens.HOME
    const val ERROR_ROUTE =
        "${UnixiScreens.ERROR}?${UnixiNavigationArgs.ERROR_MESSAGE}={${UnixiNavigationArgs.ERROR_MESSAGE}}"
}

/**
 * Navigation action helpers.
 * This keeps navigation logic out of composables and makes it reusable.
 */
class UnixiNavigationActions(
    private val navController: NavHostController
) {

    fun navigateToScan() {
        navController.navigate(UnixiDestinations.SCAN_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun navigateToAuth() {
        navController.navigate(UnixiDestinations.AUTH_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToError(errorMessage: String? = null) {
        val route = buildErrorRoute(errorMessage)
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun navigateToSuccess() {
        navController.navigate(UnixiDestinations.SUCCESS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToHome() {
        navController.navigate(UnixiDestinations.HOME_ROUTE) {
            popUpTo(UnixiDestinations.SCAN_ROUTE) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun popBack() {
        navController.popBackStack()
    }

    private fun buildErrorRoute(errorMessage: String?): String {
        return if (errorMessage.isNullOrBlank()) {
            UnixiScreens.ERROR
        } else {
            "${UnixiScreens.ERROR}?${UnixiNavigationArgs.ERROR_MESSAGE}=$errorMessage"
        }
    }
}