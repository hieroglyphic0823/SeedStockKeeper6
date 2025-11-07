package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.components.FamilyIcon
import com.example.seedstockkeeper6.ui.components.FamilyIconCircle
import com.example.seedstockkeeper6.util.familyRotationYearsRange
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import kotlinx.coroutines.launch
import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoSection(
    viewModel: SeedInputViewModel,
    snackbarHostState: SnackbarHostState? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showStatusBottomSheet by remember { mutableStateOf(false) }
    
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
    
    // 状態を更新する関数
    fun updateStatus(selectedStatus: String) {
        android.util.Log.d("BasicInfoSection", "updateStatus: 開始 - selectedStatus=$selectedStatus, isEditMode=${viewModel.isEditMode}, hasExistingData=${viewModel.hasExistingData}")
        android.util.Log.d("BasicInfoSection", "updateStatus: 更新前のpacket状態 - isFinished=${viewModel.packet.isFinished}, isExpired=${viewModel.packet.isExpired}, sowingDate=${viewModel.packet.sowingDate}")
        
        // 編集モード時は選択した状態を保持
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            viewModel.selectedStatus = selectedStatus
            android.util.Log.d("BasicInfoSection", "updateStatus: selectedStatusを設定 - ${viewModel.selectedStatus}")
        }
        
        when (selectedStatus) {
            "finished" -> {
                // まき終わり: isFinished = true
                val currentDate = java.time.LocalDate.now()
                val sowingDate = currentDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                
                // 編集モードまたは新規作成時はローカルのみ更新
                if (viewModel.isEditMode || !viewModel.hasExistingData) {
                    android.util.Log.d("BasicInfoSection", "updateStatus: 編集モード/新規作成 - ローカルのみ更新")
                    // 状態を即座に更新（再コンポジションを確実に発生させる）
                    viewModel.updateFinishedFlag(true)
                    android.util.Log.d("BasicInfoSection", "updateStatus: updateFinishedFlag(true) 呼び出し後 - isFinished=${viewModel.packet.isFinished}")
                    viewModel.onSowingDateChange(sowingDate)
                    android.util.Log.d("BasicInfoSection", "updateStatus: onSowingDateChange($sowingDate) 呼び出し後 - sowingDate=${viewModel.packet.sowingDate}")
                    // isExpiredをfalseに設定（まき終わりの場合は期限切れではない）
                    viewModel.updateExpirationFlag(false)
                    android.util.Log.d("BasicInfoSection", "updateStatus: updateExpirationFlag(false) 呼び出し後 - isExpired=${viewModel.packet.isExpired}")
                    android.util.Log.d("BasicInfoSection", "updateStatus: 更新後のpacket状態 - isFinished=${viewModel.packet.isFinished}, isExpired=${viewModel.packet.isExpired}, sowingDate=${viewModel.packet.sowingDate}")
                    vibrateOnce()
                } else {
                    // 既存データで編集モードでない場合のみFirebaseに即座に保存
                    scope.launch {
                        viewModel.updateFinishedFlagAndRefresh(true) { result ->
                            scope.launch {
                                if (result.isSuccess) {
                                    vibrateOnce()
                                    snackbarHostState?.showSnackbar(
                                        message = "種をまき終わりに設定しました",
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    snackbarHostState?.showSnackbar(
                                        message = "更新に失敗しました",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "expired" -> {
                // 期限切れ: isExpired = true, isFinished = false, sowingDate = ""
                // 編集モードまたは新規作成時はローカルのみ更新
                if (viewModel.isEditMode || !viewModel.hasExistingData) {
                    android.util.Log.d("BasicInfoSection", "updateStatus: 編集モード/新規作成 - ローカルのみ更新（期限切れ）")
                    viewModel.updateFinishedFlag(false)
                    android.util.Log.d("BasicInfoSection", "updateStatus: updateFinishedFlag(false) 呼び出し後 - isFinished=${viewModel.packet.isFinished}")
                    viewModel.updateExpirationFlag(true)
                    android.util.Log.d("BasicInfoSection", "updateStatus: updateExpirationFlag(true) 呼び出し後 - isExpired=${viewModel.packet.isExpired}")
                    viewModel.onSowingDateChange("")
                    android.util.Log.d("BasicInfoSection", "updateStatus: onSowingDateChange(\"\") 呼び出し後 - sowingDate=${viewModel.packet.sowingDate}")
                    android.util.Log.d("BasicInfoSection", "updateStatus: 更新後のpacket状態 - isFinished=${viewModel.packet.isFinished}, isExpired=${viewModel.packet.isExpired}, sowingDate=${viewModel.packet.sowingDate}")
                    vibrateOnce()
                } else {
                    // 既存データで編集モードでない場合のみFirebaseに即座に保存
                    scope.launch {
                        viewModel.updateFinishedFlag(false)
                        viewModel.updateExpirationFlag(true)
                        viewModel.onSowingDateChange("")
                        viewModel.updateFinishedFlagAndRefresh(false) { finishedResult ->
                            scope.launch {
                                if (finishedResult.isSuccess) {
                                    viewModel.updateExpirationFlagInFirebase { expiredResult ->
                                        scope.launch {
                                            if (expiredResult.isSuccess) {
                                                vibrateOnce()
                                                snackbarHostState?.showSnackbar(
                                                    message = "期限切れに設定しました",
                                                    duration = SnackbarDuration.Short
                                                )
                                            } else {
                                                snackbarHostState?.showSnackbar(
                                                    message = "更新に失敗しました",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    snackbarHostState?.showSnackbar(
                                        message = "更新に失敗しました",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                // 通常/期限間近/まきどき: isFinished = false, sowingDate = ""（isExpiredは自動判定される）
                // 編集モードまたは新規作成時はローカルのみ更新
                if (viewModel.isEditMode || !viewModel.hasExistingData) {
                    android.util.Log.d("BasicInfoSection", "updateStatus: 編集モード/新規作成 - ローカルのみ更新（通常/期限間近/まきどき）")
                    // 状態を即座に更新（再コンポジションを確実に発生させる）
                    viewModel.updateFinishedFlag(false)
                    android.util.Log.d("BasicInfoSection", "updateStatus: updateFinishedFlag(false) 呼び出し後 - isFinished=${viewModel.packet.isFinished}")
                    viewModel.onSowingDateChange("")
                    android.util.Log.d("BasicInfoSection", "updateStatus: onSowingDateChange(\"\") 呼び出し後 - sowingDate=${viewModel.packet.sowingDate}")
                    // isExpiredは自動判定されるので、checkAndUpdateExpirationFlagを呼ぶ
                    viewModel.checkAndUpdateExpirationFlag()
                    android.util.Log.d("BasicInfoSection", "updateStatus: checkAndUpdateExpirationFlag() 呼び出し後 - isExpired=${viewModel.packet.isExpired}")
                    android.util.Log.d("BasicInfoSection", "updateStatus: 更新後のpacket状態 - isFinished=${viewModel.packet.isFinished}, isExpired=${viewModel.packet.isExpired}, sowingDate=${viewModel.packet.sowingDate}")
                    vibrateOnce()
                } else {
                    // 既存データで編集モードでない場合のみFirebaseに即座に保存
                    scope.launch {
                        viewModel.updateFinishedFlagAndRefresh(false) { result ->
                            scope.launch {
                                if (result.isSuccess) {
                                    vibrateOnce()
                                    val statusName = when (selectedStatus) {
                                        "urgent" -> "期限間近"
                                        "thisMonth" -> "まきどき"
                                        else -> "通常"
                                    }
                                    snackbarHostState?.showSnackbar(
                                        message = "$statusName に設定しました",
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    snackbarHostState?.showSnackbar(
                                        message = "更新に失敗しました",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 種の状態を判定（編集モード時は選択した状態を優先）
        val seedStatus = if ((viewModel.isEditMode || !viewModel.hasExistingData) && viewModel.selectedStatus != null) {
            // 編集モード時は選択した状態を優先（ただし、finishedとexpiredはpacketの状態を優先）
            val selected = viewModel.selectedStatus!!
            android.util.Log.d("BasicInfoSection", "画面表示: 編集モード - selectedStatus=$selected を使用")
            if (selected == "finished" && viewModel.packet.isFinished) {
                "finished"
            } else if (selected == "expired" && viewModel.packet.isExpired) {
                "expired"
            } else if (selected != "finished" && selected != "expired") {
                // finished/expired以外の状態（urgent、thisMonth、normal）は選択した状態を使用
                selected
            } else {
                // 選択した状態とpacketの状態が一致しない場合は、packetの状態を優先
                getSeedStatus(viewModel.packet)
            }
        } else {
            getSeedStatus(viewModel.packet)
        }
        android.util.Log.d("BasicInfoSection", "画面表示: seedStatus=$seedStatus, packet.isFinished=${viewModel.packet.isFinished}, packet.isExpired=${viewModel.packet.isExpired}, packet.sowingDate=${viewModel.packet.sowingDate}, selectedStatus=${viewModel.selectedStatus}")
        val statusIconResId = when (seedStatus) {
            "finished" -> R.drawable.seed  // まき終わり：seed
            "urgent" -> R.drawable.warning  // 期限間近：warning
            "thisMonth" -> R.drawable.seed_bag_enp  // まきどき：seed_bag_enp
            "expired" -> R.drawable.close  // 期限切れ：close
            else -> R.drawable.seed_bag_full  // 通常：seed_bag_full
        }
        val statusName = when (seedStatus) {
            "finished" -> "まき終わり"
            "urgent" -> "期限間近"
            "thisMonth" -> "まきどき"
            "expired" -> "期限切れ"
            else -> "通常"
        }
        
        // 状態に応じた背景色とテキスト色（種目録の抽出条件と同じ色）
        val backgroundColor = when (seedStatus) {
            "finished" -> MaterialTheme.colorScheme.secondaryContainer  // まき終わり
            "urgent" -> MaterialTheme.colorScheme.errorContainer  // 期限間近
            "thisMonth" -> MaterialTheme.colorScheme.primaryContainer  // まきどき
            "expired" -> MaterialTheme.colorScheme.surfaceContainerHighest  // 期限切れ
            else -> MaterialTheme.colorScheme.tertiaryContainer  // 通常
        }
        val textColor = when (seedStatus) {
            "finished" -> MaterialTheme.colorScheme.onSecondaryContainer
            "urgent" -> MaterialTheme.colorScheme.onErrorContainer
            "thisMonth" -> MaterialTheme.colorScheme.onPrimaryContainer
            "expired" -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onTertiaryContainer
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 左側：seed_bag_fullアイコンとタイトル
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.seed_bag_full),
                    contentDescription = "基本情報",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Text(
                    "基本情報",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // 右側：状態アイコンと状態名（丸で囲む）
            // DisplayModeまたはEditModeの時クリック可能
            val isClickable = viewModel.hasExistingData || viewModel.isEditMode
            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor,
                        shape = CircleShape
                    )
                    .then(
                        if (isClickable) {
                            Modifier.clickable {
                                if (viewModel.isEditMode) {
                                    // 編集モード: BottomSheetを表示
                                    showStatusBottomSheet = true
                                } else {
                                    // DisplayMode: まき終わりの切り替え
                                    val isChecked = if (seedStatus == "finished") {
                                        // まき終わりの場合は前の状態に戻す（isFinishedをfalseに）
                                        false
                                    } else {
                                        // その他の状態の場合はまき終わりに設定
                                        true
                                    }
                                    scope.launch {
                                        viewModel.updateFinishedFlagAndRefresh(isChecked) { result ->
                                            scope.launch {
                                                if (result.isSuccess) {
                                                    // バイブレーション
                                                    vibrateOnce()
                                                    val message = if (isChecked) "種をまき終わりました" else "まき終わりを解除しました"
                                                    snackbarHostState?.showSnackbar(
                                                        message = message,
                                                        duration = SnackbarDuration.Short
                                                    )
                                                } else {
                                                    snackbarHostState?.showSnackbar(
                                                        message = "更新に失敗しました",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = statusIconResId),
                        contentDescription = statusName,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusName,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }
            }
        }
        
        // 商品名
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.productName,
                onValueChange = viewModel::onProductNameChange,
                label = { Text("商品名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "商品名: ${viewModel.packet.productName.ifEmpty { "未設定" }}",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 品種
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.variety,
                onValueChange = viewModel::onVarietyChange,
                label = { Text("品種") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "品種: ${viewModel.packet.variety.ifEmpty { "未設定" }}",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 科名
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            // EditMode: 科名のOutlineTextと横並びで選択した科名に合ったFamilyアイコンを表示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FamilySelector(
                    value = viewModel.packet.family,
                    onValueChange = viewModel::onFamilyChange,
                    modifier = Modifier.weight(1f)
                )
                // 選択した科名に合ったFamilyアイコン
                com.example.seedstockkeeper6.ui.components.FamilyIcon(
                    family = viewModel.packet.family,
                    size = 24.dp
                )
            }
        } else {
            // DisplayMode: 「科名: せり科（アイコン）」の順に表示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "科名: ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = viewModel.packet.family.ifEmpty { "未設定" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // コンパニオンプランツと同じスタイルの丸いアイコン
                FamilyIconCircle(
                    family = viewModel.packet.family
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 連作障害年数
        val rotationYears = familyRotationYearsRange(viewModel.packet.family)
        if (rotationYears != null) {
            Text(
                text = "連作障害年数: $rotationYears",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
    }
    
    // 状態選択BottomSheet（編集モード時のみ）
    if (showStatusBottomSheet) {
        // BottomSheet内でseedStatusを再計算
        val currentSeedStatus = getSeedStatus(viewModel.packet)
        
        ModalBottomSheet(
            onDismissRequest = { showStatusBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "種の状態を選択",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 各状態の選択ボタン
                val statusOptions = listOf(
                    "finished" to Pair("まき終わり", R.drawable.seed),
                    "urgent" to Pair("期限間近", R.drawable.warning),
                    "thisMonth" to Pair("まきどき", R.drawable.seed_bag_enp),
                    "expired" to Pair("期限切れ", R.drawable.close),
                    "normal" to Pair("通常", null)
                )
                
                statusOptions.forEach { (status, pair) ->
                    val (name, iconResId) = pair
                    val optionBgColor = when (status) {
                        "finished" -> MaterialTheme.colorScheme.secondaryContainer
                        "urgent" -> MaterialTheme.colorScheme.errorContainer
                        "thisMonth" -> MaterialTheme.colorScheme.primaryContainer
                        "expired" -> MaterialTheme.colorScheme.surfaceContainerHighest
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                    val optionTextColor = when (status) {
                        "finished" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "urgent" -> MaterialTheme.colorScheme.onErrorContainer
                        "thisMonth" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "expired" -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                    val isSelected = currentSeedStatus == status
                    
                    Button(
                        onClick = {
                            android.util.Log.d("BasicInfoSection", "ボトムシート: 状態選択 - status=$status, name=$name")
                            updateStatus(status)
                            // 状態更新後にボトムシートを閉じる（状態更新が反映されるように少し遅延）
                            scope.launch {
                                kotlinx.coroutines.delay(50) // 状態更新が反映されるまで少し待つ
                                android.util.Log.d("BasicInfoSection", "ボトムシート: 閉じる前のpacket状態 - isFinished=${viewModel.packet.isFinished}, isExpired=${viewModel.packet.isExpired}, sowingDate=${viewModel.packet.sowingDate}")
                                showStatusBottomSheet = false
                                android.util.Log.d("BasicInfoSection", "ボトムシート: 閉じた後のpacket状態 - isFinished=${viewModel.packet.isFinished}, isExpired=${viewModel.packet.isExpired}, sowingDate=${viewModel.packet.sowingDate}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = optionBgColor,
                            contentColor = optionTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (iconResId != null) {
                                Icon(
                                    painter = painterResource(id = iconResId),
                                    contentDescription = name,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )
                            } else {
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "選択中",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
