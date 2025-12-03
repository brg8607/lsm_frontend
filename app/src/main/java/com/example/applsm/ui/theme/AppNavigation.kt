package com.example.applsm.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.applsm.ui.AppViewModel
import com.example.applsm.ui.screens.auth.LoginScreen
import com.example.applsm.ui.screens.auth.RegisterScreen
import com.example.applsm.ui.screens.detail.DetailScreen
import com.example.applsm.ui.screens.dictionary.DictionaryListScreen
import com.example.applsm.ui.screens.main.MainScreen
import com.example.applsm.ui.screens.quiz.QuizScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("register") { RegisterScreen(navController, viewModel) }

        composable("main") { MainScreen(navController, viewModel) }

        composable("dictionary_list/{catId}/{catName}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId")?.toIntOrNull()
            val catName = backStackEntry.arguments?.getString("catName") ?: "Diccionario"
            DictionaryListScreen(navController, viewModel, catId, catName)
        }

        composable("detail/{senaId}") { backStackEntry ->
            val senaId = backStackEntry.arguments?.getString("senaId")?.toIntOrNull()
            val senaObjeto = remember(senaId) {
                if (senaId != null) viewModel.obtenerSenaPorId(senaId) else null
            }

            if (senaObjeto != null) {
                DetailScreen(navController, senaObjeto)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando seña...")
                    Button(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            }
        }

        composable(
            "quiz?catId={catId}&level={level}&resume={resume}",
            arguments = listOf(
                navArgument("catId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("level") { type = NavType.IntType; defaultValue = 1 },
                navArgument("resume") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getInt("catId") ?: -1
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val resume = backStackEntry.arguments?.getBoolean("resume") ?: false

            QuizScreen(navController, viewModel, catId, level, resume)
        }
    }
}