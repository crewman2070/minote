package com.example.ui

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.FolioDatabase
import com.example.data.NoteKind
import com.example.data.NotesRepository
import com.example.data.ScriptCueHelper
import com.example.data.ScriptCueType
import com.example.data.ScriptNoteEntity
import com.example.data.ThemePreferencesRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LibraryFilter {
    ALL,
    PINNED,
    SCRIPTS,
    NOTES
}

enum class SaveStatus {
    IDLE,
    SAVING,
    SAVED
}

data class LibraryUiState(
    val allNotes: List<ScriptNoteEntity> = emptyList(),
    val filteredNotes: List<ScriptNoteEntity> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: LibraryFilter = LibraryFilter.ALL,
    val totalCount: Int = 0,
    val pinnedCount: Int = 0,
    val scriptCount: Int = 0,
    val noteCount: Int = 0,
    val isLoading: Boolean = true
)

data class EditorUiState(
    val noteId: Long = 0L,
    val title: String = "",
    val contentValue: TextFieldValue = TextFieldValue(""),
    val kind: NoteKind = NoteKind.NOTE,
    val pinned: Boolean = false,
    val isReadMode: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.SAVED,
    val wordCount: Int = 0,
    val readTimeLabel: String = "0 words",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isNewUntouched: Boolean = false
)

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    data class ShareText(val subject: String, val body: String) : UiEvent
}

class NotesViewModel(
    private val repository: NotesRepository,
    private val themePreferences: ThemePreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(LibraryFilter.ALL)

    private val _editorState = MutableStateFlow<EditorUiState?>(null)
    val editorState: StateFlow<EditorUiState?> = _editorState.asStateFlow()

    private val _notePendingDelete = MutableStateFlow<ScriptNoteEntity?>(null)
    val notePendingDelete: StateFlow<ScriptNoteEntity?> = _notePendingDelete.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var autoSaveJob: Job? = null

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ThemeMode.SYSTEM
        )

    val libraryState: StateFlow<LibraryUiState> = combine(
        repository.allNotes,
        _searchQuery,
        _activeFilter
    ) { notes, query, filter ->
        val trimmedQuery = query.trim()
        val pinnedCount = notes.count { it.pinned }
        val scriptCount = notes.count { it.noteKind == NoteKind.SCRIPT || ScriptCueHelper.hasScriptCues(it.content) }
        val noteCount = notes.count { it.noteKind == NoteKind.NOTE && !ScriptCueHelper.hasScriptCues(it.content) }

        val byFilter = when (filter) {
            LibraryFilter.ALL -> notes
            LibraryFilter.PINNED -> notes.filter { it.pinned }
            LibraryFilter.SCRIPTS -> notes.filter {
                it.noteKind == NoteKind.SCRIPT || ScriptCueHelper.hasScriptCues(it.content)
            }
            LibraryFilter.NOTES -> notes.filter {
                it.noteKind == NoteKind.NOTE && !ScriptCueHelper.hasScriptCues(it.content)
            }
        }

        val filtered = if (trimmedQuery.isEmpty()) {
            byFilter
        } else {
            byFilter.filter { note ->
                note.title.contains(trimmedQuery, ignoreCase = true) ||
                    note.content.contains(trimmedQuery, ignoreCase = true)
            }
        }

        LibraryUiState(
            allNotes = notes,
            filteredNotes = filtered,
            searchQuery = query,
            activeFilter = filter,
            totalCount = notes.size,
            pinnedCount = pinnedCount,
            scriptCount = scriptCount,
            noteCount = noteCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = LibraryUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: LibraryFilter) {
        _activeFilter.value = filter
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _activeFilter.value = LibraryFilter.ALL
    }

    fun cycleThemeMode() {
        val next = when (themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        viewModelScope.launch {
            themePreferences.setThemeMode(next)
            val label = when (next) {
                ThemeMode.SYSTEM -> "Theme: System"
                ThemeMode.LIGHT -> "Theme: Warm Paper"
                ThemeMode.DARK -> "Theme: Warm Charcoal"
            }
            _events.emit(UiEvent.ShowMessage(label))
        }
    }

    fun createNewNote() {
        viewModelScope.launch {
            flushPendingSave()
            val initialKind = if (_activeFilter.value == LibraryFilter.SCRIPTS) {
                NoteKind.SCRIPT
            } else {
                NoteKind.NOTE
            }
            val created = repository.createEmptyNote(initialKind)
            _editorState.value = created.toEditorUiState(isNewUntouched = true)
        }
    }

    fun openNote(noteId: Long) {
        val current = _editorState.value
        if (current != null && current.noteId == noteId) return
        viewModelScope.launch {
            flushPendingSave()
            val note = repository.getById(noteId) ?: return@launch
            _editorState.value = note.toEditorUiState(isNewUntouched = false)
        }
    }

    fun closeEditor() {
        viewModelScope.launch {
            val current = _editorState.value
            if (current != null) {
                autoSaveJob?.cancel()
                if (current.isNewUntouched && current.title.isBlank() && current.contentValue.text.isBlank()) {
                    repository.deleteNoteById(current.noteId)
                } else {
                    persistState(current)
                }
            }
            _editorState.value = null
        }
    }

    fun onTitleChange(newTitle: String) {
        _editorState.update { state ->
            state?.let {
                val words = ScriptCueHelper.countWords(newTitle, it.contentValue.text)
                it.copy(
                    title = newTitle,
                    wordCount = words,
                    readTimeLabel = ScriptCueHelper.formatReadTime(words),
                    saveStatus = SaveStatus.SAVING,
                    isNewUntouched = false
                )
            }
        }
        scheduleAutoSave()
    }

    fun onContentChange(newValue: TextFieldValue) {
        _editorState.update { state ->
            state?.let {
                val words = ScriptCueHelper.countWords(it.title, newValue.text)
                it.copy(
                    contentValue = newValue,
                    wordCount = words,
                    readTimeLabel = ScriptCueHelper.formatReadTime(words),
                    saveStatus = SaveStatus.SAVING,
                    isNewUntouched = false
                )
            }
        }
        scheduleAutoSave()
    }

    fun onInsertScriptCue(cue: ScriptCueType) {
        _editorState.update { state ->
            state?.let {
                val updatedValue = ScriptCueHelper.insertCue(it.contentValue, cue)
                val updatedKind = if (cue != ScriptCueType.NOTE && it.kind == NoteKind.NOTE) {
                    NoteKind.SCRIPT
                } else {
                    it.kind
                }
                val words = ScriptCueHelper.countWords(it.title, updatedValue.text)
                it.copy(
                    contentValue = updatedValue,
                    kind = updatedKind,
                    wordCount = words,
                    readTimeLabel = ScriptCueHelper.formatReadTime(words),
                    saveStatus = SaveStatus.SAVING,
                    isReadMode = false,
                    isNewUntouched = false
                )
            }
        }
        scheduleAutoSave()
    }

    fun onToggleKind() {
        val updated = _editorState.updateAndGet { state ->
            state?.let {
                val nextKind = if (it.kind == NoteKind.NOTE) NoteKind.SCRIPT else NoteKind.NOTE
                it.copy(
                    kind = nextKind,
                    saveStatus = SaveStatus.SAVING,
                    isNewUntouched = false
                )
            }
        }
        if (updated != null) {
            viewModelScope.launch { persistState(updated) }
        }
    }

    fun onToggleReadMode() {
        val current = _editorState.value ?: return
        if (!current.isReadMode) {
            viewModelScope.launch { flushPendingSave() }
        }
        _editorState.update { it?.copy(isReadMode = !it.isReadMode) }
    }

    fun onToggleEditorPin() {
        val updated = _editorState.updateAndGet { state ->
            state?.copy(pinned = !state.pinned, saveStatus = SaveStatus.SAVING)
        }
        if (updated != null) {
            viewModelScope.launch { persistState(updated) }
        }
    }

    fun onToggleCardPin(note: ScriptNoteEntity) {
        viewModelScope.launch {
            repository.togglePin(note)
            _editorState.update { state ->
                if (state != null && state.noteId == note.id) {
                    state.copy(pinned = !note.pinned)
                } else {
                    state
                }
            }
        }
    }

    fun onDuplicateNote(note: ScriptNoteEntity) {
        viewModelScope.launch {
            flushPendingSave()
            val fresh = repository.getById(note.id) ?: note
            val copy = repository.duplicateNote(fresh)
            _editorState.value = copy.toEditorUiState(isNewUntouched = false)
            _events.emit(UiEvent.ShowMessage("Duplicated page"))
        }
    }

    fun onDuplicateCurrentEditorNote() {
        val current = _editorState.value ?: return
        viewModelScope.launch {
            persistState(current)
            val entity = repository.getById(current.noteId) ?: return@launch
            val copy = repository.duplicateNote(entity)
            _editorState.value = copy.toEditorUiState(isNewUntouched = false)
            _events.emit(UiEvent.ShowMessage("Duplicated page"))
        }
    }

    fun onShareNote(note: ScriptNoteEntity) {
        viewModelScope.launch {
            val subject = note.title.ifBlank { "Untitled Page" }
            val body = buildShareBody(subject, note.content)
            _events.emit(UiEvent.ShareText(subject = subject, body = body))
        }
    }

    fun onShareCurrentEditorNote() {
        val current = _editorState.value ?: return
        viewModelScope.launch {
            flushPendingSave()
            val subject = current.title.ifBlank { "Untitled Page" }
            val body = buildShareBody(subject, current.contentValue.text)
            _events.emit(UiEvent.ShareText(subject = subject, body = body))
        }
    }

    fun requestDelete(note: ScriptNoteEntity) {
        _notePendingDelete.value = note
    }

    fun requestDeleteCurrentEditorNote() {
        val current = _editorState.value ?: return
        _notePendingDelete.value = ScriptNoteEntity(
            id = current.noteId,
            title = current.title,
            content = current.contentValue.text,
            kind = current.kind.name,
            pinned = current.pinned,
            wordCount = current.wordCount,
            createdAt = current.createdAt,
            updatedAt = current.updatedAt
        )
    }

    fun dismissDeleteDialog() {
        _notePendingDelete.value = null
    }

    fun confirmDelete() {
        val target = _notePendingDelete.value ?: return
        _notePendingDelete.value = null
        viewModelScope.launch {
            if (_editorState.value?.noteId == target.id) {
                autoSaveJob?.cancel()
                _editorState.value = null
            }
            repository.deleteNoteById(target.id)
            _events.emit(UiEvent.ShowMessage("Page deleted"))
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(550L)
            val snapshot = _editorState.value ?: return@launch
            persistState(snapshot)
        }
    }

    private suspend fun flushPendingSave() {
        autoSaveJob?.cancel()
        val snapshot = _editorState.value ?: return
        if (snapshot.isNewUntouched && snapshot.title.isBlank() && snapshot.contentValue.text.isBlank()) {
            repository.deleteNoteById(snapshot.noteId)
            return
        }
        persistState(snapshot)
    }

    private suspend fun persistState(snapshot: EditorUiState) {
        val saved = repository.saveNote(
            id = snapshot.noteId,
            title = snapshot.title,
            content = snapshot.contentValue.text,
            kind = snapshot.kind,
            pinned = snapshot.pinned,
            createdAt = snapshot.createdAt
        )
        _editorState.update { current ->
            if (current != null && current.noteId == snapshot.noteId) {
                current.copy(
                    saveStatus = SaveStatus.SAVED,
                    updatedAt = saved.updatedAt,
                    wordCount = saved.wordCount,
                    readTimeLabel = ScriptCueHelper.formatReadTime(saved.wordCount)
                )
            } else {
                current
            }
        }
    }

    private fun ScriptNoteEntity.toEditorUiState(isNewUntouched: Boolean): EditorUiState {
        val words = ScriptCueHelper.countWords(title, content)
        return EditorUiState(
            noteId = id,
            title = title,
            contentValue = TextFieldValue(
                text = content,
                selection = TextRange(content.length)
            ),
            kind = noteKind,
            pinned = pinned,
            isReadMode = false,
            saveStatus = SaveStatus.SAVED,
            wordCount = words,
            readTimeLabel = ScriptCueHelper.formatReadTime(words),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isNewUntouched = isNewUntouched
        )
    }

    private fun buildShareBody(subject: String, content: String): String {
        return if (content.isBlank()) subject else "$subject\n\n$content"
    }

    private inline fun <T> MutableStateFlow<T>.updateAndGet(transform: (T) -> T): T {
        while (true) {
            val prev = value
            val next = transform(prev)
            if (compareAndSet(prev, next)) return next
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                val db = FolioDatabase.getInstance(appContext)
                NotesViewModel(
                    repository = NotesRepository(db.scriptNoteDao()),
                    themePreferences = ThemePreferencesRepository(appContext)
                )
            }
        }
    }
}
