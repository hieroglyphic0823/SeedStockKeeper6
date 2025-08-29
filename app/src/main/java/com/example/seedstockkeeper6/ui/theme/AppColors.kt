package com.example.seedstockkeeper6.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * アプリ固有のビジネスロジック色定義
 * 種子管理アプリの機能に特化した色を提供
 */

/**
 * 有効期限内の種まき色
 */
@Composable
fun sowingWithinExpiration(): Color = MaterialTheme.colorScheme.primary

/**
 * 有効期限内の収穫色
 */
@Composable
fun harvestWithinExpiration(): Color = MaterialTheme.colorScheme.secondary

/**
 * 期限切れ色（半透明）
 */
@Composable
fun expired(): Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

/**
 * カレンダー月背景色（有効期限内）
 */
@Composable
fun calendarMonthBackgroundWithinExpiration(): Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)

/**
 * カレンダー月背景色（期限切れ）
 */
@Composable
fun calendarMonthBackgroundExpired(): Color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)

/**
 * テキスト描画色
 */
@Composable
fun textPaintColor(): Color = MaterialTheme.colorScheme.onSurface

/**
 * アウトライン色
 */
@Composable
fun outline(): Color = MaterialTheme.colorScheme.outline

/**
 * 有効期限警告色（期限が近い場合）
 */
@Composable
fun expirationWarning(): Color = MaterialTheme.colorScheme.tertiary

/**
 * 種子情報カードの背景色
 */
@Composable
fun seedCardBackground(): Color = MaterialTheme.colorScheme.surfaceVariant

/**
 * 種子情報カードの境界線色
 */
@Composable
fun seedCardBorder(): Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
