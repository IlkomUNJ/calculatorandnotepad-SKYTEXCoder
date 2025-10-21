package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AndroidSuperApplicationNavigation(calculatorViewModel: CalculatorViewModel) {
    val navigationController = rememberNavController()
    NavHost(navController = navigationController, startDestination = "super_application_menu_screen_layout") {
        composable("super_application_menu_screen_layout") {
            SuperApplicationMenuScreenLayout(navController = navigationController)
        }
        composable("scientific_calculator_application_screen_layout") {
            ScientificCalculatorApplicationScreenLayout(viewModel = calculatorViewModel, navController = navigationController)
        }
        composable("notepad_rich_text_editor_menu_list_screen_layout") {
            NotesListScreen(navController = navigationController)
        }
        composable(
            route = "noteEditorScreenLayout/{NoteID}",
            arguments = listOf(navArgument("NoteID") { type = NavType.LongType })
        ) {
            backStackEntry ->
                val NoteID = backStackEntry.arguments?.getLong("NoteID") ?: -1L
                NotepadRichTextEditorScreenLayout(navController = navigationController, NoteID = NoteID)
        }
    }
}