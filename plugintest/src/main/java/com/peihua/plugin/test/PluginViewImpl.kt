package com.peihua.plugin.test

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import com.peihua.plugin.api.IPluginView

class PluginViewImpl : IPluginView {
    override fun pluginView(context: Context): ComposeView {
        return ComposeView(context).apply {
            setContent {
                PluginView()
            }
        }
    }

    override val pluginView: Context.(String) -> ComposeView
        get() = {
            ComposeView(this).apply {
                setContent {
                    PluginView2()
                }
            }
        }

}