package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json

@Composable
fun NoteListItem(
    note: Note,
    isCurrentlySelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {

    val previewText = remember(note.id, note.content) {
        try {
            Json.decodeFromString<RichTextData>(note.content).text
        } catch (_: Exception) {
            note.content
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isCurrentlySelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last Modified: ${note.getFormattedModifiedDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Created At: ${note.getFormattedCreationDateOfNote()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = previewText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    navController: NavController,
    viewModel: NotesListViewModel = viewModel()
) {
    val currentUserInterfaceState by viewModel.currentUserInterfaceState.collectAsState()

    BackHandler {
        if (currentUserInterfaceState.isCurrentlyInSelectionMode) {
            viewModel.clearSelection()
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            if (currentUserInterfaceState.isCurrentlyInSelectionMode) {
                TopAppBar(
                    title = { Text("${currentUserInterfaceState.selectedNoteIDs.size} note(s) currently selected") },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.clearSelection() }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Clear Selected Notes")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.deleteSelectedNotes() }
                        ) {
                            Icon(
                                Icons.Default.Delete, "Delete Selected Notes"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("All Of My Saved Notes") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

        },
        floatingActionButton = {
            if (!currentUserInterfaceState.isCurrentlyInSelectionMode) {
                FloatingActionButton(onClick = {
                    navController.navigate("noteEditorScreenLayout/-1")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add A New Note")
                }
            }
        }
    ) { paddingValues ->
        if (currentUserInterfaceState.notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "No Notes Available Icon",
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .size(100.dp),

                )
                Text(
                    text = "You currently have no saved notes.\n Click or Tap the + button in order to add a new note.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            items(
                items = currentUserInterfaceState.notes,
                key = { it.id }
            ) { note ->
                NoteListItem(
                    note = note,
                    isCurrentlySelected = currentUserInterfaceState.selectedNoteIDs.contains(note.id),
                    onLongClick = { viewModel.toggleNoteSelection(note.id) },
                    onClick = {
                        if (currentUserInterfaceState.isCurrentlyInSelectionMode) {
                            viewModel.toggleNoteSelection(note.id)
                        } else {
                            navController.navigate("noteEditorScreenLayout/${note.id}")
                        }
                    }
                )
            }
        }
    }
}