package com.example.seedstockkeeper6.service

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class GoogleMapsAuthService(private val context: Context) {
    
    private var googleSignInClient: GoogleSignInClient? = null
    
    init {
        initializeGoogleMaps()
        initializeGoogleSignIn()
    }
    
    private fun initializeGoogleMaps() {
        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
        }
    }
    
    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            // Google Maps API用の認証では特別なスコープは不要
            // .requestScopes(Scope("https://www.googleapis.com/auth/maps.readonly"))
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun getCurrentUser(): GoogleSignInAccount? {
        return try {
            GoogleSignIn.getLastSignedInAccount(context)
        } catch (e: Exception) {
            null
        }
    }
    
    fun isUserSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
    
    suspend fun signInSilently(): GoogleSignInAccount? {
        return try {
            googleSignInClient?.silentSignIn()?.await()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getSignInIntent() = googleSignInClient?.signInIntent
    
    suspend fun signOut() {
        try {
            googleSignInClient?.signOut()?.await()
        } catch (e: Exception) {
        }
    }
    
    // Firebase認証関連のメソッド
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    fun getFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    fun isFirebaseUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    fun isBothAuthenticated(): Boolean {
        return isUserSignedIn() && isFirebaseUserSignedIn()
    }
    
    fun getFirebaseUserInfo(): String {
        val user = getFirebaseUser()
        return if (user != null) {
            "ユーザー: ${user.displayName ?: user.email}"
        } else {
            "未ログイン"
        }
    }
    
    fun getAuthenticationStatus(): String {
        return when {
            isBothAuthenticated() -> "両方の認証完了"
            isFirebaseUserSignedIn() -> "Firebase認証のみ"
            isUserSignedIn() -> "Google認証のみ"
            else -> "未認証"
        }
    }
    
    fun getAuthenticationMessage(): String {
        return when {
            isBothAuthenticated() -> "✓ アプリログイン済み\n✓ Googleマップ認証済み"
            isFirebaseUserSignedIn() -> "✓ アプリログイン済み\n⚠ Googleマップ認証が必要です"
            isUserSignedIn() -> "⚠ アプリにログインしていません\n✓ Googleマップ認証済み"
            else -> "⚠ アプリにログインしていません\n⚠ Googleマップ認証が必要です"
        }
    }
    
    fun getAuthenticationColor(): androidx.compose.ui.graphics.Color {
        return when {
            isBothAuthenticated() -> androidx.compose.ui.graphics.Color.Green
            isFirebaseUserSignedIn() || isUserSignedIn() -> androidx.compose.ui.graphics.Color(0xFFFFA500) // Orange
            else -> androidx.compose.ui.graphics.Color.Red
        }
    }
}
