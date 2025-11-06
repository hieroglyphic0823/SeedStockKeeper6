package com.example.seedstockkeeper6.service

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.service.CalendarColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    
    /**
     * カレンダーAPIサービスを作成（共通処理）
     */
    private fun createCalendarService(accessToken: String): Calendar {
        val credential = GoogleCredential().setAccessToken(accessToken)
        val transport: HttpTransport = NetHttpTransport()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
        return Calendar.Builder(
            transport,
            jsonFactory,
            credential
        )
            .setApplicationName("SeedStockKeeper")
            .build()
    }
    
    /**
     * 終日イベントのEventDateTimeを作成
     */
    private fun createAllDayEventDateTime(dateString: String): EventDateTime {
        // Google Calendarのall-dayイベントは日付のみ（YYYY-MM-DD形式）
        return EventDateTime().setDate(DateTime(dateString))
    }
    
    /**
     * 日付文字列の翌日を取得（YYYY-MM-DD形式）
     */
    private fun getNextDay(dateString: String): String {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        val nextDay = date.plusDays(1)
        return nextDay.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    
    /**
     * イベントの説明文を作成
     */
    private fun createEventDescription(packet: SeedPacket, farmName: String?): String {
        val builder = StringBuilder()
        
        if (farmName?.isNotEmpty() == true) {
            builder.appendLine("農園名: $farmName")
        }
        
        if (packet.variety.isNotEmpty()) {
            builder.appendLine("品種: ${packet.variety}")
        }
        
        if (packet.productName.isNotEmpty()) {
            builder.appendLine("商品名: ${packet.productName}")
        }
        
        if (packet.company.isNotEmpty()) {
            builder.appendLine("メーカー: ${packet.company}")
        }
        
        if (packet.cultivation.notes.isNotEmpty()) {
            builder.appendLine("栽培メモ: ${packet.cultivation.notes}")
        }
        
        if (packet.germinationRate.isNotEmpty()) {
            builder.appendLine("発芽率: ${packet.germinationRate}")
        }
        
        return builder.toString().trim()
    }
    
    /**
     * 種覚書の播種期間・収穫期間・まいた日のイベントを作成
     * @return Triple(sowingEventId, harvestEventId, plantedEventId)
     */
    suspend fun createEventsForSeedPacket(
        accessToken: String,
        calendarId: String,
        packet: SeedPacket,
        farmName: String? = null
    ): Result<Triple<String?, String?, String?>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "種覚書のカレンダーイベント作成開始: ${packet.variety}")
            
            val service = createCalendarService(accessToken)
            val description = createEventDescription(packet, farmName)
            
            var sowingEventId: String? = null
            var harvestEventId: String? = null
            var plantedEventId: String? = null
            
            // 播種期間のイベントを作成
            val calendarEntry = packet.calendar.firstOrNull()
            if (calendarEntry != null) {
                // 播種期間イベント
                if (calendarEntry.sowing_start_date.isNotEmpty() && calendarEntry.sowing_end_date.isNotEmpty()) {
                    try {
                        val sowingEvent = Event().apply {
                            summary = "【🌱播種期間】${packet.productName}"
                            this.description = description
                            start = createAllDayEventDateTime(calendarEntry.sowing_start_date)
                            // all-dayイベントのendは翌日（排他的）
                            end = createAllDayEventDateTime(getNextDay(calendarEntry.sowing_end_date))
                            colorId = CalendarColors.SOWING_COLOR_ID
                        }
                        
                        val createdEvent = service.events().insert(calendarId, sowingEvent).execute()
                        sowingEventId = createdEvent.id
                        Log.d(TAG, "播種期間イベント作成成功: $sowingEventId")
                    } catch (e: Exception) {
                        Log.e(TAG, "播種期間イベント作成失敗: ${e.message}", e)
                    }
                }
                
                // 収穫期間イベント
                if (calendarEntry.harvest_start_date.isNotEmpty() && calendarEntry.harvest_end_date.isNotEmpty()) {
                    try {
                        val harvestEvent = Event().apply {
                            summary = "【🧺収穫予定】${packet.productName}"
                            this.description = description
                            start = createAllDayEventDateTime(calendarEntry.harvest_start_date)
                            // all-dayイベントのendは翌日（排他的）
                            end = createAllDayEventDateTime(getNextDay(calendarEntry.harvest_end_date))
                            colorId = CalendarColors.HARVEST_COLOR_ID
                        }
                        
                        val createdEvent = service.events().insert(calendarId, harvestEvent).execute()
                        harvestEventId = createdEvent.id
                        Log.d(TAG, "収穫期間イベント作成成功: $harvestEventId")
                    } catch (e: Exception) {
                        Log.e(TAG, "収穫期間イベント作成失敗: ${e.message}", e)
                    }
                }
            }
            
            // まいた日のイベントを作成
            if (packet.sowingDate.isNotEmpty()) {
                try {
                    val plantedDescription = description + "\n実際に種をまいた日"
                    val plantedEvent = Event().apply {
                        summary = "【✋まいた】${packet.productName}"
                        this.description = plantedDescription
                        start = createAllDayEventDateTime(packet.sowingDate)
                        // all-dayイベントのendは翌日（排他的）
                        end = createAllDayEventDateTime(getNextDay(packet.sowingDate))
                        colorId = CalendarColors.PLANTED_COLOR_ID
                    }
                    
                    val createdEvent = service.events().insert(calendarId, plantedEvent).execute()
                    plantedEventId = createdEvent.id
                    Log.d(TAG, "まいた日イベント作成成功: $plantedEventId")
                } catch (e: Exception) {
                    Log.e(TAG, "まいた日イベント作成失敗: ${e.message}", e)
                }
            }
            
            Log.d(TAG, "種覚書のカレンダーイベント作成完了: sowing=$sowingEventId, harvest=$harvestEventId, planted=$plantedEventId")
            Result.success(Triple(sowingEventId, harvestEventId, plantedEventId))
        } catch (e: Exception) {
            Log.e(TAG, "種覚書のカレンダーイベント作成エラー: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 種覚書のカレンダーイベントを更新
     * @return Triple(sowingEventId, harvestEventId, plantedEventId) - 更新後のeventId
     */
    suspend fun updateEventsForSeedPacket(
        accessToken: String,
        calendarId: String,
        packet: SeedPacket,
        farmName: String? = null
    ): Result<Triple<String?, String?, String?>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== 種覚書のカレンダーイベント更新開始 ===")
            Log.d(TAG, "品種: ${packet.variety}, 商品名: ${packet.productName}")
            Log.d(TAG, "カレンダーID: $calendarId")
            Log.d(TAG, "既存イベントID - 播種: ${packet.sowingEventId}, 収穫: ${packet.harvestEventId}, まいた日: ${packet.plantedEventId}")
            
            val service = createCalendarService(accessToken)
            val description = createEventDescription(packet, farmName)
            
            var sowingEventId: String? = packet.sowingEventId
            var harvestEventId: String? = packet.harvestEventId
            var plantedEventId: String? = packet.plantedEventId
            
            val calendarEntry = packet.calendar.firstOrNull()
            
            // 播種期間イベントの更新または作成
            if (calendarEntry != null && calendarEntry.sowing_start_date.isNotEmpty() && calendarEntry.sowing_end_date.isNotEmpty()) {
                try {
                    Log.d(TAG, "播種期間イベント処理開始: ${calendarEntry.sowing_start_date} ～ ${calendarEntry.sowing_end_date}")
                    val sowingEvent = Event().apply {
                        summary = "【🌱まきどき】${packet.productName}"
                        this.description = description
                        start = createAllDayEventDateTime(calendarEntry.sowing_start_date)
                        end = createAllDayEventDateTime(getNextDay(calendarEntry.sowing_end_date))
                        colorId = CalendarColors.SOWING_COLOR_ID
                    }
                    Log.d(TAG, "播種期間イベント詳細 - タイトル: ${sowingEvent.summary}, 色ID: ${sowingEvent.colorId}, 開始: ${sowingEvent.start?.date}, 終了: ${sowingEvent.end?.date}")
                    
                    if (packet.sowingEventId.isNotEmpty()) {
                        // 既存イベントを更新
                        Log.d(TAG, "既存の播種期間イベントを更新: ${packet.sowingEventId}")
                        val updatedEvent = service.events().update(calendarId, packet.sowingEventId, sowingEvent).execute()
                        Log.d(TAG, "✅ 播種期間イベント更新成功: ${updatedEvent.id}")
                        Log.d(TAG, "   更新後のURL: ${updatedEvent.htmlLink}")
                    } else {
                        // 新規作成
                        Log.d(TAG, "新規の播種期間イベントを作成")
                        val createdEvent = service.events().insert(calendarId, sowingEvent).execute()
                        sowingEventId = createdEvent.id
                        Log.d(TAG, "✅ 播種期間イベント作成成功: $sowingEventId")
                        Log.d(TAG, "   作成後のURL: ${createdEvent.htmlLink}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 播種期間イベント更新/作成失敗: ${e.message}", e)
                }
            } else {
                // 播種期間が無い場合は既存イベントを削除
                if (packet.sowingEventId.isNotEmpty()) {
                    try {
                        service.events().delete(calendarId, packet.sowingEventId).execute()
                        sowingEventId = null
                        Log.d(TAG, "播種期間イベント削除成功: ${packet.sowingEventId}")
                    } catch (e: Exception) {
                        Log.e(TAG, "播種期間イベント削除失敗: ${e.message}", e)
                    }
                }
            }
            
            // 収穫期間イベントの更新または作成
            if (calendarEntry != null && calendarEntry.harvest_start_date.isNotEmpty() && calendarEntry.harvest_end_date.isNotEmpty()) {
                try {
                    Log.d(TAG, "収穫期間イベント処理開始: ${calendarEntry.harvest_start_date} ～ ${calendarEntry.harvest_end_date}")
                    val harvestEvent = Event().apply {
                        summary = "【🧺収穫予定】${packet.productName}"
                        this.description = description
                        start = createAllDayEventDateTime(calendarEntry.harvest_start_date)
                        end = createAllDayEventDateTime(getNextDay(calendarEntry.harvest_end_date))
                        colorId = CalendarColors.HARVEST_COLOR_ID
                    }
                    Log.d(TAG, "収穫期間イベント詳細 - タイトル: ${harvestEvent.summary}, 色ID: ${harvestEvent.colorId}, 開始: ${harvestEvent.start?.date}, 終了: ${harvestEvent.end?.date}")
                    
                    if (packet.harvestEventId.isNotEmpty()) {
                        // 既存イベントを更新
                        Log.d(TAG, "既存の収穫期間イベントを更新: ${packet.harvestEventId}")
                        val updatedEvent = service.events().update(calendarId, packet.harvestEventId, harvestEvent).execute()
                        Log.d(TAG, "✅ 収穫期間イベント更新成功: ${updatedEvent.id}")
                        Log.d(TAG, "   更新後のURL: ${updatedEvent.htmlLink}")
                    } else {
                        // 新規作成
                        Log.d(TAG, "新規の収穫期間イベントを作成")
                        val createdEvent = service.events().insert(calendarId, harvestEvent).execute()
                        harvestEventId = createdEvent.id
                        Log.d(TAG, "✅ 収穫期間イベント作成成功: $harvestEventId")
                        Log.d(TAG, "   作成後のURL: ${createdEvent.htmlLink}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 収穫期間イベント更新/作成失敗: ${e.message}", e)
                }
            } else {
                // 収穫期間が無い場合は既存イベントを削除
                if (packet.harvestEventId.isNotEmpty()) {
                    try {
                        service.events().delete(calendarId, packet.harvestEventId).execute()
                        harvestEventId = null
                        Log.d(TAG, "収穫期間イベント削除成功: ${packet.harvestEventId}")
                    } catch (e: Exception) {
                        Log.e(TAG, "収穫期間イベント削除失敗: ${e.message}", e)
                    }
                }
            }
            
            // まいた日イベントの更新または作成
            if (packet.sowingDate.isNotEmpty()) {
                try {
                    Log.d(TAG, "まいた日イベント処理開始: ${packet.sowingDate}")
                    val plantedDescription = description + "\n実際に種をまいた日"
                    val plantedEvent = Event().apply {
                        summary = "【✋まいた】${packet.productName}"
                        this.description = plantedDescription
                        start = createAllDayEventDateTime(packet.sowingDate)
                        end = createAllDayEventDateTime(getNextDay(packet.sowingDate))
                        colorId = CalendarColors.PLANTED_COLOR_ID
                    }
                    Log.d(TAG, "まいた日イベント詳細 - タイトル: ${plantedEvent.summary}, 色ID: ${plantedEvent.colorId}, 開始: ${plantedEvent.start?.date}, 終了: ${plantedEvent.end?.date}")
                    
                    if (packet.plantedEventId.isNotEmpty()) {
                        // 既存イベントを更新
                        Log.d(TAG, "既存のまいた日イベントを更新: ${packet.plantedEventId}")
                        val updatedEvent = service.events().update(calendarId, packet.plantedEventId, plantedEvent).execute()
                        Log.d(TAG, "✅ まいた日イベント更新成功: ${updatedEvent.id}")
                        Log.d(TAG, "   更新後のURL: ${updatedEvent.htmlLink}")
                    } else {
                        // 新規作成
                        Log.d(TAG, "新規のまいた日イベントを作成")
                        val createdEvent = service.events().insert(calendarId, plantedEvent).execute()
                        plantedEventId = createdEvent.id
                        Log.d(TAG, "✅ まいた日イベント作成成功: $plantedEventId")
                        Log.d(TAG, "   作成後のURL: ${createdEvent.htmlLink}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ まいた日イベント更新/作成失敗: ${e.message}", e)
                }
            } else {
                // まいた日が無い場合は既存イベントを削除
                if (packet.plantedEventId.isNotEmpty()) {
                    try {
                        service.events().delete(calendarId, packet.plantedEventId).execute()
                        plantedEventId = null
                        Log.d(TAG, "まいた日イベント削除成功: ${packet.plantedEventId}")
                    } catch (e: Exception) {
                        Log.e(TAG, "まいた日イベント削除失敗: ${e.message}", e)
                    }
                }
            }
            
            Log.d(TAG, "=== 種覚書のカレンダーイベント更新完了 ===")
            Log.d(TAG, "最終イベントID - 播種: $sowingEventId, 収穫: $harvestEventId, まいた日: $plantedEventId")
            Result.success(Triple(sowingEventId, harvestEventId, plantedEventId))
        } catch (e: Exception) {
            Log.e(TAG, "種覚書のカレンダーイベント更新エラー: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 種覚書のカレンダーイベントを削除
     */
    suspend fun deleteEventsForSeedPacket(
        accessToken: String,
        calendarId: String,
        packet: SeedPacket
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "種覚書のカレンダーイベント削除開始: ${packet.variety}")
            
            val service = createCalendarService(accessToken)
            
            // 播種期間イベントを削除
            if (packet.sowingEventId.isNotEmpty()) {
                try {
                    service.events().delete(calendarId, packet.sowingEventId).execute()
                    Log.d(TAG, "播種期間イベント削除成功: ${packet.sowingEventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "播種期間イベント削除失敗: ${e.message}", e)
                    // 404エラー（既に削除済み）は無視
                    val is404Error = e.message?.contains("404", ignoreCase = true) == true
                    if (!is404Error) {
                        throw e
                    }
                }
            }
            
            // 収穫期間イベントを削除
            if (packet.harvestEventId.isNotEmpty()) {
                try {
                    service.events().delete(calendarId, packet.harvestEventId).execute()
                    Log.d(TAG, "収穫期間イベント削除成功: ${packet.harvestEventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "収穫期間イベント削除失敗: ${e.message}", e)
                    // 404エラー（既に削除済み）は無視
                    val is404Error = e.message?.contains("404", ignoreCase = true) == true
                    if (!is404Error) {
                        throw e
                    }
                }
            }
            
            // まいた日イベントを削除
            if (packet.plantedEventId.isNotEmpty()) {
                try {
                    service.events().delete(calendarId, packet.plantedEventId).execute()
                    Log.d(TAG, "まいた日イベント削除成功: ${packet.plantedEventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "まいた日イベント削除失敗: ${e.message}", e)
                    // 404エラー（既に削除済み）は無視
                    val is404Error = e.message?.contains("404", ignoreCase = true) == true
                    if (!is404Error) {
                        throw e
                    }
                }
            }
            
            Log.d(TAG, "種覚書のカレンダーイベント削除完了")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "種覚書のカレンダーイベント削除エラー: ${e.message}", e)
            Result.failure(e)
        }
    }
}
