package gurjar.komal.musicplayer.models

class Events {
    class SleepTimerChanged(val seconds: Int)
    class PlaylistsUpdated
    class RefreshFragments
    class RefreshTracks
}
