package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageSelector(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stageOptions = listOf("上旬", "中旬", "下旬")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            modifier = modifier
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            stageOptions.forEach { stage ->
                DropdownMenuItem(
                    text = { Text(stage) },
                    onClick = {
                        onValueChange(stage)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("AI_network.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilySelector(
    label: String = "科名",
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val familyOptions = listOf(
        "アブラナ科",
        "アマランサス科",
        "イネ科",
        "ウリ科",
        "キク科",
        "セリ科",
        "ネギ科",
        "ナス科",
        "バラ科",
        "ヒルガオ科",
        "マメ科",
        "ミカン科",
        "その他"
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            familyOptions.forEach { family ->
                DropdownMenuItem(
                    text = { Text(family) },
                    onClick = {
                        onValueChange(family)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CropConfirmDialog(viewModel: SeedInputViewModel) {
    val ctx = LocalContext.current
    if (!viewModel.showCropConfirmDialog) return
    val preview = viewModel.pendingCropBitmap

    AlertDialog(
        onDismissRequest = { viewModel.cancelCropReplace() },
        title = { Text("画像を差し替えますか？") },
        text = {
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "切り抜きプレビュー",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 360.dp)
                )
            } else {
                Text("プレビューを表示できませんでした")
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.confirmCropReplace(ctx) }) {
                Text("はい")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.cancelCropReplace() }) {
                Text("いいえ")
            }
        }
    )
}
