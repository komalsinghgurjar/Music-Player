package gurjar.komal.musicplayer.extensions

import android.os.Bundle
import androidx.media3.session.MediaController
import gurjar.komal.musicplayer.playback.CustomCommands

fun MediaController.sendCommand(command: CustomCommands, extras: Bundle = Bundle.EMPTY) = sendCustomCommand(command.sessionCommand, extras)
