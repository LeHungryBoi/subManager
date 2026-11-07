package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

enum class EdgeType(val label: String, val systemId: Int) {
  NONE("None", 0),
  OUTLINE("Outline", 1),
  DROP_SHADOW("Drop Shadow", 2),
  RAISED("Raised", 3),
  DEPRESSED("Depressed", 4)
}

enum class CaptionFontFamily(val label: String, val composeFontFamily: FontFamily) {
  DEFAULT("Default (Roboto)", FontFamily.Default),
  SANS_SERIF("Sans-Serif", FontFamily.SansSerif),
  SERIF("Serif", FontFamily.Serif),
  MONOSPACE("Monospace", FontFamily.Monospace),
  CASUAL("Casual", FontFamily.Cursive)
}

data class CaptionConfig(
  val fontScale: Float = 1.0f,
  val textColor: Long = 0xFFFFFFFF,
  val textOpacity: Float = 1.0f,
  val edgeType: EdgeType = EdgeType.DROP_SHADOW,
  val edgeColor: Long = 0xFF000000,
  val edgeWidth: Float = 1.5f,
  val backgroundColor: Long = 0xFF000000,
  val backgroundOpacity: Float = 0.8f,
  val backgroundCornerRadius: Float = 4f, // dp
  val backgroundPaddingH: Float = 6f, // dp
  val backgroundPaddingV: Float = 2f, // dp
  val windowColor: Long = 0x00000000,
  val windowOpacity: Float = 0.0f,
  val fontFamily: CaptionFontFamily = CaptionFontFamily.DEFAULT,
  val isBold: Boolean = false,
  val letterSpacing: Float = 0.0f, // sp
  val lineHeightMultiplier: Float = 1.25f,
  val verticalPositionPercent: Float = 0.12f // distance from bottom (0.05 - 0.5)
) {
  val resolvedTextColor: Color
    get() = Color(textColor).copy(alpha = textOpacity.coerceIn(0f, 1f))

  val resolvedEdgeColor: Color
    get() = Color(edgeColor)

  val resolvedBackgroundColor: Color
    get() = Color(backgroundColor).copy(alpha = backgroundOpacity.coerceIn(0f, 1f))

  val resolvedWindowColor: Color
    get() = Color(windowColor).copy(alpha = windowOpacity.coerceIn(0f, 1f))
}

data class PresetConfig(
  val id: String,
  val name: String,
  val description: String,
  val config: CaptionConfig
)

object DefaultPresets {
  val list = listOf(
    PresetConfig(
      id = "youtube_default",
      name = "YouTube Standard",
      description = "Default 1.0x size, solid black pill background, drop shadow",
      config = CaptionConfig(
        fontScale = 1.0f,
        textColor = 0xFFFFFFFF,
        textOpacity = 1.0f,
        backgroundColor = 0xFF000000,
        backgroundOpacity = 0.85f,
        edgeType = EdgeType.DROP_SHADOW,
        edgeColor = 0xFF000000,
        backgroundCornerRadius = 4f
      )
    ),
    PresetConfig(
      id = "youtube_compact",
      name = "Compact (0.75x)",
      description = "Smaller subtitles suited for mobile portrait viewing",
      config = CaptionConfig(
        fontScale = 0.75f,
        textColor = 0xFFFFFFFF,
        textOpacity = 1.0f,
        backgroundColor = 0xFF000000,
        backgroundOpacity = 0.80f,
        edgeType = EdgeType.DROP_SHADOW,
        edgeColor = 0xFF000000,
        backgroundCornerRadius = 3f,
        backgroundPaddingH = 5f
      )
    ),
    PresetConfig(
      id = "youtube_ultra_fine",
      name = "Ultra-Fine (0.60x)",
      description = "Subtle unobtrusive captions that do not cover video content",
      config = CaptionConfig(
        fontScale = 0.60f,
        textColor = 0xFFFFFFFF,
        textOpacity = 0.95f,
        backgroundColor = 0xFF000000,
        backgroundOpacity = 0.70f,
        edgeType = EdgeType.DROP_SHADOW,
        edgeColor = 0xFF000000,
        backgroundCornerRadius = 3f
      )
    ),
    PresetConfig(
      id = "cinema_minimal",
      name = "Cinema Minimalist",
      description = "Transparent background with strong drop shadow",
      config = CaptionConfig(
        fontScale = 0.85f,
        textColor = 0xFFFFFFFF,
        textOpacity = 1.0f,
        backgroundColor = 0x00000000,
        backgroundOpacity = 0.0f,
        edgeType = EdgeType.DROP_SHADOW,
        edgeColor = 0xFF000000,
        edgeWidth = 2.0f
      )
    ),
    PresetConfig(
      id = "high_contrast_yellow",
      name = "High Contrast Yellow",
      description = "Yellow text on dark background for high legibility",
      config = CaptionConfig(
        fontScale = 0.90f,
        textColor = 0xFFFFEB3B,
        textOpacity = 1.0f,
        backgroundColor = 0xFF000000,
        backgroundOpacity = 0.90f,
        edgeType = EdgeType.OUTLINE,
        edgeColor = 0xFF000000,
        backgroundCornerRadius = 4f
      )
    ),
    PresetConfig(
      id = "anime_outline",
      name = "Anime Outline",
      description = "Crisp outlined text without background box",
      config = CaptionConfig(
        fontScale = 0.80f,
        textColor = 0xFFFFF176,
        textOpacity = 1.0f,
        backgroundColor = 0x00000000,
        backgroundOpacity = 0.0f,
        edgeType = EdgeType.OUTLINE,
        edgeColor = 0xFF000000,
        edgeWidth = 2.2f,
        isBold = true
      )
    )
  )
}
