package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NoteKind
import com.example.data.ScriptCueHelper
import com.example.data.ScriptCueType
import com.example.data.ScriptNoteEntity
import com.example.ui.theme.LocalFolioColors
import com.example.ui.theme.ThemeMode

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    themeMode: ThemeMode,
    selectedNoteId: Long?,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (LibraryFilter) -> Unit,
    onClearFilters: () -> Unit,
    onCycleTheme: () -> Unit,
    onCreateNote: () -> Unit,
    onSelectNote: (Long) -> Unit,
    onTogglePin: (ScriptNoteEntity) -> Unit,
    onDuplicateNote: (ScriptNoteEntity) -> Unit,
    onShareNote: (ScriptNoteEntity) -> Unit,
    onDeleteNote: (ScriptNoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("new_note_fab"),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = stringResource(R.string.cd_new_note)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.action_new_page),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            LibraryHeader(
                totalCount = uiState.totalCount,
                themeMode = themeMode,
                onCycleTheme = onCycleTheme
            )

            SearchField(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterStrip(
                uiState = uiState,
                onFilterSelected = onFilterSelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize())
                }
                uiState.allNotes.isEmpty() -> {
                    EmptyLibraryState(
                        onCreateNote = onCreateNote,
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    )
                }
                uiState.filteredNotes.isEmpty() -> {
                    EmptyFilteredState(
                        onClearFilters = onClearFilters,
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    )
                }
                else -> {
                    NotesList(
                        notes = uiState.filteredNotes,
                        showSectionHeaders = uiState.activeFilter == LibraryFilter.ALL &&
                            uiState.searchQuery.isBlank(),
                        selectedNoteId = selectedNoteId,
                        onSelectNote = onSelectNote,
                        onTogglePin = onTogglePin,
                        onDuplicateNote = onDuplicateNote,
                        onShareNote = onShareNote,
                        onDeleteNote = onDeleteNote
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryHeader(
    totalCount: Int,
    themeMode: ThemeMode,
    onCycleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (totalCount == 0) {
                    stringResource(R.string.library_subtitle_empty)
                } else {
                    stringResource(R.string.library_subtitle_count, totalCount)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onCycleTheme,
            modifier = Modifier
                .size(48.dp)
                .testTag("theme_toggle_button")
        ) {
            val icon = when (themeMode) {
                ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                ThemeMode.LIGHT -> Icons.Filled.LightMode
                ThemeMode.DARK -> Icons.Filled.DarkMode
            }
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.cd_theme_toggle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val folioColors = LocalFolioColors.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.testTag("clear_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = folioColors.paperCard,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = folioColors.subtleDivider
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_notes_input")
    )
}

@Composable
private fun FilterStrip(
    uiState: LibraryUiState,
    onFilterSelected: (LibraryFilter) -> Unit
) {
    val filters = listOf(
        Triple(LibraryFilter.ALL, stringResource(R.string.filter_all), uiState.totalCount),
        Triple(LibraryFilter.PINNED, stringResource(R.string.filter_pinned), uiState.pinnedCount),
        Triple(LibraryFilter.SCRIPTS, stringResource(R.string.filter_scripts), uiState.scriptCount),
        Triple(LibraryFilter.NOTES, stringResource(R.string.filter_notes), uiState.noteCount)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters, key = { it.first.name }) { (filter, label, count) ->
            val selected = uiState.activeFilter == filter
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}"),
                label = {
                    Text(
                        text = if (count > 0) "$label ($count)" else label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun NotesList(
    notes: List<ScriptNoteEntity>,
    showSectionHeaders: Boolean,
    selectedNoteId: Long?,
    onSelectNote: (Long) -> Unit,
    onTogglePin: (ScriptNoteEntity) -> Unit,
    onDuplicateNote: (ScriptNoteEntity) -> Unit,
    onShareNote: (ScriptNoteEntity) -> Unit,
    onDeleteNote: (ScriptNoteEntity) -> Unit
) {
    val pinnedNotes = remember(notes) { notes.filter { it.pinned } }
    val unpinnedNotes = remember(notes) { notes.filterNot { it.pinned } }
    val splitSections = showSectionHeaders && pinnedNotes.isNotEmpty() && unpinnedNotes.isNotEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (splitSections) {
            item(key = "header_pinned") {
                SectionLabel(text = stringResource(R.string.section_pinned))
            }
            items(pinnedNotes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    isSelected = selectedNoteId == note.id,
                    onClick = { onSelectNote(note.id) },
                    onTogglePin = { onTogglePin(note) },
                    onDuplicate = { onDuplicateNote(note) },
                    onShare = { onShareNote(note) },
                    onDelete = { onDeleteNote(note) }
                )
            }
            item(key = "header_recent") {
                SectionLabel(
                    text = stringResource(R.string.section_recent),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(unpinnedNotes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    isSelected = selectedNoteId == note.id,
                    onClick = { onSelectNote(note.id) },
                    onTogglePin = { onTogglePin(note) },
                    onDuplicate = { onDuplicateNote(note) },
                    onShare = { onShareNote(note) },
                    onDelete = { onDeleteNote(note) }
                )
            }
        } else {
            items(notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    isSelected = selectedNoteId == note.id,
                    onClick = { onSelectNote(note.id) },
                    onTogglePin = { onTogglePin(note) },
                    onDuplicate = { onDuplicateNote(note) },
                    onShare = { onShareNote(note) },
                    onDelete = { onDeleteNote(note) }
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun NoteCard(
    note: ScriptNoteEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val folioColors = LocalFolioColors.current
    var menuExpanded by remember { mutableStateOf(false) }
    val detectedCues = remember(note.content) { ScriptCueHelper.detectCues(note.content) }
    val isScript = note.noteKind == NoteKind.SCRIPT || detectedCues.isNotEmpty()

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        folioColors.paperCard
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        folioColors.subtleDivider
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}")
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    KindPill(isScript = isScript)
                    Text(
                        text = ScriptCueHelper.formatRelativeTime(note.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("pin_note_${note.id}")
                ) {
                    Icon(
                        imageVector = if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = stringResource(
                            if (note.pinned) R.string.action_unpin else R.string.action_pin
                        ),
                        tint = if (note.pinned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("note_menu_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (note.pinned) R.string.action_unpin else R.string.action_pin
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onTogglePin()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_duplicate)) },
                            leadingIcon = {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_share)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Share, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.title.ifBlank { stringResource(R.string.untitled_page) },
                style = MaterialTheme.typography.titleLarge,
                color = if (note.title.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            val snippet = remember(note.content) {
                note.content
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .take(3)
                    .joinToString("  ")
            }
            Text(
                text = snippet.ifBlank { stringResource(R.string.empty_snippet) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 8.dp)
            )

            if (detectedCues.isNotEmpty() || note.wordCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        detectedCues.take(3).forEach { cue ->
                            MiniCueBadge(cue = cue)
                        }
                    }

                    if (note.wordCount > 0) {
                        Text(
                            text = ScriptCueHelper.formatReadTime(note.wordCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KindPill(isScript: Boolean) {
    val bg = if (isScript) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = if (isScript) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = if (isScript) Icons.Outlined.MovieCreation else Icons.Outlined.Description,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = stringResource(if (isScript) R.string.kind_script else R.string.kind_note).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg
        )
    }
}

@Composable
private fun MiniCueBadge(cue: ScriptCueType) {
    val folioColors = LocalFolioColors.current
    val (bg, fg) = when (cue) {
        ScriptCueType.SCENE -> folioColors.sceneContainer to folioColors.sceneOnContainer
        ScriptCueType.ACTION -> folioColors.actionContainer to folioColors.actionOnContainer
        ScriptCueType.DIALOGUE -> folioColors.dialogueContainer to folioColors.dialogueOnContainer
        ScriptCueType.NOTE -> folioColors.noteContainer to folioColors.noteOnContainer
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = cue.badgeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = fg
        )
    }
}

@Composable
private fun EmptyLibraryState(
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folioColors = LocalFolioColors.current
    Box(
        modifier = modifier.padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, folioColors.subtleDivider),
                color = folioColors.paperCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_desk_1795429585717),
                    contentDescription = stringResource(R.string.cd_empty_illustration),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.empty_library_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.empty_library_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onCreateNote,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("empty_start_writing_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.empty_library_cta),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun EmptyFilteredState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.empty_filtered_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_filtered_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedButton(
                onClick = onClearFilters,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.testTag("clear_filters_button")
            ) {
                Text(
                    text = stringResource(R.string.action_clear_filters),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
