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
import android.graphics.Rect
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** 既存のバウンディングボックスを“内側の強いエッジ”に合わせて締める */
fun tightenRectToEdges(
    src: Bitmap,
    rect: Rect,
    scanFrac: Float = 0.06f,    // 端から何割分を探索するか（0.04～0.10推奨）
    percentile: Float = 0.85f,  // エッジ強度の上位何割を“壁”とみなすか（0.80～0.92）
    smoothWin: Int = 5          // プロファイルの移動平均窓（奇数, 3～9）
): Rect {
    // ROI を切り出して計算（速さ優先でダウンサンプルは省略）
    val w = rect.width().coerceAtLeast(4)
    val h = rect.height().coerceAtLeast(4)
    val px = IntArray(w * h)
    src.getPixels(px, 0, w, rect.left, rect.top, w, h)

    // グレースケール
    val gray = IntArray(w * h)
    for (i in px.indices) {
        val c = px[i]
        val r = (c ushr 16) and 0xFF
        val g = (c ushr 8) and 0xFF
        val b =  c and 0xFF
        gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    // Sobel 勾配の大きさ
    fun at(x: Int, y: Int) = gray[y * w + x]
    val mag = FloatArray(w * h)
    for (y in 1 until h - 1) for (x in 1 until w - 1) {
        val gx = -at(x - 1, y - 1) + at(x + 1, y - 1) - 2 * at(x - 1, y) + 2 * at(x + 1, y) - at(x - 1, y + 1) + at(x + 1, y + 1)
        val gy =  at(x - 1, y - 1) + 2 * at(x, y - 1) + at(x + 1, y - 1) - at(x - 1, y + 1) - 2 * at(x, y + 1) - at(x + 1, y + 1)
        mag[y * w + x] = sqrt((gx * gx + gy * gy).toFloat())
    }

    // 端から一定距離の帯で1次元プロファイルを作る
    val depth = (min(w, h) * scanFrac).toInt().coerceIn(2, min(w, h) / 3)

    fun smooth(a: FloatArray, win: Int): FloatArray {
        if (win <= 1) return a
        val n = a.size
        val out = FloatArray(n)
        val half = win / 2
        for (i in 0 until n) {
            var s = 0f; var c = 0
            for (k in (i - half)..(i + half)) if (k in 0 until n) { s += a[k]; c++ }
            out[i] = if (c > 0) s / c else a[i]
        }
        return out
    }

    // 左右上下の各プロファイル（帯全域の合計勾配）
    fun leftProfile(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val x = (1 + i).coerceAtMost(w - 2)
            var s = 0f
            for (y in 1 until h - 1) s += mag[y * w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }
    fun rightProfile(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val x = (w - 2 - i).coerceAtLeast(1)
            var s = 0f
            for (y in 1 until h - 1) s += mag[y * w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }
    fun topProfile(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val y = (1 + i).coerceAtMost(h - 2)
            var s = 0f
            for (x in 1 until w - 1) s += mag[y * w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }
    fun bottomProfile(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val y = (h - 2 - i).coerceAtLeast(1)
            var s = 0f
            for (x in 1 until w - 1) s += mag[y * w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }

    fun pickInset(p: FloatArray): Int {
        var mx = 0f
        for (v in p) if (v > mx) mx = v
        if (mx <= 0f) return 0
        val th = mx * percentile
        for (i in 0 until p.size) if (p[i] >= th) return i
        // 念のため最大値位置
        var idx = 0
        for (i in 1 until p.size) if (p[i] > p[idx]) idx = i
        return idx
    }

    val insetL = pickInset(leftProfile())
    val insetR = pickInset(rightProfile())
    val insetT = pickInset(topProfile())
    val insetB = pickInset(bottomProfile())

    val L = rect.left + insetL
    val R = rect.right - insetR
    val T = rect.top + insetT
    val B = rect.bottom - insetB

    return Rect(
        L.coerceIn(0, src.width - 1),
        T.coerceIn(0, src.height - 1),
        R.coerceIn(L + 1, src.width),
        B.coerceIn(T + 1, src.height)
    )
}
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
    var marginRatio by remember { mutableStateOf(0.04f) }
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
            Button(onClick = { pick.launch("image/*") }) { Text("画像") }

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
            ) { Text("検出") }

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
            ) { Text("フォールバック") }

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
                    val top = lastCandidates.first()  // 1位候補

                    // まずは marginRatio 分だけ外側に広げた下ごしらえ矩形
                    val pre = Rect(
                        (top.rect.left  - base.width  * marginRatio).toInt().coerceAtLeast(0),
                        (top.rect.top   - base.height * marginRatio).toInt().coerceAtLeast(0),
                        (top.rect.right + base.width  * marginRatio).toInt().coerceAtMost(base.width),
                        (top.rect.bottom+ base.height * marginRatio).toInt().coerceAtMost(base.height)
                    )

                    // ←ここで“袋のエッジ”に吸着させてタイト化
                    val tight = tightenRectToEdges(
                        src = base,
                        rect = pre,
                        scanFrac = 0.06f,   // 端から6%を探索
                        percentile = 0.86f, // 強いエッジ上位14%を壁とみなす
                        smoothWin = 5
                    )

                    // オーバーレイ＆切り抜き
                    overlayBmp = drawRectOverlay(base, tight)
                    cropBmp = Bitmap.createBitmap(base, tight.left, tight.top, tight.width(), tight.height())

                    lastStats = "tighten: pre=$pre -> tight=$tight (scan=0.06, p=0.86)"
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
