package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication.ui.theme.AndroidSuperApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // To Support Edge-To-Edge Screen Layout Rendering, uncomment the following line:
        enableEdgeToEdge()
        setContent {
            AndroidSuperApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AndroidSuperApplicationNavigation(calculatorViewModel = ViewModelProvider(this)[CalculatorViewModel::class.java])
                }
            }
        }
    }
}
