package com.example.seedstockkeeper6.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object NetworkUtils {
    private const val TAG = "NetworkUtils"
    
    /**
     * ネットワーク接続状態をチェック
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    
    /**
     * ネットワーク接続の詳細情報を取得
     */
    fun getNetworkInfo(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No network"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No capabilities"
        
        val transports = mutableListOf<String>()
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            transports.add("WiFi")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            transports.add("Cellular")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            transports.add("Ethernet")
        }
        
        val isMetered = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val isInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        
        return "Transports: ${transports.joinToString(", ")}, Metered: $isMetered, Internet: $isInternet"
    }
    
    /**
     * ネットワーク接続の監視
     */
    fun observeNetworkConnectivity(context: Context): Flow<Boolean> = flow {
        while (true) {
            emit(isNetworkAvailable(context))
            delay(5000) // 5秒間隔でチェック
        }
    }
    
    /**
     * ネットワーク接続を待機
     */
    suspend fun waitForNetwork(context: Context, timeoutMs: Long = 30000): Boolean {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isNetworkAvailable(context)) {
                return true
            }
            delay(1000)
        }
        
        return false
    }
}
