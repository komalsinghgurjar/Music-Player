package gurjar.komal.musicplayer.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gurjar.komal.musicplayer.models.Genre

@Dao
interface GenresDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(genre: Genre): Long

    @Query("SELECT * FROM genres")
    fun getAll(): List<Genre>

    @Query("DELETE FROM genres WHERE id = :id")
    fun deleteGenre(id: Long)
}
