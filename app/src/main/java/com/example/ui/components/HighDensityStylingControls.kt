package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CaptionConfig
import com.example.model.CaptionFontFamily
import com.example.model.EdgeType
import kotlin.math.roundToInt

@Composable
fun HighDensityStylingControls(
  config: CaptionConfig,
  onConfigChange: (CaptionConfig) -> Unit,
  modifier: Modifier = Modifier
) {
  // State for active Photoshop Color Picker (resource id doubles as the stable target key)
  var activeColorPickerTarget by remember { mutableStateOf<Int?>(null) }
  var activeColorValue by remember { mutableStateOf(0xFFFFFFFFL) }

  // Color picker dialog handler
  activeColorPickerTarget?.let { target ->
    PhotoshopColorPickerDialog(
      title = stringResource(target),
      initialColor = activeColorValue,
      onColorSelected = { selectedArgb ->
        when (target) {
          R.string.color_target_text -> onConfigChange(config.copy(textColor = selectedArgb))
          R.string.color_target_background -> onConfigChange(config.copy(backgroundColor = selectedArgb))
          R.string.color_target_edge -> onConfigChange(config.copy(edgeColor = selectedArgb))
          R.string.color_target_window -> onConfigChange(config.copy(windowColor = selectedArgb))
        }
        activeColorPickerTarget = null
      },
      onDismiss = { activeColorPickerTarget = null }
    )
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // -------------------------------------------------------------
    // SECTION 1: SUBTITLE SCALE (GRANULAR)
    // -------------------------------------------------------------
    DensitySectionCard(title = stringResource(R.string.section_subtitle_scale)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        IconButton(
          onClick = {
            val newScale = (config.fontScale - 0.05f).coerceIn(0.20f, 3.00f)
            onConfigChange(config.copy(fontScale = ((newScale * 100).roundToInt() / 100f)))
          },
          modifier = Modifier
            .size(32.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .testTag("scale_decrease_button")
        ) {
          Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.cd_decrease_scale), modifier = Modifier.size(16.dp))
        }

        Slider(
          value = config.fontScale,
          onValueChange = { raw ->
            val rounded = (raw * 100).roundToInt() / 100f
            onConfigChange(config.copy(fontScale = rounded))
          },
          valueRange = 0.20f..2.50f,
          modifier = Modifier
            .weight(1f)
            .testTag("scale_slider"),
          colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary
          )
        )

        IconButton(
          onClick = {
            val newScale = (config.fontScale + 0.05f).coerceIn(0.20f, 3.00f)
            onConfigChange(config.copy(fontScale = ((newScale * 100).roundToInt() / 100f)))
          },
          modifier = Modifier
            .size(32.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .testTag("scale_increase_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_increase_scale), modifier = Modifier.size(16.dp))
        }

        // Value Badge
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.testTag("scale_value_chip")
        ) {
          Text(
            text = "${String.format("%.2f", config.fontScale)}x",
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Text(
        text = stringResource(
          R.string.scale_explanation,
          String.format("%.2f", config.fontScale)
        ),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        modifier = Modifier.padding(top = 2.dp, start = 2.dp)
      )
    }

    // -------------------------------------------------------------
    // SECTION 2: TEXT APPEARANCE
    // -------------------------------------------------------------
    DensitySectionCard(title = stringResource(R.string.section_text_appearance)) {
      // Row 1: Color Button + Bold Switch
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        CompactColorButton(
          label = stringResource(R.string.color_target_text),
          colorArgb = config.textColor,
          opacity = config.textOpacity,
          onClick = {
            activeColorValue = config.textColor
            activeColorPickerTarget = R.string.color_target_text
          },
          testTag = "btn_text_color"
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = stringResource(R.string.label_bold),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Switch(
            checked = config.isBold,
            onCheckedChange = { onConfigChange(config.copy(isBold = it)) },
            modifier = Modifier.size(width = 44.dp, height = 28.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Row 2: Text Opacity
      CompactSliderRow(
        label = stringResource(R.string.label_text_opacity),
        value = config.textOpacity,
        valueDisplay = "${(config.textOpacity * 100).roundToInt()}%",
        onValueChange = { onConfigChange(config.copy(textOpacity = it)) },
        range = 0.1f..1.0f
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Row 3: Font Family Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CaptionFontFamily.values().forEach { family ->
          val isSelected = config.fontFamily == family
          FilterChip(
            selected = isSelected,
            onClick = { onConfigChange(config.copy(fontFamily = family)) },
            label = { Text(stringResource(family.labelRes), fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }
    }

    // -------------------------------------------------------------
    // SECTION 3: BACKGROUND CONTAINER
    // -------------------------------------------------------------
    DensitySectionCard(title = stringResource(R.string.section_background_container)) {
      // Row 1: Background Color Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        CompactColorButton(
          label = stringResource(R.string.color_target_background),
          colorArgb = config.backgroundColor,
          opacity = config.backgroundOpacity,
          onClick = {
            activeColorValue = config.backgroundColor
            activeColorPickerTarget = R.string.color_target_background
          },
          testTag = "btn_bg_color"
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Row 2: Opacity Slider
      CompactSliderRow(
        label = stringResource(R.string.label_bg_opacity),
        value = config.backgroundOpacity,
        valueDisplay = "${(config.backgroundOpacity * 100).roundToInt()}%",
        onValueChange = { onConfigChange(config.copy(backgroundOpacity = it)) },
        range = 0f..1.0f
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Row 3: Corner Radius
      CompactSliderRow(
        label = stringResource(R.string.label_corner_radius),
        value = config.backgroundCornerRadius,
        valueDisplay = "${config.backgroundCornerRadius.roundToInt()} dp",
        onValueChange = { onConfigChange(config.copy(backgroundCornerRadius = it)) },
        range = 0f..16f
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Row 4: Horizontal Padding
      CompactSliderRow(
        label = stringResource(R.string.label_horiz_padding),
        value = config.backgroundPaddingH,
        valueDisplay = "${config.backgroundPaddingH.roundToInt()} dp",
        onValueChange = { onConfigChange(config.copy(backgroundPaddingH = it)) },
        range = 2f..16f
      )
    }

    // -------------------------------------------------------------
    // SECTION 4: EDGE & SHADOW
    // -------------------------------------------------------------
    DensitySectionCard(title = stringResource(R.string.section_edge_effect)) {
      // Edge Type Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        EdgeType.values().forEach { type ->
          val isSelected = config.edgeType == type
          FilterChip(
            selected = isSelected,
            onClick = { onConfigChange(config.copy(edgeType = type)) },
            label = { Text(stringResource(type.labelRes), fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      if (config.edgeType != EdgeType.NONE) {
        Spacer(modifier = Modifier.height(4.dp))

        // Edge Color Button
        CompactColorButton(
          label = stringResource(R.string.color_button_edge_shadow),
          colorArgb = config.edgeColor,
          opacity = 1f,
          onClick = {
            activeColorValue = config.edgeColor
            activeColorPickerTarget = R.string.color_target_edge
          },
          testTag = "btn_edge_color"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Edge Thickness / Depth
        CompactSliderRow(
          label = stringResource(R.string.label_edge_thickness),
          value = config.edgeWidth,
          valueDisplay = "${String.format("%.1f", config.edgeWidth)}x",
          onValueChange = { onConfigChange(config.copy(edgeWidth = it)) },
          range = 0.5f..3.0f
        )
      }
    }

    // -------------------------------------------------------------
    // SECTION 5: WINDOW & POSITION
    // -------------------------------------------------------------
    DensitySectionCard(title = stringResource(R.string.section_window_position)) {
      // Window Color Button
      CompactColorButton(
        label = stringResource(R.string.color_button_window_box),
        colorArgb = config.windowColor,
        opacity = config.windowOpacity,
        onClick = {
          activeColorValue = config.windowColor
          activeColorPickerTarget = R.string.color_target_window
        },
        testTag = "btn_window_color"
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Window Opacity
      CompactSliderRow(
        label = stringResource(R.string.label_window_opacity),
        value = config.windowOpacity,
        valueDisplay = "${(config.windowOpacity * 100).roundToInt()}%",
        onValueChange = { onConfigChange(config.copy(windowOpacity = it)) },
        range = 0f..1.0f
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Vertical Offset from Bottom
      CompactSliderRow(
        label = stringResource(R.string.label_bottom_distance),
        value = config.verticalPositionPercent,
        valueDisplay = "${(config.verticalPositionPercent * 100).roundToInt()}%",
        onValueChange = { onConfigChange(config.copy(verticalPositionPercent = it)) },
        range = 0.05f..0.45f
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Line Height Spacing
      CompactSliderRow(
        label = stringResource(R.string.label_line_height),
        value = config.lineHeightMultiplier,
        valueDisplay = "${String.format("%.2f", config.lineHeightMultiplier)}x",
        onValueChange = { onConfigChange(config.copy(lineHeightMultiplier = it)) },
        range = 1.0f..1.8f
      )
    }
  }
}

/**
 * Compact section wrapper with 10sp subtle uppercase header and tight padding
 */
@Composable
private fun DensitySectionCard(
  title: String,
  content: @Composable () -> Unit
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      content()
    }
  }
}

/**
 * Single-line compact slider row: Label, Slider, Value Badge
 */
@Composable
private fun CompactSliderRow(
  label: String,
  value: Float,
  valueDisplay: String,
  onValueChange: (Float) -> Unit,
  range: ClosedFloatingPointRange<Float>
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.width(96.dp)
    )

    Slider(
      value = value,
      onValueChange = {
        val rounded = ((it * 100).roundToInt()) / 100f
        onValueChange(rounded)
      },
      valueRange = range,
      modifier = Modifier.weight(1f)
    )

    Surface(
      shape = RoundedCornerShape(4.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier.width(48.dp)
    ) {
      Text(
        text = valueDisplay,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
      )
    }
  }
}

/**
 * Compact interactive button that previews color swatch + hex code and launches Photoshop Color Picker
 */
@Composable
private fun CompactColorButton(
  label: String,
  colorArgb: Long,
  opacity: Float,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Swatch pill
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(CircleShape)
          .background(Color(colorArgb).copy(alpha = opacity))
          .border(1.dp, Color.Gray.copy(alpha = 0.6f), CircleShape)
      )

      Column {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = argbToHex(colorArgb),
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Icon(
        imageVector = Icons.Default.Palette,
        contentDescription = stringResource(R.string.cd_pick_color),
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}
