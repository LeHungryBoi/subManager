package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CaptionConfig
import com.example.model.DefaultPresets
import com.example.model.PresetConfig
import com.example.service.SystemCaptionBridge
import com.example.ui.components.PreviewBackdrop
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TunerTab(val label: String) {
  SCALE("Size & Scale"),
  COLORS("Colors & Opacity"),
  EDGES("Edges & Shadow"),
  HIDDEN("Hidden Settings"),
  PRESETS("Presets & Text"),
  SYSTEM("System Sync")
}

class CaptionViewModel(application: Application) : AndroidViewModel(application) {

  private val bridge = SystemCaptionBridge(application)

  private val _systemConfig = MutableStateFlow(bridge.readSystemConfig())
  val systemConfig: StateFlow<CaptionConfig> = _systemConfig.asStateFlow()

  // Initialize custom config initially from system, or provide slightly more compact 0.75x if system is 1.0
  private val _customConfig = MutableStateFlow(
    bridge.readSystemConfig().let { sys ->
      if (sys.fontScale == 1.0f) sys.copy(fontScale = 0.75f) else sys
    }
  )
  val customConfig: StateFlow<CaptionConfig> = _customConfig.asStateFlow()

  private val _hasWritePermission = MutableStateFlow(bridge.hasWriteSecureSettingsPermission())
  val hasWritePermission: StateFlow<Boolean> = _hasWritePermission.asStateFlow()

  val adbCommand: String = bridge.getAdbCommand()

  private val _previewText = MutableStateFlow("字幕呈现效果示例")
  val previewText: StateFlow<String> = _previewText.asStateFlow()

  private val _selectedBackdrop = MutableStateFlow(PreviewBackdrop.BEACH_SUNSET)
  val selectedBackdrop: StateFlow<PreviewBackdrop> = _selectedBackdrop.asStateFlow()

  private val _isFullscreen = MutableStateFlow(false)
  val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

  private val _activeTab = MutableStateFlow(TunerTab.SCALE)
  val activeTab: StateFlow<TunerTab> = _activeTab.asStateFlow()

  private val _userPresets = MutableStateFlow<List<PresetConfig>>(emptyList())
  val userPresets: StateFlow<List<PresetConfig>> = _userPresets.asStateFlow()

  private val _uiEvents = MutableSharedFlow<String>()
  val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

  init {
    // Observe system caption setting changes in real time
    viewModelScope.launch {
      bridge.observeSystemChanges().collect { updatedSystem ->
        _systemConfig.value = updatedSystem
      }
    }
  }

  fun updateCustomConfig(transform: (CaptionConfig) -> CaptionConfig) {
    _customConfig.value = transform(_customConfig.value)
  }

  fun setTab(tab: TunerTab) {
    _activeTab.value = tab
  }

  fun setPreviewText(text: String) {
    _previewText.value = text
  }

  fun setSelectedBackdrop(backdrop: PreviewBackdrop) {
    _selectedBackdrop.value = backdrop
  }

  fun toggleFullscreen() {
    _isFullscreen.value = !_isFullscreen.value
  }

  fun setFullscreen(fullscreen: Boolean) {
    _isFullscreen.value = fullscreen
  }

  fun syncFromSystem() {
    val currentSystem = bridge.readSystemConfig()
    _systemConfig.value = currentSystem
    _customConfig.value = currentSystem
    viewModelScope.launch {
      _uiEvents.emit("Synced custom preview with Android OS settings")
    }
  }

  fun refreshPermission() {
    val granted = bridge.hasWriteSecureSettingsPermission()
    _hasWritePermission.value = granted
    viewModelScope.launch {
      if (granted) {
        _uiEvents.emit("WRITE_SECURE_SETTINGS is granted!")
      } else {
        _uiEvents.emit("Permission not yet granted. Run the ADB command in terminal.")
      }
    }
  }

  fun applyToSystem() {
    val result = bridge.applyToSystem(_customConfig.value)
    viewModelScope.launch {
      if (result.isSuccess) {
        _uiEvents.emit(result.getOrDefault("Successfully applied to Android OS!"))
        _systemConfig.value = bridge.readSystemConfig()
      } else {
        _uiEvents.emit("Failed: ${result.exceptionOrNull()?.message}")
      }
    }
  }

  fun openSystemSettings() {
    val opened = bridge.openSystemCaptionSettings()
    if (!opened) {
      viewModelScope.launch {
        _uiEvents.emit("Could not open Android Captioning Settings directly.")
      }
    }
  }

  fun savePreset(name: String, config: CaptionConfig) {
    val newPreset = PresetConfig(
      id = "custom_${System.currentTimeMillis()}",
      name = name,
      description = "Custom scale: ${String.format("%.2f", config.fontScale)}x",
      config = config
    )
    _userPresets.value = _userPresets.value + newPreset
    viewModelScope.launch {
      _uiEvents.emit("Saved preset: $name")
    }
  }
}
