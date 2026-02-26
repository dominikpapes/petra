package com.petra.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.vector.ImageVector

enum class ActivityType(val icon: ImageVector) {
    Grooming(Icons.Filled.ContentCut),
    Veterinary(Icons.Filled.LocalHospital),
    Vaccine(Icons.Filled.Science)
}