package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CaptionConfig
import com.example.model.EdgeType
import kotlinx.coroutines.delay

enum class PreviewBackdrop(val label: String, val resId: Int?) {
  BEACH_SUNSET("Sunset", R.drawable.preview_beach_sunset_1793956013368),
  CITY_BRIGHT("Day City", R.drawable.preview_city_bright_1793956029007),
  DARK_STUDIO("Dark Studio", null);

  fun next(): PreviewBackdrop {
    val values = values()
    return values[(ordinal + 1) % values.size]
  }
}

@Composable
fun YouTubePlayerPreview(
  customConfig: CaptionConfig,
  systemConfig: CaptionConfig,
  previewText: String,
  selectedBackdrop: PreviewBackdrop,
  onSelectBackdrop: (PreviewBackdrop) -> Unit,
  isFullscreen: Boolean,
  onToggleFullscreen: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isPlaying by remember { mutableStateOf(true) }
  var playbackProgress by remember { mutableFloatStateOf(0.35f) }
  var showControls by remember { mutableStateOf(false) }
  var isCaptionsEnabled by remember { mutableStateOf(true) }
  var showSystemComparison by remember { mutableStateOf(false) }
  // Power-user request: default to shorter height to just show the subtitle!
  var isFullAspectRatio by remember { mutableStateOf(false) }

  // Auto-hide playback controls if expanded
  LaunchedEffect(showControls, isPlaying) {
    if (showControls && isPlaying) {
      delay(4000)
      showControls = false
    }
  }

  // Simulated playback progression
  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      delay(1000)
      playbackProgress = (playbackProgress + 0.005f).let { if (it > 1f) 0f else it }
    }
  }

  val activeConfig = if (showSystemComparison) systemConfig else customConfig

  Card(
    shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Black),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = modifier
      .fillMaxWidth()
      .animateContentSize()
      .testTag("youtube_player_card")
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .then(
          when {
            isFullscreen -> Modifier.fillMaxHeight()
            isFullAspectRatio -> Modifier.aspectRatio(16f / 9f)
            else -> Modifier.height(112.dp) // Default compact preview height
          }
        )
        .background(Color(0xFF0F0F12))
        .clickable { if (isFullAspectRatio || isFullscreen) showControls = !showControls }
    ) {
      val boxHeight = maxHeight

      // Video Backdrop Image or Gradient
      if (selectedBackdrop.resId != null) {
        Image(
          painter = painterResource(id = selectedBackdrop.resId),
          contentDescription = "Video preview background",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.radialGradient(
                colors = listOf(Color(0xFF2C2D35), Color(0xFF0F1015)),
                center = Offset(200f, 200f),
                radius = 700f
              )
            )
        )
      }

      // Vignette Gradient Overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.45f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.60f)
              )
            )
          )
      )

      // Active Subtitles Display
      if (isCaptionsEnabled) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(
              horizontal = 14.dp,
              vertical = if (isFullAspectRatio || isFullscreen) {
                (boxHeight.value * activeConfig.verticalPositionPercent).coerceIn(8f, 100f).dp
              } else {
                8.dp
              }
            ),
          contentAlignment = Alignment.BottomCenter
        ) {
          CaptionTextRenderer(
            text = previewText,
            config = activeConfig,
            playerHeightDp = if (isFullAspectRatio || isFullscreen) boxHeight.value else 220f
          )
        }
      }

      // Top Left Overlay: OS Compare & Scale Badge
      Row(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(start = 8.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (showSystemComparison) Color(0xFFD32F2F) else Color(0xFF1E88E5),
          shadowElevation = 2.dp
        ) {
          Text(
            text = if (showSystemComparison) "OS: ${String.format("%.2f", systemConfig.fontScale)}x"
            else "CUSTOM: ${String.format("%.2f", customConfig.fontScale)}x",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Overlay Compare Toggle Button
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color.Black.copy(alpha = 0.65f),
          modifier = Modifier
            .clickable { showSystemComparison = !showSystemComparison }
            .testTag("preview_compare_overlay_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Compare,
              contentDescription = "Compare OS",
              tint = Color.White,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (showSystemComparison) "Custom" else "Compare",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // Top Right Overlay Action Buttons: Backdrop Scene Switcher & View Mode Toggles
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(end = 6.dp, top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Overlay Button 1: Scene / Background Cycle
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color.Black.copy(alpha = 0.65f),
          modifier = Modifier
            .clickable { onSelectBackdrop(selectedBackdrop.next()) }
            .testTag("preview_scene_overlay_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Image,
              contentDescription = "Change scene",
              tint = Color(0xFFFFD54F),
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = selectedBackdrop.label,
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Overlay Button 2: Full 16:9 vs Compact Height Toggle
        if (!isFullscreen) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
              .clickable { isFullAspectRatio = !isFullAspectRatio }
              .testTag("preview_expand_overlay_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isFullAspectRatio) Icons.Default.CropFree else Icons.Default.AspectRatio,
                contentDescription = "Toggle 16:9 player vs compact height",
                tint = Color.White,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = if (isFullAspectRatio) "Compact" else "16:9",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Overlay Button 3: Fullscreen Toggle
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color.Black.copy(alpha = 0.65f),
          modifier = Modifier
            .clickable { onToggleFullscreen() }
            .testTag("preview_fullscreen_overlay_button")
        ) {
          Box(modifier = Modifier.padding(3.dp)) {
            Icon(
              imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
              contentDescription = "Fullscreen",
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }

      // If in 16:9 or Fullscreen mode, show interactive playback controls overlay when tapped
      if ((isFullAspectRatio || isFullscreen) && showControls) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
        ) {
          // Center Play/Pause & Replay controls
          Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { playbackProgress = (playbackProgress - 0.05f).coerceAtLeast(0f) },
              modifier = Modifier
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
              Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Replay 10s",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = { isPlaying = !isPlaying },
              modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }

            IconButton(
              onClick = { isCaptionsEnabled = !isCaptionsEnabled },
              modifier = Modifier
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
              Icon(
                imageVector = if (isCaptionsEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                contentDescription = "Toggle captions",
                tint = if (isCaptionsEnabled) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Bottom Timeline Scrubber
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomCenter)
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "04:18 / 15:20",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              )
              Surface(
                shape = RoundedCornerShape(3.dp),
                color = Color.White.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "HD",
                  color = Color.White,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.5.dp))
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(playbackProgress)
                  .fillMaxHeight()
                  .background(Color(0xFFFF0000), RoundedCornerShape(1.5.dp))
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Accurately replicates YouTube's Android subtitle rendering:
 * - Base text size is calculated proportional to player height (~5% of player height) multiplied by fontScale
 * - Renders background pill padding
 * - Emulates edge types: Drop shadow, Outline, Raised, Depressed
 * - Window container color and opacity
 */
@Composable
fun CaptionTextRenderer(
  text: String,
  config: CaptionConfig,
  playerHeightDp: Float,
  modifier: Modifier = Modifier
) {
  val baseSp = (playerHeightDp * 0.052f).coerceIn(10f, 22f)
  val calculatedFontSizeSp = (baseSp * config.fontScale).coerceIn(6f, 48f).sp

  val shadow = when (config.edgeType) {
    EdgeType.NONE -> null
    EdgeType.DROP_SHADOW -> Shadow(
      color = config.resolvedEdgeColor,
      offset = Offset(2.2f * config.edgeWidth, 2.2f * config.edgeWidth),
      blurRadius = 3f * config.edgeWidth
    )
    EdgeType.OUTLINE -> Shadow(
      color = config.resolvedEdgeColor,
      offset = Offset(1.5f * config.edgeWidth, 1.5f * config.edgeWidth),
      blurRadius = 1.5f * config.edgeWidth
    )
    EdgeType.RAISED -> Shadow(
      color = Color.White.copy(alpha = 0.5f),
      offset = Offset(-1.5f, -1.5f),
      blurRadius = 1.5f
    )
    EdgeType.DEPRESSED -> Shadow(
      color = Color.Black.copy(alpha = 0.7f),
      offset = Offset(-1.5f, -1.5f),
      blurRadius = 2f
    )
  }

  val textStyle = TextStyle(
    color = config.resolvedTextColor,
    fontSize = calculatedFontSizeSp,
    fontWeight = if (config.isBold) FontWeight.Bold else FontWeight.Normal,
    fontFamily = config.fontFamily.composeFontFamily,
    letterSpacing = config.letterSpacing.sp,
    lineHeight = calculatedFontSizeSp * config.lineHeightMultiplier,
    textAlign = TextAlign.Center,
    shadow = shadow
  )

  // Subtitle Window Container
  Box(
    modifier = modifier
      .then(
        if (config.windowOpacity > 0f) {
          Modifier
            .background(config.resolvedWindowColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        } else {
          Modifier
        }
      ),
    contentAlignment = Alignment.Center
  ) {
    val lines = text.split("\n")
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      lines.forEach { line ->
        Box(
          modifier = Modifier
            .background(
              color = config.resolvedBackgroundColor,
              shape = RoundedCornerShape(config.backgroundCornerRadius.dp)
            )
            .padding(
              horizontal = config.backgroundPaddingH.dp,
              vertical = config.backgroundPaddingV.dp
            )
        ) {
          Text(
            text = line,
            style = textStyle,
            modifier = Modifier.testTag("caption_text_rendered")
          )
        }
      }
    }
  }
}
