package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HighDensityStylingControls
import com.example.ui.components.SystemToolsBottomSheet
import com.example.ui.components.YouTubePlayerPreview
import com.example.viewmodel.CaptionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptionTunerScreen(
  viewModel: CaptionViewModel,
  modifier: Modifier = Modifier
) {
  val customConfig by viewModel.customConfig.collectAsState()
  val systemConfig by viewModel.systemConfig.collectAsState()
  val hasWritePermission by viewModel.hasWritePermission.collectAsState()
  val previewText by viewModel.previewText.collectAsState()
  val selectedBackdrop by viewModel.selectedBackdrop.collectAsState()
  val isFullscreen by viewModel.isFullscreen.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val scrollState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  // Non-styling settings bottom sheet state
  var showToolsSheet by remember { mutableStateOf(false) }
  val toolsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Back button handling in device fullscreen preview
  BackHandler(enabled = isFullscreen) {
    viewModel.setFullscreen(false)
  }

  // UI Event / Toast listener
  LaunchedEffect(Unit) {
    viewModel.uiEvents.collect { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  if (isFullscreen) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
      YouTubePlayerPreview(
        customConfig = customConfig,
        systemConfig = systemConfig,
        previewText = previewText,
        selectedBackdrop = selectedBackdrop,
        onSelectBackdrop = viewModel::setSelectedBackdrop,
        isFullscreen = true,
        onToggleFullscreen = viewModel::toggleFullscreen,
        modifier = Modifier.fillMaxSize()
      )
    }
    return
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "SubScale",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Granular Subtitle Tuner",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          // Action 1: Tools & ADB Sheet (Houses all non-styling settings)
          IconButton(
            onClick = { showToolsSheet = true },
            modifier = Modifier.testTag("topbar_tools_button")
          ) {
            Icon(
              imageVector = Icons.Default.Build,
              contentDescription = "System Tools & ADB",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Action 2: Fast sync from OS
          IconButton(
            onClick = viewModel::syncFromSystem,
            modifier = Modifier.testTag("topbar_sync_system")
          ) {
            Icon(
              imageVector = Icons.Default.Sync,
              contentDescription = "Sync from OS caption settings"
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Action 3: Pinned High-Frequency APPLY Action Button
          Button(
            onClick = viewModel::applyToSystem,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2E7D32),
              contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
              .height(36.dp)
              .testTag("topbar_apply_button")
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Apply",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = modifier
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          top = paddingValues.calculateTopPadding(),
          bottom = paddingValues.calculateBottomPadding()
        )
    ) {
      // 1. YouTube Player Preview - PINNED & ALWAYS VISIBLE AT THE TOP
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        YouTubePlayerPreview(
          customConfig = customConfig,
          systemConfig = systemConfig,
          previewText = previewText,
          selectedBackdrop = selectedBackdrop,
          onSelectBackdrop = viewModel::setSelectedBackdrop,
          isFullscreen = false,
          onToggleFullscreen = viewModel::toggleFullscreen
        )
      }

      // 2. High-Density Styling Controls - INDEPENDENTLY SCROLLING BENEATH
      LazyColumn(
        state = scrollState,
        contentPadding = PaddingValues(
          start = 12.dp,
          end = 12.dp,
          top = 2.dp,
          bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        item {
          HighDensityStylingControls(
            config = customConfig,
            onConfigChange = { newConfig ->
              viewModel.updateCustomConfig { newConfig }
            }
          )
        }
      }
    }
  }

  // Non-Styling Settings Bottom Sheet (ADB, Permissions, OS Sync, Test Dialogue)
  if (showToolsSheet) {
    SystemToolsBottomSheet(
      systemConfig = systemConfig,
      hasWritePermission = hasWritePermission,
      adbCommand = viewModel.adbCommand,
      previewText = previewText,
      onTextChange = viewModel::setPreviewText,
      onApplyToSystem = viewModel::applyToSystem,
      onSyncFromSystem = viewModel::syncFromSystem,
      onOpenSystemSettings = viewModel::openSystemSettings,
      onRefreshPermissionCheck = viewModel::refreshPermission,
      sheetState = toolsSheetState,
      onDismiss = {
        scope.launch { toolsSheetState.hide() }
          .invokeOnCompletion { showToolsSheet = false }
      }
    )
  }
}
