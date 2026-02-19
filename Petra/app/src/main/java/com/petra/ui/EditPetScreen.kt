package com.petra.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.petra.viewmodel.PetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(viewModel: PetViewModel, navController: NavController) {
    val pets by viewModel.allPets.collectAsState()
    val currentPet = pets.find { it.id == viewModel.selectedPetId }
    val activities by viewModel.petActivities.collectAsState()

    var name by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(currentPet) {
        currentPet?.let {
            name = it.name
            imageUri = it.imageUri?.let { Uri.parse(it) }
            selectedDate = LocalDate.ofEpochDay(it.birthdayEpochDay)
        }
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Edit Pet") },
            actions = {
                IconButton(onClick = {
                    currentPet?.let {
                        viewModel.deletePet(it)
                        navController.popBackStack()
                    }
                }) {
                    Icon(Icons.Default.Delete, "Delete Pet", tint = Color.Red)
                }
            }
        ) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.LightGray)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Add, "Add Photo", tint = Color.White)
                }
            }
            Text("Tap to change photo", fontSize = 12.sp, color = Color.Gray)

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Pet Name") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                onValueChange = { }, label = { Text("Birthday") }, readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.clickable { showDatePicker = true })
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    currentPet?.let {
                        viewModel.updatePet(it.id, name, selectedDate, imageUri)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            Text("Activities", style = MaterialTheme.typography.headlineSmall)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(activities) { activity ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activity.type, style = MaterialTheme.typography.bodyLarge)
                            Text(activity.description ?: "", style = MaterialTheme.typography.bodyMedium)
                            Text(activity.dateTime.format(DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.SHORT)), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.deletePetActivity(activity) }) {
                            Icon(Icons.Default.Delete, "Delete Activity")
                        }
                    }
                }
            }
        }
    }
}
