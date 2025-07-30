package com.example.petplace.presentation.feature.missing_report

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingMapScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "지도에서 위치 확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Adjust color if needed
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back click */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle search click */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White // Set top app bar background to white
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // White background for the bottom button area
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { /* Handle set location click */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF25A3F)), // Red-orange color from image
                    shape = RoundedCornerShape(8.dp) // Slightly rounded corners
                ) {
                    Text(
                        text = "이 위치로 설정",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Simulate the map image (replace with actual map SDK in a real app)
            // You would need to add your map image to your drawable resources.
            // For example, in res/drawable/map_placeholder.png
            Image(
                painter = painterResource(id = R.drawable.ic_map), // Replace with your map image resource
                contentDescription = "Map",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Or ContentScale.FillBounds depending on your image
            )

            // Location Selection Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Assuming you have a drawable for the dog icon, e.g., R.drawable.dog_icon
                    Icon(
                        painter = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24), // Replace with your dog icon resource
                        contentDescription = "Dog icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFF25A3F) // Assuming the dog icon has a similar color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "지도를 움직여 위치를 설정하세요.",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { /* Handle close click */ }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }

            // Target location marker (red flag and address bubble)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-30).dp) // Adjust to place the bubble slightly above the flag base
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Address Bubble
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "경상북도 구미시 인의동 366-5",
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                    // A small triangle or pointer for the bubble (can be drawn with a custom shape or a simple Box)
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(Color.White) // Match card background
                            .offset(y = (-5).dp) // Adjust to point downwards
                        // This would ideally be a custom shape to make a perfect triangle,
                        // but for simplicity, a small box centered below the text bubble.
                    )

                    Spacer(modifier = Modifier.height(2.dp)) // Space between bubble and flag

                    // Red Flag icon
                    // You'd likely have a custom drawable for the flag
                    Icon(
                        painter = painterResource(id = R.drawable.flag_2_24px), // Replace with your red flag icon resource
                        contentDescription = "Location marker",
                        tint = Color(0xFFE53935), // A strong red color
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "장소",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.offset(y = (-15).dp) // Adjust to overlap with flag a bit
                    )
                }
            }

            // Current location icon on map (top right)
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp), // Adjust padding to position it relative to the top bar
                shape = RoundedCornerShape(50), // Circular shape
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                IconButton(
                    onClick = { /* Handle current location click */ },
                    modifier = Modifier.size(48.dp) // Size of the circular button
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.my_location_24px), // Replace with your current location icon resource
                        contentDescription = "Current location",
                        tint = Color.Gray // Or a blue color if that's what your icon uses
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMissingMapScreen() {
    // For preview, you might need to provide dummy drawables or mock the resources
    // In a real project, ensure R.drawable.map_placeholder, R.drawable.ic_dog_face,
    // R.drawable.ic_red_flag, and R.drawable.ic_current_location exist.
    MissingMapScreen()
}