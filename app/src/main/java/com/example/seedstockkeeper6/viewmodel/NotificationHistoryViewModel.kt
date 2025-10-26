package com.example.seedstockkeeper6.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.repository.NotificationHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 通知履歴画面のViewModel
 * データ取得、状態管理、ビジネスロジックを担当
 */
class NotificationHistoryViewModel : ViewModel() {
    private val repository = NotificationHistoryRepository()
    
    private val _notificationDataList = MutableStateFlow<List<NotificationData>>(emptyList())
    val notificationDataList: StateFlow<List<NotificationData>> = _notificationDataList.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    private val _deletingDocumentId = MutableStateFlow<String?>(null)
    val deletingDocumentId: StateFlow<String?> = _deletingDocumentId.asStateFlow()
    
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()
    
    init {
        loadNotificationData()
    }
    
    /**
     * 通知データを読み込み
     */
    fun loadNotificationData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = ""
                val result = repository.getUserNotificationData()
                _notificationDataList.value = result
            } catch (e: Exception) {
                _errorMessage.value = "通知データの読み込みに失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 通知を既読にマーク
     */
    fun markNotificationAsRead(documentId: String, onRefreshUnreadCount: () -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.markNotificationAsRead(documentId)
                if (success) {
                    // ローカルのリストも更新
                    _notificationDataList.value = _notificationDataList.value.map { data ->
                        if (data.documentId == documentId) {
                            data.copy(isRead = 1)
                        } else {
                            data
                        }
                    }
                    // 未読通知数を更新
                    onRefreshUnreadCount()
                }
            } catch (e: Exception) {
                // エラーハンドリング（必要に応じてエラーメッセージを設定）
            }
        }
    }
    
    /**
     * 削除ダイアログを表示
     */
    fun showDeleteDialog(documentId: String) {
        _deletingDocumentId.value = documentId
        _showDeleteDialog.value = true
    }
    
    /**
     * 削除ダイアログを非表示
     */
    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
        _deletingDocumentId.value = null
    }
    
    /**
     * 通知データを削除
     */
    fun deleteNotificationData(onRefreshUnreadCount: () -> Unit) {
        val documentId = _deletingDocumentId.value ?: return
        
        viewModelScope.launch {
            try {
                val success = repository.deleteNotificationData(documentId)
                if (success) {
                    // 削除成功時はリストからも削除
                    _notificationDataList.value = _notificationDataList.value.filter { 
                        it.documentId != documentId 
                    }
                    // 未読通知数を更新
                    onRefreshUnreadCount()
                }
                hideDeleteDialog()
            } catch (e: Exception) {
                // エラーハンドリング（必要に応じてエラーメッセージを設定）
                hideDeleteDialog()
            }
        }
    }
}
