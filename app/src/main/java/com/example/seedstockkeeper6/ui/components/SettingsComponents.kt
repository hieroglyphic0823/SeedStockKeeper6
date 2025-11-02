package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.*

/**
 * 地域選択ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionBottomSheet(
    selectedRegion: String,
    onRegionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = SettingsConstants.REGION_SELECTION_DESCRIPTION,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = SettingsConstants.REGION_SELECTION_TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            REGION_OPTIONS.forEach { region ->
                Button(
                    onClick = { onRegionSelected(region) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getRegionColor(region),
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.large,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (region == selectedRegion) 4.dp else 2.dp
                    ),
                    border = if (region == selectedRegion) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                    } else null
                ) {
                    Text(
                        text = region,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (region == selectedRegion) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 農園名設定セクション
 */
@Composable
fun FarmNameSection(
    farmName: String,
    isEditMode: Boolean,
    hasExistingData: Boolean,
    onFarmNameChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.home),
                contentDescription = SettingsConstants.FARM_NAME_DESCRIPTION,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = SettingsConstants.FARM_NAME_DESCRIPTION,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (isEditMode || !hasExistingData) {
            OutlinedTextField(
                value = farmName,
                onValueChange = onFarmNameChange,
                label = { Text(SettingsConstants.FARM_NAME_LABEL) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
            
            Text(
                text = SettingsConstants.FARM_NAME_HELP,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = farmName.ifEmpty { SettingsConstants.NOT_SET_DISPLAY_TEXT },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (farmName.isEmpty()) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 農園位置設定セクション
 */
@Composable
fun FarmLocationSection(
    farmLatitude: Double,
    farmLongitude: Double,
    farmAddress: String,
    isEditMode: Boolean,
    hasExistingData: Boolean,
    onNavigateToMap: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = SettingsConstants.FARM_POSITION_DESCRIPTION,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = SettingsConstants.FARM_POSITION_DESCRIPTION,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (isEditMode || !hasExistingData) {
            // 座標表示
            if (farmLatitude != 0.0 && farmLongitude != 0.0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = SettingsConstants.CURRENT_POSITION_DISPLAY_TITLE,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${SettingsConstants.LATITUDE_DISPLAY_PREFIX}${String.format("%.6f", farmLatitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${SettingsConstants.LONGITUDE_DISPLAY_PREFIX}${String.format("%.6f", farmLongitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (farmAddress.isNotEmpty()) {
                            Text(
                                text = "${SettingsConstants.ADDRESS_DISPLAY_PREFIX}$farmAddress",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = onNavigateToMap,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_maps_icon),
                    contentDescription = SettingsConstants.GOOGLE_MAPS_DESCRIPTION,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(SettingsConstants.GOOGLE_MAPS_BUTTON_TEXT)
            }
            
            Text(
                text = SettingsConstants.GOOGLE_MAPS_HELP,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        } else {
            // 表示モード
            if (farmLatitude != 0.0 && farmLongitude != 0.0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = SettingsConstants.FARM_POSITION_DISPLAY_TITLE,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${SettingsConstants.LATITUDE_DISPLAY_PREFIX}${String.format("%.6f", farmLatitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${SettingsConstants.LONGITUDE_DISPLAY_PREFIX}${String.format("%.6f", farmLongitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (farmAddress.isNotEmpty()) {
                            Text(
                                text = "${SettingsConstants.ADDRESS_DISPLAY_PREFIX}$farmAddress",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = SettingsConstants.NOT_SET_DISPLAY_TEXT,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 農園主設定セクション
 */
@Composable
fun FarmOwnerSection(
    farmOwner: String,
    customFarmOwner: String,
    isEditMode: Boolean,
    hasExistingData: Boolean,
    onFarmOwnerChange: (String) -> Unit,
    onCustomFarmOwnerChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = SettingsConstants.PERSON_DESCRIPTION,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = SettingsConstants.PERSON_DESCRIPTION,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (isEditMode || !hasExistingData) {
            // 編集モード: ラジオボタンで選択
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FARM_OWNER_OPTIONS.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onFarmOwnerChange(option)
                                if (option != "その他") {
                                    onCustomFarmOwnerChange("")
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = farmOwner == option,
                            onClick = { 
                                onFarmOwnerChange(option)
                                if (option != "その他") {
                                    onCustomFarmOwnerChange("")
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // その他選択時のフリー入力フィールド
                if (farmOwner == "その他") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customFarmOwner,
                        onValueChange = onCustomFarmOwnerChange,
                        label = { Text(SettingsConstants.FARM_OWNER_LABEL) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                }
            }
            
            Text(
                text = SettingsConstants.FARM_OWNER_HELP,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        } else {
            // DisplayMode: リスト項目として表示
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (farmOwner == "その他" && customFarmOwner.isNotEmpty()) {
                        customFarmOwner
                    } else {
                        farmOwner
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 地域設定セクション
 */
@Composable
fun RegionSettingsSection(
    defaultRegion: String,
    selectedPrefecture: String,
    isEditMode: Boolean,
    hasExistingData: Boolean,
    onShowRegionBottomSheet: () -> Unit,
    onShowPrefectureBottomSheet: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Public,
                contentDescription = SettingsConstants.PUBLIC_DESCRIPTION,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = SettingsConstants.REGION_SETTING_DESCRIPTION,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (isEditMode || !hasExistingData) {
            // 編集モード時は色付きボタン
            Button(
                onClick = onShowRegionBottomSheet,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = getRegionColor(defaultRegion),
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.large,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = defaultRegion.ifEmpty { SettingsConstants.REGION_DISPLAY_TITLE },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // 表示モード時は色付きSurface
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = getRegionColor(defaultRegion).copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = getRegionColor(defaultRegion),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = defaultRegion.ifEmpty { SettingsConstants.NOT_SET_DISPLAY_TEXT },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (defaultRegion.isEmpty()) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Text(
            text = SettingsConstants.REGION_HELP,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        // 県設定セクション
        if (isEditMode || !hasExistingData) {
            // EditMode: ボタンで県選択
            Button(
                onClick = onShowPrefectureBottomSheet,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = MaterialTheme.shapes.large,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = selectedPrefecture.ifEmpty { SettingsConstants.PREFECTURE_SELECTION_TITLE },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // DisplayMode: 表示のみ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SettingsConstants.PREFECTURE_DISPLAY_TITLE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = selectedPrefecture.ifEmpty { SettingsConstants.NOT_SET_DISPLAY_TEXT },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedPrefecture.isEmpty()) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 通知設定セクション
 */
@Composable
fun NotificationSettingsSection(
    notificationFrequency: String,
    selectedWeekday: String,
    isEditMode: Boolean,
    hasExistingData: Boolean,
    onNotificationSettingsChange: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.NotificationsActive,
                contentDescription = SettingsConstants.NOTIFICATION_ACTIVE_DESCRIPTION,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "通知タイミング",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 通知設定の表示/編集
        if (isEditMode || !hasExistingData) {
            // EditMode: ラジオボタンで編集可能
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NOTIFICATION_FREQUENCY_OPTIONS.forEach { frequency ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNotificationSettingsChange(frequency, selectedWeekday) }
                    ) {
                        RadioButton(
                            selected = notificationFrequency == frequency,
                            onClick = { onNotificationSettingsChange(frequency, selectedWeekday) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = frequency,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // 週１回の場合の曜日選択
                if (notificationFrequency == "週１回") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = SettingsConstants.WEEKDAY_SELECTION_LABEL,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WEEKDAY_OPTIONS.forEach { weekday ->
                            FilterChip(
                                selected = selectedWeekday == weekday,
                                onClick = { onNotificationSettingsChange("週１回", weekday) },
                                label = { Text(weekday) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            // DisplayMode: 表示のみ
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 通知頻度の表示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = SettingsConstants.NOTIFICATION_FREQUENCY_DISPLAY_TITLE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = notificationFrequency,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 週１回の場合の曜日表示
                if (notificationFrequency == "週１回") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = SettingsConstants.NOTIFICATION_WEEKDAY_DISPLAY_TITLE,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = selectedWeekday,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        Text(
            text = SettingsConstants.NOTIFICATION_HELP,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * BGM設定セクション
 */
@Composable
fun BgmSettingsSection(
    isBgmEnabled: Boolean,
    isEditMode: Boolean,
    hasExistingData: Boolean,
    onBgmEnabledChange: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = "BGM設定",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "BGM設定",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // BGM設定の表示/編集
        if (isEditMode || !hasExistingData) {
            // EditMode: スイッチで編集可能
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BGMを再生する",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isBgmEnabled,
                    onCheckedChange = onBgmEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        } else {
            // DisplayMode: 表示のみ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BGM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = if (isBgmEnabled) "ON" else "OFF",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Text(
            text = "アプリ使用中にBGMを再生します",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * 種情報URL設定セクション
 */
@Composable
fun SeedInfoUrlSettingsSection(
    seedInfoUrlProvider: String,
    customSeedInfoUrl: String,
    isEditMode: Boolean,
    hasExistingData: Boolean,
    onSeedInfoUrlProviderChange: (String) -> Unit,
    onCustomSeedInfoUrlChange: (String) -> Unit,
    onNavigateToNotificationPreview: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Link,
                contentDescription = SettingsConstants.LINK_DESCRIPTION,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = SettingsConstants.SEED_INFO_URL_DISPLAY_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 種情報URL設定の表示/編集
        if (isEditMode || !hasExistingData) {
            // EditMode: ラジオボタンで編集可能
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SEED_INFO_URL_PROVIDER_OPTIONS.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onSeedInfoUrlProviderChange(provider) }
                    ) {
                        RadioButton(
                            selected = seedInfoUrlProvider == provider,
                            onClick = { onSeedInfoUrlProviderChange(provider) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = provider,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // その他選択時のURL入力欄
                if (seedInfoUrlProvider == "その他") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customSeedInfoUrl,
                        onValueChange = onCustomSeedInfoUrlChange,
                        label = { Text(SettingsConstants.URL_INPUT_LABEL) },
                        placeholder = { Text(SettingsConstants.CUSTOM_URL_PLACEHOLDER) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                }
            }
        } else {
            // DisplayMode: 表示のみ
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 種情報URLプロバイダーの表示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = SettingsConstants.SEED_INFO_URL_DISPLAY_TITLE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = seedInfoUrlProvider,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // その他選択時のURL表示
                if (seedInfoUrlProvider == "その他" && customSeedInfoUrl.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = SettingsConstants.CUSTOM_URL_DISPLAY_TITLE,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = customSeedInfoUrl,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        Text(
            text = SettingsConstants.SEED_INFO_URL_HELP,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        // 通知テストボタン（EditModeでのみ表示）
        if (isEditMode || !hasExistingData) {
            Button(
                onClick = onNavigateToNotificationPreview,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = SettingsConstants.NOTIFICATIONS_DESCRIPTION,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(SettingsConstants.NOTIFICATION_TEST_BUTTON_TEXT)
            }
        }
    }
}
