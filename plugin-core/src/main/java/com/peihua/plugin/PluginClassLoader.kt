package com.peihua.plugin

import android.content.Context
import androidx.annotation.WorkerThread
import dalvik.system.DexFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Array.newInstance
import java.lang.reflect.Field
import java.util.zip.ZipFile

internal class PluginClassLoader(
    dexPath: String,
    optimizedDirectory: String?,
    librarySearchPath: String?,
    parent: ClassLoader,
) : BaseDexClassLoader(
    dexPath,
    optimizedDirectory,
    librarySearchPath,
    parent
) {
    private var dexElements: Array<*>? = null
    private val dexFiles = arrayListOf<DexFile>()


    override fun loadClass(name: String?): Class<*>? {
        var clazz = parent.loadClass(name)

        return clazz
    }

    override fun findClass(name: String?): Class<*>? {
        var clazz = super.findClass(name)
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
        @WorkerThread
        fun loadPlugin(
            context: Context,
            pluginApkPath: String,
            parent: ClassLoader,
        ): PluginClassLoader {
            if (pluginApkPath.isBlank()) {
                throw IllegalArgumentException("pluginApkPath is blank")
            }
            val pluginPath = context.pluginFile
            if (!pluginPath.exists()) {
                pluginPath.mkdirs()
            }
            if (pluginApkPath.endsWith(".apk")) {
                return loadApk(pluginPath, pluginApkPath, parent)
            } else {
                val apkFile = pluginPath.listFiles()?.find { it.endsWith(".apk") }
                if (apkFile != null) {
                    return loadApk(
                        pluginPath,
                        apkFile.absolutePath,
                        context.classLoader
                    )
                }
            }
            throw IllegalArgumentException("pluginApkPath is not apk")
        }

        private fun loadApk(
            pluginPath: File,
            pluginApkPath: String,
            parent: ClassLoader,
        ): PluginClassLoader {
            val apkFile = File(pluginApkPath)
            println("mergeDexElement: pluginApkPath: $pluginApkPath")
            println("mergeDexElement: apkFile.exists: ${apkFile.exists()}")
            if (!apkFile.exists()) {
                throw IllegalArgumentException("pluginApkPath is not apk")
            }

            val dexFile = File(pluginPath, apkFile.nameWithoutExtension + "-" + "dex")
            if (dexFile.exists()) {
                dexFile.delete()
            }
            if (!dexFile.exists()) dexFile.mkdirs()
            println("mergeDexElement: apkFile: ${apkFile.absolutePath}")
            println("输出dex路径: $dexFile")
            val classLoader = PluginClassLoader(
                apkFile.absolutePath,
                dexFile.absolutePath,
                null, parent
            )
            classLoader.mergeDexElement(parent)
            return classLoader
        }

        private fun copyDexFilesFromApk(apkFile: File, destDir: File): List<File> {
            val copiedDexFiles = mutableListOf<File>()

            // 使用 ZipFile 解压 APK 文件
            try {
                val zipFile = ZipFile(apkFile)
                val entries = zipFile.entries()
                var dexIndex = 0 // DEX 文件的索引

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()

                    // 检查是否为 DEX 文件
                    if (entry.name.endsWith(".dex")) {
                        val newFileName = "plugin_classes${dexIndex}.dex"
                        val destFile = File(destDir, newFileName)

                        // 读取 DEX 文件内容并写入到目标文件
                        zipFile.getInputStream(entry).use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }

                        copiedDexFiles.add(destFile) // 添加到列表
                        dexIndex++ // 增加索引
                    }
                }

                zipFile.close()
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException("Failed to copy DEX files from APK: ${e.message}")
            }

            return copiedDexFiles
        }
    }

    /**
     * 合并DexElement数组: 宿主新dexElements = 宿主原始dexElements + 插件dexElements
     * 1、创建插件的 DexClassLoader 类加载器，然后通过反射获取插件的 dexElements 值。
     * 2、获取宿主的 PathClassLoader 类加载器，然后通过反射获取宿主的 dexElements 值。
     * 3、合并宿主的 dexElements 与 插件的 dexElements，生成新的 Element[]。
     * 4、最后通过反射将新的 Element[] 赋值给宿主的 dexElements。
     */
    private fun mergeDexElement(classLoader: ClassLoader): Boolean {
        val pluginClassLoader = this
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
            println("mergeDexElement: hostDexElements: ${hostDexElements.contentToString()}")
            // 插件的 类加载器
            val dexClassLoader = pluginClassLoader
            // DexPathList类的对象
            val pluginPathListObj = pathListField[dexClassLoader]
            println("mergeDexElement: pluginPathListObj: ${pluginPathListObj}")
            // 插件的 dexElements
            val pluginDexElements = dexElementsField[pluginPathListObj] as Array<*>

            println("mergeDexElement: pluginDexElements: ${pluginDexElements.contentToString()}")
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
//            dexElementsField[pluginPathListObj] = newDexElements
            dexElements = newDexElements
            println("mergeDexElement: newDexElements: ${newDexElements.size}")
            println("mergeDexElement: hostDexElements: ${hostDexElements.size}")
            println("mergeDexElement: pluginDexElements: ${pluginDexElements.size}")
            newDexElements.forEach {
                println("mergeDexElement: $it")
            }
            println("mergeDexElement: success")
            return true
        } catch (e: Exception) {
            println("mergeDexElement: $e")
            return false
        }
    }

    suspend fun <T> findClassImpl(clazz: Class<T>): List<Class<T>> {
        val returnClassList = mutableListOf<Class<T>>()
        if (dexFiles.isEmpty()) {
            if (dexElements.isNullOrEmpty()) {
                mergeDexElement(parent)
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