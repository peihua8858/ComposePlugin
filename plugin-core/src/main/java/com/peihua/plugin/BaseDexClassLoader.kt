package com.peihua.plugin

import java.net.URL


internal open class BaseDexClassLoader(
    val dexPath: String,
    val optimizedDirectory: String?,
    val librarySearchPath: String?,
    parent: ClassLoader,
) : dalvik.system.DexClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent) {
    public override fun findClass(name: String?): Class<*>? {
        return super.findClass(name)
    }

    public override fun findLibrary(name: String?): String? {
        return super.findLibrary(name)
    }

    public override fun findResource(name: String?): java.net.URL? {
        return super.findResource(name)
    }

    public override fun findResources(name: String?): java.util.Enumeration<java.net.URL>? {
        return super.findResources(name)
    }

    override fun getPackage(name: String?): Package? {
        return super.getPackage(name)
    }

    public override fun getPackages(): Array<Package>? {
        return super.getPackages()
    }

    override fun loadClass(name: String?): Class<*>? {
        return super.loadClass(name)
    }

    public override fun loadClass(name: String?, resolve: Boolean): Class<*>? {
        try {
            return super.loadClass(name, resolve)
        } catch (e: Exception) {
            return null
        }
    }

    public override fun definePackage(
        name: String?,
        specTitle: String?,
        specVersion: String?,
        specVendor: String?,
        implTitle: String?,
        implVersion: String?,
        implVendor: String?,
        sealBase: URL?,
    ): Package? {
        return super.definePackage(
            name,
            specTitle,
            specVersion,
            specVendor,
            implTitle,
            implVersion,
            implVendor,
            sealBase
        )
    }
}
