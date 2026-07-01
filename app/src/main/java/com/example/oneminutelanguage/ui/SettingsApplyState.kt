package com.example.oneminutelanguage.ui

sealed class SettingsApplyState {
    object Idle : SettingsApplyState()
    object DownloadingModels : SettingsApplyState()
    data class Translating(val current: Int, val total: Int) : SettingsApplyState()
    object Success : SettingsApplyState()
    data class Error(val message: String) : SettingsApplyState()
}
