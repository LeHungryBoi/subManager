package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

val CuratedSubtitlePresets = listOf(
  0xFFFFFFFF to "White",
  0xFF000000 to "Black",
  0xFFFFEB3B to "Yellow",
  0xFF00E5FF to "Cyan",
  0xFF4CAF50 to "Green",
  0xFFE91E63 to "Magenta",
  0xFFF44336 to "Red",
  0xFF2196F3 to "Blue",
  0xFFFF9800 to "Orange",
  0xFF9E9E9E to "Grey",
  0xFF212121 to "Dark Grey"
)

fun argbToHsv(colorLong: Long): Triple<Float, Float, Float> {
  val r = ((colorLong shr 16) and 0xFF).toInt()
  val g = ((colorLong shr 8) and 0xFF).toInt()
  val b = (colorLong and 0xFF).toInt()
  val hsv = FloatArray(3)
  AndroidColor.RGBToHSV(r, g, b, hsv)
  return Triple(hsv[0], hsv[1], hsv[2])
}

fun hsvToArgb(hue: Float, saturation: Float, value: Float): Long {
  val hsv = floatArrayOf(
    hue.coerceIn(0f, 360f),
    saturation.coerceIn(0f, 1f),
    value.coerceIn(0f, 1f)
  )
  val colorInt = AndroidColor.HSVToColor(hsv)
  return (colorInt.toLong() and 0xFFFFFFFFL) or 0xFF000000L
}

fun argbToHex(colorLong: Long): String {
  val rgb = colorLong and 0x00FFFFFF
  return String.format("#%06X", rgb)
}

fun parseHexToColor(hex: String): Long? {
  val cleanHex = hex.trim().removePrefix("#")
  if (cleanHex.length == 6) {
    cleanHex.toLongOrNull(16)?.let { rgb ->
      return 0xFF000000L or rgb
    }
  }
  return null
}

@Composable
fun PhotoshopColorPickerDialog(
  title: String,
  initialColor: Long,
  onColorSelected: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  val initialHsv = remember(initialColor) { argbToHsv(initialColor) }
  var hue by remember { mutableFloatStateOf(initialHsv.first) }
  var saturation by remember { mutableFloatStateOf(initialHsv.second) }
  var value by remember { mutableFloatStateOf(initialHsv.third) }

  val currentColor = remember(hue, saturation, value) {
    hsvToArgb(hue, saturation, value)
  }

  var hexInputText by remember(currentColor) {
    mutableStateOf(argbToHex(currentColor))
  }

  val rainbowBrush = remember {
    Brush.horizontalGradient(
      listOf(
        Color(0xFFFF0000),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF00FFFF),
        Color(0xFF0000FF),
        Color(0xFFFF00FF),
        Color(0xFFFF0000)
      )
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .padding(horizontal = 20.dp)
      .fillMaxWidth()
      .testTag("photoshop_color_picker_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        // Original vs New Swatch Box
        Row(
          modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
        ) {
          Box(
            modifier = Modifier
              .size(width = 28.dp, height = 22.dp)
              .background(Color(initialColor))
          )
          Box(
            modifier = Modifier
              .size(width = 28.dp, height = 22.dp)
              .background(Color(currentColor))
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 1. Photoshop 2D Saturation-Value Color Box
        val pureHueColor = remember(hue) {
          val baseInt = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
          Color(baseInt)
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .pointerInput(hue) {
              fun updateSV(offset: Offset) {
                val clampedX = offset.x.coerceIn(0f, size.width.toFloat())
                val clampedY = offset.y.coerceIn(0f, size.height.toFloat())
                saturation = if (size.width > 0) (clampedX / size.width).coerceIn(0f, 1f) else 1f
                value = if (size.height > 0) (1f - (clampedY / size.height)).coerceIn(0f, 1f) else 1f
              }
              detectTapGestures { offset -> updateSV(offset) }
            }
            .pointerInput(hue) {
              detectDragGestures { change, _ ->
                val clampedX = change.position.x.coerceIn(0f, size.width.toFloat())
                val clampedY = change.position.y.coerceIn(0f, size.height.toFloat())
                saturation = if (size.width > 0) (clampedX / size.width).coerceIn(0f, 1f) else 1f
                value = if (size.height > 0) (1f - (clampedY / size.height)).coerceIn(0f, 1f) else 1f
                change.consume()
              }
            }
            .testTag("sv_gradient_box")
        ) {
          Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Base Hue fill
            drawRect(color = pureHueColor, size = size)

            // Horizontal White to Transparent gradient (Saturation 0 -> 1)
            drawRect(
              brush = Brush.horizontalGradient(
                colors = listOf(Color.White, Color.Transparent),
                startX = 0f,
                endX = canvasWidth
              ),
              size = size
            )

            // Vertical Transparent to Black gradient (Value 1 -> 0)
            drawRect(
              brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startY = 0f,
                endY = canvasHeight
              ),
              size = size
            )

            // Reticle marker at current (Saturation, Value)
            val markerX = saturation * canvasWidth
            val markerY = (1f - value) * canvasHeight
            val markerOffset = Offset(markerX, markerY)

            // Black outer ring for contrast against light areas
            drawCircle(
              color = Color.Black,
              radius = 7.dp.toPx(),
              center = markerOffset,
              style = Stroke(width = 3.dp.toPx())
            )
            // White inner ring for contrast against dark areas
            drawCircle(
              color = Color.White,
              radius = 5.dp.toPx(),
              center = markerOffset,
              style = Stroke(width = 2.dp.toPx())
            )
          }
        }

        // 2. Hue Rainbow Spectrum Bar
        Text(
          text = "Hue Spectrum",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
              fun updateHue(offset: Offset) {
                val clampedX = offset.x.coerceIn(0f, size.width.toFloat())
                hue = if (size.width > 0) (clampedX / size.width * 360f).coerceIn(0f, 360f) else 0f
              }
              detectTapGestures { offset -> updateHue(offset) }
            }
            .pointerInput(Unit) {
              detectDragGestures { change, _ ->
                val clampedX = change.position.x.coerceIn(0f, size.width.toFloat())
                hue = if (size.width > 0) (clampedX / size.width * 360f).coerceIn(0f, 360f) else 0f
                change.consume()
              }
            }
            .testTag("hue_slider_bar")
        ) {
          Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(brush = rainbowBrush, size = size)
            val thumbX = (hue / 360f) * size.width
            drawCircle(
              color = Color.White,
              radius = 8.dp.toPx(),
              center = Offset(thumbX, size.height / 2f),
              style = Stroke(width = 2.5.dp.toPx())
            )
            drawCircle(
              color = Color.Black,
              radius = 9.dp.toPx(),
              center = Offset(thumbX, size.height / 2f),
              style = Stroke(width = 1.dp.toPx())
            )
          }
        }

        // 3. Hex & RGB Values in single compact row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = hexInputText,
            onValueChange = { input ->
              hexInputText = input
              parseHexToColor(input)?.let { parsed ->
                val hsv = argbToHsv(parsed)
                hue = hsv.first
                saturation = hsv.second
                value = hsv.third
              }
            },
            label = { Text("HEX", fontSize = 10.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("hex_code_input"),
            colors = OutlinedTextFieldDefaults.colors()
          )

          // RGB readout
          val r = ((currentColor shr 16) and 0xFF).toInt()
          val g = ((currentColor shr 8) and 0xFF).toInt()
          val b = (currentColor and 0xFF).toInt()

          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(top = 6.dp)
          ) {
            Text(
              text = "R:$r G:$g B:$b",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
          }
        }

        // 4. One-Line Preset Bar (Required: exactly 1 line)
        Text(
          text = "Quick Swatches",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          CuratedSubtitlePresets.forEach { (presetArgb, _) ->
            val isSelected = (presetArgb and 0x00FFFFFF) == (currentColor and 0x00FFFFFF)
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(presetArgb))
                .border(
                  width = if (isSelected) 2.5.dp else 1.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.6f),
                  shape = CircleShape
                )
                .clickable {
                  val hsv = argbToHsv(presetArgb)
                  hue = hsv.first
                  saturation = hsv.second
                  value = hsv.third
                },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = if (presetArgb == 0xFFFFFFFF || presetArgb == 0xFFFFEB3B || presetArgb == 0xFF00E5FF) Color.Black else Color.White,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onColorSelected(currentColor)
          onDismiss()
        },
        modifier = Modifier.testTag("color_picker_select_button")
      ) {
        Text("Select Color")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
