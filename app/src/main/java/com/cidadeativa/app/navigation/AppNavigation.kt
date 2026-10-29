package com.cidadeativa.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cidadeativa.app.ui.auth.AuthViewModel
import com.cidadeativa.app.ui.auth.LoginScreen
import com.cidadeativa.app.ui.auth.RegisterScreen
import com.cidadeativa.app.ui.home.HomeScreen
import com.cidadeativa.app.ui.reports.DetalheDenunciaScreen
import com.cidadeativa.app.ui.reports.MapaGeralScreen
import com.cidadeativa.app.ui.reports.MinhasDenunciasScreen
import com.cidadeativa.app.ui.reports.NovaDenunciaScreen
import com.cidadeativa.app.ui.reports.ReportViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val reportViewModel: ReportViewModel = viewModel()
    val state by authViewModel.state.collectAsStateWithLifecycle()

    val startDestination = if (state.isLoggedIn) Routes.HOME else Routes.LOGIN

    // Redireciona ao login quando a sessão é encerrada
    LaunchedEffect(state.isLoggedIn) {
        if (!state.isLoggedIn) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            val denuncias by reportViewModel.minhasDenuncias.collectAsStateWithLifecycle()
            HomeScreen(
                nomeUsuario = authViewModel.usuarioAtual?.nome?.substringBefore(" ").orEmpty(),
                totalDenuncias = denuncias.size,
                aguardandoRecebimento = denuncias.count { !it.recebidaPeloOrgao },
                onNewReport = { navController.navigate(Routes.NOVA_DENUNCIA) },
                onMyReports = { navController.navigate(Routes.MINHAS_DENUNCIAS) },
                onMap = { navController.navigate(Routes.MAPA) },
                onLogout = { authViewModel.logout() }
            )
        }
        composable(Routes.NOVA_DENUNCIA) {
            NovaDenunciaScreen(
                viewModel = reportViewModel,
                onVoltar = { navController.popBackStack() },
                onVerDenuncias = {
                    navController.navigate(Routes.MINHAS_DENUNCIAS) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(Routes.MINHAS_DENUNCIAS) {
            MinhasDenunciasScreen(
                viewModel = reportViewModel,
                onVoltar = { navController.popBackStack() },
                onDenunciaClick = { id -> navController.navigate(Routes.detalhe(id)) }
            )
        }
        composable(
            route = Routes.DETALHE,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.IntType })
        ) { entrada ->
            DetalheDenunciaScreen(
                viewModel = reportViewModel,
                denunciaId = entrada.arguments?.getInt(Routes.ARG_ID) ?: -1,
                onVoltar = { navController.popBackStack() }
            )
        }
        composable(Routes.MAPA) {
            MapaGeralScreen(
                viewModel = reportViewModel,
                onVoltar = { navController.popBackStack() },
                onDenunciaClick = { id -> navController.navigate(Routes.detalhe(id)) }
            )
        }
    }
}