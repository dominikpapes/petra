package com.petra.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.petra.data.ActivityType
import com.petra.data.PetActivity
import com.petra.viewmodel.PetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetActivitySheet(
    viewModel: PetViewModel,
    onDismiss: () -> Unit,
    activityToEdit: PetActivity? = null
) {
    val sheetState = rememberModalBottomSheetState()
    val isEditMode = activityToEdit != null

    var activityType by remember { mutableStateOf(activityToEdit?.type ?: ActivityType.Grooming) }
    var description by remember { mutableStateOf(activityToEdit?.description ?: "") }
    val currentPetId = viewModel.selectedPetId

    var selectedDate by remember { mutableStateOf(activityToEdit?.dateTime?.toLocalDate() ?: LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(activityToEdit?.dateTime?.toLocalTime() ?: LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var activityTypeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (selectedDate == LocalDate.now() && selectedTime.isBefore(LocalTime.now())) {
            selectedTime = LocalTime.now()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(if (isEditMode) "Edit Activity" else "Add New Activity", style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(
                expanded = activityTypeExpanded,
                onExpandedChange = { activityTypeExpanded = !activityTypeExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable),
                    readOnly = true,
                    value = activityType.name,
                    onValueChange = {},
                    label = { Text("Activity Type") },
                    leadingIcon = { Icon(activityType.icon, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityTypeExpanded) },
                )
                ExposedDropdownMenu(
                    expanded = activityTypeExpanded,
                    onDismissRequest = { activityTypeExpanded = false },
                ) {
                    ActivityType.values().forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(type.icon, null, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(type.name)
                                }
                            },
                            onClick = {
                                activityType = type
                                activityTypeExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Date: ${selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}")
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Select Time",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Time: ${selectedTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}")
                }

            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            return utcTimeMillis >= LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }
                    }
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val newSelectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                selectedDate = newSelectedDate
                                if (newSelectedDate == LocalDate.now() && selectedTime.isBefore(LocalTime.now())) {
                                    selectedTime = LocalTime.now()
                                }
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedTime.hour,
                    initialMinute = selectedTime.minute,
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val pickedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                if (selectedDate == LocalDate.now() && pickedTime.isBefore(LocalTime.now())) {
                                    selectedTime = LocalTime.now()
                                } else {
                                    selectedTime = pickedTime
                                }
                                showTimePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TimeInput(state = timePickerState)
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    Button(
                        onClick = {
                            activityToEdit?.let {
                                viewModel.deletePetActivity(it)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("Delete")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    val dateTime = selectedDate.atTime(selectedTime)
                    if (isEditMode) {
                        activityToEdit?.let {
                            val updatedActivity = it.copy(
                                type = activityType,
                                description = description,
                                dateTime = dateTime
                            )
                            viewModel.updatePetActivity(updatedActivity)
                            onDismiss()
                        }
                    } else {
                        currentPetId?.let { petId ->
                            viewModel.addPetActivity(petId, activityType, description, dateTime)
                            onDismiss()
                        }
                    }
                }) {
                    Text(if (isEditMode) "Save Changes" else "Save Activity")
                }
            }
        }
    }
}
