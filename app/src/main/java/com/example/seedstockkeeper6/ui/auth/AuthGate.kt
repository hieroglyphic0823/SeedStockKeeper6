package com.example.seedstockkeeper6.ui.auth


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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
        currentUser == null -> {
            // 開発環境では匿名認証でログイン
            LaunchedEffect(Unit) {
                try {
                    auth.signInAnonymously()
                        .addOnFailureListener { exception ->
                            // エラーが発生した場合はログに記録
                            println("Anonymous auth failed: ${exception.message}")
                        }
                } catch (e: Exception) {
                    println("Auth error: ${e.message}")
                }
            }
            Splash(modifier)
        }
        else -> content(currentUser!!)
    }
}

@Composable
private fun Splash(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
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
                        val credentialManager = androidx.credentials.CredentialManager.create(ctx)

                        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                            .setServerClientId(ctx.getString(com.example.seedstockkeeper6.R.string.default_web_client_id))
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
            }
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
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
