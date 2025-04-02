package com.peihua.composeplugin

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peihua.plugin.PluginManager
import com.peihua.plugin.api.IPluginView
import com.peihua.plugin.pluginFile
import kotlinx.coroutines.launch
import java.io.File

class DemoViewModel(app: Application) : AndroidViewModel(app) {
    var mPluginView: IPluginView? = null
    fun loadApk(context: Context, callback: (IPluginView?) -> Unit) {
        viewModelScope.launch {
            try {
                val pluginPath = context.pluginFile
                val apkFile = File(pluginPath, "plugintest-debug.apk")
                PluginManager.getInstance().loadPlugin(context, apkFile.absolutePath)
                val pluginView2: IPluginView? =
                    PluginManager.getInstance().findPlugin(IPluginView::class.java)
                if (pluginView2 != null) {
                    mPluginView = pluginView2
                }
                println("mPluginView: $mPluginView")
                println("pluginView2: $pluginView2")
                callback(pluginView2)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }
}