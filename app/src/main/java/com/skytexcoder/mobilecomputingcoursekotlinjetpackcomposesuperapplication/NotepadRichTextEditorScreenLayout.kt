package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun NotepadRichTextEditorScreenLayout(navController: NavController) {
    var text by remember { mutableStateOf("Start typing some things or texts here.....") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TextField(
            value = text,
            onValueChange = { newTextContents -> text = newTextContents },
        )
    }
}

