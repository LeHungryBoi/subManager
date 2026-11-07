package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionConfig
import kotlin.math.roundToInt

val SampleSubtitleTexts = listOf(
  "字幕呈现效果示例" to "Chinese (Settings)",
  "This is a closed caption preview." to "Short English",
  "Welcome back! Today we are fine-tuning closed caption sizes for YouTube on Android." to "Multi-line Dialogue",
  "字幕のフォントサイズと背景の透明度を微調整します" to "Japanese",
  "El tamaño de los subtítulos se adapta perfectamente a la pantalla del móvil." to "Spanish"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SystemToolsBottomSheet(
  systemConfig: CaptionConfig,
  hasWritePermission: Boolean,
  adbCommand: String,
  previewText: String,
  onTextChange: (String) -> Unit,
  onApplyToSystem: () -> Unit,
  onSyncFromSystem: () -> Unit,
  onOpenSystemSettings: () -> Unit,
  onRefreshPermissionCheck: () -> Unit,
  sheetState: SheetState,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var customTextDraft by remember(previewText) { mutableStateOf(previewText) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = Modifier.testTag("system_tools_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "System Tools & ADB",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Android OS integration & preview dialogue text",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close tools sheet")
        }
      }

      // 1. Android Permission & ADB Card
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (hasWritePermission) Icons.Default.CheckCircle else Icons.Default.Security,
              contentDescription = null,
              tint = if (hasWritePermission) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (hasWritePermission) "WRITE_SECURE_SETTINGS: GRANTED" else "WRITE_SECURE_SETTINGS: REQUIRED",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = if (hasWritePermission) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          if (hasWritePermission) {
            Text(
              text = "Permission is active! The top bar 'Apply' button will directly write granular subtitle scales to Android OS.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            Text(
              text = "Android OS protects global subtitle scale with WRITE_SECURE_SETTINGS. Grant it once via computer terminal or Shizuku:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Monospace ADB snippet with copy button
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B1C22), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = adbCommand,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF80D8FF),
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCommand))
                  Toast.makeText(context, "Copied ADB command!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy ADB Command",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = onRefreshPermissionCheck,
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh", fontSize = 12.sp)
              }

              Button(
                onClick = onApplyToSystem,
                modifier = Modifier.weight(1f)
              ) {
                Text("Try Apply", fontSize = 12.sp)
              }
            }
          }
        }
      }

      // 2. Direct Android Settings Shortcut & OS State
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Active OS Caption State",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• Current OS Scale: ${String.format("%.2f", systemConfig.fontScale)}x\n" +
              "• OS Edge Type: ${systemConfig.edgeType.label}\n" +
              "• OS Background Opacity: ${(systemConfig.backgroundOpacity * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = onSyncFromSystem,
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sync from OS", fontSize = 12.sp)
            }

            Button(
              onClick = onOpenSystemSettings,
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("OS Settings", fontSize = 12.sp)
            }
          }
        }
      }

      // 3. Subtitle Sample Dialogue Editor
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Test Dialogue Text",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = customTextDraft,
            onValueChange = {
              customTextDraft = it
              onTextChange(it)
            },
            label = { Text("Preview text") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Quick Sample Dialogue:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(4.dp))

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            SampleSubtitleTexts.forEach { (text, label) ->
              FilterChip(
                selected = previewText == text,
                onClick = {
                  customTextDraft = text
                  onTextChange(text)
                },
                label = { Text(label, fontSize = 11.sp) }
              )
            }
          }
        }
      }
    }
  }
}
