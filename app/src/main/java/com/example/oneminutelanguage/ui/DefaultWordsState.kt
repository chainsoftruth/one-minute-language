package com.example.oneminutelanguage.ui

sealed class DefaultWordsState {
    object Idle : DefaultWordsState()
    object DownloadingModels : DefaultWordsState()
    data class Importing(val current: Int, val total: Int) : DefaultWordsState()
    object Removing : DefaultWordsState()
    object Done : DefaultWordsState()
    data class Error(val message: String) : DefaultWordsState()
}
