package com.example.seedstockkeeper6.debug

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun DebugDetectOuterScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val detector = remember { SeedOuterDebugDetector() }
    DisposableEffect(Unit) { onDispose { detector.close() } }

    var srcBmp by remember { mutableStateOf<Bitmap?>(null) }
    var overlayBmp by remember { mutableStateOf<Bitmap?>(null) }
    var cropBmp by remember { mutableStateOf<Bitmap?>(null) }
    var lastCandidates by remember { mutableStateOf<List<OuterCandidate>>(emptyList()) }
    var lastStats by remember { mutableStateOf("") }

    // パラメータ
    var minAreaRatio by remember { mutableStateOf(0.20f) }
    var arMin by remember { mutableStateOf(1.0f) }     // 長辺/短辺
    var arMax by remember { mutableStateOf(2.5f) }
    var centerBias by remember { mutableStateOf(0.20f) }
    var marginRatio by remember { mutableStateOf(0.08f) }
    var disableFilters by remember { mutableStateOf(false) }

    val pick = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { s ->
                srcBmp = BitmapFactory.decodeStream(s)
                overlayBmp = null
                cropBmp = null
                lastCandidates = emptyList()
                lastStats = ""
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { pick.launch("image/*") }) { Text("画像を選択") }

            Button(
                enabled = srcBmp != null,
                onClick = {
                    scope.launch {
                        val b = srcBmp ?: return@launch
                        val cands = detector.detectCandidates(
                            bitmap = b,
                            minAreaRatio = if (disableFilters) null else minAreaRatio,
                            arMin = if (disableFilters) null else arMin,
                            arMax = if (disableFilters) null else arMax,
                            centerBias = if (disableFilters) 0f else centerBias
                        )
                        lastCandidates = cands
                        overlayBmp = detector.drawOverlay(b, cands, marginRatio)
                        cropBmp = null
                        lastStats = buildString {
                            append("candidates="); append(cands.size)
                            cands.take(3).forEachIndexed { i, c ->
                                append("\n#${i + 1} rect=${c.rect} area=${c.area} score=${"%.1f".format(c.score)}")
                                val ls = if (c.labels.isEmpty()) "labels=∅"
                                else c.labels.joinToString { (t, conf) -> "$t:${"%.2f".format(conf)}" }
                                append(" lf=${"%.2f".format(c.labelFactor)} $ls")
                            }
                            if (cands.isEmpty()) append("\n(検出0件。フィルタOFFで試す/別画像で再確認)")
                        }
                    }
                }
            ) { Text("検出を実行") }

            Button(
                enabled = overlayBmp != null,
                onClick = { overlayBmp?.let { saveToCache(context, it, "overlay_${System.currentTimeMillis()}.jpg") } }
            ) { Text("オーバーレイ保存") }

            // フォールバック（長方形）
            Button(
                enabled = srcBmp != null,
                onClick = {
                    val b = srcBmp ?: return@Button
                    val res = fallbackEdgeCropWithRect(
                        b,
                        percentile = 0.92f,
                        borderIgnoreRatio = 0.08f,
                        centerBias = 0.20f,
                        marginRatio = marginRatio
                    )
                    if (res != null) {
                        overlayBmp = drawRectOverlay(b, res.rect)
                        cropBmp = res.bitmap
                        lastStats = "fallback rect=${res.rect} crop=${res.bitmap.width}x${res.bitmap.height}"
                    } else {
                        lastStats = "fallback: 内側候補なし"
                    }
                }
            ) { Text("フォールバックでクロップ") }

            Button(
                enabled = cropBmp != null,
                onClick = { cropBmp?.let { saveToCache(context, it, "crop_${System.currentTimeMillis()}.jpg") } }
            ) { Text("クロップ保存") }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("フィルタを無効化（生の出力）"); Spacer(Modifier.width(8.dp))
            Switch(checked = disableFilters, onCheckedChange = { disableFilters = it })
        }

        Spacer(Modifier.height(8.dp))
        ParamSlider("minAreaRatio", minAreaRatio, 0.00f..0.40f, 0.005f, !disableFilters) { minAreaRatio = it }
        ParamSlider("arMin (long/short)", arMin, 1.0f..1.5f, 0.02f, !disableFilters) { arMin = it }
        ParamSlider("arMax (long/short)", arMax, 1.5f..3.5f, 0.05f, !disableFilters) { arMax = it }
        ParamSlider("centerBias", centerBias, 0.0f..0.5f, 0.01f, !disableFilters) { centerBias = it }
        ParamSlider("marginRatio", marginRatio, 0.0f..0.12f, 0.005f, true) { marginRatio = it }

        Spacer(Modifier.height(4.dp))
        if (lastStats.isNotBlank()) {
            Text(lastStats)
            Spacer(Modifier.height(8.dp))
        }

        // 画像表示（オーバーレイ → 原画像）
        (overlayBmp ?: srcBmp)?.let { img ->
            val aspect = img.width.toFloat() / img.height.toFloat()
            Image(
                bitmap = img.asImageBitmap(),
                contentDescription = "overlay",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect),
                contentScale = ContentScale.FillWidth
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // 1位でクロップ（ML Kit側）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = srcBmp != null && lastCandidates.isNotEmpty(),
                onClick = {
                    val base = srcBmp ?: return@Button
                    cropBmp = detector.cropTopCandidate(base, lastCandidates, marginRatio)
                }
            ) { Text("1位でクロップ") }

            Button(
                enabled = cropBmp != null,
                onClick = { cropBmp?.let { saveToCache(context, it, "crop_${System.currentTimeMillis()}.jpg") } }
            ) { Text("クロップ保存") }
        }

        Spacer(Modifier.height(8.dp))
        cropBmp?.let { img ->
            Text("Crop: ${img.width} x ${img.height}")
            val aspect = img.width.toFloat() / img.height.toFloat()
            Image(
                bitmap = img.asImageBitmap(),
                contentDescription = "crop",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

@Composable
private fun ParamSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float,
    enabled: Boolean,
    onChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label); Text("%.3f".format(value))
        }
        Slider(
            value = value,
            onValueChange = { onChange((it / step).toInt() * step) },
            valueRange = valueRange,
            enabled = enabled
        )
    }
}

private fun saveToCache(context: Context, bmp: Bitmap, name: String) {
    val file = File(context.cacheDir, name)
    FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 90, out) }
}
