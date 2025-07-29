package com.example.seedstockkeeper6.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ImagePickerScreen(
    onImagePicked: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            onImagePicked(it)
        }
    }

    Button(onClick = { launcher.launch("image/*") }) {
        Text("画像を選ぶ")
    }
}

