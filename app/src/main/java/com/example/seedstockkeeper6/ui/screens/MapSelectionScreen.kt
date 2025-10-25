package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.MarkerOptions
import com.example.seedstockkeeper6.service.GoogleMapsAuthService
import com.example.seedstockkeeper6.service.GeocodingService
import kotlinx.coroutines.launch
import android.location.Geocoder
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@Composable
fun MapSelectionScreen(
    initialLatitude: Double = 35.6762, // 東京の緯度
    initialLongitude: Double = 139.6503, // 東京の経度
    onLocationSelected: (Double, Double, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var address by remember { mutableStateOf("農園位置") }
    var isLoadingAddress by remember { mutableStateOf(false) }
    
    // Google Maps認証サービス
    val googleMapsAuthService = remember { 
        GoogleMapsAuthService(context) 
    }
    val geocodingService = remember { GeocodingService(context) }
    
    // 認証状態の管理
    var isFirebaseAuthenticated by remember { mutableStateOf(false) }
    var isGoogleMapsAuthenticated by remember { mutableStateOf(false) }
    var firebaseUserInfo by remember { mutableStateOf<String?>(null) }
    var googleMapsUserInfo by remember { mutableStateOf<String?>(null) }
    
    // 検索機能の状態管理
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    
    // 地図タイプの状態管理
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showMapTypeSelector by remember { mutableStateOf(false) }
    
    // 地図プロパティを地図タイプの変更に応じて再作成
    val mapProperties = remember(mapType) {
        MapProperties(
            mapType = mapType,
            isMyLocationEnabled = false,
            isTrafficEnabled = false
        )
    }
    
    // 認証スキップの状態管理
    var isAuthSkipped by remember { mutableStateOf(false) }
    
    // Firebase保存状態の管理
    var isSavingToFirebase by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    
    // Google認証のActivityResultLauncher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            try {
                
                // 認証結果を処理
                val signInResult = googleMapsAuthService.getCurrentUser()
                if (signInResult != null) {
                    // 認証状態を更新
                    isGoogleMapsAuthenticated = true
                    googleMapsUserInfo = signInResult.email
                    
                    // 認証成功時のスナックバー表示（オプション）
        } else {
                    // 認証がキャンセルされた場合の処理
                    isGoogleMapsAuthenticated = false
                    googleMapsUserInfo = null
                }
            } catch (e: Exception) {
                isGoogleMapsAuthenticated = false
                googleMapsUserInfo = null
            }
        }
    }
    
    // 初期位置とカメラ状態（performSearchより前に宣言）
    val initialPosition = LatLng(initialLatitude, initialLongitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
                .target(initialPosition)
                .zoom(15f)
                .build()
    }
    
    // 保存済みの農園位置がある場合は初期選択位置として設定
    LaunchedEffect(initialLatitude, initialLongitude) {
        if (initialLatitude != 35.6762 || initialLongitude != 139.6503) {
            // デフォルトの東京座標でない場合は、保存済みの農園位置として扱う
            selectedLocation = LatLng(initialLatitude, initialLongitude)
            
            // 保存済み位置の住所を取得
            scope.launch {
                try {
                    address = geocodingService.getAddressFromLatLng(LatLng(initialLatitude, initialLongitude))
                } catch (e: Exception) {
                    address = "住所を取得できませんでした: ${e.message}"
                }
            }
        }
    }
    
    // 検索機能の実装
    fun performSearch(query: String) {
        if (query.isBlank()) return
        
        scope.launch {
            isSearching = true
            searchError = null
            
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 1)
                
                if (addresses?.isNotEmpty() == true) {
                    val foundAddress = addresses[0]
                    val latLng = LatLng(foundAddress.latitude, foundAddress.longitude)
                    
                    // 地図を検索結果に移動
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                    
                    // 選択位置を更新
                    selectedLocation = latLng
                    scope.launch {
                        try {
                            address = geocodingService.getAddressFromLatLng(latLng)
                        } catch (e: Exception) {
                            address = "住所を取得できませんでした: ${e.message}"
                        }
                    }
                } else {
                    searchError = "場所が見つかりませんでした"
                }
            } catch (e: Exception) {
                searchError = "検索エラー: ${e.message}"
            } finally {
                isSearching = false
            }
        }
    }
    
    // 地図タイプ切り替え機能
    fun toggleMapType() {
        val previousType = mapType
        mapType = when (mapType) {
            MapType.NORMAL -> MapType.SATELLITE
            MapType.SATELLITE -> MapType.HYBRID
            MapType.HYBRID -> MapType.TERRAIN
            MapType.TERRAIN -> MapType.NORMAL
            else -> MapType.NORMAL
        }
    }
    
    // Firebaseに農園位置を保存する機能
    suspend fun saveFarmLocationToFirebase(latitude: Double, longitude: Double, address: String) {
        try {
            isSavingToFirebase = true
            saveError = null
            
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid
            
            if (uid == null) {
                saveError = "ログインが必要です"
                return
            }
            
            val db = Firebase.firestore
            val settingsDoc = db.collection("users").document(uid).collection("settings").document("general")
            
            val farmLocationData: Map<String, Any> = mapOf(
                "farmLatitude" to latitude,
                "farmLongitude" to longitude,
                "farmAddress" to address,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            
            // 既存の設定を取得して、農園位置のみを更新
            val existingDoc = settingsDoc.get().await()
            if (existingDoc.exists()) {
                // 既存の設定を保持して、農園位置のみを更新
                val existingData: Map<String, Any> = existingDoc.data ?: emptyMap()
                val updatedData = existingData.toMutableMap()
                updatedData.putAll(farmLocationData)
                settingsDoc.set(updatedData).await()
            } else {
                // 新規作成
                settingsDoc.set(farmLocationData).await()
            }
            
            
        } catch (e: Exception) {
            saveError = "保存エラー: ${e.message}"
        } finally {
            isSavingToFirebase = false
        }
    }
    
    // 認証状態の確認
    LaunchedEffect(Unit) {
        try {
            
            // Firebase認証状態を確認
            isFirebaseAuthenticated = googleMapsAuthService.isFirebaseUserSignedIn()
            firebaseUserInfo = googleMapsAuthService.getFirebaseUserInfo()
            
            // Googleマップ認証状態を確認
            val googleUser = googleMapsAuthService.getCurrentUser()
            isGoogleMapsAuthenticated = googleUser != null
            googleMapsUserInfo = googleUser?.email
            
            
            // 認証インテントの確認
            val signInIntent = googleMapsAuthService.getSignInIntent()
            
        } catch (e: Exception) {
        }
    }
    
    // 認証状態の更新を監視
    LaunchedEffect(isGoogleMapsAuthenticated) {
        if (isGoogleMapsAuthenticated) {
            // 認証成功時の処理
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // ヘッダー
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Close, contentDescription = "キャンセル")
                }
                
                // 中央のスペーサー（タイトルがAppTopBarに移動したため）
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                onClick = {
                    selectedLocation?.let { location ->
                            // Firebaseに保存してからコールバックを呼び出し
                            scope.launch {
                                saveFarmLocationToFirebase(location.latitude, location.longitude, address)
                                if (saveError == null) {
                        onLocationSelected(location.latitude, location.longitude, address)
                                }
                            }
                        }
                    },
                    enabled = selectedLocation != null && !isSavingToFirebase
                ) {
                    if (isSavingToFirebase) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "決定",
                    tint = if (selectedLocation != null) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
                }
            }
            
            // 検索バー
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("場所や住所を検索") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isSearching,
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(
                                    onClick = { performSearch(searchQuery) },
                                    enabled = searchQuery.isNotBlank()
                                ) {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = "検索",
                                        tint = if (searchQuery.isNotBlank()) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    )
                    
                    // 地図タイプ切り替えボタン
                    IconButton(
                        onClick = { 
                            toggleMapType() 
                        }
                    ) {
                        Icon(
                            imageVector = when (mapType) {
                                MapType.NORMAL -> Icons.Filled.Map
                                MapType.SATELLITE -> Icons.Filled.Satellite
                                MapType.HYBRID -> Icons.Filled.Satellite
                                MapType.TERRAIN -> Icons.Filled.Map
                                else -> Icons.Filled.Map
                            },
                            contentDescription = "地図タイプ切り替え",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 検索エラー表示
                searchError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                // Firebase保存状態表示
                if (isSavingToFirebase) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Firebaseに保存中...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                saveError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
        }
        
        // GoogleMap
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = false,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true,
                    tiltGesturesEnabled = true,
                    rotationGesturesEnabled = true
                ),
                onMapLoaded = {
                    // 地図読み込み完了時の処理
                },
                onMapClick = { latLng ->
                    // 地図をタップした時の処理
                    selectedLocation = latLng
                    isLoadingAddress = true
                    
                    // 逆ジオコーディングで住所を取得
                    scope.launch {
                        try {
                            address = geocodingService.getAddressFromLatLng(latLng)
                        } catch (e: Exception) {
                            address = "住所を取得できませんでした: ${e.message}"
                        } finally {
                            isLoadingAddress = false
                        }
                    }
                }
            ) {
                // マーカー表示
                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "農園位置",
                        snippet = address
                    )
                }
            }
            
            // 中央のピンアイコン
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "選択位置",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // 位置情報表示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "選択された位置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                    // 地図タイプ表示
                    Text(
                        text = when (mapType) {
                            MapType.NORMAL -> "通常地図"
                            MapType.SATELLITE -> "航空写真"
                            MapType.HYBRID -> "ハイブリッド"
                            MapType.TERRAIN -> "地形"
                            else -> "通常地図"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    // デバッグ用：現在の地図タイプを表示
                    Text(
                        text = "($mapType)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                // 認証状態に応じた表示
                if (!isFirebaseAuthenticated) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⚠ アプリにログインしていません",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "農園位置を保存するには、まずアプリにログインしてください。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else if (!isGoogleMapsAuthenticated && !isAuthSkipped) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ℹ Googleマップ認証が必要です",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "地図機能を使用するには、Googleアカウントでの認証が必要です。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Googleマップ認証を開始
                                        try {
                                            val signInIntent = googleMapsAuthService.getSignInIntent()
                                            
                                            if (signInIntent != null) {
                                                googleSignInLauncher.launch(signInIntent)
                                            } else {
                                                // エラーメッセージを表示
                                                saveError = "認証インテントが取得できません。Google Play Servicesが正しくインストールされているか確認してください。"
                                            }
                                        } catch (e: Exception) {
                                            saveError = "認証エラー: ${e.message}"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Googleマップ認証")
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        // 認証をスキップして基本機能を使用
                                        isAuthSkipped = true
                                    }
                                ) {
                                    Text("認証をスキップ")
                                }
                            }
                        }
                    }
                } else if (isAuthSkipped) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ℹ 基本機能モード",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "認証をスキップして基本機能を使用しています。一部の高度な機能は制限される場合があります。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else {
                    // 認証済みの場合の通常表示
                    selectedLocation?.let { location ->
                        // 保存済みの農園位置かどうかを判定
                        val isSavedLocation = (initialLatitude != 35.6762 || initialLongitude != 139.6503) && 
                                             location.latitude == initialLatitude && 
                                             location.longitude == initialLongitude
                        
                        if (isSavedLocation) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "✓ 保存済みの農園位置",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        
                    Text(
                        text = "緯度: ${String.format("%.6f", location.latitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "経度: ${String.format("%.6f", location.longitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isLoadingAddress) {
                        Text(
                            text = "住所を取得中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (address.isNotEmpty()) {
                        Text(
                            text = "住所: $address",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } ?: run {
                    Text(
                        text = "地図をタップして位置を選択してください",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    }
                }
            }
        }
    }
}
