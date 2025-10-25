package com.example.seedstockkeeper6.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

object FirestoreUtils {
    
    /**
     * Firestore接続のリトライ設定
     */
    private const val MAX_RETRY_ATTEMPTS = 3
    private const val RETRY_DELAY_MS = 1000L
    
    /**
     * Firestore接続のタイムアウト設定（秒）
     */
    private const val CONNECTION_TIMEOUT_SECONDS = 10L
    
    /**
     * エラーハンドリング付きのFirestore接続を実行
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        maxRetries: Int = MAX_RETRY_ATTEMPTS
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val result = operation()
                return Result.success(result)
            } catch (e: Exception) {
                lastException = e
                
                // リトライ可能なエラーかチェック
                if (!isRetryableError(e)) {
                    return Result.failure(e)
                }
                
                // 最後の試行でない場合は待機
                if (attempt < maxRetries - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1)) // 指数バックオフ
                }
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error"))
    }
    
    /**
     * リトライ可能なエラーかどうかを判定
     */
    private fun isRetryableError(exception: Exception): Boolean {
        return when (exception) {
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> true
                    else -> false
                }
            }
            else -> {
                // ネットワーク関連のエラー
                exception.message?.contains("network", ignoreCase = true) == true ||
                exception.message?.contains("timeout", ignoreCase = true) == true ||
                exception.message?.contains("connection", ignoreCase = true) == true ||
                exception.message?.contains("SSL", ignoreCase = true) == true
            }
        }
    }
    
    /**
     * エラーメッセージをユーザーフレンドリーに変換
     */
    fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE -> 
                        "ネットワーク接続エラーが発生しました。インターネット接続を確認してください。"
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> 
                        "接続がタイムアウトしました。しばらく待ってから再試行してください。"
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                        "アクセス権限がありません。ログインし直してください。"
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                        "認証が必要です。ログインし直してください。"
                    else -> "データの取得に失敗しました: ${exception.message}"
                }
            }
            else -> {
                when {
                    exception.message?.contains("network", ignoreCase = true) == true -> 
                        "ネットワーク接続エラーが発生しました。"
                    exception.message?.contains("timeout", ignoreCase = true) == true -> 
                        "接続がタイムアウトしました。"
                    exception.message?.contains("SSL", ignoreCase = true) == true -> 
                        "セキュア接続エラーが発生しました。"
                    else -> "予期しないエラーが発生しました: ${exception.message}"
                }
            }
        }
    }
    
    /**
     * Firestore接続の状態をチェック
     */
    suspend fun checkConnection(): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            // 簡単なクエリで接続をテスト
            db.collection("_test").limit(1).get().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * ネットワーク接続を確認してからFirestore操作を実行
     */
    suspend fun <T> executeWithNetworkCheck(
        context: android.content.Context,
        operation: suspend () -> T,
        maxRetries: Int = MAX_RETRY_ATTEMPTS
    ): Result<T> {
        // ネットワーク接続を確認
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return Result.failure(Exception("No network connection"))
        }
        
        // ネットワーク接続を待機
        val networkAvailable = NetworkUtils.waitForNetwork(context, 10000) // 10秒待機
        if (!networkAvailable) {
            return Result.failure(Exception("Network connection timeout"))
        }
        
        // ネットワーク接続が確認できたら操作を実行
        return executeWithRetry(operation, maxRetries)
    }
    
    /**
     * オフライン対応のためのキャッシュ設定
     */
    fun enableOfflinePersistence() {
        try {
            val db = FirebaseFirestore.getInstance()
            db.enableNetwork().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                } else {
                }
            }
        } catch (e: Exception) {
        }
    }
}
