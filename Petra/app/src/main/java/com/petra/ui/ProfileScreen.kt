package com.petra.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.petra.data.Pet
import com.petra.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: PetViewModel, navController: NavController) {
    val pets by viewModel.allPets.collectAsState()
    LaunchedEffect(pets) { viewModel.ensureSelection(pets) }
    val currentPet = pets.find { it.id == viewModel.selectedPetId }

    Scaffold(
        topBar = {
            if (currentPet != null) {
                TopAppBar(
                    title = { },
                    actions = {
                        IconButton(onClick = { viewModel.deletePet(currentPet) }) {
                            Icon(Icons.Default.Delete, "Delete Pet", tint = Color.Red)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create") }) {
                Icon(Icons.Default.Add, "Add Pet")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (pets.isNotEmpty()) {
                PetDropdown(
                    pets = pets,
                    selectedPet = currentPet,
                    onPetSelected = { viewModel.selectedPetId = it.id }
                )
            } else {
                Text("No pets yet. Click + to add one!", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (currentPet != null) {
                Box(
                    modifier = Modifier.size(200.dp).clip(CircleShape).background(Color(0xFF3F51B5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPet.imageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentPet.imageUri).crossfade(true).build(),
                            contentDescription = "Pet Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Pets, null, tint = Color.White, modifier = Modifier.size(100.dp))
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoCard(text = "Age: ${currentPet.getAge()}")
                    InfoCard(text = "Birthday: ${currentPet.getFormattedDate()}")
                }
            }
        }
    }
}

@Composable
fun PetDropdown(pets: List<Pet>, selectedPet: Pet?, onPetSelected: (Pet) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.clickable { expanded = true }.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedPet?.name ?: "Select Pet", style = MaterialTheme.typography.displaySmall)
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(32.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            pets.forEach { pet ->
                DropdownMenuItem(
                    text = { Text(pet.name) },
                    onClick = { onPetSelected(pet); expanded = false }
                )
            }
        }
    }
}

@Composable
fun InfoCard(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = text, modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold
        )
    }
}
