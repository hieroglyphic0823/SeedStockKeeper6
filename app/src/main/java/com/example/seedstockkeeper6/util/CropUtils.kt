package com.example.seedstockkeeper6.util

import android.graphics.*
import kotlin.math.min
import kotlin.math.sqrt

/** 紫の太い点線で矩形オーバーレイを描いたビットマップを返す */
fun drawRectOverlay(bitmap: Bitmap, rect: Rect): Bitmap {
    val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val c = Canvas(out)
    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        color = Color.parseColor("#9C27B0")
        pathEffect = DashPathEffect(floatArrayOf(28f, 16f), 0f)
    }
    val rf = RectF(rect).apply { inset(7f, 7f) }
    c.drawRect(rf, p)
    return out
}

/** 端から勾配プロファイルを見て“袋のエッジ”に吸着させる（軸平行） */
fun tightenRectToEdges(
    src: Bitmap,
    rect: Rect,
    scanFrac: Float = 0.06f,
    percentile: Float = 0.88f,
    smoothWin: Int = 5,
    maxInsetRatioX: Float = 0.15f,
    maxInsetRatioY: Float = 0.15f
): Rect {
    val w = rect.width().coerceAtLeast(4)
    val h = rect.height().coerceAtLeast(4)
    val px = IntArray(w * h)
    src.getPixels(px, 0, w, rect.left, rect.top, w, h)

    val gray = IntArray(w * h)
    for (i in px.indices) {
        val c = px[i]; val r=(c ushr 16) and 0xFF; val g=(c ushr 8) and 0xFF; val b=c and 0xFF
        gray[i] = (0.299*r + 0.587*g + 0.114*b).toInt()
    }
    fun at(x:Int,y:Int)=gray[y*w+x]
    val mag = FloatArray(w*h)
    for (y in 1 until h-1) for (x in 1 until w-1) {
        val gx = -at(x-1,y-1)+at(x+1,y-1) -2*at(x-1,y)+2*at(x+1,y) -at(x-1,y+1)+at(x+1,y+1)
        val gy =  at(x-1,y-1)+2*at(x,y-1)+at(x+1,y-1) -at(x-1,y+1)-2*at(x,y+1)-at(x+1,y+1)
        mag[y*w+x] = sqrt((gx*gx + gy*gy).toFloat())
    }

    val depth = (min(w, h) * scanFrac).toInt().coerceIn(2, min(w, h) / 3)

    fun smooth(a: FloatArray, win: Int): FloatArray {
        if (win <= 1) return a
        val out = FloatArray(a.size); val half = win/2
        for (i in a.indices) {
            var s=0f; var c=0
            for (k in (i-half)..(i+half)) if (k in a.indices) { s+=a[k]; c++ }
            out[i] = if (c>0) s/c else a[i]
        }
        return out
    }
    fun profLeft(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val x = (1 + i).coerceAtMost(w - 2); var s=0f
            for (y in 1 until h - 1) s += mag[y*w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }
    fun profRight(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val x = (w - 2 - i).coerceAtLeast(1); var s=0f
            for (y in 1 until h - 1) s += mag[y*w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }
    fun profTop(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val y = (1 + i).coerceAtMost(h - 2); var s=0f
            for (x in 1 until w - 1) s += mag[y*w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }
    fun profBottom(): FloatArray {
        val p = FloatArray(depth)
        for (i in 0 until depth) {
            val y = (h - 2 - i).coerceAtLeast(1); var s=0f
            for (x in 1 until w - 1) s += mag[y*w + x]
            p[i] = s
        }
        return smooth(p, smoothWin)
    }
    fun pickInset(p: FloatArray): Int {
        var mx=0f; for (v in p) if (v>mx) mx=v
        if (mx<=0f) return 0
        val th = mx * percentile
        for (i in p.indices) if (p[i] >= th) return i
        var idx=0; for (i in 1 until p.size) if (p[i] > p[idx]) idx=i
        return idx
    }

    val insetL = pickInset(profLeft())
    val insetR = pickInset(profRight())
    val insetT = pickInset(profTop())
    val insetB = pickInset(profBottom())

    val maxInsetX = (w * maxInsetRatioX).toInt()
    val maxInsetY = (h * maxInsetRatioY).toInt()

    val L = (rect.left + insetL.coerceAtMost(maxInsetX)).coerceIn(0, src.width - 1)
    val R = (rect.right - insetR.coerceAtMost(maxInsetX)).coerceIn(L + 1, src.width)
    val T = (rect.top + insetT.coerceAtMost(maxInsetY)).coerceIn(0, src.height - 1)
    val B = (rect.bottom - insetB.coerceAtMost(maxInsetY)).coerceIn(T + 1, src.height)
    return Rect(L, T, R, B)
}
