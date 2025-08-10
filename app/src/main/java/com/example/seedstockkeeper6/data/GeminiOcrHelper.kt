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

    val inputContent = content {
        image(bitmap)
        text(
            """
        この画像は植物の種です。この種のパッケージ写真を解析して、以下の情報を基に、日本語で厳密なJSON形式で製品情報を返してください。JSON文字列ではなく、直接JSONオブジェクトとして出力してください。すべてのキーはダブルクォーテーションで囲み、文字列の値もダブルクォーテーションで囲んでください。空でも構造がある場合（例：soilPrep_per_sqmなど）は、必ず0などの初期値を含む形でJSONを返してください。

「科名」の特定について：
1. パッケージに「科名」（例：アブラナ科、ナス科、ウリ科など）が明記されていれば、それをJSONの "family" フィールドに正確に記載してください。
2. パッケージに「科名」の明記がない場合でも、製品名、品種名、会社名、その他の記載情報から、その植物が属する可能性が最も高い「科名」を推測し、"family" フィールドに記載してください。例：カイワレ大根 → アブラナ科
3. どうしても科名が特定・推測できない場合は、"family" フィールドは空文字列 "" としてください。

expirationDate フィールドは、「yyyy年MM月」（例：2025年07月）の形式で記入してください。

companionPlants 配列について：
- パッケージに記載されている場合は、それをもとに植物名と効果を記載してください。
- パッケージに記載がない場合でも、該当植物に対して一般的または推奨される代表的なコンパニオンプランツとその効果を、必ず1つ以上含めて記載してください。
- 各 companionPlants オブジェクトには、"plant" に1つの植物名、"effect" に1つの効果をそれぞれ記載してください。複数の植物や複数の効果を1つのオブジェクトにまとめないでください。
- 1つの植物に複数の効果がある場合は、それぞれ別の companionPlants オブジェクトを作成し、同じ植物名で効果ごとに分けて記載してください。
- 効果のフィールド（"effect"）は、以下の分類のいずれかを**厳密に**使用してください。該当が明確でない場合は「その他」を使用してください：

  - 害虫予防
  - 病気予防
  - 生育促進
  - 空間活用
  - 風味向上
  - 土壌改善
  - 受粉促進
  - 雑草抑制
  - 景観美化
  - 水分保持の助け
  - 土壌pHの調整
  - 作業性向上
  - 収量の安定化
  - その他

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
          ],
          "companionPlants": [
            {
              "plant": "",
              "effect": ""
            }
          ]
        }
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