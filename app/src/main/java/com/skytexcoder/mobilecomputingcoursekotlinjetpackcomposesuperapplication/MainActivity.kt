package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.composable
import com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication.ui.theme.CalculatorApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // To Support Edge-To-Edge Layout Rendering, uncomment the following line:
        // enableEdgeToEdge()
        val calculatorViewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]
        setContent {
            CalculatorApplicationTheme {
                ApplicationNavigation(calculatorViewModel = calculatorViewModel)
            }
        }
    }
}

@Composable
fun ApplicationNavigation(calculatorViewModel: CalculatorViewModel) {
    val navigationController = rememberNavController()
    NavHost(navController = navigationController, startDestination = "super_application_menu_screen_layout") {
        composable("super_application_menu_screen_layout") {
            SuperApplicationMenuScreenLayout(navController = navigationController)
        }
        composable("scientific_calculator_application_screen_layout") {
            ScientificCalculatorApplicationScreenLayout(viewModel = calculatorViewModel, navController = navigationController)
        }
        composable("notepad_rich_text_editor_screen_layout") {
            NotepadRichTextEditorScreenLayout(navController = navigationController)
        }
    }
}