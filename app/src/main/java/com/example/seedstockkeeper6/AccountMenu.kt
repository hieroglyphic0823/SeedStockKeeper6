package com.example.seedstockkeeper6

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AccountMenuButton(
    user: FirebaseUser?,
    size: Dp = 32.dp,
    onSignOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val photo = user?.photoUrl
    val emailOrName = user?.displayName ?: user?.email ?: "未ログイン"

    Box {
        IconButton(onClick = { expanded = true }) {
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = "プロフィール",
                    modifier = Modifier.size(size).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "プロフィール",
                    modifier = Modifier.size(size),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(emailOrName) },
                onClick = { /* no-op */ },
                enabled = false,
                leadingIcon = {
                    if (photo != null) {
                        AsyncImage(
                            model = photo,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                    }
                }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                text = { Text("サインアウト") },
                onClick = {
                    expanded = false
                    onSignOut()
                }
            )
        }
    }
}

fun signOut(
    context: Context,
    scope: CoroutineScope
) {
    FirebaseAuth.getInstance().signOut()
    scope.launch {
        try {
            CredentialManager.create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // ignore
        }
    }
}
