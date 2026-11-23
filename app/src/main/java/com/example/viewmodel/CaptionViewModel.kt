package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
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

enum class TunerTab(val labelRes: Int) {
  SCALE(R.string.tab_size_scale),
  COLORS(R.string.tab_colors_opacity),
  EDGES(R.string.tab_edges_shadow),
  HIDDEN(R.string.tab_hidden_settings),
  PRESETS(R.string.tab_presets_text),
  SYSTEM(R.string.tab_system_sync)
}

class CaptionViewModel(application: Application) : AndroidViewModel(application) {

  private val appContext = getApplication<Application>()
  private fun text(resId: Int): String = appContext.getString(resId)
  private fun text(resId: Int, vararg args: Any): String = appContext.getString(resId, *args)

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
      _uiEvents.emit(text(R.string.msg_synced_from_os))
    }
  }

  fun refreshPermission() {
    val granted = bridge.hasWriteSecureSettingsPermission()
    _hasWritePermission.value = granted
    viewModelScope.launch {
      if (granted) {
        _uiEvents.emit(text(R.string.msg_permission_granted))
      } else {
        _uiEvents.emit(text(R.string.msg_permission_not_granted))
      }
    }
  }

  fun applyToSystem() {
    val result = bridge.applyToSystem(_customConfig.value)
    viewModelScope.launch {
      if (result.isSuccess) {
        _uiEvents.emit(result.getOrDefault(text(R.string.msg_applied_success)))
        _systemConfig.value = bridge.readSystemConfig()
      } else {
        _uiEvents.emit(text(R.string.msg_failed, result.exceptionOrNull()?.message.orEmpty()))
      }
    }
  }

  fun openSystemSettings() {
    val opened = bridge.openSystemCaptionSettings()
    if (!opened) {
      viewModelScope.launch {
        _uiEvents.emit(text(R.string.msg_open_settings_failed))
      }
    }
  }

  fun savePreset(name: String, config: CaptionConfig) {
    val newPreset = PresetConfig(
      id = "custom_${System.currentTimeMillis()}",
      name = name,
      description = text(R.string.preset_custom_description, String.format("%.2f", config.fontScale)),
      config = config
    )
    _userPresets.value = _userPresets.value + newPreset
    viewModelScope.launch {
      _uiEvents.emit(text(R.string.preset_saved, name))
    }
  }
}
