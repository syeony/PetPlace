package com.example.petplace.presentation.feature.missing_report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petplace.R // Ensure this R points to your resources
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController
) {
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.of(2024, 1, 15)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(14, 30)) }
    var selectedLocation by remember { mutableStateOf("경상북도 구미시 인의동 365-5") } // Default value from image
    val locations = listOf(
        "경상북도 구미시 인의동 365-5",
        "서울시 강남구 테헤란로 123",
        "부산시 해운대구 마린시티 777"
    )
    var expanded by remember { mutableStateOf(false) } // For dropdown menu

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "목격 제보",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Placeholder for "img" text if needed, otherwise can remove
                    Text(
                        text = "img",
                        fontSize = 16.sp,
                        color = Color.Transparent, // Make it transparent if not actively used
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    // 작성 완료 시
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), // Orange color from image
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "작성 완료", color = Color.White, fontSize = 16.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Image selection section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)) // Light gray background
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { /* Handle image selection */ },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_photo_camera_24), // Replace with your camera icon
                            contentDescription = "Upload Image",
                            modifier = Modifier.size(36.dp),
                            colorFilter = ColorFilter.tint(Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "1 / 5", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                // Placeholder images
                Image(
                    painter = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24), // Replace with your hamster image
                    contentDescription = "Hamster 1",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray), // Placeholder background
                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24), // Replace with your hamster image
                    contentDescription = "Hamster 2",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray), // Placeholder background
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "한마리의 동물만 나오게 해주세요.\n얼굴이 잘 나온 사진을 등록해주세요.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description TextField
            TextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp) // Adjust height as per design
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = {
                    Text(
                        "목격 장소, 상황, 특징 등을 작성해주세요. 애타게 찾고 있는 집사님께 큰 도움이 됩니다.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF3F4F6), // Light gray background
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Witness Date and Time
            Text(
                text = "목격 일시",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date picker (simplified for UI, actual date picker would be more complex)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { /* Open Date Picker */ },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                        modifier = Modifier.padding(start = 12.dp),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
                // Time picker (simplified for UI, actual time picker would be more complex)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { /* Open Time Picker */ },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = selectedTime.format(DateTimeFormatter.ofPattern("a HH:mm", Locale.KOREAN)),
                        modifier = Modifier.padding(start = 12.dp),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Witness Location
            Text(
                text = "목격 장소",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedLocation,
                    onValueChange = {}, // Read-only for dropdown
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor() // This is important for ExposedDropdownMenuBox
                        .clip(RoundedCornerShape(8.dp)),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown Arrow"
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF3F4F6),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location) },
                            onClick = {
                                selectedLocation = location
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "작성 완료 후에는 장소를 변경할 수 없어요",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Preview for ReportScreen
//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true, widthDp = 360)
//@Composable
//fun ReportScreenPreview() {
//    ReportScreen(navController = TODO())
//}

// Dummy drawables for the preview
// You should replace these with your actual drawable resources in your project
// R.drawable.ic_camera_placeholder
// R.drawable.hamster_placeholder
// For preview to work, ensure these exist in your res/drawable folder
/*
Example of how you might add dummy drawables for preview if they don't exist:
In res/drawable/ic_camera_placeholder.xml:
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M12,12m-3.2,0a3.2,3.2 0,1 1,6.4 0a3.2,3.2 0,1 1,-6.4 0" />
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M9,2L7.17,4L4,4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2L22,6c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2L9,2zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z" />
</vector>

In res/drawable/hamster_placeholder.xml (simple placeholder for demonstration):
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFC107"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,12m-3,0a3,3 0,1 1,6 0a3,3 0,1 1,-6 0" />
    <path
        android:fillColor="#FF5722"
        android:pathData="M12,9.5c0.83,0 1.5,0.67 1.5,1.5s-0.67,1.5 -1.5,1.5 -1.5,-0.67 -1.5,-1.5 0.67,-1.5 1.5,-1.5z" />
</vector>
*/