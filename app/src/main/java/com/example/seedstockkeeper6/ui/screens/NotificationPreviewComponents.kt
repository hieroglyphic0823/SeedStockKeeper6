package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R

/**
 * 通知テストセクションのカード
 */
@Composable
fun NotificationTestCard(
    onMonthlyTest: () -> Unit,
    onWeeklyTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.yabumi0),
                    contentDescription = "通知テスト",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "通知テスト",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Text(
                text = "実際の通知を送信してテストできます",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMonthlyTest,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("月次通知テスト")
                }
                
                Button(
                    onClick = onWeeklyTest,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("週次通知テスト")
                }
            }
        }
    }
}

/**
 * 通知プレビューセクションのカード
 */
@Composable
fun NotificationPreviewCard(
    showMonthlyPreview: Boolean,
    showWeeklyPreview: Boolean,
    monthlyPreviewContent: String,
    weeklyPreviewContent: String,
    monthlyPreviewTitle: String = "",
    weeklyPreviewTitle: String = "",
    isOcrSuccessful: Boolean = false,
    onMonthlyPreviewToggle: () -> Unit,
    onWeeklyPreviewToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.fumi),
                    contentDescription = "通知プレビュー",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "通知プレビュー",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Text(
                text = "通知の内容をプレビューできます",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMonthlyPreviewToggle,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("月次プレビュー")
                }
                
                Button(
                    onClick = onWeeklyPreviewToggle,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("週次プレビュー")
                }
            }
            
            // プレビュー内容表示
            if (showMonthlyPreview) {
                NotificationPreviewContentCard(
                    title = if (monthlyPreviewTitle.isNotEmpty()) monthlyPreviewTitle else "月次通知プレビュー",
                    content = monthlyPreviewContent,
                    isOcrSuccessful = isOcrSuccessful
                )
            }
            
            if (showWeeklyPreview) {
                NotificationPreviewContentCard(
                    title = if (weeklyPreviewTitle.isNotEmpty()) weeklyPreviewTitle else "週次通知プレビュー",
                    content = weeklyPreviewContent
                )
            }
        }
    }
}

/**
 * 通知プレビュー内容のカード
 */
@Composable
private fun NotificationPreviewContentCard(
    title: String,
    content: String,
    isOcrSuccessful: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // タイトル部分（OCR成功時はアイコンを表示）
            if (isOcrSuccessful && title.contains("月次")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star_opc),
                        contentDescription = "OCR成功",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}
