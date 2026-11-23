package com.example.data

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

enum class ScriptCueType(val marker: String, val badgeLabel: String) {
    SCENE("[SCENE] ", "SCENE"),
    ACTION("ACTION: ", "ACTION"),
    DIALOGUE("DIALOGUE: ", "DIALOGUE"),
    NOTE("[NOTE] ", "NOTE")
}

sealed interface FormattedScriptLine {
    data class Scene(val text: String) : FormattedScriptLine
    data class Action(val text: String) : FormattedScriptLine
    data class Dialogue(val text: String) : FormattedScriptLine
    data class MarginNote(val text: String) : FormattedScriptLine
    data class Body(val text: String) : FormattedScriptLine
    data object Blank : FormattedScriptLine
}

object ScriptCueHelper {

    /**
     * Inserts a script cue marker cleanly at the cursor position, ensuring the cue
     * starts on a fresh line without awkward crowding, and places the caret right
     * after the inserted cue prefix ready for typing.
     */
    fun insertCue(current: TextFieldValue, cue: ScriptCueType): TextFieldValue {
        val text = current.text
        val selectionStart = current.selection.min.coerceIn(0, text.length)
        val selectionEnd = current.selection.max.coerceIn(0, text.length)

        val before = text.substring(0, selectionStart)
        val after = text.substring(selectionEnd)

        val leadingBreak = when {
            before.isEmpty() -> ""
            before.endsWith("\n\n") -> ""
            before.endsWith("\n") -> if (cue == ScriptCueType.SCENE) "\n" else ""
            else -> if (cue == ScriptCueType.SCENE) "\n\n" else "\n"
        }

        val inserted = leadingBreak + cue.marker
        val newText = before + inserted + after
        val newCursor = before.length + inserted.length

        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    fun detectCues(content: String): List<ScriptCueType> {
        if (content.isBlank()) return emptyList()
        val found = mutableListOf<ScriptCueType>()
        val lines = content.lineSequence().map { it.trimStart() }
        for (line in lines) {
            when {
                line.startsWith("[SCENE]", ignoreCase = true) ||
                    line.startsWith("SCENE:", ignoreCase = true) -> {
                    if (ScriptCueType.SCENE !in found) found.add(ScriptCueType.SCENE)
                }
                line.startsWith("ACTION:", ignoreCase = true) -> {
                    if (ScriptCueType.ACTION !in found) found.add(ScriptCueType.ACTION)
                }
                line.startsWith("DIALOGUE:", ignoreCase = true) -> {
                    if (ScriptCueType.DIALOGUE !in found) found.add(ScriptCueType.DIALOGUE)
                }
                line.startsWith("[NOTE]", ignoreCase = true) -> {
                    if (ScriptCueType.NOTE !in found) found.add(ScriptCueType.NOTE)
                }
            }
            if (found.size == ScriptCueType.entries.size) break
        }
        return found
    }

    fun hasScriptCues(content: String): Boolean = detectCues(content).isNotEmpty()

    fun countWords(title: String, content: String): Int {
        val combined = "$title $content".trim()
        if (combined.isEmpty()) return 0
        return combined.split(Regex("\\s+")).count { it.isNotBlank() }
    }

    fun formatReadTime(wordCount: Int): String {
        if (wordCount == 0) return "0 words"
        val minutes = ceil(wordCount / 150.0).toInt().coerceAtLeast(1)
        val wordLabel = if (wordCount == 1) "1 word" else "$wordCount words"
        return "$wordLabel · ~${minutes}m read"
    }

    fun formatRelativeTime(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diff = (nowMillis - timestampMillis).coerceAtLeast(0L)
        val minuteMs = 60_000L
        val hourMs = 3_600_000L
        val dayMs = 86_400_000L
        return when {
            diff < minuteMs -> "Just now"
            diff < hourMs -> "${diff / minuteMs}m ago"
            diff < dayMs -> "${diff / hourMs}h ago"
            diff < 7 * dayMs -> "${diff / dayMs}d ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestampMillis))
        }
    }

    fun formatFullDate(timestampMillis: Long): String {
        return SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
            .format(Date(timestampMillis))
    }

    fun parseForReader(content: String): List<FormattedScriptLine> {
        if (content.isBlank()) return emptyList()
        return content.lines().map { rawLine ->
            val trimmed = rawLine.trim()
            when {
                trimmed.isEmpty() -> FormattedScriptLine.Blank
                trimmed.startsWith("[SCENE]", ignoreCase = true) -> {
                    FormattedScriptLine.Scene(trimmed.drop(7).trim())
                }
                trimmed.startsWith("SCENE:", ignoreCase = true) -> {
                    FormattedScriptLine.Scene(trimmed.drop(6).trim())
                }
                trimmed.startsWith("ACTION:", ignoreCase = true) -> {
                    FormattedScriptLine.Action(trimmed.drop(7).trim())
                }
                trimmed.startsWith("DIALOGUE:", ignoreCase = true) -> {
                    FormattedScriptLine.Dialogue(trimmed.drop(9).trim())
                }
                trimmed.startsWith("[NOTE]", ignoreCase = true) -> {
                    FormattedScriptLine.MarginNote(trimmed.drop(6).trim())
                }
                else -> FormattedScriptLine.Body(rawLine)
            }
        }
    }
}
