package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication.ui.theme.AndroidSuperApplicationTheme
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadRichTextEditorScreenLayout(
    navController: NavController,
    NoteID: Long,
    viewModel: NotesEditorViewModel = viewModel()
) {
    LaunchedEffect(key1 = NoteID) {
        viewModel.loadNoteByID(NoteID)
    }

    val scope = rememberCoroutineScope()
    val currentUserInterfaceState by viewModel.userInterfaceState.collectAsStateWithLifecycle()

    /* DisposableEffect(Unit) {
        onDispose {
            viewModel.saveNote()
        }
    } */

    BackHandler {
        scope.launch {
            viewModel.saveNoteANDWait()
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentUserInterfaceState.isThisNoteANewNote) "Create A Brand New Note" else "Edit The Currently Existing Note") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.saveNoteANDWait()
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back To Notes List Menu Screen")
                    }
                },
                actions = {
                    if (!currentUserInterfaceState.isThisNoteANewNote) {
                        IconButton(onClick = {
                            viewModel.deleteNote()
                            navController.navigateUp()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete This Current Note")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // .padding(16.dp)
        ) {
            OutlinedTextField(
                value = currentUserInterfaceState.noteTitle,
                onValueChange = { viewModel.updateNoteTitle(it) },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge
            )
            TextFormattingToolbar(
                userInterfaceState = currentUserInterfaceState,
                onBoldClick = { viewModel.toggleBold() },
                onItalicClick = { viewModel.toggleItalic() },
                onUnderLineClick = { viewModel.toggleUnderline() }
            )
            // Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = currentUserInterfaceState.noteContent,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Start writing the content of your own specific note here.....") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun TextFormattingToolbar(
    userInterfaceState: NotesEditorUserInterfaceState,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderLineClick: () -> Unit,
) {
    val selection = userInterfaceState.noteContent.selection
    if (!selection.collapsed) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconToggleButton(checked = userInterfaceState.isBold, onCheckedChange = { onBoldClick() }) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                }
                IconToggleButton(checked = userInterfaceState.isItalic, onCheckedChange = { onItalicClick() }) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                }
                IconToggleButton(checked = userInterfaceState.isUnderLine, onCheckedChange = { onUnderLineClick() }) {
                    Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotepadRichTextEditorScreenLayoutPreview() {
    AndroidSuperApplicationTheme {
        NotepadRichTextEditorScreenLayout(navController = rememberNavController(), NoteID = -1L)
    }
}
