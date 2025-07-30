package com.example.seedstockkeeper6.ui.screens
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ImagePickerScreen( // この関数が定義されている必要がある
    onImagePicked: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onImagePicked(it)
        }
    }

    Button(onClick = { launcher.launch("image/*") }) {
        Text("画像を選ぶ")
    }
}
