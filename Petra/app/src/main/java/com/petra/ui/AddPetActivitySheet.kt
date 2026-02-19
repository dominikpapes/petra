package com.petra.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.petra.viewmodel.PetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetActivitySheet(viewModel: PetViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var activityType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val currentPetId = viewModel.selectedPetId

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
            Text("Add New Activity", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            OutlinedTextField(value = activityType, onValueChange = { activityType = it }, label = { Text("Activity Type") })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") })

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}",
                    modifier = Modifier.clickable { showDatePicker = true }
                )
                Text(
                    text = "Time: ${selectedTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}",
                    modifier = Modifier.clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedTime = LocalTime.of(hour, minute)
                            },
                            selectedTime.hour,
                            selectedTime.minute,
                            false
                        ).show()
                    }
                )
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = Instant.now().toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
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

            Button(onClick = {
                if (currentPetId != null) {
                    val dateTime = selectedDate.atTime(selectedTime)
                    viewModel.addPetActivity(currentPetId, activityType, description, dateTime)
                    onDismiss()
                }
            }) {
                Text("Save Activity")
            }
        }
    }
}
