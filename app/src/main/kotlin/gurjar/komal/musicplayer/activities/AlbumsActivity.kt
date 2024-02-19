package gurjar.komal.musicplayer.activities

import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.fossify.commons.dialogs.PermissionRequiredDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.helpers.ensureBackgroundThread
import gurjar.komal.musicplayer.R
import gurjar.komal.musicplayer.adapters.AlbumsTracksAdapter
import gurjar.komal.musicplayer.databinding.ActivityAlbumsBinding
import gurjar.komal.musicplayer.extensions.audioHelper
import gurjar.komal.musicplayer.helpers.ALBUM
import gurjar.komal.musicplayer.helpers.ARTIST
import gurjar.komal.musicplayer.models.*

// Artists -> Albums -> Tracks
class AlbumsActivity : SimpleMusicActivity() {

    private val binding by viewBinding(ActivityAlbumsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateMaterialActivityViews(binding.albumsCoordinator, binding.albumsHolder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.albumsList, binding.albumsToolbar)

        binding.albumsFastscroller.updateColors(getProperPrimaryColor())

        val artistType = object : TypeToken<Artist>() {}.type
        val artist = Gson().fromJson<Artist>(intent.getStringExtra(ARTIST), artistType)
        binding.albumsToolbar.title = artist.title

        ensureBackgroundThread {
            val albums = audioHelper.getArtistAlbums(artist.id)
            val listItems = ArrayList<ListItem>()
            val albumsSectionLabel = resources.getQuantityString(R.plurals.albums_plural, albums.size, albums.size)
            listItems.add(AlbumSection(albumsSectionLabel))
            listItems.addAll(albums)

            val albumTracks = audioHelper.getAlbumTracks(albums)
            val trackFullDuration = albumTracks.sumOf { it.duration }

            var tracksSectionLabel = resources.getQuantityString(R.plurals.tracks_plural, albumTracks.size, albumTracks.size)
            tracksSectionLabel += " • ${trackFullDuration.getFormattedDuration(true)}"
            listItems.add(AlbumSection(tracksSectionLabel))
            listItems.addAll(albumTracks)

            runOnUiThread {
                AlbumsTracksAdapter(this, listItems, binding.albumsList) {
                    hideKeyboard()
                    if (it is Album) {
                        Intent(this, TracksActivity::class.java).apply {
                            putExtra(ALBUM, Gson().toJson(it))
                            startActivity(this)
                        }
                    } else {
                        handleNotificationPermission { granted ->
                            if (granted) {
                                val startIndex = albumTracks.indexOf(it as Track)
                                prepareAndPlay(albumTracks, startIndex)
                            } else {
                                PermissionRequiredDialog(
                                    this,
                                    org.fossify.commons.R.string.allow_notifications_music_player,
                                    { openNotificationSettings() }
                                )
                            }
                        }
                    }
                }.apply {
                    binding.albumsList.adapter = this
                }

                if (areSystemAnimationsEnabled) {
                    binding.albumsList.scheduleLayoutAnimation()
                }
            }
        }

        setupCurrentTrackBar(binding.currentTrackBar.root)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.albumsToolbar, NavigationIcon.Arrow)
    }
}
