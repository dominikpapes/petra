package com.petra.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "pet_activities",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PetActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val petId: Int,
    val type: String,
    val description: String?,
    val dateTime: LocalDateTime
)
