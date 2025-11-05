package com.example.seedstockkeeper6.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.seedstockkeeper6.AccountMenuButton
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.*
import com.example.seedstockkeeper6.signOut
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

/**
 * MainScaffoldのTopAppBarコンポーネント
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldTopAppBar(
    currentRoute: String?,
    navController: NavHostController,
    user: FirebaseUser?,
    settingsViewModel: SettingsViewModel? = null,
    seedInputViewModel: SeedInputViewModel? = null,
    selectedIds: List<String> = emptyList(),
    onDeleteSelected: () -> Unit = {},
    farmName: String = ""
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        navigationIcon = {
            when (currentRoute) {
                NavigationConstants.SETTINGS_ROUTE,
                NavigationConstants.INPUT_ROUTE_PREFIX,
                NavigationConstants.NOTIFICATION_PREVIEW_ROUTE,
                NavigationConstants.NOTIFICATION_HISTORY_ROUTE,
                NavigationConstants.CALENDAR_ROUTE -> {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = IconDescriptionConstants.BACK_BUTTON)
                    }
                }
                else -> {
                    if (currentRoute?.startsWith(NavigationConstants.INPUT_ROUTE_PREFIX) == true) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = IconDescriptionConstants.BACK_BUTTON)
                        }
                    } else if (user != null) {
                        Box(
                            modifier = Modifier.padding(horizontal = LayoutConstants.HORIZONTAL_PADDING),
                            contentAlignment = Alignment.Center
                        ) {
                            AccountMenuButton(
                                user = user,
                                size = IconSizeConstants.DEFAULT_SIZE,
                                onSignOut = { signOut(ctx, scope) }
                            )
                        }
                    }
                }
            }
        },
        title = { 
            when (currentRoute) {
                NavigationConstants.NOTIFICATION_PREVIEW_ROUTE -> {
                    Text(ScreenTitleConstants.NOTIFICATION_PREVIEW_TITLE)
                }
                NavigationConstants.SETTINGS_ROUTE -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.garden_cart),
                            contentDescription = IconDescriptionConstants.FARM_SETTINGS_ICON,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                        )
                        Text(
                            text = ScreenTitleConstants.SETTINGS_TITLE,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                NavigationConstants.NOTIFICATION_HISTORY_ROUTE -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = ScreenTitleConstants.NOTIFICATION_TITLE,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                NavigationConstants.MAP_SELECTION_ROUTE -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.google_maps_icon),
                            contentDescription = IconDescriptionConstants.GOOGLE_MAPS_ICON,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                        )
                        Text(
                            text = ScreenTitleConstants.MAP_SELECTION_TITLE,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                else -> {
                    if (currentRoute?.startsWith(NavigationConstants.INPUT_ROUTE_PREFIX) == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when {
                                // DisplayMode: Familyアイコンと商品名
                                seedInputViewModel?.isEditMode == false && seedInputViewModel?.hasExistingData == true -> {
                                    FamilyIcon(
                                        family = seedInputViewModel.packet.family,
                                        size = IconSizeConstants.TOP_APP_BAR_SIZE
                                    )
                                    Text(
                                        text = getSeedInfoTitle(
                                            seedInputViewModel.packet.productName,
                                            seedInputViewModel.packet.variety,
                                            seedInputViewModel.packet.family,
                                            false,
                                            true
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                                // EditMode新規作成: 現行アイコン+「新規作成」
                                seedInputViewModel?.isEditMode == true && seedInputViewModel?.hasExistingData == false -> {
                                    Image(
                                        painter = painterResource(id = R.drawable.seed_bag_full),
                                        contentDescription = IconDescriptionConstants.PACKET_ICON,
                                        modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                                    )
                                    Text(
                                        text = ScreenTitleConstants.NEW_INPUT_TITLE,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                                // EditMode編集: Familyアイコンと商品名+（科名）
                                seedInputViewModel?.isEditMode == true && seedInputViewModel?.hasExistingData == true -> {
                                    FamilyIcon(
                                        family = seedInputViewModel.packet.family,
                                        size = IconSizeConstants.TOP_APP_BAR_SIZE
                                    )
                                    Text(
                                        text = getSeedInfoTitle(
                                            seedInputViewModel.packet.productName,
                                            seedInputViewModel.packet.variety,
                                            seedInputViewModel.packet.family,
                                            true,
                                            true
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                                // デフォルト: 商品名がある場合は商品名、ない場合は「種札」
                                else -> {
                                    if (seedInputViewModel?.packet?.productName?.isNotEmpty() == true) {
                                        // 商品名がある場合はFamilyアイコンと商品名を表示
                                        FamilyIcon(
                                            family = seedInputViewModel.packet.family,
                                            size = IconSizeConstants.TOP_APP_BAR_SIZE
                                        )
                                        Text(
                                            text = getSeedInfoTitle(
                                                seedInputViewModel.packet.productName,
                                                seedInputViewModel.packet.variety,
                                                seedInputViewModel.packet.family,
                                                false,
                                                false
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    } else {
                                        // 商品名がない場合は現行アイコン+「種札」
                                        Image(
                                            painter = painterResource(id = R.drawable.seed_bag_full),
                                            contentDescription = IconDescriptionConstants.SEED_INFO_ICON,
                                            modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                                        )
                                        Text(
                                            text = ScreenTitleConstants.INPUT_TITLE,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }
                    } else if (currentRoute == NavigationConstants.CASTLE_ROUTE) {
                        // お城画面のタイトル表示
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.home),
                                contentDescription = IconDescriptionConstants.HOME_ICON,
                                modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE),
                                tint = ComposeColor.Unspecified
                            )
                            Text(
                                text = getCastleTitle(farmName),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    } else if (currentRoute == NavigationConstants.CALENDAR_ROUTE) {
                        // 種暦画面のタイトル表示
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = IconDescriptionConstants.CALENDAR_ICON,
                                modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE),
                                tint = ComposeColor.Unspecified
                            )
                            Text(
                                text = ScreenTitleConstants.CALENDAR_TITLE,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    } else {
                        // 種目録画面のタイトル表示
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.list),
                                contentDescription = IconDescriptionConstants.LIST_ICON,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                            )
                            Text(
                                text = ScreenTitleConstants.LIST_TITLE,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        },
        actions = {
            when (currentRoute) {
                NavigationConstants.SETTINGS_ROUTE -> {
                    // 設定画面では編集アイコンを表示
                    Box(
                        modifier = Modifier.padding(horizontal = LayoutConstants.HORIZONTAL_PADDING),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { 
                                // 編集モードの切り替え
                                settingsViewModel?.let { viewModel ->
                                    if (viewModel.isEditMode) {
                                        viewModel.exitEditMode()
                                    } else {
                                        viewModel.enterEditMode()
                                    }
                                }
                            },
                        ) {
                            if (settingsViewModel?.isEditMode == true) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = IconDescriptionConstants.CANCEL_BUTTON,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit),
                                    contentDescription = IconDescriptionConstants.EDIT_BUTTON,
                                    modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                                )
                            }
                        }
                    }
                }
                NavigationConstants.CALENDAR_ROUTE -> {
                    // 種暦画面では何も表示しない
                }
                NavigationConstants.INPUT_ROUTE_PREFIX -> {
                    // DisplayModeの時はEDITアイコン、EditModeの時は×ボタンを表示
                    Box(
                        modifier = Modifier.padding(horizontal = LayoutConstants.HORIZONTAL_PADDING),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { 
                                if (seedInputViewModel?.isEditMode == true) {
                                    // 編集モードを終了
                                    seedInputViewModel?.exitEditMode()
                                } else {
                                    // 編集モードに切り替え
                                    seedInputViewModel?.enterEditMode()
                                }
                            },
                        ) {
                            if (seedInputViewModel?.isEditMode == true) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = IconDescriptionConstants.CANCEL_BUTTON,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit),
                                    contentDescription = IconDescriptionConstants.EDIT_BUTTON,
                                    modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                                )
                            }
                        }
                    }
                }
                NavigationConstants.NOTIFICATION_HISTORY_ROUTE -> {
                    // 通知テスト・プレビュー画面への遷移ボタン
                    IconButton(
                        onClick = { navController.navigate(NavigationConstants.NOTIFICATION_PREVIEW_ROUTE) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.yabumi0),
                            contentDescription = IconDescriptionConstants.NOTIFICATION_PREVIEW_BUTTON,
                            tint = ComposeColor.Unspecified,
                            modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                        )
                    }
                }
                NavigationConstants.NOTIFICATION_PREVIEW_ROUTE -> {
                    // 通知テスト・プレビュー画面では何も表示しない
                }
                else -> {
                    if (currentRoute?.startsWith(NavigationConstants.INPUT_ROUTE_PREFIX) == true) {
                        // DisplayModeの時はEDITアイコン、EditModeの時は×ボタンを表示
                        Box(
                            modifier = Modifier.padding(horizontal = LayoutConstants.HORIZONTAL_PADDING),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { 
                                    if (seedInputViewModel?.isEditMode == true) {
                                        // 編集モードを終了
                                        seedInputViewModel?.exitEditMode()
                                    } else {
                                        // 編集モードに切り替え
                                        seedInputViewModel?.enterEditMode()
                                    }
                                },
                            ) {
                                if (seedInputViewModel?.isEditMode == true) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = IconDescriptionConstants.CANCEL_BUTTON,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.edit),
                                        contentDescription = IconDescriptionConstants.EDIT_BUTTON,
                                        modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(LayoutConstants.ACTION_SPACING),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 種一覧画面でチェックが入っている場合は削除ボタンを表示
                            if (currentRoute?.startsWith(NavigationConstants.LIST_ROUTE) == true && selectedIds.isNotEmpty()) {
                                IconButton(
                                    onClick = onDeleteSelected
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.delete),
                                        contentDescription = IconDescriptionConstants.DELETE_BUTTON,
                                        modifier = Modifier.size(IconSizeConstants.TOP_APP_BAR_SIZE)
                                    )
                                }
                            }
                            
                            // 設定ボタン
                            IconButton(
                                onClick = { navController.navigate(NavigationConstants.SETTINGS_ROUTE) },
                            ) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = IconDescriptionConstants.SETTINGS_BUTTON,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

/**
 * MainScaffoldのNavigationBarコンポーネント
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldNavigationBar(
    currentRoute: String?,
    navController: NavHostController,
    selectedIds: List<String>,
    isListScreen: Boolean,
    isInputScreen: Boolean,
    inputViewModel: SeedInputViewModel?,
    settingsViewModel: SettingsViewModel,
    unreadNotificationCount: Int,
    onSaveRequest: () -> Unit
) {
    // 回転アニメーション用の状態
    var isRotating by remember { mutableStateOf(false) }
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // お城アイコン
        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = { 
                val isSelected = currentRoute?.startsWith(NavigationConstants.CASTLE_ROUTE) == true
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = IconDescriptionConstants.CASTLE_ICON,
                    tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (isSelected) IconSizeConstants.SELECTED_SIZE else IconSizeConstants.DEFAULT_SIZE)
                )
            },
            label = { Text(ScreenTitleConstants.CASTLE_TITLE) },
            selected = currentRoute?.startsWith(NavigationConstants.CASTLE_ROUTE) == true,
            onClick = { navController.navigate(NavigationConstants.CASTLE_ROUTE) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        // 棚場アイコン
        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = { 
                val isSelected = currentRoute?.startsWith(NavigationConstants.LIST_ROUTE) == true
                val iconTint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                Image(
                    painter = painterResource(id = R.drawable.seeds_pack_bw),
                    contentDescription = IconDescriptionConstants.LIST_ICON,
                    colorFilter = ColorFilter.tint(iconTint),
                    modifier = Modifier.size(if (isSelected) IconSizeConstants.SELECTED_SIZE else IconSizeConstants.DEFAULT_SIZE)
                )
            },
            label = { Text(ScreenTitleConstants.LIST_TITLE) },
            selected = currentRoute?.startsWith(NavigationConstants.LIST_ROUTE) == true,
            onClick = { navController.navigate(NavigationConstants.LIST_ROUTE) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        // 中央のFab
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                currentRoute == NavigationConstants.SETTINGS_ROUTE && settingsViewModel.isEditMode -> {
                    FloatingActionButton(
                        onClick = onSaveRequest,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = IconDescriptionConstants.SAVE_BUTTON
                        )
                    }
                }
                isInputScreen && !(inputViewModel?.isLoading ?: false) -> {
                    FloatingActionButton(
                        onClick = { /* 入力画面の保存処理は別途実装 */ },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = IconDescriptionConstants.SAVE_BUTTON)
                    }
                }
                else -> {
                    FloatingActionButton(
                        onClick = { navController.navigate("${NavigationConstants.INPUT_ROUTE_PREFIX}/") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = IconDescriptionConstants.ADD_BUTTON)
                    }
                }
            }
        }
        
        // カレンダーアイコン
        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = { 
                val isSelected = currentRoute?.startsWith(NavigationConstants.CALENDAR_ROUTE) == true
                val iconTint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                val isDarkTheme = isSystemInDarkTheme()
                if (isDarkTheme) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                        contentDescription = IconDescriptionConstants.CALENDAR_ICON,
                        tint = iconTint,
                        modifier = Modifier.size(if (isSelected) IconSizeConstants.SELECTED_SIZE else IconSizeConstants.DEFAULT_SIZE)
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            id = if (isSelected) 
                                R.drawable.calendar_dark 
                            else 
                                R.drawable.calendar_light
                        ),
                        contentDescription = IconDescriptionConstants.CALENDAR_ICON,
                        tint = iconTint,
                        modifier = Modifier.size(if (isSelected) IconSizeConstants.SELECTED_SIZE else IconSizeConstants.DEFAULT_SIZE)
                    )
                }
            },
            label = { Text(ScreenTitleConstants.CALENDAR_TITLE) },
            selected = currentRoute?.startsWith(NavigationConstants.CALENDAR_ROUTE) == true,
            onClick = { navController.navigate(NavigationConstants.CALENDAR_ROUTE) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        // 通知アイコン（yabumi - 矢文）
        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = { 
                Box {
                    val isSelected = currentRoute?.startsWith(NavigationConstants.NOTIFICATION_HISTORY_ROUTE) == true
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isRotating) 360f else 0f,
                        animationSpec = tween(durationMillis = AnimationConstants.ROTATION_DURATION_MS),
                        finishedListener = { isRotating = false }
                    )
                    
                    Icon(
                        painter = painterResource(id = R.drawable.yabumi0),
                        contentDescription = IconDescriptionConstants.NOTIFICATION_ICON,
                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(if (isSelected) IconSizeConstants.SELECTED_SIZE else IconSizeConstants.DEFAULT_SIZE)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                            }
                    )
                    // 未読通知バッジ
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(IconSizeConstants.BADGE_SIZE)
                                .offset(x = LayoutConstants.BADGE_OFFSET_X, y = LayoutConstants.BADGE_OFFSET_Y)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getBadgeText(unreadNotificationCount),
                                color = MaterialTheme.colorScheme.onTertiary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            },
            label = { Text(ScreenTitleConstants.NOTIFICATION_TITLE) },
            selected = currentRoute?.startsWith(NavigationConstants.NOTIFICATION_HISTORY_ROUTE) == true,
            onClick = { 
                // 回転アニメーションを開始
                isRotating = true
                navController.navigate(NavigationConstants.NOTIFICATION_HISTORY_ROUTE) 
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
