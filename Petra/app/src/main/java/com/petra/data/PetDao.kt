package com.petra.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets")
    fun getAllPets(): Flow<List<Pet>>

    @Insert
    fun insertPet(pet: Pet): Long

    @Delete
    fun deletePet(pet: Pet)
}
