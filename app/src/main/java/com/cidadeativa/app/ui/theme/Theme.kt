package com.cidadeativa.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EscuroVerde = darkColorScheme(
    primary = Color(0xFF66BB6A),
    onPrimary = Color(0xFF0B2713),
    primaryContainer = Color(0xFF1F4D2B),
    onPrimaryContainer = Color(0xFFC8E6C9),
    inversePrimary = Color(0xFF1B5E20),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF0B2713),
    secondaryContainer = Color(0xFF254632),
    onSecondaryContainer = Color(0xFFD0EBD3),
    tertiary = Color(0xFF4DB6AC),
    onTertiary = Color(0xFF00201D),
    background = Color(0xFF0D1511),
    onBackground = Color(0xFFE3EDE6),
    surface = Color(0xFF15211A),
    onSurface = Color(0xFFE3EDE6),
    surfaceVariant = Color(0xFF22332A),
    onSurfaceVariant = Color(0xFFA9BDB0),
    surfaceContainerLowest = Color(0xFF0A100D),
    surfaceContainerLow = Color(0xFF121C16),
    surfaceContainer = Color(0xFF1A2820),
    surfaceContainerHigh = Color(0xFF203128),
    surfaceContainerHighest = Color(0xFF273A2F),
    inverseSurface = Color(0xFFD8E8DC),
    inverseOnSurface = Color(0xFF10201A),
    outline = Color(0xFF5E7566),
    outlineVariant = Color(0xFF33473B),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF601410)
)

@Composable
fun CidadeAtivaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EscuroVerde,
        content = content
    )
}