package com.petra.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val birthdayEpochDay: Long,
    val imageUri: String?
) {
    fun getAge(): Int {
        val birthDate = LocalDate.ofEpochDay(birthdayEpochDay)
        val currentDate = LocalDate.now()
        return Period.between(birthDate, currentDate).years
    }

    fun getFormattedDate(): String {
        val date = LocalDate.ofEpochDay(birthdayEpochDay)
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yy"))
    }
}
