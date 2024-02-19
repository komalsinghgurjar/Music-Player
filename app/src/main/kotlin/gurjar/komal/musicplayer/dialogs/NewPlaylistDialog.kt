package gurjar.komal.musicplayer.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.ensureBackgroundThread
import gurjar.komal.musicplayer.R
import gurjar.komal.musicplayer.databinding.DialogNewPlaylistBinding
import gurjar.komal.musicplayer.extensions.audioHelper
import gurjar.komal.musicplayer.extensions.getPlaylistIdWithTitle
import gurjar.komal.musicplayer.models.Playlist

class NewPlaylistDialog(val activity: Activity, var playlist: Playlist? = null, val callback: (playlistId: Int) -> Unit) {
    private var isNewPlaylist = playlist == null
    private val binding by activity.viewBinding(DialogNewPlaylistBinding::inflate)

    init {
        if (playlist == null) {
            playlist = Playlist(0, "")
        }

        binding.newPlaylistTitle.setText(playlist!!.title)
        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok, null)
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .apply {
                val dialogTitle = if (isNewPlaylist) R.string.create_new_playlist else R.string.rename_playlist
                activity.setupDialogStuff(binding.root, this, dialogTitle) { alertDialog ->
                    alertDialog.showKeyboard(binding.newPlaylistTitle)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val title = binding.newPlaylistTitle.value
                        ensureBackgroundThread {
                            val playlistIdWithTitle = activity.getPlaylistIdWithTitle(title)
                            var isPlaylistTitleTaken = isNewPlaylist && playlistIdWithTitle != -1
                            if (!isPlaylistTitleTaken) {
                                isPlaylistTitleTaken = !isNewPlaylist && playlist!!.id != playlistIdWithTitle && playlistIdWithTitle != -1
                            }

                            if (title.isEmpty()) {
                                activity.toast(org.fossify.commons.R.string.empty_name)
                                return@ensureBackgroundThread
                            } else if (isPlaylistTitleTaken) {
                                activity.toast(R.string.playlist_name_exists)
                                return@ensureBackgroundThread
                            }

                            playlist!!.title = title

                            val eventTypeId = if (isNewPlaylist) {
                                activity.audioHelper.insertPlaylist(playlist!!).toInt()
                            } else {
                                activity.audioHelper.updatePlaylist(playlist!!)
                                playlist!!.id
                            }

                            if (eventTypeId != -1) {
                                alertDialog.dismiss()
                                callback(eventTypeId)
                            } else {
                                activity.toast(org.fossify.commons.R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
    }
}
