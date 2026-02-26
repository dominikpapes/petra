package com.petra.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.petra.data.Pet
import com.petra.data.PetActivity
import com.petra.viewmodel.PetViewModel
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProfileScreen(viewModel: PetViewModel, navController: NavController) {
    val pets by viewModel.allPets.collectAsState()
    val activities by viewModel.petActivities.collectAsState()
    LaunchedEffect(pets) { viewModel.ensureSelection(pets) }
    val currentPet = pets.find { it.id == viewModel.selectedPetId }
    var showActivitySheet by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<PetActivity?>(null) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (showActivitySheet) {
        AddPetActivitySheet(
            viewModel = viewModel,
            onDismiss = { showActivitySheet = false },
            activityToEdit = activityToEdit
        )
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
                                    activityToEdit = null
                                    showActivitySheet = true
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

                        SmallFloatingActionButton(
                            onClick = {
                                expanded = false
                                navController.navigate("settings")
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
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

                val groupedActivities = activities.groupBy { it.dateTime.year to it.dateTime.monthValue }
                val sortedGroupedActivities = groupedActivities.toSortedMap(
                    compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second }
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (activities.isEmpty()) {
                        item {
                            Text(
                                "No activities for this pet yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        sortedGroupedActivities.forEach { (yearMonth, monthActivities) ->
                            item {
                                val monthName = Month.of(yearMonth.second).getDisplayName(TextStyle.FULL, Locale.getDefault())
                                Text(
                                    text = "$monthName, ${yearMonth.first}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(monthActivities) { activity ->
                                Card(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            activityToEdit = activity
                                            showActivitySheet = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = activity.type, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            if (!activity.description.isNullOrBlank()) {
                                                Text(text = activity.description, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = activity.dateTime.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = activity.dateTime.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
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
