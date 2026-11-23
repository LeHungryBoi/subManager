package com.example.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.R

enum class EdgeType(val labelRes: Int, val systemId: Int) {
  NONE(R.string.edge_none, 0),
  OUTLINE(R.string.edge_outline, 1),
  DROP_SHADOW(R.string.edge_drop_shadow, 2),
  RAISED(R.string.edge_raised, 3),
  DEPRESSED(R.string.edge_depressed, 4)
}

enum class CaptionFontFamily(val labelRes: Int, val composeFontFamily: FontFamily) {
  DEFAULT(R.string.font_default, FontFamily.Default),
  SANS_SERIF(R.string.font_sans_serif, FontFamily.SansSerif),
  SERIF(R.string.font_serif, FontFamily.Serif),
  MONOSPACE(R.string.font_monospace, FontFamily.Monospace),
  CASUAL(R.string.font_casual, FontFamily.Cursive)
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
  val name: String = "",
  val description: String = "",
  val nameRes: Int = 0,
  val descriptionRes: Int = 0,
  val config: CaptionConfig
) {
  fun displayName(context: Context): String = if (nameRes != 0) context.getString(nameRes) else name
  fun displayDescription(context: Context): String =
    if (descriptionRes != 0) context.getString(descriptionRes) else description
}

object DefaultPresets {
  val list = listOf(
    PresetConfig(
      id = "youtube_default",
      nameRes = R.string.preset_youtube_default_name,
      descriptionRes = R.string.preset_youtube_default_description,
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
      nameRes = R.string.preset_youtube_compact_name,
      descriptionRes = R.string.preset_youtube_compact_description,
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
      nameRes = R.string.preset_youtube_ultra_fine_name,
      descriptionRes = R.string.preset_youtube_ultra_fine_description,
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
      nameRes = R.string.preset_cinema_minimal_name,
      descriptionRes = R.string.preset_cinema_minimal_description,
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
      nameRes = R.string.preset_high_contrast_yellow_name,
      descriptionRes = R.string.preset_high_contrast_yellow_description,
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
      nameRes = R.string.preset_anime_outline_name,
      descriptionRes = R.string.preset_anime_outline_description,
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
