package gurjar.komal.musicplayer.fragments

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.fossify.commons.activities.BaseSimpleActivity
import gurjar.komal.musicplayer.activities.SimpleActivity
import gurjar.komal.musicplayer.activities.SimpleControllerActivity
import gurjar.komal.musicplayer.models.Track

abstract class MyViewPagerFragment(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {
    abstract fun setupFragment(activity: BaseSimpleActivity)

    abstract fun finishActMode()

    abstract fun onSearchQueryChanged(text: String)

    abstract fun onSearchClosed()

    abstract fun onSortOpen(activity: SimpleActivity)

    abstract fun setupColors(textColor: Int, adjustedPrimaryColor: Int)

    fun prepareAndPlay(tracks: List<Track>, startIndex: Int = 0, startPositionMs: Long = 0, startActivity: Boolean = true) {
        (context as SimpleControllerActivity).prepareAndPlay(tracks, startIndex, startPositionMs, startActivity)
    }
}
