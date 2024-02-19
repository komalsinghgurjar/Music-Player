package gurjar.komal.musicplayer.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import org.fossify.commons.dialogs.ColorPickerDialog
import org.fossify.commons.dialogs.FeatureLockedDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.IS_CUSTOMIZING_COLORS
import gurjar.komal.musicplayer.R
import gurjar.komal.musicplayer.databinding.WidgetConfigBinding
import gurjar.komal.musicplayer.extensions.config
import gurjar.komal.musicplayer.helpers.MyWidgetProvider
import gurjar.komal.musicplayer.playback.PlaybackService

class WidgetConfigureActivity : SimpleActivity() {
    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColor = 0
    private var mTextColor = 0
    private var mBgColorWithoutTransparency = 0
    private var mFeatureLockedDialog: FeatureLockedDialog? = null

    private val binding by viewBinding(WidgetConfigBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(binding.root)
        initVariables()

        val isCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        mWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID && !isCustomizingColors) {
            finish()
        }

        binding.configSave.setOnClickListener { saveConfig() }
        binding.configBgColor.setOnClickListener { pickBackgroundColor() }
        binding.configTextColor.setOnClickListener { pickTextColor() }

        val primaryColor = getProperPrimaryColor()
        binding.configBgSeekbar.setColors(mTextColor, primaryColor, primaryColor)
        binding.configPlayer.apply {
            val currSong = PlaybackService.currentMediaItem?.mediaMetadata
            if (currSong != null) {
                songInfoTitle.text = currSong.title
                songInfoArtist.text = currSong.artist
            } else {
                songInfoTitle.text = getString(org.fossify.commons.R.string.artist)
                songInfoArtist.text = getString(org.fossify.commons.R.string.song_title)
            }
        }

        if (!isCustomizingColors && !isOrWasThankYouInstalled()) {
            mFeatureLockedDialog = FeatureLockedDialog(this) {
                if (!isOrWasThankYouInstalled()) {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mFeatureLockedDialog != null && isOrWasThankYouInstalled()) {
            mFeatureLockedDialog?.dismissDialog()
        }
    }

    private fun initVariables() {
        mBgColor = config.widgetBgColor
        mBgAlpha = Color.alpha(mBgColor) / 255.toFloat()

        mBgColorWithoutTransparency = Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))
        binding.configBgSeekbar.progress = (mBgAlpha * 100).toInt()
        updateBackgroundColor()
        binding.configBgSeekbar.onSeekBarChangeListener { progress ->
            mBgAlpha = progress / 100.toFloat()
            updateBackgroundColor()
        }

        mTextColor = config.widgetTextColor
        if (mTextColor == resources.getColor(org.fossify.commons.R.color.default_widget_text_color) && config.isUsingSystemTheme) {
            mTextColor = resources.getColor(org.fossify.commons.R.color.you_primary_color, theme)
        }

        updateTextColor()
    }

    private fun saveConfig() {
        val appWidgetManager = AppWidgetManager.getInstance(this) ?: return
        val views = RemoteViews(packageName, R.layout.widget).apply {
            applyColorFilter(R.id.widget_background, mBgColor)
        }

        appWidgetManager.updateAppWidget(mWidgetId, views)

        storeWidgetColors()
        requestWidgetUpdate()

        if (config.initialWidgetHeight == 0) {
            config.widgetIdToMeasure = mWidgetId
        }

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun storeWidgetColors() {
        config.apply {
            widgetBgColor = mBgColor
            widgetTextColor = mTextColor
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateBackgroundColor() = binding.apply {
        mBgColor = mBgColorWithoutTransparency.adjustAlpha(mBgAlpha)
        configPlayer.widgetBackground.applyColorFilter(mBgColor)
        configBgColor.setFillWithStroke(mBgColor, mBgColor)
        configSave.backgroundTintList = ColorStateList.valueOf(getProperPrimaryColor())
    }

    private fun updateTextColor() = binding.apply {
        configTextColor.setFillWithStroke(mTextColor, mTextColor)

        configPlayer.songInfoTitle.setTextColor(mTextColor)
        configPlayer.songInfoArtist.setTextColor(mTextColor)
        configSave.setTextColor(getProperPrimaryColor().getContrastColor())

        configPlayer.widgetControls.previousBtn.drawable.applyColorFilter(mTextColor)
        configPlayer.widgetControls.playPauseBtn.drawable.applyColorFilter(mTextColor)
        configPlayer.widgetControls.nextBtn.drawable.applyColorFilter(mTextColor)
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, mBgColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mBgColorWithoutTransparency = color
                updateBackgroundColor()
            }
        }
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, mTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mTextColor = color
                updateTextColor()
            }
        }
    }
}
