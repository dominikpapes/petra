package com.petra.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    val activities by viewModel.petActivities.collectAsState()
    LaunchedEffect(pets) { viewModel.ensureSelection(pets) }
    val currentPet = pets.find { it.id == viewModel.selectedPetId }
    var showAddActivitySheet by remember { mutableStateOf(false) }

    if (showAddActivitySheet) {
        AddPetActivitySheet(viewModel = viewModel, onDismiss = { showAddActivitySheet = false })
    }

    Scaffold(
        floatingActionButton = {
            var expanded by remember { mutableStateOf(false) }

            val rotation by animateFloatAsState(
                targetValue = if (expanded) 45f else 0f,
                label = "fab_rotation"
            )

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                expanded = false
                                navController.navigate("create")
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Pet")
                        }

                        if (pets.isNotEmpty()) {
                            SmallFloatingActionButton(
                                onClick = {
                                    expanded = false
                                    showAddActivitySheet = true
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Add Activity")
                            }

                            SmallFloatingActionButton(
                                onClick = {
                                    expanded = false
                                    navController.navigate("edit")
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Pet")
                            }
                        }

                    }
                }

                FloatingActionButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = if (expanded) "Close menu" else "Expand menu",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F51B5)),
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

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(activities) { activity ->
                        Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = activity.type, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                if (activity.description != null) {
                                    Text(text = activity.description, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(text = activity.dateTime.toString(), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
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
