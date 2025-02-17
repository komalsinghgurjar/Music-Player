package gurjar.komal.musicplayer.models

import org.fossify.commons.helpers.AlphanumericComparator
import org.fossify.commons.helpers.SORT_DESCENDING
import gurjar.komal.musicplayer.extensions.sortSafely
import gurjar.komal.musicplayer.helpers.PLAYER_SORT_BY_TITLE

data class Folder(val title: String, val trackCount: Int, val path: String) {
    companion object {
        fun getComparator(sorting: Int) = Comparator<Folder> { first, second ->
            var result = when {
                sorting and PLAYER_SORT_BY_TITLE != 0 -> AlphanumericComparator().compare(first.title.lowercase(), second.title.lowercase())
                else -> first.trackCount.compareTo(second.trackCount)
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }

            return@Comparator result
        }
    }

    fun getBubbleText(sorting: Int) = when {
        sorting and PLAYER_SORT_BY_TITLE != 0 -> title
        else -> trackCount.toString()
    }
}

fun ArrayList<Folder>.sortSafely(sorting: Int) = sortSafely(Folder.getComparator(sorting))
