package com.peihua.plugin

import android.content.Context
import com.peihua.plugin.api.IPluginView

class PluginManager {
    companion object {
        private val instance: PluginManager = PluginManager()
        fun getInstance(): PluginManager {
            return instance
        }
    }

    private var pluginClassLoader: PluginClassLoader? = null

    private var pluginView: IPluginView? = null
    suspend fun loadPlugin(context: Context, pluginApkPath: String) {
        pluginClassLoader =
            PluginClassLoader.loadPlugin(context, pluginApkPath, context.classLoader)
    }

    private fun findPluginView(context: Context) {
        // 1、查找实现IPluginView的类
        //扫描所有实现 IPluginView 的类
        val clazzList = pluginClassLoader?.findLoadedPluginClass("com.peihua.plugin.api.IPluginView")

        val clazz = pluginClassLoader?.loadClass("com.peihua.plugin.test.PluginViewImpl")


        pluginClassLoader?.loadClass("com.peihua.plugin.test.PluginViewImpl")
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

    fun <T : IPluginView> loadClass(clazz: Class<T>): Class<T>? {
        return loadClass(clazz.name) as? Class<T>
    }

    fun invokeMethod(clazz: Class<IPluginView>, methodName: String, args: Array<Any>): Any? {
        return clazz.getMethod(methodName).invoke(clazz.newInstance(), *args)
    }


}