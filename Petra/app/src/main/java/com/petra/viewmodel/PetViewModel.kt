package com.petra.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petra.data.Pet
import com.petra.data.PetDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.UUID

class PetViewModel(
    private val dao: PetDao,
    private val context: Context
) : ViewModel() {

    val allPets: StateFlow<List<Pet>> = dao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var selectedPetId by mutableStateOf<Int?>(null)

    fun addPet(name: String, birthday: LocalDate, sourceUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath = sourceUri?.let { uri -> copyImageToInternalStorage(uri) }
            dao.insertPet(Pet(
                name = name.ifBlank { "Unknown Pet" },
                birthdayEpochDay = birthday.toEpochDay(),
                imageUri = internalPath
            ))
        }
    }

    fun updatePet(id: Int, name: String, birthday: LocalDate, sourceUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath = sourceUri?.let { uri -> copyImageToInternalStorage(uri) }
            dao.updatePet(Pet(
                id = id,
                name = name.ifBlank { "Unknown Pet" },
                birthdayEpochDay = birthday.toEpochDay(),
                imageUri = internalPath
            ))
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
            dao.deletePet(pet)
            if (selectedPetId == pet.id) selectedPetId = null
        }
    }

    fun ensureSelection(pets: List<Pet>) {
        if (selectedPetId == null && pets.isNotEmpty()) {
            selectedPetId = pets.first().id
        }
    }
}
