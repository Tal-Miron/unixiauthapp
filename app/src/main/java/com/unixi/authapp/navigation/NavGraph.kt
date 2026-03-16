package com.unixi.authapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ── Route Constants ──────────────────────────────────────────
object AppRoutes {
    const val SCAN = "scan"
    const val AUTH = "auth"
    const val ERROR = "error"
    const val SUCCESS = "success"
    const val HOME = "home"
}

// ── NavGraph ─────────────────────────────────────────────────
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.SCAN,
        modifier = modifier
    ) {
        composable(AppRoutes.SCAN) {
            // ScanScreen() — coming soon
        }

        composable(AppRoutes.AUTH) {
            // AuthScreen() — coming soon
        }

        composable(AppRoutes.ERROR) {
            // ErrorScreen() — coming soon
        }

        composable(AppRoutes.SUCCESS) {
            // SuccessScreen() — coming soon
        }

        composable(AppRoutes.HOME) {
            // HomeScreen() — coming soon
        }
    }
}