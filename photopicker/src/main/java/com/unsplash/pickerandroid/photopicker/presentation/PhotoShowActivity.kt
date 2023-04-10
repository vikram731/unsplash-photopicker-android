package com.unsplash.pickerandroid.photopicker.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.unsplash.pickerandroid.photopicker.R
import com.unsplash.pickerandroid.photopicker.databinding.ActivityImageShowBinding
import com.unsplash.pickerandroid.photopicker.databinding.ActivityPickerBinding

class PhotoShowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageShowBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // loading the image thanks to its url
        Picasso.get().load(intent.getStringExtra(EXTRA_URL))
            .into(binding.imageShowView)
        // click listener
        binding.imageShowLayout.setOnClickListener { supportFinishAfterTransition() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    companion object {
        private const val EXTRA_URL = "EXTRA_URL"

        /**
         * @param callingContext the calling context
         * @param url the url of the image to show
         *
         * @return the intent needed to come to this activity
         */
        fun getStartingIntent(callingContext: Context, url: String): Intent {
            val intent = Intent(callingContext, PhotoShowActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            return intent
        }
    }
}
