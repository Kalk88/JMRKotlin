package com.kalk.jmr

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kalk.jmr.db.AppDatabase
import com.kalk.jmr.db.genre.Genre
import com.kalk.jmr.db.genre.GenreDao
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenreDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: GenreDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.genreDao()
    }

    @Test
    fun genre_is_added_to_database() {
        val grunge = Genre(1, "grunge")
        dao.addGenre(grunge)
        val queryResult = dao.byId(1)
        assertEquals(queryResult.id, grunge.id)
        assertEquals(queryResult.genre, grunge.genre)
    }
}