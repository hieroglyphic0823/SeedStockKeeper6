package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AIDiffDialog(
    showDialog: Boolean,
    diffList: List<Triple<String, String, String>>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI解析結果の確認") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("以下の項目に差異があります。上書きしてよいですか？")
                Spacer(Modifier.height(8.dp))
                LazyColumn {
                    items(diffList) { (label, current, ai) ->
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                            Row(Modifier.fillMaxWidth()) {
                                Text("現: $current", modifier = Modifier.weight(1f), color = Color.Gray)
                                Text("新: $ai", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("上書きする")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
