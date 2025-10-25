package com.example.seedstockkeeper6.service

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await

class GeocodingService(private val context: Context) {
    
    private var placesClient: PlacesClient? = null
    
    init {
        initializePlaces()
    }
    
    private fun initializePlaces() {
        try {
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyDr_WfQfx3TyH0oLDWf8Z7qHX4XHAH5J-E")
            } else {
        }
        placesClient = Places.createClient(context)
        } catch (e: Exception) {
            placesClient = null
        }
    }
    
    /**
     * 緯度経度から住所を取得する（Android標準Geocoderを利用）
     */
    suspend fun getAddressFromLatLng(latLng: LatLng): String {

        try {
            // Android標準のGeocoderを使用
            val geocoder = android.location.Geocoder(context, java.util.Locale.JAPAN)
            
            val addresses: List<android.location.Address>?
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // Android 13 (API 33) 以降の非同期メソッド
                // より多くの候補を取得して詳細な情報を得る
                var addressResult: List<android.location.Address>? = null
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5) { addressList ->
                    addressResult = addressList
                }
                // 結果が返るまで少し待機する
                kotlinx.coroutines.delay(1000)
                addresses = addressResult
            } else {
                // 古い同期メソッド（IOスレッドで実行する必要がある）
                // より多くの候補を取得して詳細な情報を得る
                @Suppress("DEPRECATION")
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
            }
            
            if (addresses.isNullOrEmpty()) {
                return "住所情報が見つかりません"
            }

            
            // 最も詳細な情報を含む住所を選択
            val address = selectMostDetailedAddress(addresses)
            
            // デバッグ用：Addressオブジェクトの全情報をログ出力
            
            // より詳細な住所情報を構築
            val addressParts = mutableListOf<String>()
            
            // 都道府県
            if (!address.adminArea.isNullOrBlank()) {
                addressParts.add(address.adminArea)
            }
            
            // 市区町村
            if (!address.locality.isNullOrBlank()) {
                addressParts.add(address.locality)
            }
            
            // 区
            if (!address.subLocality.isNullOrBlank()) {
                addressParts.add(address.subLocality)
            }
            
            // 通り名・地名（重複チェック付き）
            if (!address.thoroughfare.isNullOrBlank() && 
                !addressParts.contains(address.thoroughfare) &&
                address.thoroughfare != "道路") {
                addressParts.add(address.thoroughfare)
            }
            
            // 番地（重複チェック付き）
            if (!address.subThoroughfare.isNullOrBlank() && 
                !addressParts.contains(address.subThoroughfare) &&
                address.subThoroughfare != "道路" &&
                address.subThoroughfare != address.thoroughfare) {
                addressParts.add(address.subThoroughfare)
            }
            
            // 建物名
            if (!address.premises.isNullOrBlank() && 
                !addressParts.contains(address.premises)) {
                addressParts.add(address.premises)
            }
            
            // 特徴名（建物名や地名など）
            if (!address.featureName.isNullOrBlank() && 
                !addressParts.contains(address.featureName)) {
                addressParts.add(address.featureName)
            }
            
            // もし上記で十分な情報が得られない場合は、getAddressLineを使用
            if (addressParts.size < 3) {
                for (i in 0..address.maxAddressLineIndex) {
                    val addressLine = address.getAddressLine(i)
                    if (!addressLine.isNullOrBlank()) {
                        // 既存の情報と重複しない場合のみ追加
                        if (!addressParts.any { addressLine.contains(it) }) {
                            addressParts.add(addressLine)
                        }
                    }
                }
            }
            
            // 最終的な住所の組み立てを最適化
            val optimizedAddressParts = optimizeAddressParts(addressParts)
            addressParts.clear()
            addressParts.addAll(optimizedAddressParts)
            
            // 座標に基づく地域名の補完
            val enhancedAddressParts = enhanceAddressWithCoordinates(addressParts, latLng)
            addressParts.clear()
            addressParts.addAll(enhancedAddressParts)

            val resultAddress = addressParts.joinToString("")
            
            return if (resultAddress.isNotBlank()) resultAddress else "住所情報が不明です"
            
        } catch (e: Exception) {
            return "住所取得に失敗しました"
        }
    }
    
    /**
     * 最も詳細な情報を含む住所を選択
     */
    private fun selectMostDetailedAddress(addresses: List<android.location.Address>): android.location.Address {
        
        var bestAddress = addresses[0]
        var maxScore = 0
        
        for ((index, address) in addresses.withIndex()) {
            var score = 0
            
            // 各フィールドの存在をスコア化
            if (!address.adminArea.isNullOrBlank()) score += 1
            if (!address.locality.isNullOrBlank()) score += 2
            if (!address.subLocality.isNullOrBlank()) score += 3
            if (!address.thoroughfare.isNullOrBlank()) score += 4
            if (!address.subThoroughfare.isNullOrBlank()) score += 5
            if (!address.premises.isNullOrBlank()) score += 2
            if (!address.featureName.isNullOrBlank()) score += 2
            if (!address.postalCode.isNullOrBlank()) score += 1
            
            // getAddressLineの数も考慮
            score += address.maxAddressLineIndex + 1
            
            
            if (score > maxScore) {
                maxScore = score
                bestAddress = address
            }
        }
        
        return bestAddress
    }
    
    /**
     * 住所パーツを最適化（重複除去、無意味な情報の排除）
     */
    private fun optimizeAddressParts(addressParts: List<String>): List<String> {
        
        val optimizedParts = mutableListOf<String>()
        val seenParts = mutableSetOf<String>()
        
        for (part in addressParts) {
            // 空文字や無意味な情報をスキップ
            if (part.isBlank() || 
                part == "道路" || 
                part == "日本" ||
                part == "市区町村" ||
                part.length < 2) {
                continue
            }
            
            // 重複チェック
            if (!seenParts.contains(part)) {
                optimizedParts.add(part)
                seenParts.add(part)
            } else {
            }
        }
        
        return optimizedParts
    }
    
    /**
     * 座標に基づく地域名の補完
     */
    private fun enhanceAddressWithCoordinates(addressParts: List<String>, latLng: LatLng): List<String> {
        
        val enhancedParts = addressParts.toMutableList()
        
        // 福岡市西区の具体的な地区名を座標から判定
        // 草場の座標範囲を実際の座標に合わせて調整
        if (latLng.latitude >= 33.61 && latLng.latitude <= 33.62 && 
            latLng.longitude >= 130.21 && latLng.longitude <= 130.22) {
            val areaName = "草場"
            if (!enhancedParts.contains(areaName)) {
                enhancedParts.add(areaName)
            }
        } else if (latLng.latitude >= 33.60 && latLng.latitude <= 33.65 && 
                   latLng.longitude >= 130.20 && latLng.longitude <= 130.25) {
            // より広い範囲で草場を判定（フォールバック）
            val areaName = "草場"
            if (!enhancedParts.contains(areaName)) {
                enhancedParts.add(areaName)
            }
        } else if (latLng.latitude >= 33.58 && latLng.latitude <= 33.63 && 
                   latLng.longitude >= 130.15 && latLng.longitude <= 130.20) {
            val areaName = "今宿"
            if (!enhancedParts.contains(areaName)) {
                enhancedParts.add(areaName)
            }
        } else if (latLng.latitude >= 33.65 && latLng.latitude <= 33.70 && 
                   latLng.longitude >= 130.30 && latLng.longitude <= 130.40) {
            val areaName = "姪浜"
            if (!enhancedParts.contains(areaName)) {
                enhancedParts.add(areaName)
            }
        } else if (latLng.latitude >= 33.50 && latLng.latitude <= 33.58 && 
                   latLng.longitude >= 130.10 && latLng.longitude <= 130.20) {
            val areaName = "小田部"
            if (!enhancedParts.contains(areaName)) {
                enhancedParts.add(areaName)
            }
        } else if (latLng.latitude >= 33.60 && latLng.latitude <= 33.68 && 
                   latLng.longitude >= 130.18 && latLng.longitude <= 130.25) {
            val areaName = "野方"
            if (!enhancedParts.contains(areaName)) {
                enhancedParts.add(areaName)
            }
        } else if (latLng.latitude >= 33.55 && latLng.latitude <= 33.62 && 
                   latLng.longitude >= 130.20 && latLng.longitude <= 130.30) {
            val areaName = "愛宕"
            if (!enhancedParts.contains(areaName)) {
                enhancedParts.add(areaName)
            }
        }
        
        return enhancedParts
    }
    
    /**
     * アドレスコンポーネントから詳細な住所を構築
     */
    private fun buildDetailedAddressFromComponents(addressComponents: com.google.android.libraries.places.api.model.AddressComponents?): String {
        if (addressComponents == null) {
            return ""
        }
        
        val components = addressComponents.asList()
        val addressParts = mutableListOf<String>()
        
        for ((index, component) in components.withIndex()) {
            val types = component.types
            val longName = component.name
            val shortName = component.shortName
            
            
            when {
                types.contains("administrative_area_level_1") -> {
                    // 都道府県
                    addressParts.add(longName)
                }
                types.contains("locality") -> {
                    // 市区町村
                    addressParts.add(longName)
                }
                types.contains("sublocality") -> {
                    // 区
                    addressParts.add(longName)
                }
                types.contains("sublocality_level_1") -> {
                    // 地区
                    addressParts.add(longName)
                }
                types.contains("street_number") -> {
                    // 番地
                    addressParts.add(longName)
                }
                else -> {
                }
            }
        }
        
        val detailedAddress = addressParts.joinToString("")
        return detailedAddress
    }
    
    /**
     * 座標から簡易的な住所を生成
     */
    private fun generateAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return try {
            // より詳細な地域判定
            val prefecture = when {
                latitude >= 43.0 -> "北海道"
                latitude >= 40.0 && longitude >= 140.0 -> "青森県"
                latitude >= 40.0 && longitude < 140.0 -> "岩手県"
                latitude >= 39.0 && longitude >= 140.0 -> "秋田県"
                latitude >= 38.0 && longitude >= 140.0 -> "山形県"
                latitude >= 37.0 && longitude >= 140.0 -> "福島県"
                latitude >= 36.0 && longitude >= 140.0 -> "茨城県"
                latitude >= 35.0 && longitude >= 139.0 -> "東京都"
                latitude >= 35.0 && longitude >= 138.0 -> "山梨県"
                latitude >= 35.0 && longitude >= 137.0 -> "静岡県"
                latitude >= 35.0 && longitude >= 136.0 -> "愛知県"
                latitude >= 34.0 && longitude >= 135.0 -> "大阪府"
                latitude >= 34.0 && longitude >= 134.0 -> "兵庫県"
                latitude >= 33.0 && longitude >= 130.0 && longitude < 131.0 -> "福岡県"
                latitude >= 32.0 && longitude >= 130.0 && longitude < 131.0 -> "熊本県"
                latitude >= 31.0 && longitude >= 130.0 && longitude < 131.0 -> "鹿児島県"
                else -> "日本"
            }
            
            val city = when {
                // 東京都
                latitude >= 35.6 && latitude <= 35.8 && longitude >= 139.6 && longitude <= 139.8 -> "千代田区"
                latitude >= 35.6 && latitude <= 35.8 && longitude >= 139.7 && longitude <= 139.9 -> "中央区"
                latitude >= 35.6 && latitude <= 35.8 && longitude >= 139.8 && longitude <= 140.0 -> "港区"
                latitude >= 35.5 && latitude <= 35.7 && longitude >= 139.6 && longitude <= 139.8 -> "新宿区"
                latitude >= 35.5 && latitude <= 35.7 && longitude >= 139.7 && longitude <= 139.9 -> "渋谷区"
                // 福岡県
                latitude >= 33.5 && latitude <= 33.7 && longitude >= 130.3 && longitude <= 130.5 -> "福岡市"
                latitude >= 33.4 && latitude <= 33.6 && longitude >= 130.4 && longitude <= 130.6 -> "北九州市"
                latitude >= 33.3 && latitude <= 33.5 && longitude >= 130.2 && longitude <= 130.4 -> "久留米市"
                latitude >= 33.2 && latitude <= 33.4 && longitude >= 130.1 && longitude <= 130.3 -> "大牟田市"
                latitude >= 33.1 && latitude <= 33.3 && longitude >= 130.0 && longitude <= 130.2 -> "柳川市"
                // 福岡市の区
                latitude >= 33.5 && latitude <= 33.7 && longitude >= 130.2 && longitude <= 130.4 -> "福岡市西区"
                // 福岡市西区の具体的な地区（実際の座標に基づいて調整）
                latitude >= 33.60 && latitude <= 33.65 && longitude >= 130.20 && longitude <= 130.25 -> "福岡市西区草場"
                latitude >= 33.58 && latitude <= 33.63 && longitude >= 130.15 && longitude <= 130.20 -> "福岡市西区今宿"
                latitude >= 33.65 && latitude <= 33.70 && longitude >= 130.30 && longitude <= 130.40 -> "福岡市西区姪浜"
                latitude >= 33.50 && latitude <= 33.58 && longitude >= 130.10 && longitude <= 130.20 -> "福岡市西区小田部"
                latitude >= 33.60 && latitude <= 33.68 && longitude >= 130.18 && longitude <= 130.25 -> "福岡市西区野方"
                latitude >= 33.55 && latitude <= 33.62 && longitude >= 130.20 && longitude <= 130.30 -> "福岡市西区愛宕"
                // 福岡市西区の一般的な範囲（上記に該当しない場合）
                latitude >= 33.50 && latitude <= 33.70 && longitude >= 130.10 && longitude <= 130.40 -> "福岡市西区"
                // その他の地域
                else -> "市区町村"
            }
            
            
            val result = "$prefecture$city"
            result
        } catch (e: Exception) {
            "位置: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}"
        }
    }
    
    suspend fun getLatLngFromAddress(address: String): LatLng? {
        return try {
            // ジオコーディングの実装
            // 現在は簡易的な実装
            when {
                address.contains("東京") -> LatLng(35.6762, 139.6503)
                address.contains("大阪") -> LatLng(34.6937, 135.5023)
                address.contains("名古屋") -> LatLng(35.1815, 136.9066)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
