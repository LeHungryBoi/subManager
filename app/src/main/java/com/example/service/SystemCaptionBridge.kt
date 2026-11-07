package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.provider.Settings
import android.view.accessibility.CaptioningManager
import com.example.model.CaptionConfig
import com.example.model.EdgeType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SystemCaptionBridge(private val context: Context) {

  private val captioningManager =
    context.getSystemService(Context.CAPTIONING_SERVICE) as? CaptioningManager

  fun hasWriteSecureSettingsPermission(): Boolean {
    return context.checkCallingOrSelfPermission(
      android.Manifest.permission.WRITE_SECURE_SETTINGS
    ) == PackageManager.PERMISSION_GRANTED
  }

  fun getAdbCommand(): String {
    return "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
  }

  /**
   * Reads current active system caption configuration from CaptioningManager / Settings.Secure
   */
  fun readSystemConfig(): CaptionConfig {
    val cm = captioningManager
    val fontScale = cm?.fontScale ?: try {
      Settings.Secure.getFloat(context.contentResolver, "accessibility_captioning_font_scale", 1.0f)
    } catch (_: Exception) {
      1.0f
    }

    val style = cm?.userStyle
    val fgColor = if (style != null && style.hasForegroundColor()) {
      style.foregroundColor.toLong() and 0xFFFFFFFFL
    } else {
      0xFFFFFFFFL
    }

    val bgColor = if (style != null && style.hasBackgroundColor()) {
      style.backgroundColor.toLong() and 0xFFFFFFFFL
    } else {
      0xFF000000L
    }

    val windowColor = if (style != null && style.hasWindowColor()) {
      style.windowColor.toLong() and 0xFFFFFFFFL
    } else {
      0x00000000L
    }

    val edgeType = when (style?.edgeType) {
      CaptioningManager.CaptionStyle.EDGE_TYPE_OUTLINE -> EdgeType.OUTLINE
      CaptioningManager.CaptionStyle.EDGE_TYPE_DROP_SHADOW -> EdgeType.DROP_SHADOW
      CaptioningManager.CaptionStyle.EDGE_TYPE_RAISED -> EdgeType.RAISED
      CaptioningManager.CaptionStyle.EDGE_TYPE_DEPRESSED -> EdgeType.DEPRESSED
      else -> EdgeType.NONE
    }

    val edgeColor = if (style != null && style.hasEdgeColor()) {
      style.edgeColor.toLong() and 0xFFFFFFFFL
    } else {
      0xFF000000L
    }

    // Extract alpha fractions
    val textAlpha = AndroidColor.alpha(fgColor.toInt()) / 255f
    val bgAlpha = AndroidColor.alpha(bgColor.toInt()) / 255f
    val windowAlpha = AndroidColor.alpha(windowColor.toInt()) / 255f

    return CaptionConfig(
      fontScale = fontScale,
      textColor = fgColor,
      textOpacity = if (textAlpha > 0f) textAlpha else 1.0f,
      backgroundColor = bgColor,
      backgroundOpacity = bgAlpha,
      windowColor = windowColor,
      windowOpacity = windowAlpha,
      edgeType = edgeType,
      edgeColor = edgeColor
    )
  }

  /**
   * Listens to live system changes so if the user changes settings in Android OS,
   * our app updates in real-time.
   */
  fun observeSystemChanges(): Flow<CaptionConfig> = callbackFlow {
    val listener = object : CaptioningManager.CaptioningChangeListener() {
      override fun onEnabledChanged(enabled: Boolean) {
        trySend(readSystemConfig())
      }

      override fun onFontScaleChanged(fontScale: Float) {
        trySend(readSystemConfig())
      }

      override fun onUserStyleChanged(userStyle: CaptioningManager.CaptionStyle) {
        trySend(readSystemConfig())
      }
    }

    captioningManager?.addCaptioningChangeListener(listener)
    // Send initial
    trySend(readSystemConfig())

    awaitClose {
      captioningManager?.removeCaptioningChangeListener(listener)
    }
  }

  /**
   * Applies caption font scale & style system-wide if WRITE_SECURE_SETTINGS is granted
   */
  fun applyToSystem(config: CaptionConfig): Result<String> {
    if (!hasWriteSecureSettingsPermission()) {
      return Result.failure(
        SecurityException("Permission WRITE_SECURE_SETTINGS is not granted. Please run the ADB command.")
      )
    }

    return try {
      val resolver = context.contentResolver

      // Set font scale
      Settings.Secure.putFloat(
        resolver,
        "accessibility_captioning_font_scale",
        config.fontScale
      )

      // Convert ARGB with alpha to int
      val fgAlphaInt = (config.textOpacity * 255).toInt().coerceIn(0, 255)
      val fg = (config.textColor.toInt() and 0x00FFFFFF) or (fgAlphaInt shl 24)

      val bgAlphaInt = (config.backgroundOpacity * 255).toInt().coerceIn(0, 255)
      val bg = (config.backgroundColor.toInt() and 0x00FFFFFF) or (bgAlphaInt shl 24)

      val winAlphaInt = (config.windowOpacity * 255).toInt().coerceIn(0, 255)
      val win = (config.windowColor.toInt() and 0x00FFFFFF) or (winAlphaInt shl 24)

      Settings.Secure.putInt(resolver, "accessibility_captioning_foreground_color", fg)
      Settings.Secure.putInt(resolver, "accessibility_captioning_background_color", bg)
      Settings.Secure.putInt(resolver, "accessibility_captioning_window_color", win)
      Settings.Secure.putInt(resolver, "accessibility_captioning_edge_type", config.edgeType.systemId)
      Settings.Secure.putInt(resolver, "accessibility_captioning_edge_color", config.edgeColor.toInt())

      // Set preset to custom (-1) so system respects our custom values
      Settings.Secure.putInt(resolver, "accessibility_captioning_preset", -1)

      Result.success("System subtitle settings updated! Font scale: ${config.fontScale}x")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun openSystemCaptionSettings(): Boolean {
    val intents = listOf(
      Intent(Settings.ACTION_CAPTIONING_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
      Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
      Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    for (intent in intents) {
      try {
        context.startActivity(intent)
        return true
      } catch (_: Exception) {
        // try next fallback
      }
    }
    return false
  }
}
