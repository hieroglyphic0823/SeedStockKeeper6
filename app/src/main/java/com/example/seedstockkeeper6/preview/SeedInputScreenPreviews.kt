package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報表示画面 - 表示モード", heightDp = 1200)
@Composable
fun SeedInputScreenPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("恋むすめ（ニンジン）")
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("画像管理セクション")
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                Text("基本情報セクション")
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                Text("栽培情報セクション")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報新規作成画面", heightDp = 1200)
@Composable
fun SeedInputScreenPreview_NewCreation() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("新規作成")
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("画像管理セクション")
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                Text("基本情報セクション")
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                Text("栽培情報セクション")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報登録画面 - 編集モード", heightDp = 1200)
@Composable
fun SeedInputScreenPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("恋むすめ（せり科）")
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("画像管理セクション")
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                Text("基本情報セクション")
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                Text("栽培情報セクション")
            }
        }
    }
}
