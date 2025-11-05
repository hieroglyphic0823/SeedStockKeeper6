package com.example.seedstockkeeper6.service

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Google Calendar APIを使用してカレンダー一覧を取得するService
 * アクセストークンを直接受け取り、GoogleCredentialで認証を行います
 */
class GoogleCalendarService(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "GoogleCalendarService"
    }
    
    /**
     * アクセストークンを使用してカレンダー一覧を取得
     * @param accessToken GoogleSignInから取得したアクセストークン
     */
    suspend fun getCalendarList(accessToken: String?): Result<List<CalendarListEntry>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "カレンダー一覧取得開始")
            
            if (accessToken.isNullOrBlank()) {
                Log.e(TAG, "アクセストークンが提供されていません")
                return@withContext Result.failure(
                    IllegalArgumentException("アクセストークンが提供されていません")
                )
            }
            
            Log.d(TAG, "GoogleCredentialを作成")
            val credential = GoogleCredential().setAccessToken(accessToken)
            
            Log.d(TAG, "Calendar APIサービスを作成")
            val transport: HttpTransport = NetHttpTransport()
            val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
            val service = Calendar.Builder(
                transport,
                jsonFactory,
                credential
            )
                .setApplicationName("SeedStockKeeper")
                .build()
            
            Log.d(TAG, "カレンダー一覧API呼び出し開始")
            val calendarList = try {
                service.calendarList().list().execute()
            } catch (e: Exception) {
                Log.e(TAG, "execute()エラー: ${e.javaClass.simpleName} - ${e.message}", e)
                Log.e(TAG, "エラー詳細: ${e.stackTraceToString()}")
                
                // 認証エラーの場合の詳細ログ
                when {
                    e.message?.contains("401", ignoreCase = true) == true -> {
                        Log.e(TAG, "認証エラー(401): アクセストークンが無効または期限切れの可能性があります")
                    }
                    e.message?.contains("403", ignoreCase = true) == true -> {
                        Log.e(TAG, "権限エラー(403): カレンダーへのアクセス権限がありません")
                    }
                }
                
                throw e
            }
            
            val calendars = calendarList.items ?: emptyList<CalendarListEntry>()
            
            Log.d(TAG, "カレンダー一覧取得成功: ${calendars.size}件")
            calendars.forEachIndexed { index, calendar ->
                Log.d(TAG, "  カレンダー[$index]: id=${calendar.id}, summary=${calendar.summary}")
            }
            
            Result.success(calendars)
        } catch (e: Exception) {
            Log.e(TAG, "=== カレンダー一覧取得エラー ===")
            Log.e(TAG, "エラータイプ: ${e.javaClass.name}")
            Log.e(TAG, "エラーメッセージ: ${e.message}")
            Log.e(TAG, "スタックトレース:")
            e.printStackTrace()
            Log.e(TAG, "========================")
            
            Result.failure(e)
        }
    }
}
