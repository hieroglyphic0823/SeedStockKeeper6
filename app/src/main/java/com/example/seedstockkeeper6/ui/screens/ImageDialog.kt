package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun ImageDialog(viewModel: SeedInputViewModel) {
    if (viewModel.selectedImageBitmap != null) {
        Dialog(onDismissRequest = { viewModel.clearSelectedImage() }) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val context = LocalContext.current

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset += pan
                        }
                    }
            ) {
                Image(
                    bitmap = viewModel.selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .align(Alignment.Center)
                        .widthIn(max = 300.dp)
                        .heightIn(max = 400.dp)
                )

                IconButton(
                    onClick = {
                        viewModel.selectedImageUri?.let { uri ->
                            viewModel.rotateAndReplaceImage(context, uri, 90f)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "回転",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
