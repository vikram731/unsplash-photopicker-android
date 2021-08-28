package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import java.util.*

open class BaseActivity: AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
// get chosen language from shread preference
        val localeToSwitchTo = Locale(UnsplashPhotoPicker.getAppLanguage())
        val localeUpdatedContext: ContextWrapper = LocaleUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

}
