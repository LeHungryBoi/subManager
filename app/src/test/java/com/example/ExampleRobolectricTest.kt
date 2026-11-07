package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CaptionConfig
import com.example.model.DefaultPresets
import com.example.model.EdgeType
import com.example.service.SystemCaptionBridge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun testAppNameIsSubScale() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SubScale", appName)
  }

  @Test
  fun testSystemCaptionBridgeReadsConfig() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val bridge = SystemCaptionBridge(context)
    val config = bridge.readSystemConfig()

    assertNotNull(config)
    assertTrue("Font scale should be positive", config.fontScale > 0f)
    assertTrue("Text opacity should be between 0 and 1", config.textOpacity in 0f..1f)
  }

  @Test
  fun testCaptionConfigResolvedColors() {
    val config = CaptionConfig(
      fontScale = 0.75f,
      textColor = 0xFFFFFFFF,
      textOpacity = 0.5f,
      backgroundColor = 0xFF000000,
      backgroundOpacity = 0.8f,
      edgeType = EdgeType.OUTLINE
    )

    assertEquals(0.75f, config.fontScale, 0.01f)
    assertEquals(0.5f, config.resolvedTextColor.alpha, 0.01f)
    assertEquals(0.8f, config.resolvedBackgroundColor.alpha, 0.01f)
    assertEquals(EdgeType.OUTLINE, config.edgeType)
  }

  @Test
  fun testDefaultPresetsList() {
    val presets = DefaultPresets.list
    assertTrue("Default presets list should not be empty", presets.isNotEmpty())
    val compact = presets.find { it.id == "youtube_compact" }
    assertNotNull(compact)
    assertEquals(0.75f, compact!!.config.fontScale, 0.01f)
  }
}
