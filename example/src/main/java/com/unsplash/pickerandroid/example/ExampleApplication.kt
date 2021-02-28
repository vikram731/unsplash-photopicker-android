package com.unsplash.pickerandroid.example

import android.app.Application
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // initializing the picker library
        UnsplashPhotoPicker.init(
            this,
            "zpLQLD0bmqAmbQboHwIdhGJRImOdgUhLMkrkzyUdn-o",
            "sJNQ8GDaYLaJyDjBCoz7UVHCgLUEGGAd8XLvFb7EVv8", 20
            /* optional page size (number of photos per page) */
        )
            /* .setLoggingEnabled(true) // if you want to see the http requests */
    }
}
