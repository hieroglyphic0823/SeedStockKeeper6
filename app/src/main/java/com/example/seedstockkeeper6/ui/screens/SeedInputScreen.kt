// SeedInputScreen.kt

package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.seedstockkeeper6.FullScreenSaveAnimation
import com.example.seedstockkeeper6.ui.components.AIDiffDialog
import com.example.seedstockkeeper6.ui.components.RegionSelectionDialog
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlin.math.abs
import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context

// 分離したコンポーネントのインポート
import com.example.seedstockkeeper6.ui.screens.*



@Composable
fun SeedInputScreen(
    navController: NavController,
    viewModel: SeedInputViewModel,
    settingsViewModel: com.example.seedstockkeeper6.viewmodel.SettingsViewModel? = null,
    onSaveRequest: () -> Unit = {} // MainScaffoldからの保存リクエストコールバック
) {
    
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 削除確認ダイアログの表示状態
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    // バイブレーション機能
    fun vibrateOnce() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(50)
                }
            }
        } catch (e: Exception) {
            // バイブレーションが利用できない場合は無視
        }
    }
    
    // 加速度センサーで振動を検知して「まき終わり」に設定
    val sensorManager = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    
    // 前回の加速度値と振動検知フラグ
    var lastAcceleration by remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }
    var lastShakeTime by remember { mutableStateOf(0L) }
    var isProcessingShake by remember { mutableStateOf(false) }
    
    DisposableEffect(accelerometer) {
        // センサーリスナーの定義は外側のスコープに移動
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    
                    // 重力を除いた加速度の変化量を計算
                    val deltaX = abs(x - lastAcceleration[0])
                    val deltaY = abs(y - lastAcceleration[1])
                    val deltaZ = abs(z - lastAcceleration[2])
                    
                    // 振動の閾値（適切な値に調整）
                    val shakeThreshold = 15.0f
                    val totalAcceleration = deltaX + deltaY + deltaZ
                    
                    val currentTime = System.currentTimeMillis()
                    // 連続して検知しないように、2秒間隔で制限し、処理中は無視
                    if (totalAcceleration > shakeThreshold && 
                        currentTime - lastShakeTime > 2000 && 
                        !isProcessingShake) {
                        lastShakeTime = currentTime
                        
                        // 既存データがある場合のみ「まき終わり」に設定
                        if (viewModel.hasExistingData && !viewModel.packet.isFinished) {
                            isProcessingShake = true
                            scope.launch {
                                try {
                                    viewModel.updateFinishedFlagAndRefresh(true) { result ->
                                        scope.launch {
                                            isProcessingShake = false
                                            if (result.isSuccess) {
                                                // バイブレーション
                                                vibrateOnce()
                                                snackbarHostState.showSnackbar(
                                                    message = "種をまき終わりました",
                                                    duration = SnackbarDuration.Short
                                                )
                                            } else {
                                                snackbarHostState.showSnackbar(
                                                    message = "更新に失敗しました",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    isProcessingShake = false
                                    android.util.Log.e("SeedInputScreen", "振動検知処理エラー", e)
                                }
                            }
                        }
                    }
                    
                    // 配列の値を直接更新（メモリリークを防ぐ）
                    lastAcceleration[0] = x
                    lastAcceleration[1] = y
                    lastAcceleration[2] = z
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // 精度変更時は何もしない
            }
        }
        
        // accelerometerがnullでない場合のみ、センサーリスナーを登録する
        if (accelerometer != null) {
            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        // onDisposeブロックに登録と解除をまとめる
        // 画面破棄時に必ずセンサーリスナーを解除する
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    
    // 農園情報の地域を設定
    LaunchedEffect(settingsViewModel?.defaultRegion) {
        settingsViewModel?.defaultRegion?.let { region ->
            viewModel.farmDefaultRegion = region
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // AIで読み取り処理中、地域選択ダイアログ表示中はFABを非表示
            if ((viewModel.isEditMode || !viewModel.hasExistingData) && 
                !viewModel.isLoading && 
                !viewModel.showRegionSelectionDialog) {
                // 保存FAB（右側）
                FloatingActionButton(
                    onClick = {
                        onSaveRequest() // MainScaffoldの保存アニメーションを表示
                        viewModel.saveSeedData(context) { result ->
                            if (result.isSuccess) {
                                viewModel.exitEditMode()
                                viewModel.markAsExistingData() // DisplayModeにする
                                
                                // 保存完了のSnackbarを表示
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "保存しました",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                // 保存失敗のSnackbarを表示
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "保存に失敗しました",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "保存",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
            // 画像管理セクション
            ImageManagementSection(viewModel)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // 基本情報セクション
            BasicInfoSection(viewModel, snackbarHostState)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // カレンダーセクション
            CalendarSection(viewModel)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // メモ・コンパニオンプランツカード（DisplayModeのみ）
            NotesCardSection(viewModel)
            
            // 栽培情報セクション
            CultivationInfoSection(viewModel)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            }
            
            // 削除FAB（既存データがある場合のみ表示、保存FABの左側に配置）
            if ((viewModel.isEditMode || !viewModel.hasExistingData) && 
                viewModel.hasExistingData &&
                !viewModel.isLoading && 
                !viewModel.showRegionSelectionDialog) {
                FloatingActionButton(
                    onClick = {
                        showDeleteConfirmDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 80.dp, bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "削除",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // ダイアログ類
            if (viewModel.showCropConfirmDialog) {
                CropConfirmDialog(viewModel = viewModel)
            }
            
            // AI処理中のみSukesanGifAnimationを表示（保存処理中は表示しない）
            if (viewModel.isLoading && !viewModel.isSaving) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .zIndex(999f),
                    contentAlignment = Alignment.Center
                ) {
                    SukesanGifAnimation()
                }
            }
        }
    }

    // AI差分ダイアログ
    AIDiffDialog(
        showDialog = viewModel.showAIDiffDialog,
        diffList = viewModel.aiDiffList,
        onConfirm = { viewModel.applyAIDiffResult() },
        onDismiss = { viewModel.onAIDiffDialogDismiss() }
    )
    
    // 地域選択ダイアログ
    RegionSelectionDialog(
        showDialog = viewModel.showRegionSelectionDialog,
        regionList = viewModel.detectedRegions,
        ocrResult = viewModel.ocrResult,
        croppedCalendarBitmap = viewModel.croppedCalendarBitmap,
        editingCalendarEntry = viewModel.editingCalendarEntry,
        defaultRegion = viewModel.packet.calendar?.firstOrNull()?.region
            ?: viewModel.farmDefaultRegion, // CalendarEntry.regionを優先、なければ農園情報の地域
        onRegionSelected = { viewModel.onRegionSelected(it) },
        onStartEditing = { viewModel.startEditingCalendarEntry(it) },
        onUpdateEditing = { viewModel.updateEditingCalendarEntry(it) },
        onSaveEditing = { viewModel.saveEditingCalendarEntry() },
        onCancelEditing = { viewModel.cancelEditingCalendarEntry() },
        onDismiss = { viewModel.onRegionSelectionDismiss() },
        onUpdateExpiration = { viewModel.updateExpirationFromCalendarEntry(it) } // 有効期限更新のコールバック
    )
    
    // 画像拡大表示ダイアログ
    ImageDialog(viewModel)
    
    // 削除確認ダイアログ
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("削除確認") },
            text = { Text("削除しますか？\nY/N") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        // Yの場合：削除を実行
                        viewModel.deleteSeedData(context) { result ->
                            scope.launch {
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar(
                                        message = "削除しました",
                                        duration = SnackbarDuration.Short
                                    )
                                    // 削除後、種目録画面に戻る
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "削除に失敗しました",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Text("Y")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("N")
                }
            }
        )
    }
    
    // 保存アニメーション（MainScaffoldで管理されるため削除）
}
