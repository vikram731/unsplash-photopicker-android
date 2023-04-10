package com.unsplash.pickerandroid.photopicker.presentation

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.ChipGroup
import com.unsplash.pickerandroid.photopicker.Injector
import com.unsplash.pickerandroid.photopicker.R
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.databinding.ActivityPickerBinding
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import com.yalantis.ucrop.view.CropImageView
import java.io.File
import kotlin.collections.ArrayList

/**
 * Main screen for the picker.
 * This will show a list a photos and a search component.
 * The list is has an infinite scroll.
 */
class UnsplashPickerActivity : BaseActivity(), OnPhotoSelectedListener {

    val INTENT_ASPECT_RATIO_X = "aspect_ratio_x"
    val INTENT_ASPECT_RATIO_Y = "aspect_ratio_Y"
    val INTENT_LOCK_ASPECT_RATIO = "lock_aspect_ratio"
    val INTENT_IMAGE_COMPRESSION_QUALITY = "compression_quality"
    val INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT = "set_bitmap_max_width_height"
    val INTENT_BITMAP_MAX_WIDTH = "max_width"
    val INTENT_BITMAP_MAX_HEIGHT = "max_height"
    val INTENT_SEARCH_TERMS = "search_terms"

    private lateinit var binding: ActivityPickerBinding

    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    private lateinit var mAdapter: UnsplashPhotoAdapter

    private lateinit var mViewModel: UnsplashPickerViewModel

    private var mIsMultipleSelection = false

    private var mCurrentState = UnsplashPickerState.IDLE

    private var mPreviousState = UnsplashPickerState.IDLE

    private var lockAspectRatio: Boolean = false
    private  var setBitmapMaxWidthHeight: Boolean = false
    private var ASPECT_RATIO_X: Float = 1.0f
    private  var ASPECT_RATIO_Y: Float = 1.0f
    private  var bitmapMaxWidth:Int = 2048
    private  var bitmapMaxHeight:Int = 2048
    private var IMAGE_COMPRESSION: Int = 80
    private var defaultSearchTerm: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent ?: return

        ASPECT_RATIO_X = intent.getIntExtra(
                INTENT_ASPECT_RATIO_X,
                1
        ).toFloat()
        ASPECT_RATIO_Y = intent.getIntExtra(
                INTENT_ASPECT_RATIO_Y,
                1
        ).toFloat()
        IMAGE_COMPRESSION = intent.getIntExtra(
                INTENT_IMAGE_COMPRESSION_QUALITY,
                IMAGE_COMPRESSION
        )
        lockAspectRatio = intent.getBooleanExtra(
                INTENT_LOCK_ASPECT_RATIO,
                false
        )
        setBitmapMaxWidthHeight = intent.getBooleanExtra(
                INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT,
                false
        )
        bitmapMaxWidth = intent.getIntExtra(
                INTENT_BITMAP_MAX_WIDTH,
                bitmapMaxWidth
        )
        bitmapMaxHeight = intent.getIntExtra(
                INTENT_BITMAP_MAX_HEIGHT,
                bitmapMaxHeight
        )
        defaultSearchTerm = intent.getStringExtra(
                INTENT_SEARCH_TERMS
        ) ?: ""


        mIsMultipleSelection = intent.getBooleanExtra(EXTRA_IS_MULTIPLE, false)
        // recycler view layout manager
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        // recycler view adapter
        mAdapter = UnsplashPhotoAdapter(this, mIsMultipleSelection)
        mAdapter.setOnImageSelectedListener(this)
        // recycler view configuration
        binding.unsplashPickerRecyclerView.setHasFixedSize(true)
        binding.unsplashPickerRecyclerView.itemAnimator = null
        binding.unsplashPickerRecyclerView.layoutManager = mLayoutManager
        binding.unsplashPickerRecyclerView.adapter = mAdapter
        // click listeners
        binding.unsplashPickerBackImageView.setOnClickListener { onBackPressed() }
        binding.unsplashPickerCancelImageView.setOnClickListener { onBackPressed() }
        binding.unsplashPickerClearImageView.setOnClickListener { onBackPressed() }
        binding.unsplashPickerSearchImageView.setOnClickListener {
            // updating state
            mCurrentState = UnsplashPickerState.SEARCHING
            updateUiFromState()
        }
        binding.unsplashPickerDoneImageView.setOnClickListener { sendPhotosAsResult() }
        binding.unsplashPickerEditText.setText(defaultSearchTerm)
        binding.chipGroupCategories.setOnCheckedChangeListener { chipGroup: ChipGroup, i: Int ->
            var selectedCategory = ""
            var type = 0 //Type=0 for collection, 1 for search
            when (i) {
                R.id.chip_posts_trending -> {
                    selectedCategory = "317099"
                }
                R.id.chip_posts_nature -> {
                    selectedCategory = "327760"
                }
                R.id.chip_posts_animals -> {
                    selectedCategory = "4760062"
                }
                R.id.chip_posts_abstract -> {
                    selectedCategory = "993190"
                }
                R.id.chip_posts_textures -> {
                    selectedCategory = "1848157"
                }
                R.id.chip_posts_festive -> {
                    type = 1
                    selectedCategory = "celebration"
                }
                R.id.chip_posts_wallpapers -> {
                    type = 1
                    selectedCategory = "wallpapers"
                }
                R.id.chip_posts_birthday -> {
                    type = 1
                    selectedCategory = "birthday"
                }
                R.id.chip_posts_writers -> {
                    selectedCategory = "40402531"
                }
            }
            //unsplash_picker_edit_text.setText(selectedCategory)
            if (type == 0) {
                mViewModel.getUnsplashCollection(selectedCategory)
            } else if (type == 1) {
                mViewModel.getUnsplashForKeyword(selectedCategory);
            }

        }

        // get the view model and bind search edit text
        mViewModel =
                ViewModelProviders.of(this, Injector.createPickerViewModelFactory())
                    .get(UnsplashPickerViewModel::class.java)
        observeViewModel()
        mViewModel.bindSearch(binding.unsplashPickerEditText)
    }

    /**
     * Observes the live data in the view model.
     */
    private fun observeViewModel() {
        mViewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
        })
        mViewModel.messageLiveData.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        mViewModel.loadingLiveData.observe(this, Observer {
            binding.unsplashPickerProgressBarLayout.visibility =
                    if (it != null && it) View.VISIBLE else View.GONE
        })
        mViewModel.photosLiveData.observe(this, Observer {
            binding.unsplashPickerNoResultTextView.visibility =
                    if (it == null || it.isEmpty()) View.VISIBLE
                    else View.GONE
            mAdapter.submitList(it)
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // we want the recycler view to have 3 columns when in landscape and 2 in portrait
        mLayoutManager.spanCount =
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 3
                else 2
        mAdapter.notifyDataSetChanged()
    }

    override fun onPhotoSelected(nbOfSelectedPhotos: Int) {
        // if multiple selection
        if (mIsMultipleSelection) {
            // update the title
            binding.unsplashPickerTitleTextView.text = when (nbOfSelectedPhotos) {
                0 -> getString(R.string.unsplash)
                1 -> getString(R.string.photo_selected)
                else -> getString(R.string.photos_selected, nbOfSelectedPhotos)
            }
            // updating state
            if (nbOfSelectedPhotos > 0) {
                // only once, ignoring all subsequent photo selections
                if (mCurrentState != UnsplashPickerState.PHOTO_SELECTED) {
                    mPreviousState = mCurrentState
                    mCurrentState = UnsplashPickerState.PHOTO_SELECTED
                }
                updateUiFromState()
            } else { // no photo selected means un-selection
                onBackPressed()
            }
        }
        // if single selection send selected photo as a result
        else if (nbOfSelectedPhotos > 0) {
            val photos: ArrayList<UnsplashPhoto> = mAdapter.getImages()
            // track the downloads
            mViewModel.trackDownloads(photos)

            val photo = photos[0];
            var photoUrl: String? = photo.urls.regular
            if (photoUrl == null) {
                photoUrl = photo.urls.small
            }
            if (photoUrl != null) {
                cropImage(Uri.parse(photoUrl))
            }
            //sendPhotosAsResult()
        }
    }

    private fun cropImage(sourceUri: Uri) {
        var fname = "quotepic" + System.currentTimeMillis();
        if (!sourceUri.toString().startsWith("http")) {
            try {
                val tmpfname: String? = queryName(contentResolver, sourceUri)
                if (tmpfname != null &&!tmpfname.isEmpty()) {
                    fname = tmpfname
                }
            } catch (e: Exception) {
                //
            }
        }
        val file = File(cacheDir, fname + "_crop")
        if (file.exists()) {
            //Delete
            file.delete()
        }
        val destinationUri = Uri.fromFile(file)
        val options = UCrop.Options()
        //options.setCompressionQuality(IMAGE_COMPRESSION);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setToolbarWidgetColor(Color.WHITE)

        if(ASPECT_RATIO_X == 1.0f && ASPECT_RATIO_Y == 1.0f) {
            options.setAspectRatioOptions(
                0,
                AspectRatio(null, 1.0f, 1.0f),
                AspectRatio(
                    "Original",
                    CropImageView.DEFAULT_ASPECT_RATIO,
                    CropImageView.DEFAULT_ASPECT_RATIO
                )
            )
        } else {
            options.setAspectRatioOptions(
                0,
                AspectRatio(
                    null,
                    ASPECT_RATIO_X,
                    ASPECT_RATIO_Y
                ),
                AspectRatio(null, 1.0f, 1.0f),
                AspectRatio(
                    "Original",
                    CropImageView.DEFAULT_ASPECT_RATIO,
                    CropImageView.DEFAULT_ASPECT_RATIO
                )
            )
        }

//        if (lockAspectRatio)
//            options.withAspectRatio(ASPECT_RATIO_X, ASPECT_RATIO_Y);
        if (setBitmapMaxWidthHeight) options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight)

        UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .start(this)
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor = resolver.query(uri, null, null, null, null) ?: return ""
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    /**
     * Sends images in the result intent as a result for the calling activity.
     */
    private fun sendPhotosAsResult() {
        // get the selected photos
        val photos: ArrayList<UnsplashPhoto> = mAdapter.getImages()
        // track the downloads
        mViewModel.trackDownloads(photos)
        // send them back to the calling activity
        val data = Intent()
        data.putExtra(EXTRA_PHOTOS, photos)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onPhotoLongPress(imageView: ImageView, url: String) {
        startActivity(PhotoShowActivity.getStartingIntent(this, url))
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (mCurrentState) {
            UnsplashPickerState.IDLE -> {
                super.onBackPressed()
            }
            UnsplashPickerState.SEARCHING -> {
                // updating states
                mCurrentState = UnsplashPickerState.IDLE
                mPreviousState = UnsplashPickerState.SEARCHING
                // updating ui
                updateUiFromState()
            }
            UnsplashPickerState.PHOTO_SELECTED -> {
                // updating states
                mCurrentState = if (mPreviousState == UnsplashPickerState.SEARCHING) {
                    UnsplashPickerState.SEARCHING
                } else {
                    UnsplashPickerState.IDLE
                }
                mPreviousState = UnsplashPickerState.PHOTO_SELECTED
                // updating ui
                updateUiFromState()
            }
        }
    }

    private fun setResultOk(imagePath: Uri?) {
        val intent = Intent()
        intent.putExtra("path", imagePath)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setResultCancelled() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            setResultCancelled()
            return
        }
        val resultUri = UCrop.getOutput(data)
        setResultOk(resultUri)
    }

    /*
    STATES
     */

    private fun updateUiFromState() {
        when (mCurrentState) {
            UnsplashPickerState.IDLE -> {
                // back and search buttons visible
                binding.unsplashPickerBackImageView.visibility = View.VISIBLE
                binding.unsplashPickerSearchImageView.visibility = View.VISIBLE
                // cancel and done buttons gone
                binding.unsplashPickerCancelImageView.visibility = View.GONE
                binding.unsplashPickerClearImageView.visibility = View.GONE
                // edit text cleared and gone
                if (!TextUtils.isEmpty(binding.unsplashPickerEditText.text)) {
                    binding.unsplashPickerEditText.setText("")
                }
                binding.unsplashPickerEditText.visibility = View.GONE
                // right clear button on top of edit text gone
                binding.unsplashPickerClearImageView.visibility = View.GONE
                // keyboard down
                binding.unsplashPickerEditText.closeKeyboard(this)
                // action bar with unsplash
                binding.unsplashPickerTitleTextView.text = getString(R.string.unsplash)
                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            UnsplashPickerState.SEARCHING -> {
                // back, cancel, done or search buttons gone
                binding.unsplashPickerBackImageView.visibility = View.GONE
                binding.unsplashPickerCancelImageView.visibility = View.GONE
                binding.unsplashPickerDoneImageView.visibility = View.GONE
                binding.unsplashPickerSearchImageView.visibility = View.GONE
                // edit text visible and focused
                binding.unsplashPickerEditText.visibility = View.VISIBLE
                // right clear button on top of edit text visible
                binding.unsplashPickerClearImageView.visibility = View.VISIBLE
                // keyboard up
                binding.unsplashPickerEditText.requestFocus()
                binding.unsplashPickerEditText.openKeyboard(this)
                // clear list selection
                mAdapter.clearSelection()
                mAdapter.notifyDataSetChanged()
            }
            UnsplashPickerState.PHOTO_SELECTED -> {
                // back and search buttons gone
                binding.unsplashPickerBackImageView.visibility = View.GONE
                binding.unsplashPickerSearchImageView.visibility = View.GONE
                // cancel and done buttons visible
                binding.unsplashPickerCancelImageView.visibility = View.VISIBLE
                binding.unsplashPickerDoneImageView.visibility = View.VISIBLE
                // edit text gone
                binding.unsplashPickerEditText.visibility = View.GONE
                // right clear button on top of edit text gone
                binding.unsplashPickerClearImageView.visibility = View.GONE
                // keyboard down
                binding.unsplashPickerEditText.closeKeyboard(this)
            }
        }
    }

    companion object {
        const val EXTRA_PHOTOS = "EXTRA_PHOTOS"
        private const val EXTRA_IS_MULTIPLE = "EXTRA_IS_MULTIPLE"

        /**
         * @param callingContext the calling context
         * @param isMultipleSelection true if multiple selection, false otherwise
         *
         * @return the intent needed to come to this activity
         */
        fun getStartingIntent(callingContext: Context, isMultipleSelection: Boolean): Intent {
            val intent = Intent(callingContext, UnsplashPickerActivity::class.java)
            intent.putExtra(EXTRA_IS_MULTIPLE, isMultipleSelection)
            return intent
        }
    }
}
