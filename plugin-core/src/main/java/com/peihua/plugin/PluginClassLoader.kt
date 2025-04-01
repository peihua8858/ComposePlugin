package com.peihua.plugin

import android.content.Context
import dalvik.system.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Array.newInstance
import java.lang.reflect.Field

internal class PluginClassLoader(
    private val specialClassLoader: BaseDexClassLoader,
    parent: ClassLoader,
) : BaseDexClassLoader(
    specialClassLoader.dexPath,
    specialClassLoader.optimizedDirectory,
    specialClassLoader.librarySearchPath,
    parent
) {
    private var dexElements: Array<*>? = null
    private val dexFiles = arrayListOf<DexFile>()
    override fun loadClass(name: String?): Class<*>? {
        var clazz = super.loadClass(name)
        if (clazz == null) {
            clazz = specialClassLoader.loadClass(name)
        }
        return clazz
    }

    override fun findClass(name: String?): Class<*>? {
        var clazz = super.findClass(name)
        if (clazz == null) {
            clazz = specialClassLoader.findClass(name)
        }
        return clazz
    }

    fun findClass(name: String?, callback: (Class<*>?) -> Unit) {
        val clazz = findClass(name)
        callback(clazz)
    }

    fun findLoadedPluginClass(name: String): Class<*>? {
        return super.findLoadedClass(name)
    }

    fun findLoadedClass(name: String, callback: (Class<*>) -> Unit) {
        val clazz = findLoadedClass(name)
        callback(clazz)
    }

    companion object {
        suspend fun loadPlugin(
            context: Context,
            pluginApkPath: String,
            parent: ClassLoader,
        ): PluginClassLoader {
            return withContext(Dispatchers.IO) {
                if (pluginApkPath.isBlank()) {
                    throw IllegalArgumentException("pluginApkPath is blank")
                }
                var filesDir = context.externalCacheDir
                if (filesDir == null) {
                    filesDir = context.cacheDir
                }
                val pluginPath = File(filesDir.absolutePath, PluginManager.PLUGIN_PATH)
                if (!pluginPath.exists()) {
                    pluginPath.mkdirs()
                }
                if (pluginApkPath.endsWith(".apk")) {
                    return@withContext loadApk(pluginPath, pluginApkPath, parent)
                } else {
                    val apkFile = pluginPath.listFiles()?.find { it.endsWith(".apk") }
                    if (apkFile != null) {
                        return@withContext loadApk(
                            pluginPath,
                            apkFile.absolutePath,
                            parent
                        )
                    }
                }
                throw IllegalArgumentException("pluginApkPath is not apk")
            }
        }

        private suspend fun loadApk(
            pluginPath: File,
            pluginApkPath: String,
            parent: ClassLoader,
        ): PluginClassLoader {
            val apkFile = File(pluginApkPath)
            println("mergeDexElement: apkFile.exists: ${apkFile.exists()}")
            if (!apkFile.exists()) {
                throw IllegalArgumentException("pluginApkPath is not apk")
            }
            val dexFile = File(pluginPath, apkFile.nameWithoutExtension + "-" + "dex")
            if (!dexFile.exists()) dexFile.mkdirs()
            println("mergeDexElement: apkFile: ${apkFile.absolutePath}")
            println("输出dex路径: $dexFile")
           val files= Test.splitPaths(pluginApkPath,false)
            println("mergeDexElement: files: ${files.size}")
            files.forEach {
                println("mergeDexElement: dexFile:${it.absolutePath}")
            }
            val classLoader = PluginClassLoader(
                BaseDexClassLoader(
                    apkFile.absolutePath,
                    dexFile.absolutePath,
                    null,
                    parent
                ), parent
            )
            classLoader.mergeDexElement(parent)
            return classLoader
        }
    }

    /**
     * 合并DexElement数组: 宿主新dexElements = 宿主原始dexElements + 插件dexElements
     * 1、创建插件的 DexClassLoader 类加载器，然后通过反射获取插件的 dexElements 值。
     * 2、获取宿主的 PathClassLoader 类加载器，然后通过反射获取宿主的 dexElements 值。
     * 3、合并宿主的 dexElements 与 插件的 dexElements，生成新的 Element[]。
     * 4、最后通过反射将新的 Element[] 赋值给宿主的 dexElements。
     */
    private suspend fun mergeDexElement(classLoader: ClassLoader): Boolean {
        val pluginClassLoader = this
        return withContext(Dispatchers.IO) {
            try {
                val clazz = Class.forName("dalvik.system.BaseDexClassLoader")
                val pathListField: Field = clazz.getDeclaredField("pathList")
                pathListField.isAccessible = true

                val dexPathListClass = Class.forName("dalvik.system.DexPathList")
                val dexElementsField = dexPathListClass.getDeclaredField("dexElements")
                dexElementsField.isAccessible = true

                // 宿主的 类加载器
                val pathClassLoader: ClassLoader = classLoader
                // DexPathList类的对象
                val hostPathListObj = pathListField[pathClassLoader]
                // 宿主的 dexElements
                val hostDexElements = dexElementsField[hostPathListObj] as Array<*>
                println("mergeDexElement: hostPathListObj: ${hostPathListObj}")
                // 插件的 类加载器
                val dexClassLoader = pluginClassLoader
                // DexPathList类的对象
                val pluginPathListObj = pathListField[dexClassLoader]
                println("mergeDexElement: pluginPathListObj: ${pluginPathListObj}")
                // 插件的 dexElements
                val pluginDexElements = dexElementsField[pluginPathListObj] as Array<*>

                println("mergeDexElement: dexElementsField: ${dexElementsField.name}")
                val hostDexSize = hostDexElements.size
                val pluginDexSize = pluginDexElements.size
                // 宿主dexElements = 宿主dexElements + 插件dexElements
                // 创建一个新数组
                val newDexElements = hostDexElements.javaClass.componentType?.let {
                    newInstance(it, hostDexSize + pluginDexSize)
                } as Array<*>
                System.arraycopy(hostDexElements, 0, newDexElements, 0, hostDexSize)
                System.arraycopy(pluginDexElements, 0, newDexElements, hostDexSize, pluginDexSize)

                // 赋值 hostDexElements = newDexElements
                dexElementsField[hostPathListObj] = newDexElements
                dexElements = newDexElements
                println("mergeDexElement: newDexElements: ${newDexElements.size}")
                println("mergeDexElement: hostDexElements: ${hostDexElements.size}")
                println("mergeDexElement: pluginDexElements: ${pluginDexElements.size}")
                newDexElements.forEach {
                    println("mergeDexElement: $it")
                }
                println("mergeDexElement: success")
                return@withContext true
            } catch (e: Exception) {
                println("mergeDexElement: $e")
                return@withContext false
            }
        }
    }


    suspend fun <T> findClassImpl(clazz: Class<T>): List<Class<T>> {
        val returnClassList = mutableListOf<Class<T>>()
        if (dexFiles.isEmpty()) {
            if (dexElements.isNullOrEmpty()) {
                mergeDexElement(specialClassLoader)
            }
            dexElements?.let { pluginDexElements ->
                for (item in pluginDexElements) {
                    val dexFileField = item?.javaClass?.getDeclaredField("dexFile")
                    dexFileField?.isAccessible = true
                    val dexFile = dexFileField?.get(item) as DexFile
                    dexFiles.add(dexFile)
                    findClassByDexFile(dexFile, clazz, returnClassList)
                }
            }
        } else {
            dexFiles.forEach {
                findClassByDexFile(it, clazz, returnClassList)
            }
        }
        return returnClassList
    }

    private fun <T> findClassByDexFile(
        dexFile: DexFile,
        clazz: Class<T>,
        returnClassList: MutableList<Class<T>>,
    ) {
        val enumeration = dexFile.entries()
        while (enumeration.hasMoreElements()) {
            val className = enumeration.nextElement() as String
            checkClass(clazz, className, returnClassList)
        }
    }

    private fun <T> checkClass(
        clazz: Class<T>,
        className: String,
        classes: MutableList<Class<T>>,
    ) {
        try {
            val scanClass = loadClass(className)
            println("scanClass: $scanClass")
            if (scanClass == null) {
                return
            }
            if (clazz.isAssignableFrom(scanClass)) { // 判断是不是一个接口
                if (clazz != scanClass) { //` 本身不加进去
                    classes.add(scanClass as Class<T>)
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}