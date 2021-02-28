package com.unsplash.pickerandroid.photopicker

import android.app.Application

/**
 * Configuration singleton object.
 */
object UnsplashPhotoPicker {

    private lateinit var application: Application

    private lateinit var accessKey: String

    private lateinit var secretKey: String

    private const val DEFAULT_PAGE_SIZE = 20

    private var pageSize: Int = DEFAULT_PAGE_SIZE

    private var defaultSearchTerm: String = ""

    private var isLoggingEnabled = false

    fun init(
        application: Application,
        accessKey: String,
        secretKey: String,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        defaultSearchTerm: String = ""
    ): UnsplashPhotoPicker {
        this.application = application
        this.accessKey = accessKey
        this.secretKey = secretKey
        this.pageSize = pageSize
        this.defaultSearchTerm = defaultSearchTerm
        return this
    }

    fun getApplication(): Application {
        return application
    }

    fun getAccessKey(): String {
        return accessKey
    }

    fun getSecretKey(): String {
        return secretKey
    }

    fun getPageSize(): Int {
        return pageSize
    }

    fun getDefaultSearchTerm(): String {
        return defaultSearchTerm
    }

    fun setLoggingEnabled(isEnabled: Boolean) {
        isLoggingEnabled = isEnabled
    }

    fun isLoggingEnabled(): Boolean {
        return isLoggingEnabled
    }
}
