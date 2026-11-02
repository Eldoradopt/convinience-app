package com.example.convinenceapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.convinenceapp.ui.boards.BoardEditorScreen
import com.example.convinenceapp.ui.boards.BoardsScreen
import com.example.convinenceapp.ui.boards.BoardsViewModel
import com.example.convinenceapp.ui.calculator.CalculatorScreen
import com.example.convinenceapp.ui.finance.FinanceScreen

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Finance    : Screen("finance",    "Finance",    Icons.Default.Home)
    object Boards     : Screen("boards",     "Boards",     Icons.Default.Dashboard)
    object Calculator : Screen("calculator", "Calculator", Icons.Default.Calculate)
}

private val topLevelScreens = listOf(Screen.Finance, Screen.Boards, Screen.Calculator)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val boardsViewModel: BoardsViewModel = viewModel(factory = BoardsViewModel.Factory(context))

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Only show bottom bar on top-level screens
    val showBottomBar = topLevelScreens.any { currentRoute?.startsWith(it.route) == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute?.startsWith(screen.route) == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Finance.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Finance.route) {
                FinanceScreen()
            }
            composable(Screen.Boards.route) {
                BoardsScreen(
                    viewModel = boardsViewModel,
                    onOpenBoard = { boardId ->
                        navController.navigate("board_editor/$boardId")
                    }
                )
            }
            composable(
                route = "board_editor/{boardId}",
                arguments = listOf(navArgument("boardId") { type = NavType.StringType })
            ) { backStack ->
                val boardId = backStack.arguments?.getString("boardId") ?: return@composable
                BoardEditorScreen(
                    viewModel = boardsViewModel,
                    boardId = boardId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calculator.route) {
                CalculatorScreen(viewModel = boardsViewModel)
            }
        }
    }
}
