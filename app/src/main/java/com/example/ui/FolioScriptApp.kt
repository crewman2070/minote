package com.example.ui

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.LocalFolioColors

@Composable
fun FolioScriptApp(
    viewModel: NotesViewModel
) {
    val libraryState by viewModel.libraryState.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val pendingDelete by viewModel.notePendingDelete.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.share_chooser_title)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ShareText -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, event.subject)
                        putExtra(Intent.EXTRA_TEXT, event.body)
                    }
                    val chooser = Intent.createChooser(sendIntent, shareChooserTitle)
                    context.startActivity(chooser)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 840.dp

            if (isExpanded) {
                Row(modifier = Modifier.fillMaxSize()) {
                    LibraryScreen(
                        uiState = libraryState,
                        themeMode = themeMode,
                        selectedNoteId = editorState?.noteId,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onFilterSelected = viewModel::onFilterSelected,
                        onClearFilters = viewModel::clearFilters,
                        onCycleTheme = viewModel::cycleThemeMode,
                        onCreateNote = viewModel::createNewNote,
                        onSelectNote = viewModel::openNote,
                        onTogglePin = viewModel::onToggleCardPin,
                        onDuplicateNote = viewModel::onDuplicateNote,
                        onShareNote = viewModel::onShareNote,
                        onDeleteNote = viewModel::requestDelete,
                        modifier = Modifier
                            .width(372.dp)
                            .fillMaxHeight()
                    )

                    VerticalDivider(
                        thickness = 1.dp,
                        color = LocalFolioColors.current.subtleDivider
                    )

                    val activeEditor = editorState
                    if (activeEditor != null) {
                        EditorScreen(
                            state = activeEditor,
                            showBackButton = false,
                            onBack = viewModel::closeEditor,
                            onTitleChange = viewModel::onTitleChange,
                            onContentChange = viewModel::onContentChange,
                            onInsertCue = viewModel::onInsertScriptCue,
                            onToggleKind = viewModel::onToggleKind,
                            onTogglePin = viewModel::onToggleEditorPin,
                            onToggleReadMode = viewModel::onToggleReadMode,
                            onDuplicate = viewModel::onDuplicateCurrentEditorNote,
                            onShare = viewModel::onShareCurrentEditorNote,
                            onDelete = viewModel::requestDeleteCurrentEditorNote,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    } else {
                        SplitPanePlaceholder(
                            onCreateNote = viewModel::createNewNote,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = editorState != null,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "compact_screen_transition"
                ) { showingEditor ->
                    val currentEditor = editorState
                    if (showingEditor && currentEditor != null) {
                        EditorScreen(
                            state = currentEditor,
                            showBackButton = true,
                            onBack = viewModel::closeEditor,
                            onTitleChange = viewModel::onTitleChange,
                            onContentChange = viewModel::onContentChange,
                            onInsertCue = viewModel::onInsertScriptCue,
                            onToggleKind = viewModel::onToggleKind,
                            onTogglePin = viewModel::onToggleEditorPin,
                            onToggleReadMode = viewModel::onToggleReadMode,
                            onDuplicate = viewModel::onDuplicateCurrentEditorNote,
                            onShare = viewModel::onShareCurrentEditorNote,
                            onDelete = viewModel::requestDeleteCurrentEditorNote
                        )
                    } else {
                        LibraryScreen(
                            uiState = libraryState,
                            themeMode = themeMode,
                            selectedNoteId = null,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onFilterSelected = viewModel::onFilterSelected,
                            onClearFilters = viewModel::clearFilters,
                            onCycleTheme = viewModel::cycleThemeMode,
                            onCreateNote = viewModel::createNewNote,
                            onSelectNote = viewModel::openNote,
                            onTogglePin = viewModel::onToggleCardPin,
                            onDuplicateNote = viewModel::onDuplicateNote,
                            onShareNote = viewModel::onShareNote,
                            onDeleteNote = viewModel::requestDelete
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    pendingDelete?.let { target ->
        val displayTitle = target.title.ifBlank { stringResource(R.string.untitled_page) }
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_body, displayTitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.dialog_confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun SplitPanePlaceholder(
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folioColors = LocalFolioColors.current
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, folioColors.subtleDivider),
                color = folioColors.paperCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_desk_1795429585717),
                    contentDescription = stringResource(R.string.cd_empty_illustration),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.split_pane_placeholder_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.split_pane_placeholder_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onCreateNote,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.action_new_page))
            }
        }
    }
}
