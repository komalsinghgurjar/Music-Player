package gurjar.komal.musicplayer.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.google.gson.Gson
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.areSystemAnimationsEnabled
import org.fossify.commons.extensions.beGoneIf
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.helpers.ensureBackgroundThread
import gurjar.komal.musicplayer.R
import gurjar.komal.musicplayer.activities.AlbumsActivity
import gurjar.komal.musicplayer.activities.SimpleActivity
import gurjar.komal.musicplayer.adapters.ArtistsAdapter
import gurjar.komal.musicplayer.databinding.FragmentArtistsBinding
import gurjar.komal.musicplayer.dialogs.ChangeSortingDialog
import gurjar.komal.musicplayer.extensions.audioHelper
import gurjar.komal.musicplayer.extensions.config
import gurjar.komal.musicplayer.extensions.mediaScanner
import gurjar.komal.musicplayer.extensions.viewBinding
import gurjar.komal.musicplayer.helpers.ARTIST
import gurjar.komal.musicplayer.helpers.TAB_ARTISTS
import gurjar.komal.musicplayer.models.Artist
import gurjar.komal.musicplayer.models.sortSafely

// Artists -> Albums -> Tracks
class ArtistsFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private var artists = ArrayList<Artist>()
    private val binding by viewBinding(FragmentArtistsBinding::bind)

    override fun setupFragment(activity: BaseSimpleActivity) {
        ensureBackgroundThread {
            val cachedArtists = activity.audioHelper.getAllArtists()
            activity.runOnUiThread {
                gotArtists(activity, cachedArtists)
            }
        }
    }

    private fun gotArtists(activity: BaseSimpleActivity, cachedArtists: ArrayList<Artist>) {
        artists = cachedArtists
        activity.runOnUiThread {
            val scanning = activity.mediaScanner.isScanning()
            binding.artistsPlaceholder.text = if (scanning) {
                context.getString(R.string.loading_files)
            } else {
                context.getString(org.fossify.commons.R.string.no_items_found)
            }
            binding.artistsPlaceholder.beVisibleIf(artists.isEmpty())

            val adapter = binding.artistsList.adapter
            if (adapter == null) {
                ArtistsAdapter(activity, artists, binding.artistsList) {
                    activity.hideKeyboard()
                    Intent(activity, AlbumsActivity::class.java).apply {
                        putExtra(ARTIST, Gson().toJson(it as Artist))
                        activity.startActivity(this)
                    }
                }.apply {
                    binding.artistsList.adapter = this
                }

                if (context.areSystemAnimationsEnabled) {
                    binding.artistsList.scheduleLayoutAnimation()
                }
            } else {
                val oldItems = (adapter as ArtistsAdapter).items
                if (oldItems.sortedBy { it.id }.hashCode() != artists.sortedBy { it.id }.hashCode()) {
                    adapter.updateItems(artists)
                }
            }
        }
    }

    override fun finishActMode() {
        getAdapter()?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = artists.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<Artist>
        getAdapter()?.updateItems(filtered, text)
        binding.artistsPlaceholder.beVisibleIf(filtered.isEmpty())
    }

    override fun onSearchClosed() {
        getAdapter()?.updateItems(artists)
        binding.artistsPlaceholder.beGoneIf(artists.isNotEmpty())
    }

    override fun onSortOpen(activity: SimpleActivity) {
        ChangeSortingDialog(activity, TAB_ARTISTS) {
            val adapter = getAdapter() ?: return@ChangeSortingDialog
            artists.sortSafely(activity.config.artistSorting)
            adapter.updateItems(artists, forceUpdate = true)
        }
    }

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        binding.artistsPlaceholder.setTextColor(textColor)
        binding.artistsFastscroller.updateColors(adjustedPrimaryColor)
        getAdapter()?.updateColors(textColor)
    }

    private fun getAdapter() = binding.artistsList.adapter as? ArtistsAdapter
}
