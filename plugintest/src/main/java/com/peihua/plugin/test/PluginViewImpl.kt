package com.peihua.plugin.test

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import com.peihua.plugin.api.IPluginView
class PluginViewImpl : IPluginView {
    override fun pluginView(context: Context): ComposeView = ComposeView(context).apply {
        print("pluginView: ,<<<<>>>>>??111$this")
        setContent {
            print("pluginView: $this")
            PluginView()
        }
    }

    override val newPluginView: Context.() -> ComposeView = {
        print("pluginView: >>>>>$this")
        ComposeView(this).apply {
            setContent {
                PluginView2("没有参数")
            }
        }
    }

    override val pluginView: Context.(String) -> ComposeView = { name ->
        print("pluginView: <><><><$this, name: $name")
        ComposeView(this).apply {
            setContent {
                PluginView2("参数是：$name")
            }
        }
    }

}