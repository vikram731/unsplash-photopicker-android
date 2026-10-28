package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unsplash.pickerandroid.photopicker.R
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import java.util.*

open class BaseActivity: AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
// get chosen language from shread preference
        val localeToSwitchTo = Locale(UnsplashPhotoPicker.getAppLanguage())
        val localeUpdatedContext: ContextWrapper = LocaleUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    // Edge-to-edge is enforced from SDK 35: keep content clear of the system bars
    // and draw a colored strip behind the status bar, like the host app does.
    override fun setContentView(view: View) {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val statusBarBg = View(this)
        statusBarBg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        root.addView(statusBarBg, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))
        root.addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        super.setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            statusBarBg.layoutParams.height = bars.top
            statusBarBg.requestLayout()
            insets
        }
    }

}
