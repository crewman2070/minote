package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.data.FormattedScriptLine
import com.example.data.ScriptCueHelper
import com.example.data.ScriptCueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptCueHelperTest {

    @Test
    fun insertCue_intoEmptyDocument_startsAtZeroAndPositionsCaret() {
        val result = ScriptCueHelper.insertCue(TextFieldValue(""), ScriptCueType.SCENE)
        assertEquals("[SCENE] ", result.text)
        assertEquals(TextRange("[SCENE] ".length), result.selection)
    }

    @Test
    fun insertCue_afterExistingLine_addsCleanLineBreak() {
        val initial = TextFieldValue(
            text = "Opening beat",
            selection = TextRange("Opening beat".length)
        )
        val result = ScriptCueHelper.insertCue(initial, ScriptCueType.DIALOGUE)
        assertEquals("Opening beat\nDIALOGUE: ", result.text)
        assertEquals(TextRange(result.text.length), result.selection)
    }

    @Test
    fun detectCues_identifiesDistinctCueMarkers() {
        val script = """
            [SCENE] INT. STUDIO - DAY
            ACTION: Camera glides toward the desk.
            DIALOGUE: Welcome back to FolioScript.
            [NOTE] Keep pacing relaxed.
        """.trimIndent()

        val detected = ScriptCueHelper.detectCues(script)
        assertEquals(
            listOf(
                ScriptCueType.SCENE,
                ScriptCueType.ACTION,
                ScriptCueType.DIALOGUE,
                ScriptCueType.NOTE
            ),
            detected
        )
    }

    @Test
    fun parseForReader_mapsLinesToStructuredBlocks() {
        val script = "[SCENE] Roof\nACTION: Wind blows\nDIALOGUE: Hello\n[NOTE] Cut here\nPlain line"
        val parsed = ScriptCueHelper.parseForReader(script)

        assertEquals(5, parsed.size)
        assertEquals(FormattedScriptLine.Scene("Roof"), parsed[0])
        assertEquals(FormattedScriptLine.Action("Wind blows"), parsed[1])
        assertEquals(FormattedScriptLine.Dialogue("Hello"), parsed[2])
        assertEquals(FormattedScriptLine.MarginNote("Cut here"), parsed[3])
        assertEquals(FormattedScriptLine.Body("Plain line"), parsed[4])
    }

    @Test
    fun countWordsAndReadTime_handlesEmptyAndPopulatedText() {
        assertEquals(0, ScriptCueHelper.countWords("", "   "))
        assertEquals("0 words", ScriptCueHelper.formatReadTime(0))

        val count = ScriptCueHelper.countWords("Episode One", "Intro line for the script")
        assertEquals(7, count)
        assertTrue(ScriptCueHelper.formatReadTime(count).contains("7 words"))
    }
}
