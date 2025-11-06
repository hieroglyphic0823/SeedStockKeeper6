package com.example.seedstockkeeper6.ui.auth


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.components.LoadingAnimationVideoPlayer
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

/**
 * 認証ゲート。
 * - 未ログイン → SignInScreen を表示
 * - ログイン済み → [content]（アプリ本体）を表示
 */
@Composable
fun AuthGate(
    modifier: Modifier = Modifier,
    onAuthChanged: ((FirebaseUser?) -> Unit)? = null,
    content: @Composable (user: FirebaseUser) -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var initializing by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            currentUser = fbAuth.currentUser
            initializing = false
            onAuthChanged?.invoke(currentUser)
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    when {
        initializing -> Splash(modifier)
        currentUser == null -> SignInScreen(modifier)
        else -> content(currentUser!!)
    }
}

@Composable
private fun Splash(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingAnimationVideoPlayer(
            modifier = Modifier.fillMaxSize(),
            assetFileName = null,
            rawResId = com.example.seedstockkeeper6.R.raw.sukesan_s,
            mute = false
        )
    }
}

/**
 * サインイン画面
 * - Google でサインイン
 * - 匿名で始める（任意で残す）
 */
@Composable
fun SignInScreen(modifier: Modifier = Modifier) {
    val auth = remember { FirebaseAuth.getInstance() }
    val ctx = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("たねすけさん にサインイン", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Google でサインイン（Credential Manager）
        Button(
            enabled = !isLoading,
            onClick = {
                scope.launch {
                    isLoading = true
                    message = null
                    try {
                        val credentialManager = CredentialManager.create(ctx)

                        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                            .setServerClientId("639347804790-m1llndci906llu8inij5gr6s0g0iv6lj.apps.googleusercontent.com")
                            .setFilterByAuthorizedAccounts(false)  // 端末に保存されてるGoogleアカウントのみ絞るなら true
                            .setAutoSelectEnabled(false)           // 単一候補なら自動選択したい場合は true
                            .build()

                        val request = androidx.credentials.GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(ctx, request)
                        val cred = result.credential

                        // Google ID トークンの取り出し
                        val idToken = when (cred) {
                            is androidx.credentials.CustomCredential -> {
                                if (cred.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                                        .createFrom(cred.data).idToken
                                } else null
                            }
                            else -> null
                        }

                        if (idToken.isNullOrBlank()) {
                            message = "Googleトークンを取得できませんでした"
                            return@launch
                        }

                        val firebaseCred = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)

                        // 既に匿名ログイン中なら「リンク」で UID を継続
                        val cur = auth.currentUser
                        val task = if (cur?.isAnonymous == true) {
                            cur.linkWithCredential(firebaseCred)
                        } else {
                            auth.signInWithCredential(firebaseCred)
                        }

                        task.addOnCompleteListener { t ->
                            isLoading = false
                            if (!t.isSuccessful) {
                                message = t.exception?.localizedMessage ?: "サインインに失敗しました"
                            } else {
                                message = null
                            }
                        }
                    } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                        isLoading = false
                        // ユーザーキャンセル含む例外
                        message = e.localizedMessage ?: "サインインをキャンセルしました"
                    } catch (e: Exception) {
                        isLoading = false
                        message = e.localizedMessage ?: "予期せぬエラーが発生しました"
                    }
                }
            },
                    colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) { Text("Googleでサインイン") }

        Spacer(Modifier.height(12.dp))

        // 匿名で始める（任意）
        OutlinedButton(
            enabled = !isLoading,
            onClick = {
                isLoading = true
                auth.signInAnonymously().addOnCompleteListener { t ->
                    isLoading = false
                    if (!t.isSuccessful) message = t.exception?.localizedMessage
                }
            }
        ) { Text("匿名で始める") }

        if (isLoading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it)
        }
    }
}
