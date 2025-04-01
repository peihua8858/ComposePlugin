package com.peihua.composeplugin

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peihua.plugin.PluginManager
import com.peihua.plugin.PluginManager.Companion.PLUGIN_PATH
import com.peihua.plugin.api.IPluginView
import kotlinx.coroutines.launch
import java.io.File

class DemoViewModel(app: Application) : AndroidViewModel(app) {
    var mPluginView: IPluginView? = null
    var mPluginView2: IPluginView? = null
    var mPluginView3 = mutableStateOf<Context.() -> ComposeView>({ ComposeView(this) })
    var mPluginView4 = mutableStateOf<IPluginView?>(null)
    var pluginView5 = mutableStateOf<Context.(String) -> ComposeView>({ComposeView(this)})
    var pluginView6 = mutableStateOf<Context.() -> ComposeView>({ComposeView(this)})
    fun loadApk(context: Context,callback:(IPluginView?)->Unit) {
        viewModelScope.launch {
            try {
                val filesDir = context.externalCacheDir
                val pluginPath = File(filesDir?.absolutePath, PLUGIN_PATH)
                val apkFile = File(pluginPath, "plugintest-debug.apk")
                PluginManager.getInstance().loadPlugin(context, apkFile.absolutePath)
                PluginManager.getInstance().mergeDexElement(context)

                val composeViewProxyClass = PluginManager.getInstance().loadClass("com.peihua.plugin.test.PluginViewImpl")
                composeViewProxyClass?.let { proxyClass ->
                    val getPluginViewMethod = proxyClass.getDeclaredMethod("getPluginView")
                    val obj = proxyClass.newInstance()
                    pluginView5.value = getPluginViewMethod.invoke(obj) as (Context.(String) -> ComposeView)
                    val getPluginViewMethod1 = proxyClass.getDeclaredMethod("getNewPluginView")
                    pluginView6.value = getPluginViewMethod1.invoke(obj) as (Context.() -> ComposeView)
                }


                val pluginView = PluginManager.getInstance().pluginView
                val pluginView2: IPluginView? =
                    PluginManager.getInstance().findPlugin(context,IPluginView::class.java)
                if (pluginView != null) {
                    mPluginView4.value = pluginView
                    mPluginView3.value = { context: Context -> pluginView.pluginView(context) }
                    mPluginView = pluginView
                }
                if (pluginView2 != null) {
                    mPluginView2 = pluginView2
                }
                println("pluginView: $pluginView")
                println("mPluginView: $mPluginView")
                println("mPluginView4: ${mPluginView4.value} ")
                println("pluginView2: $pluginView2")
                callback(pluginView)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }
}