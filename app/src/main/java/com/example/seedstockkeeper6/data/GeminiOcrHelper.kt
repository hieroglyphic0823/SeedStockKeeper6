package com.example.seedstockkeeper6.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.seedstockkeeper6.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import java.io.ByteArrayOutputStream
import java.io.File

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val tempFile = File.createTempFile("temp_img", ".jpg", context.cacheDir)
            tempFile.outputStream().use { fileOut -> inputStream.copyTo(fileOut) }
            BitmapFactory.decodeFile(tempFile.absolutePath)
        } else {
            Log.e("Image", "InputStream null: $uri")
            null
        }
    } catch (e: Exception) {
        Log.e("Image", "uriToBitmap failed", e)
        null
    }
}

suspend fun runGeminiOcr(context: Context, bitmap: Bitmap): String {
    val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    val imageBytes = bitmapToByteArray(bitmap)

    val inputContent = content {
        image(bitmap)
        text(
            """
        この画像は植物の種です。この種のパッケージ写真を解析して、以下の情報を基に、日本語で厳密なJSON形式で製品情報を返してください。JSON文字列ではなく、直接JSONオブジェクトとして出力してください。キーはダブルクォーテーションで囲み、文字列の値もダブルクォーテーションで囲んでください。空でも構造がある場合（例：soilPrep_per_sqmなど）は、必ず0などの初期値を含む形でJSONを返してください。**「科名」の特定について：**1.  まず、パッケージに「科名」（例：アブラナ科、ナス科、ウリ科など）が明記されていれば、それをJSONの "family" フィールドに正確に記載してください。2.  **もしパッケージに「科名」の明記がない場合でも、製品名、品種名、会社名、またはその他の記載情報から、その植物が属する可能性が最も高い「科名」を推測し、"family" フィールドに記載してください。例えば、「カイワレ大根」であれば「アブラナ科」と推測するようにしてください。**3.  どうしても科名が特定または推測できない場合は、"family" フィールドは空文字列 "" としてください。：

        {
          "productName": "",
          "variety": "",
          "family": "",
          "productNumber": "",
          "company": "",
          "originCountry": "",
          "expirationDate": "",
          "contents": "",
          "germinationRate": "",
          "seedTreatment": "",
          "features": [
             ""
          ],
          "cultivation": {
            "spacing_cm_row_min": 0,
            "spacing_cm_row_max": 0,
            "spacing_cm_plant_min": 0,
            "spacing_cm_plant_max": 0,
            "germinationTemp_c": "",
            "growingTemp_c": "",
            "soilPrep_per_sqm": {
              "compost_kg": 0,
              "dolomite_lime_g": 0,
              "chemical_fertilizer_g": 0
            },
            "notes": "",
            "harvesting": ""
          },
          "calendar": [
            {
              "region": "",
              "sowing": "",
              "harvest": ""
            }
          ]
        }

        説明や補足は不要。レスポンスはいかなる追加の引用符やエスケープ文字も含まない、純粋でパース可能なJSONオブジェクトでなければなりません。出力は、直接 JSON.parse() (または同等のメソッド) でパースできる形式である必要があります。先頭にjsonとつけず「」で囲まず、JSONオブジェクトそのものを開始と終了の波括弧 {} で返してください。
            """.trimIndent()
        )
    }

    val response = model.generateContent(inputContent)
    return response.text ?: "結果がありません"
}

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    return outputStream.toByteArray()
}