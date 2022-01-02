package com.simplemobiletools.musicplayer.fragments

import android.content.Context
import android.util.AttributeSet
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.musicplayer.R
import com.simplemobiletools.musicplayer.activities.SimpleActivity
import com.simplemobiletools.musicplayer.adapters.FoldersAdapter
import com.simplemobiletools.musicplayer.extensions.config
import com.simplemobiletools.musicplayer.extensions.getAlbumTracksSync
import com.simplemobiletools.musicplayer.extensions.getAlbumsSync
import com.simplemobiletools.musicplayer.extensions.getArtistsSync
import com.simplemobiletools.musicplayer.models.Album
import com.simplemobiletools.musicplayer.models.Folder
import com.simplemobiletools.musicplayer.models.Track
import kotlinx.android.synthetic.main.fragment_folders.view.*

class FoldersFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    override fun setupFragment(activity: SimpleActivity) {
        val albums = ArrayList<Album>()
        val artists = context.getArtistsSync()
        artists.forEach { artist ->
            albums.addAll(context.getAlbumsSync(artist))
        }

        val tracks = ArrayList<Track>()
        albums.forEach {
            tracks.addAll(context.getAlbumTracksSync(it.id))
        }

        Track.sorting = context.config.trackSorting
        tracks.sort()

        activity.runOnUiThread {
            folders_placeholder.text = context.getString(R.string.no_items_found)
            folders_placeholder.beVisibleIf(tracks.isEmpty())
            val foldersMap = tracks.groupBy { it.folderName }
            val folders = ArrayList<Folder>()
            for ((title, folderTracks) in foldersMap) {
                val folder = Folder(title, folderTracks.size)
                folders.add(folder)
            }

            Folder.sorting = activity.config.folderSorting
            folders.sort()

            val adapter = folders_list.adapter
            if (adapter == null) {
                FoldersAdapter(activity, folders, folders_list) {

                }.apply {
                    folders_list.adapter = this
                }

                if (context.areSystemAnimationsEnabled) {
                    folders_list.scheduleLayoutAnimation()
                }
            } else {
                (adapter as FoldersAdapter).updateItems(folders)
            }
        }
    }

    override fun finishActMode() {
        (folders_list.adapter as? MyRecyclerViewAdapter)?.finishActMode()
    }

    override fun onSearchQueryChanged(text: String) {}

    override fun onSearchOpened() {}

    override fun onSearchClosed() {}

    override fun onSortOpen(activity: SimpleActivity) {}

    override fun setupColors(textColor: Int, adjustedPrimaryColor: Int) {
        folders_placeholder.setTextColor(textColor)
        folders_fastscroller.updateColors(adjustedPrimaryColor)
    }
}
