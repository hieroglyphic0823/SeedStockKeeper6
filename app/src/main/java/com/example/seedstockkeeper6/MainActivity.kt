@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.auth.AuthGate
import com.example.seedstockkeeper6.ui.screens.SeedInputScreen
import com.example.seedstockkeeper6.ui.screens.SeedListScreen
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.theme.ThemeFlavor
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.layout.offset
import kotlinx.coroutines.delay


// ナビゲーション項目の定義
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconRes: Int
) {
    object Home : BottomNavItem(
        route = "list",
        title = "ホーム",
        iconRes = 0
    )
    object Search : BottomNavItem(
        route = "search",
        title = "検索",
        iconRes = 1
    )
    object Add : BottomNavItem(
        route = "add",
        title = "追加",
        iconRes = 2
    )
    object Calendar : BottomNavItem(
        route = "calendar",
        title = "カレンダー",
        iconRes = 3
    )
    object Settings : BottomNavItem(
        route = "settings",
        title = "設定",
        iconRes = 4
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Search,
    BottomNavItem.Add,
    BottomNavItem.Calendar,
    BottomNavItem.Settings
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DebugTrace", "MainActivity.onCreate called")
        FirebaseApp.initializeApp(this)
        Log.d("DebugTrace", "FirebaseApp initialized")

        setContent {
            Log.d("DebugTrace", "setContent initializing")
            val navController = rememberNavController()

            SeedStockKeeper6Theme(
                flavor = ThemeFlavor.Onion , //Vitamin, Soil, Herb, Ocean, Plum, Sakura, WB,Onion  ← 試したい配色を指定
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false          // パレットを見たい時は false 推奨
            ) {
                Surface(                      // ★ これが“アプリ全体の背景”
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthGate { user ->
                        MainScaffold(navController = navController, user = user)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    user: FirebaseUser
) {
    val selectedIds = remember { mutableStateListOf<String>() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isListScreen = currentRoute == "list"
    val isInputScreen = currentRoute?.startsWith("input") == true

    val snackbarHostState = remember { SnackbarHostState() }
    val listViewModel: SeedListViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    
    // 全画面アニメーション用の状態
    var showSaveAnimation by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp) // 丸の大きさ（アイコンより少し大きめ）
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer, // Material 3準拠
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center // アイコンを真ん中に配置
                    ) {
                        AccountMenuButton(
                            user = user,
                            size = 38.dp, // 中のアイコンを少し小さめに
                            onSignOut = { signOut(ctx, scope) }
                        )
                    }
                },
                title = { Text("たねすけさん") },
                actions = {
                    when {
                        // 1) リスト画面で選択あり → 削除ボタン
                        isListScreen && selectedIds.isNotEmpty() -> {
                            IconButton(onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    selectedIds.forEach { id ->
                                        listViewModel.deleteSeedPacketWithImages(id) { result ->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                val message = if (result.isSuccess) "削除しました"
                                                else "削除に失敗しました: ${result.exceptionOrNull()?.localizedMessage ?: "不明なエラー"}"
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    }
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                        // 2) 入力画面 → 保存ボタン
                        isInputScreen && navBackStackEntry != null -> {
                            val inputViewModel: SeedInputViewModel = viewModel(
                                viewModelStoreOwner = navBackStackEntry!!
                            )
                            IconButton(onClick = {
                                // 全画面アニメーションを表示
                                showSaveAnimation = true
                                
                                // アニメーション完了後に保存処理を実行
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1500) // アニメーション時間
                                    showSaveAnimation = false
                                    
                                    inputViewModel.saveSeed(ctx) { result ->
                                        scope.launch(Dispatchers.Main) {
                                            val message = if (result.isSuccess) {
                                                navController.popBackStack()
                                                "保存しました"
                                            } else {
                                                "保存に失敗しました: ${result.exceptionOrNull()?.localizedMessage ?: "不明なエラー"}"
                                            }
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Save, contentDescription = "Save")
                            }
                        }
                        // 3) リスト画面で選択なし & DEBUG → 🐞デバッグボタン
                        isListScreen && selectedIds.isEmpty() && false -> { // デバッグボタンを無効化
                            IconButton(onClick = { navController.navigate("debugDetectOuter") }) {
                                Icon(Icons.Outlined.BugReport, contentDescription = "Debug: Detect Outer")
                            }
                        }
                        else -> Unit
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { 
                            when (item.iconRes) {
                                0 -> AnimatedIcon(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.indoor_plants), 
                                    contentDescription = "ホーム",
                                    tint = Color.Unspecified
                                )
                                1 -> AnimatedIcon(
                                    icon = Icons.Filled.Search, 
                                    contentDescription = "検索",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                2 -> AnimatedIcon(
                                    icon = Icons.Filled.Add, 
                                    contentDescription = "追加",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                3 -> AnimatedIcon(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.calendar), 
                                    contentDescription = "カレンダー",
                                    tint = Color.Unspecified
                                )
                                4 -> AnimatedIcon(
                                    icon = Icons.Filled.Settings, 
                                    contentDescription = "設定",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                else -> AnimatedIcon(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.indoor_plants), 
                                    contentDescription = "ホーム",
                                    tint = Color.Unspecified
                                )
                            }
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            when (item) {
                                is BottomNavItem.Add -> {
                                    // 追加ボタンが押されたら入力画面に遷移
                                    val emptyPacketJson = URLEncoder.encode(
                                        Gson().toJson(SeedPacket()),
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    navController.navigate("input/$emptyPacketJson")
                                }
                                else -> {
                                    // その他のボタンは通常のナビゲーション
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        popUpTo(navController.graph.startDestinationId) { 
                                            saveState = true 
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(padding),
                selectedIds = selectedIds
            )
            
            // 全画面保存アニメーション
            if (showSaveAnimation) {
                FullScreenSaveAnimation()
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    selectedIds: MutableList<String>
) {
    NavHost(
        navController = navController,
        startDestination = "list",
        modifier = modifier
    ) {
        composable("list") {
            Log.d("BootTrace", "Screen: SeedListScreen初期化")
            val listViewModel: SeedListViewModel = viewModel()
            SeedListScreen(
                navController = navController,
                viewModel = listViewModel,
                selectedIds = selectedIds
            )
        }
        composable("input/{packet}") { backStackEntry ->
            Log.d("BootTrace", "Screen: SeedInputScreen初期化")
            val json = backStackEntry.arguments?.getString("packet") ?: ""
            val packet = if (json.isNotEmpty()) Gson().fromJson(json, SeedPacket::class.java) else null
            val currentInputViewModel: SeedInputViewModel = viewModel(viewModelStoreOwner = backStackEntry)
            LaunchedEffect(json) {
                currentInputViewModel.setSeed(packet)
            }
            SeedInputScreen(
                navController = navController,
                viewModel = currentInputViewModel
            )
        }
        // デバッグ画面を無効化
        // if (BuildConfig.DEBUG) {
        //     composable("debugDetectOuter") { com.example.seedstockkeeper6.debug.DebugDetectOuterScreen() }
        // }
        
        // プレースホルダー画面
        composable("search") {
            PlaceholderScreen(title = "検索", description = "種子の検索機能")
        }
        composable("calendar") {
            PlaceholderScreen(title = "カレンダー", description = "種子のカレンダー機能")
        }
        composable("settings") {
            PlaceholderScreen(title = "設定", description = "アプリの設定")
        }
    }
}

@Composable
fun FullScreenSaveAnimation() {
    var showSeeds by remember { mutableStateOf(false) }
    
    // 種袋の振りアニメーション
    val animatedRotation by animateFloatAsState(
        targetValue = if (showSeeds) 30f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOutQuart
        ),
        label = "shakeAnimation"
    )
    
    // 種の落下アニメーション
    val animatedSeedOffset by animateFloatAsState(
        targetValue = if (showSeeds) 200f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = EaseInOutQuart
        ),
        label = "seedFallAnimation"
    )
    
    // 種の透明度アニメーション
    val animatedSeedAlpha by animateFloatAsState(
        targetValue = if (showSeeds) 0f else 1f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = EaseInOutQuart
        ),
        label = "seedAlphaAnimation"
    )

    LaunchedEffect(Unit) {
        delay(300)
        showSeeds = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // 種袋（中央）
        Icon(
            painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.seeds),
            contentDescription = "種袋",
            modifier = Modifier
                .graphicsLayer(
                    rotationZ = animatedRotation
                )
                .size(80.dp),
            tint = Color.Unspecified
        )
        
        // バラバラに配置された種（15個）
        if (showSeeds) {
            // 種の位置をランダムに配置
            val seedPositions = listOf(
                -120 to -80, -80 to -120, -40 to -100, 0 to -140, 40 to -100, 80 to -120, 120 to -80,
                -100 to -40, -60 to -60, -20 to -80, 20 to -80, 60 to -60, 100 to -40,
                -80 to 0, -40 to -20, 0 to -40, 40 to -20, 80 to 0,
                -60 to 40, -20 to 20, 20 to 20, 60 to 40,
                -40 to 80, 0 to 60, 40 to 80,
                -20 to 120, 20 to 120,
                0 to 160
            )
            
            seedPositions.forEachIndexed { index, (x, y) ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = (x + animatedSeedOffset * 0.3f).dp,
                            y = (y + animatedSeedOffset).dp
                        )
                        .size(6.dp)
                        .graphicsLayer(alpha = animatedSeedAlpha)
                        .background(
                            color = Color(0xFF8B4513), // 茶色の種
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun signOut(
    context: Context,
    scope: CoroutineScope
) {
    FirebaseAuth.getInstance().signOut()
    scope.launch {
        try {
            CredentialManager.create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // ignore
        }
    }
}

@Composable
fun AccountMenuButton(
    user: FirebaseUser?,
    size: Dp = 32.dp,
    onSignOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val photo = user?.photoUrl
    val emailOrName = user?.displayName ?: user?.email ?: "未ログイン"

    Box {
        IconButton(onClick = { expanded = true }) {
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = "プロフィール",
                    modifier = Modifier.size(size).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "プロフィール",
                    modifier = Modifier.size(size),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(emailOrName) },
                onClick = { /* no-op */ },
                enabled = false,
                leadingIcon = {
                    if (photo != null) {
                        AsyncImage(
                            model = photo,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                    }
                }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                text = { Text("サインアウト") },
                onClick = {
                    expanded = false
                    onSignOut()
                }
            )
        }
    }
}

@Composable
fun AnimatedLogoutIcon(
    onClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (isAnimating) -35f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = EaseInOutQuart
        ),
        label = "pullAnimation"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = if (isAnimating) 15f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseInOutQuart
        ),
        label = "rotationAnimation"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isAnimating) 1.1f else 1f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseInOutQuart
        ),
        label = "scaleAnimation"
    )
    


    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = Color(0xFF654321), // より暗い土の色（収穫時の土）
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                isAnimating = true
                // アニメーション完了後にログアウト処理を実行
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(1200)
                    onClick()
                }
            }
        ) {
            Icon(
                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.harvest),
                contentDescription = "サインアウト（ニンジンを抜く）",
                modifier = Modifier
                    .graphicsLayer(
                        translationY = animatedOffset,
                        rotationZ = animatedRotation,
                        scaleX = animatedScale,
                        scaleY = animatedScale
                    )
                    .size(20.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun AnimatedIcon(
    icon: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String?,
    tint: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = EaseInOutQuart
        ),
        label = "scaleAnimation"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = if (isPressed) 10f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = EaseInOutQuart
        ),
        label = "rotationAnimation"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                rotationZ = animatedRotation
            )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                tint = tint
            )
        } else if (painter != null) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                tint = tint
            )
        }
    }
}


