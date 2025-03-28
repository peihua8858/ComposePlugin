package com.peihua.plugin.api

import android.content.Context
import androidx.compose.ui.platform.ComposeView

interface IPluginView {
    fun pluginView(context: Context): ComposeView
    val pluginView: (Context.(String) -> ComposeView)
}