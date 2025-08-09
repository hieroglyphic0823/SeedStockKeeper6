package com.example.seedstockkeeper6.debug

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** 検出候補（デバッグ用） */
data class OuterCandidate(
    val rect: Rect,
    val area: Int,
    val centerDistNorm: Double,      // 0(中央) .. 1(端)
    val score: Double,               // 面積×中心×ラベル係数
    val labels: List<Pair<String, Float>>,
    val labelFactor: Double
)

/** フォールバック結果（長方形版） */
data class FallbackResult(val rect: Rect, val bitmap: Bitmap)

class SeedOuterDebugDetector {

    companion object { private const val TAG = "DetectDebug" }

    /** ML Kit ODT（分類ON） */
    private val objectDetector: ObjectDetector by lazy {
        val opt = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            // .enableMultipleObjects() // ←必要ならON
            .enableClassification()     // ラベルを使う
            .build()
        ObjectDetection.getClient(opt)
    }

    /** ラベル補正（1.0=無補正、0.7..1.6にクランプ） */
    private fun labelFactor(labels: List<DetectedObject.Label>): Double {
        if (labels.isEmpty()) return 1.0
        val weights = mapOf(
            "food" to 1.25,
            "home goods" to 1.15,
            "fashion goods" to 0.95,
            "place" to 0.85
        )
        var f = 1.0
        labels.forEach { l ->
            val key = l.text.lowercase(Locale.US)
            val w = weights[key] ?: 1.0
            f += (w - 1.0) * l.confidence  // confidence ∈ [0,1]
        }
        return f.coerceIn(0.7, 1.6)
    }

    /**
     * ML Kitで候補を検出してスコア順に返す
     * @param minAreaRatio 画像面積に対する最小割合(nullで無効)
     * @param arMin/Max    長辺/短辺のアスペクト比フィルタ(nullで無効)
     * @param centerBias   中央優遇 0..1
     */
    suspend fun detectCandidates(
        bitmap: Bitmap,
        minAreaRatio: Float? = null,
        arMin: Float? = null,
        arMax: Float? = null,
        centerBias: Float = 0.0f
    ): List<OuterCandidate> {
        val w = bitmap.width
        val h = bitmap.height
        val imgArea = w.toFloat() * h

        val results = runCatching {
            objectDetector.process(InputImage.fromBitmap(bitmap, 0)).await()
        }.onFailure { Log.e(TAG, "MLKit failed", it) }.getOrNull() ?: return emptyList()

        val cands = results.mapNotNull { obj ->
            val bb = obj.boundingBox
            val r = Rect(
                bb.left.coerceIn(0, w - 1),
                bb.top.coerceIn(0, h - 1),
                bb.right.coerceIn(1, w),
                bb.bottom.coerceIn(1, h)
            )
            val area = r.width() * r.height()
            if (minAreaRatio != null && area / imgArea < minAreaRatio) return@mapNotNull null

            // 向き非依存AR（長辺/短辺）
            val rw = r.width().toFloat()
            val rh = r.height().toFloat()
            val arSym = max(rw, rh) / min(rw, rh)
            if (arMin != null && arSym < arMin) return@mapNotNull null
            if (arMax != null && arSym > arMax) return@mapNotNull null

            // 中央バイアス
            val cx = (r.left + r.right) / 2f
            val cy = (r.top + r.bottom) / 2f
            val dx = cx / w - 0.5f
            val dy = cy / h - 0.5f
            val centerPenalty = (1.0 - (hypot(dx.toDouble(), dy.toDouble()) * 2.0 * centerBias))
                .coerceIn(0.0, 1.0)
            val base = area * centerPenalty

            val lf = labelFactor(obj.labels)
            val labs = obj.labels.map { it.text to it.confidence }
            val finalScore = base * lf

            OuterCandidate(
                rect = r,
                area = area,
                centerDistNorm = 1.0 - centerPenalty,
                score = finalScore,
                labels = labs,
                labelFactor = lf
            )
        }
        return cands.sortedByDescending { it.score }
    }

    /** 候補のオーバーレイ描画（赤=1位, 黄=2位, 青=その他） */
    fun drawOverlay(
        bitmap: Bitmap,
        candidates: List<OuterCandidate>,
        marginRatio: Float = 0f
    ): Bitmap {
        val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            textSize = 32f
            color = Color.WHITE
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x33000000
        }

        candidates.forEachIndexed { i, cand ->
            val rf = inflateRectF(cand.rect, bitmap.width, bitmap.height, marginRatio)
            border.color = when (i) { 0 -> Color.RED; 1 -> Color.YELLOW; else -> Color.CYAN }
            canvas.drawRect(rf, border)
            canvas.drawRect(rf.left, rf.top - 40f, rf.left + 1100f, rf.top, fill)

            val labelStr =
                if (cand.labels.isEmpty()) "labels=∅"
                else cand.labels.take(3).joinToString { (t, c) -> "$t:${"%.2f".format(c)}" }
            val info = "#${i + 1} s=${"%.0f".format(cand.score)} lf=${"%.2f".format(cand.labelFactor)} $labelStr"
            canvas.drawText(info, rf.left + 8f, rf.top - 10f, text)
        }
        return out
    }

    /** 1位でクロップ */
    fun cropTopCandidate(
        bitmap: Bitmap,
        candidates: List<OuterCandidate>,
        marginRatio: Float = 0f
    ): Bitmap? {
        if (candidates.isEmpty()) return null
        val rf = inflateRectF(candidates.first().rect, bitmap.width, bitmap.height, marginRatio)
        val l = rf.left.toInt().coerceIn(0, bitmap.width - 1)
        val t = rf.top.toInt().coerceIn(0, bitmap.height - 1)
        val r = rf.right.toInt().coerceIn(l + 1, bitmap.width)
        val b = rf.bottom.toInt().coerceIn(t + 1, bitmap.height)
        val outW = r - l
        val outH = b - t
        if (outW <= 0 || outH <= 0) return null
        return runCatching { Bitmap.createBitmap(bitmap, l, t, outW, outH) }.getOrNull()
    }

    fun close() = runCatching { objectDetector.close() }.onFailure { }.let { }

    private fun inflateRectF(src: Rect, w: Int, h: Int, marginRatio: Float): RectF {
        val rf = RectF(src)
        if (marginRatio <= 0f) return rf
        val mx = w * marginRatio
        val my = h * marginRatio
        rf.set(
            (rf.left - mx).coerceAtLeast(0f),
            (rf.top - my).coerceAtLeast(0f),
            (rf.right + mx).coerceAtMost(w.toFloat()),
            (rf.bottom + my).coerceAtMost(h.toFloat())
        )
        return rf
    }
}

/** フォールバック：回転なしの長方形で袋を切り抜く（OpenCVなし） */
fun fallbackEdgeCropWithRect(
    bitmap: Bitmap,
    percentile: Float = 0.92f,
    borderIgnoreRatio: Float = 0.08f,
    centerBias: Float = 0.15f,
    marginRatio: Float = 0.06f,
    safeBorderRatio: Float = 0.02f
): FallbackResult? {
    // 1) 縮小
    val maxSide = 900
    val scale = (max(bitmap.width, bitmap.height).toFloat() / maxSide).coerceAtLeast(1f)
    val work = if (scale > 1f)
        Bitmap.createScaledBitmap(bitmap, (bitmap.width / scale).toInt(), (bitmap.height / scale).toInt(), true)
    else bitmap
    val w = work.width; val h = work.height

    // 2) グレースケール
    val src = IntArray(w * h); work.getPixels(src, 0, w, 0, 0, w, h)
    val gray = IntArray(w * h)
    for (i in 0 until w * h) {
        val c = src[i]
        val r = (c ushr 16) and 0xFF
        val g = (c ushr 8) and 0xFF
        val b = c and 0xFF
        gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    // 3) Sobel + 中央バイアス
    fun at(x: Int, y: Int) = gray[y * w + x]
    val mag = FloatArray(w * h)
    val cx = (w - 1) / 2f; val cy = (h - 1) / 2f
    for (y in 1 until h - 1) for (x in 1 until w - 1) {
        val gx = -at(x - 1, y - 1) + at(x + 1, y - 1) - 2 * at(x - 1, y) + 2 * at(x + 1, y) - at(x - 1, y + 1) + at(x + 1, y + 1)
        val gy =  at(x - 1, y - 1) + 2 * at(x, y - 1) + at(x + 1, y - 1) - at(x - 1, y + 1) - 2 * at(x, y + 1) - at(x + 1, y + 1)
        var m = sqrt((gx * gx + gy * gy).toFloat())
        if (centerBias > 0f) {
            val dx = (x - cx) / cx; val dy = (y - cy) / cy
            val pen = (1f - (hypot(dx.toDouble(), dy.toDouble()) * centerBias * 1.8)).toFloat()
            m *= pen.coerceIn(0f, 1f)
        }
        mag[y * w + x] = m
    }

    // 4) 外周を除いたROIでしきい値
    val bx = (w * borderIgnoreRatio).toInt(); val by = (h * borderIgnoreRatio).toInt()
    if (bx >= w / 2 || by >= h / 2) return null
    val vals = ArrayList<Float>((w - 2 * bx) * (h - 2 * by))
    for (y in by until h - by) for (x in bx until w - bx) vals.add(mag[y * w + x])
    if (vals.isEmpty()) return null
    vals.sort()
    val th = vals[(vals.size * percentile).toInt().coerceIn(0, vals.lastIndex)]

    // 5) ROIの外接長方形
    var minX = w; var minY = h; var maxX = 0; var maxY = 0; var any = false
    for (y in by until h - by) for (x in bx until w - bx) {
        if (mag[y * w + x] >= th) {
            any = true
            if (x < minX) minX = x
            if (y < minY) minY = y
            if (x > maxX) maxX = x
            if (y > maxY) maxY = y
        }
    }
    if (!any) return null

    // 6) 元サイズへ戻し、安全余白を確保してから margin を付与
    val inv = scale
    var L = (minX * inv).toInt()
    var T = (minY * inv).toInt()
    var R = (maxX * inv).toInt()
    var B = (maxY * inv).toInt()

    val safeL = (bitmap.width * safeBorderRatio).toInt()
    val safeT = (bitmap.height * safeBorderRatio).toInt()
    val safeR = bitmap.width - 1 - safeL
    val safeB = bitmap.height - 1 - safeT
    L = L.coerceAtLeast(safeL); T = T.coerceAtLeast(safeT)
    R = R.coerceAtMost(safeR);  B = B.coerceAtMost(safeB)

    val mxReq = (bitmap.width * marginRatio).toInt()
    val myReq = (bitmap.height * marginRatio).toInt()
    val mx = min(mxReq, min(L - safeL, safeR - R)).coerceAtLeast(0)
    val my = min(myReq, min(T - safeT, safeB - B)).coerceAtLeast(0)
    L -= mx; T -= my; R += mx; B += my

    val outW = (R - L + 1).coerceAtLeast(1)
    val outH = (B - T + 1).coerceAtLeast(1)
    val crop = runCatching { Bitmap.createBitmap(bitmap, L, T, outW, outH) }.getOrNull() ?: return null
    return FallbackResult(Rect(L, T, R, B), crop)
}

/** 紫の太い点線（Rect版オーバーレイ） */
fun drawRectOverlay(bitmap: Bitmap, rect: Rect): Bitmap {
    val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val c = Canvas(out)
    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        color = Color.parseColor("#9C27B0")
        pathEffect = DashPathEffect(floatArrayOf(28f, 16f), 0f)
    }
    val rf = RectF(rect).apply { inset(7f, 7f) } // 太線が欠けないよう半分内側へ
    c.drawRect(rf, p)
    return out
}
