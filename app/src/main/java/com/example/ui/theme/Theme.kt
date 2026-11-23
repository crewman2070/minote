package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Immutable
data class FolioExtendedColors(
    val sceneContainer: Color,
    val sceneOnContainer: Color,
    val actionContainer: Color,
    val actionOnContainer: Color,
    val dialogueContainer: Color,
    val dialogueOnContainer: Color,
    val noteContainer: Color,
    val noteOnContainer: Color,
    val paperCard: Color,
    val subtleDivider: Color
)

private val LightFolioExtendedColors = FolioExtendedColors(
    sceneContainer = CueSceneContainerLight,
    sceneOnContainer = CueSceneTextLight,
    actionContainer = CueActionContainerLight,
    actionOnContainer = CueActionTextLight,
    dialogueContainer = CueDialogueContainerLight,
    dialogueOnContainer = CueDialogueTextLight,
    noteContainer = CueNoteContainerLight,
    noteOnContainer = CueNoteTextLight,
    paperCard = CrispPaperContainer,
    subtleDivider = KraftOutlineSubtle
)

private val DarkFolioExtendedColors = FolioExtendedColors(
    sceneContainer = CueSceneContainerDark,
    sceneOnContainer = CueSceneTextDark,
    actionContainer = CueActionContainerDark,
    actionOnContainer = CueActionTextDark,
    dialogueContainer = CueDialogueContainerDark,
    dialogueOnContainer = CueDialogueTextDark,
    noteContainer = CueNoteContainerDark,
    noteOnContainer = CueNoteTextDark,
    paperCard = DeepFolioContainer,
    subtleDivider = CharcoalOutlineSubtle
)

val LocalFolioColors = staticCompositionLocalOf { LightFolioExtendedColors }

private val WarmPaperLightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaOnContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageContainer,
    onSecondaryContainer = SageOnContainer,
    tertiary = SlateTertiary,
    onTertiary = SlateOnTertiary,
    tertiaryContainer = SlateContainer,
    onTertiaryContainer = SlateOnContainer,
    background = WarmPaperBackground,
    onBackground = EspressoInk,
    surface = WarmPaperBackground,
    onSurface = EspressoInk,
    surfaceVariant = SoftParchmentSurface,
    onSurfaceVariant = SepiaInkMuted,
    surfaceContainerLowest = CrispPaperContainer,
    surfaceContainerLow = SoftParchmentSurface,
    surfaceContainer = SoftParchmentSurface,
    surfaceContainerHigh = ToastedLinenSurfaceVariant,
    surfaceContainerHighest = ToastedLinenSurfaceVariant,
    outline = KraftOutline,
    outlineVariant = KraftOutlineSubtle,
    error = CrimsonError,
    onError = CrimsonOnError,
    errorContainer = CrimsonErrorContainer,
    onErrorContainer = CrimsonOnErrorContainer
)

private val WarmCharcoalDarkColorScheme = darkColorScheme(
    primary = TerracottaClayDark,
    onPrimary = TerracottaOnDark,
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = TerracottaOnContainerDark,
    secondary = SageDark,
    onSecondary = SageOnDark,
    secondaryContainer = SageContainerDark,
    onSecondaryContainer = SageOnContainerDark,
    tertiary = SlateDark,
    onTertiary = SlateOnDark,
    tertiaryContainer = SlateContainerDark,
    onTertiaryContainer = SlateOnContainerDark,
    background = CharcoalBackground,
    onBackground = WarmCreamInk,
    surface = CharcoalBackground,
    onSurface = WarmCreamInk,
    surfaceVariant = CharcoalFolioSurface,
    onSurfaceVariant = WarmStoneMuted,
    surfaceContainerLowest = CharcoalBackground,
    surfaceContainerLow = DeepFolioContainer,
    surfaceContainer = CharcoalFolioSurface,
    surfaceContainerHigh = SmokedUmberVariant,
    surfaceContainerHighest = SmokedUmberVariant,
    outline = CharcoalOutline,
    outlineVariant = CharcoalOutlineSubtle,
    error = CrimsonErrorDark,
    onError = CrimsonOnErrorDark,
    errorContainer = CrimsonErrorContainerDark,
    onErrorContainer = CrimsonOnErrorContainerDark
)

private val FolioShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun FolioScriptTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) WarmCharcoalDarkColorScheme else WarmPaperLightColorScheme
    val extendedColors = if (darkTheme) DarkFolioExtendedColors else LightFolioExtendedColors

    CompositionLocalProvider(LocalFolioColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FolioTypography,
            shapes = FolioShapes,
            content = content
        )
    }
}
