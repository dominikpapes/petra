package com.petra.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetActivityDao {
    @Query("SELECT * FROM pet_activities WHERE petId = :petId ORDER BY dateTime DESC")
    fun getActivitiesForPet(petId: Int): Flow<List<PetActivity>>

    @Insert
    suspend fun insertActivity(activity: PetActivity)
}
