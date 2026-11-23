package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FormattedScriptLine
import com.example.data.NoteKind
import com.example.data.ScriptCueHelper
import com.example.data.ScriptCueType
import com.example.ui.theme.LocalFolioColors

@Composable
fun EditorScreen(
    state: EditorUiState,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onInsertCue: (ScriptCueType) -> Unit,
    onToggleKind: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleReadMode: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showBackButton) {
        BackHandler(onBack = onBack)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            EditorTopBar(
                saveStatus = state.saveStatus,
                pinned = state.pinned,
                isReadMode = state.isReadMode,
                showBackButton = showBackButton,
                onBack = onBack,
                onTogglePin = onTogglePin,
                onToggleReadMode = onToggleReadMode,
                onDuplicate = onDuplicate,
                onShare = onShare,
                onDelete = onDelete
            )
        },
        bottomBar = {
            EditorBottomCueDock(
                isReadMode = state.isReadMode,
                readTimeLabel = state.readTimeLabel,
                onInsertCue = onInsertCue
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = state.isReadMode,
                label = "editor_mode_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
            ) { readMode ->
                if (readMode) {
                    EditorialReadView(
                        state = state,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    EditorialWriteView(
                        state = state,
                        onTitleChange = onTitleChange,
                        onContentChange = onContentChange,
                        onToggleKind = onToggleKind,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorTopBar(
    saveStatus: SaveStatus,
    pinned: Boolean,
    isReadMode: Boolean,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleReadMode: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val folioColors = LocalFolioColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }

            SaveStatusPill(
                saveStatus = saveStatus,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onTogglePin,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("editor_pin_button")
            ) {
                Icon(
                    imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = stringResource(
                        if (pinned) R.string.action_unpin else R.string.action_pin
                    ),
                    tint = if (pinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            IconButton(
                onClick = onToggleReadMode,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("editor_read_mode_button")
            ) {
                Icon(
                    imageVector = if (isReadMode) Icons.Outlined.Edit else Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = stringResource(
                        if (isReadMode) R.string.action_edit_mode else R.string.action_read_mode
                    ),
                    tint = if (isReadMode) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("editor_more_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.action_more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_duplicate)) },
                        leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_share)) },
                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
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
        HorizontalDivider(thickness = 1.dp, color = folioColors.subtleDivider)
    }
}

@Composable
private fun SaveStatusPill(
    saveStatus: SaveStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val dotColor = when (saveStatus) {
            SaveStatus.SAVING -> MaterialTheme.colorScheme.primary
            SaveStatus.SAVED, SaveStatus.IDLE -> MaterialTheme.colorScheme.secondary
        }
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = stringResource(
                if (saveStatus == SaveStatus.SAVING) R.string.status_saving else R.string.status_saved
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EditorialWriteView(
    state: EditorUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onToggleKind: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val folioColors = LocalFolioColors.current

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            KindToggleChip(
                kind = state.kind,
                onClick = onToggleKind
            )
            Text(
                text = ScriptCueHelper.formatFullDate(state.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            if (state.title.isEmpty()) {
                Text(
                    text = stringResource(R.string.editor_title_placeholder),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }
            BasicTextField(
                value = state.title,
                onValueChange = onTitleChange,
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(thickness = 1.dp, color = folioColors.subtleDivider)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 360.dp)
        ) {
            if (state.contentValue.text.isEmpty()) {
                Text(
                    text = stringResource(R.string.editor_body_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }
            BasicTextField(
                value = state.contentValue,
                onValueChange = onContentChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 27.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 360.dp)
                    .testTag("editor_body_input")
            )
        }
    }
}

@Composable
private fun KindToggleChip(
    kind: NoteKind,
    onClick: () -> Unit
) {
    val isScript = kind == NoteKind.SCRIPT
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isScript) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isScript) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("editor_kind_toggle")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isScript) Icons.Outlined.MovieCreation else Icons.Outlined.Description,
                contentDescription = null,
                tint = if (isScript) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(if (isScript) R.string.kind_script else R.string.kind_note).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isScript) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun EditorBottomCueDock(
    isReadMode: Boolean,
    readTimeLabel: String,
    onInsertCue: (ScriptCueType) -> Unit
) {
    val folioColors = LocalFolioColors.current

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 1.dp, color = folioColors.subtleDivider)

            if (!isReadMode) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(ScriptCueType.entries, key = { it.name }) { cue ->
                        CueActionPill(
                            cue = cue,
                            onClick = { onInsertCue(cue) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isReadMode) {
                        stringResource(R.string.action_read_mode).uppercase()
                    } else {
                        "QUICK SCRIPT CUES"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = readTimeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("editor_word_count")
                )
            }
        }
    }
}

@Composable
private fun CueActionPill(
    cue: ScriptCueType,
    onClick: () -> Unit
) {
    val folioColors = LocalFolioColors.current
    val (labelRes, icon, bg, fg) = when (cue) {
        ScriptCueType.SCENE -> CueVisual(
            R.string.cue_scene,
            Icons.Outlined.Theaters,
            folioColors.sceneContainer,
            folioColors.sceneOnContainer
        )
        ScriptCueType.ACTION -> CueVisual(
            R.string.cue_action,
            Icons.Outlined.DirectionsRun,
            folioColors.actionContainer,
            folioColors.actionOnContainer
        )
        ScriptCueType.DIALOGUE -> CueVisual(
            R.string.cue_dialogue,
            Icons.Outlined.ChatBubbleOutline,
            folioColors.dialogueContainer,
            folioColors.dialogueOnContainer
        )
        ScriptCueType.NOTE -> CueVisual(
            R.string.cue_note,
            Icons.Outlined.StickyNote2,
            folioColors.noteContainer,
            folioColors.noteOnContainer
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.18f)),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("cue_button_${cue.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "+ ${stringResource(labelRes)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}

private data class CueVisual(
    val labelRes: Int,
    val icon: ImageVector,
    val container: Color,
    val onContainer: Color
)

@Composable
private fun EditorialReadView(
    state: EditorUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val folioColors = LocalFolioColors.current
    val parsedLines = remember(state.contentValue.text) {
        ScriptCueHelper.parseForReader(state.contentValue.text)
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag("editor_read_view")
    ) {
        Text(
            text = ScriptCueHelper.formatFullDate(state.updatedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.title.ifBlank { stringResource(R.string.untitled_page) },
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp, color = folioColors.subtleDivider)
        Spacer(modifier = Modifier.height(20.dp))

        if (parsedLines.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_snippet),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                parsedLines.forEach { line ->
                    when (line) {
                        is FormattedScriptLine.Blank -> Spacer(modifier = Modifier.height(4.dp))
                        is FormattedScriptLine.Scene -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = folioColors.sceneContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = "SCENE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = folioColors.sceneOnContainer
                                    )
                                    if (line.text.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = line.text.uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = folioColors.sceneOnContainer
                                        )
                                    }
                                }
                            }
                        }
                        is FormattedScriptLine.Action -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 3.dp)
                                        .background(folioColors.actionContainer, RoundedCornerShape(5.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = folioColors.actionOnContainer
                                    )
                                }
                                Text(
                                    text = line.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        is FormattedScriptLine.Dialogue -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = folioColors.dialogueContainer.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = "DIALOGUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = folioColors.dialogueOnContainer
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                        is FormattedScriptLine.MarginNote -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = folioColors.noteContainer.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "NOTE — ${line.text}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = folioColors.noteOnContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                        is FormattedScriptLine.Body -> {
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
