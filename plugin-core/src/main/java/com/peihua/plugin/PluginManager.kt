package com.peihua.plugin

import android.content.Context
import android.os.Environment
import com.peihua.plugin.PluginManager.Companion.PLUGIN_PATH
import com.peihua.plugin.api.IPluginView
import java.io.File

val Context.pluginFile: File
    get() {
        var filesDir = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (filesDir == null) {
            filesDir = this.filesDir
        }
        return File(filesDir, PLUGIN_PATH)
    }

class PluginManager {
    companion object {
        const val PLUGIN_PATH = "plugin"
        private val instance: PluginManager = PluginManager()
        fun getInstance(): PluginManager {
            return instance
        }
    }

    private var pluginClassLoader: PluginClassLoader? = null

    val pluginView: IPluginView?
        get() {
            val clazz =
                pluginClassLoader?.loadClass("com.peihua.plugin.test.PluginViewImpl")
            return clazz?.newInstance() as? IPluginView
        }

    suspend fun loadPlugin(context: Context) {
        val pluginPath = context.pluginFile
        if (pluginClassLoader!=null) {
            return
        }
        pluginClassLoader =
            PluginClassLoader.loadPlugin(
                context,
                pluginPath.absolutePath,
                context.classLoader
            )
    }

    suspend fun loadPlugin(context: Context, pluginApkFile: String) {
        if (pluginClassLoader!=null) {
            return
        }
        pluginClassLoader =
            PluginClassLoader.loadPlugin(context, pluginApkFile, context.classLoader)
    }


    fun loadClass(className: String): Class<*>? {
        try {
            if (pluginClassLoader == null) {
                println("pluginClassLoader is null")
            }
            return pluginClassLoader?.loadClass(className)
        } catch (e: ClassNotFoundException) {
            println("loadClass ClassNotFoundException: $className")
        }
        return null
    }

    suspend fun <T : IPluginView> findPlugin(clazz: Class<T>): T? {
        val clazz = findPluginClass(clazz)
        try {
            if (clazz != null) {
                return clazz.newInstance()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun <T : IPluginView> findPluginClass(clazz: Class<T>): Class<T>? {
        try {
            if (pluginClassLoader == null) {
                println("pluginClassLoader is null")
            }
            return pluginClassLoader?.findClassImpl(clazz)?.firstOrNull()
        } catch (e: ClassNotFoundException) {
            println("loadClass ClassNotFoundException: ${e.stackTraceToString()}")
        }
        return null
    }

    fun invokeMethod(clazz: Class<IPluginView>, methodName: String, args: Array<Any>): Any? {
        return clazz.getMethod(methodName).invoke(clazz.newInstance(), *args)
    }


}