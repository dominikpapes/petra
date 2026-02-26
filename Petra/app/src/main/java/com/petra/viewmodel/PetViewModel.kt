package com.petra.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petra.data.Pet
import com.petra.data.PetActivity
import com.petra.data.PetActivityDao
import com.petra.data.PetDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class PetViewModel(
    private val petDao: PetDao,
    private val petActivityDao: PetActivityDao,
    private val context: Context
) : ViewModel() {

    val allPets: StateFlow<List<Pet>> = petDao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var selectedPetId by mutableStateOf<Int?>(null)

    val petActivities: StateFlow<List<PetActivity>> = snapshotFlow { selectedPetId }
        .flatMapLatest { petId ->
            if (petId != null) {
                petActivityDao.getActivitiesForPet(petId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addPet(name: String, birthday: LocalDate, sourceUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath = sourceUri?.let { uri -> copyImageToInternalStorage(uri) }
            petDao.insertPet(Pet(
                name = name.ifBlank { "Unknown Pet" },
                birthdayEpochDay = birthday.toEpochDay(),
                imageUri = internalPath
            ))
        }
    }

    fun addPetActivity(petId: Int, type: String, description: String?, dateTime: LocalDateTime) {
        viewModelScope.launch(Dispatchers.IO) {
            petActivityDao.insertActivity(
                PetActivity(
                    petId = petId,
                    type = type,
                    description = description,
                    dateTime = dateTime
                )
            )
        }
    }

    fun updatePet(id: Int, name: String, birthday: LocalDate, sourceUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPet = petDao.getPetById(id)
            val imagePath = if (sourceUri?.scheme == "content") {
                // A new image has been selected, so copy it and delete the old one.
                val newPath = copyImageToInternalStorage(sourceUri)
                currentPet?.imageUri?.let { oldPath ->
                    val oldFile = File(oldPath)
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }
                }
                newPath
            } else {
                // No new image selected, so keep the old path.
                currentPet?.imageUri
            }

            val updatedPet = Pet(
                id = id,
                name = name.ifBlank { "Unknown Pet" },
                birthdayEpochDay = birthday.toEpochDay(),
                imageUri = imagePath
            )
            petDao.updatePet(updatedPet)
        }
    }

    fun updatePetActivity(activity: PetActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            petActivityDao.updateActivity(activity)
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "pet_${UUID.randomUUID()}.jpg"
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputDir = File(context.filesDir, "pet_images")
            if (!outputDir.exists()) outputDir.mkdirs()

            val outputFile = File(outputDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            inputStream?.use { input ->
                outputStream.use { output -> input.copyTo(output) }
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePet(pet: Pet) {
        viewModelScope.launch(Dispatchers.IO) {
            pet.imageUri?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            petDao.deletePet(pet)
            if (selectedPetId == pet.id) selectedPetId = null
        }
    }

    fun deletePetActivity(activity: PetActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            petActivityDao.deleteActivity(activity)
        }
    }

    fun ensureSelection(pets: List<Pet>) {
        if (selectedPetId == null && pets.isNotEmpty()) {
            selectedPetId = pets.first().id
        }
    }
}
