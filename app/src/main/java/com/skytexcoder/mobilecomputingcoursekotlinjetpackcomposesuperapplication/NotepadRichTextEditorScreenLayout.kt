package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication.ui.theme.AndroidSuperApplicationTheme
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
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

    val scrollingState = rememberScrollState()

    val editorBringIntoViewRequester = remember {
        androidx.compose.foundation.relocation.BringIntoViewRequester()
    }

    val editorFocusRequester = remember { FocusRequester() }

    val coroutineScope = rememberCoroutineScope()
    val currentUserInterfaceState by viewModel.userInterfaceState.collectAsStateWithLifecycle()

    BackHandler {
        coroutineScope.launch {
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
                            coroutineScope.launch {
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
                .imePadding()
                .verticalScroll(scrollingState)
                .padding(16.dp)
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
                onUnderLineClick = { viewModel.toggleUnderline() },
                onTextFontSizeIncrease = { viewModel.changeFontSize(1) },
                onTextFontSizeDecrease = { viewModel.changeFontSize(-1) },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Transparent)
                    .padding(2.dp)
                    .bringIntoViewRequester(editorBringIntoViewRequester)
            ) {
                TextField(
                    value = currentUserInterfaceState.noteContent,
                    onValueChange = {
                        viewModel.updateContent(it)
                        coroutineScope.launch {
                            delay(50)
                            editorBringIntoViewRequester.bringIntoView()
                        }
                                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .focusRequester(editorFocusRequester)
                        .onFocusChanged {
                            focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    delay(50)
                                    editorBringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    placeholder = { Text("Start writing/adding/modifying/editing the internal text contents of your own specific note item here.....") },
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
}

@Composable
private fun TextFormattingToolbar(
    userInterfaceState: NotesEditorUserInterfaceState,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderLineClick: () -> Unit,
    onTextFontSizeIncrease: () -> Unit,
    onTextFontSizeDecrease: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconToggleButton(
                checked = userInterfaceState.isTextBoldToggleCurrentlyEnabled, onCheckedChange = { onBoldClick() }
            ) {
                Icon(Icons.Default.FormatBold, contentDescription = "Bold")
            }
            IconToggleButton(
                checked = userInterfaceState.isTextItalicToggleCurrentlyEnabled, onCheckedChange = { onItalicClick() }
            ) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
            }
            IconToggleButton(
                checked = userInterfaceState.isTextUnderLineToggleCurrentlyEnabled, onCheckedChange = { onUnderLineClick() }
            ) {
                Icon(Icons.Default.FormatUnderlined, contentDescription = "UnderLine")
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onTextFontSizeDecrease,
                enabled = userInterfaceState.currentTextFontSize > NotesEditorViewModel.MIN_FONT_SIZE
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease The Font Size")
            }
            Text(
                text = userInterfaceState.currentTextFontSize.toString(),
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = onTextFontSizeIncrease,
                enabled = userInterfaceState.currentTextFontSize < NotesEditorViewModel.MAX_FONT_SIZE
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase The Font Size")
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
