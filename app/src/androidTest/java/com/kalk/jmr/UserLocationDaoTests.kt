package com.kalk.jmr

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.kalk.jmr.db.AppDatabase
import com.kalk.jmr.db.location.Coordinates
import com.kalk.jmr.db.location.UserLocation
import com.kalk.jmr.db.location.UserLocationDao
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserLocationDaoTests {
    private lateinit var db: AppDatabase
    private lateinit var dao: UserLocationDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.locationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun location_is_added_to_database() {
        val location = UserLocation("UUID", "SymbolicLOCATION",Coordinates(1.0, 2.0))
        dao.addLocation(location)
        val queryResult = dao.byId("UUID")
        assertEquals(queryResult.id, location.id)
        assertEquals(0, queryResult.coordinates.longitude.compareTo(location.coordinates.longitude))
        assertEquals(0, queryResult.coordinates.latitude.compareTo(location.coordinates.latitude))
    }

    @Test
    fun will_not_add_location_with_same_coordinates_twice_to_db() {
        val loc1 = UserLocation("UUID","SymbolicLOCATION", Coordinates(1.0, 2.0))
        val loc2 = UserLocation("UUID2", "SymbolicLOCATION", Coordinates(1.0, 2.0))
        dao.addLocation(loc1)
        dao.addLocation(loc2)
        val queryResult = dao.byId("UUID")
        assertEquals(queryResult.id, loc1.id)
        assertEquals(0, queryResult.coordinates.longitude.compareTo(loc1.coordinates.longitude))
        assertEquals(0, queryResult.coordinates.latitude.compareTo(loc1.coordinates.latitude))
        val queryResult2 = dao.byId("UUID2")
        assertEquals(queryResult2, null)
    }

    @Test
    fun finds_userLocation_by_coordinates() {
        val loc1 = UserLocation("UUID", "SymbolicLOCATION", Coordinates(1.0, 2.0))
        dao.addLocation(loc1)
        val queryResult = dao.inRangeOfCoordinates(0.0, 0.0, 2.0, 2.0)
        assertEquals(queryResult.id, loc1.id)
        assertEquals(0, queryResult.coordinates.longitude.compareTo(loc1.coordinates.longitude))
        assertEquals(0, queryResult.coordinates.latitude.compareTo(loc1.coordinates.latitude))
    }

}