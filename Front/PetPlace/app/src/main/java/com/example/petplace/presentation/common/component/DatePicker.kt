//package com.example.petplace.presentation.common.component
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.launch
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import androidx.compose.ui.unit.toPx
//import java.util.*
//
//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun DatePicker(
//    initialDate: LocalDate = LocalDate.now(),
//    onDateSelected: (LocalDate) -> Unit,
//    onCancel: () -> Unit
//) {
//    // Current year, month, and day states
//    var selectedYear by remember { mutableStateOf(initialDate.year) }
//    var selectedMonth by remember { mutableStateOf(initialDate.monthValue) }
//    var selectedDay by remember { mutableStateOf(initialDate.dayOfMonth) }
//
//    val currentYear = LocalDate.now().year
//    val years = (currentYear - 100..currentYear + 100).toList() // A reasonable range of years
//    val months = (1..12).toList()
//    val daysInMonth = { year: Int, month: Int ->
//        LocalDate.of(year, month, 1).lengthOfMonth()
//    }
//    val days = (1..daysInMonth(selectedYear, selectedMonth)).toList()
//
//    val yearListState = rememberLazyListState()
//    val monthListState = rememberLazyListState()
//    val dayListState = rememberLazyListState()
//
//    val coroutineScope = rememberCoroutineScope()
//
//    // Scroll to initial date on first composition
//    LaunchedEffect(Unit) {
//        coroutineScope.launch {
//            yearListState.scrollToItem(years.indexOf(selectedYear))
//            monthListState.scrollToItem(months.indexOf(selectedMonth))
//            dayListState.scrollToItem(days.indexOf(selectedDay))
//        }
//    }
//
//    // Adjust days if month or year changes (e.g., February 29th)
//    LaunchedEffect(selectedYear, selectedMonth) {
//        val maxDay = daysInMonth(selectedYear, selectedMonth)
//        if (selectedDay > maxDay) {
//            selectedDay = maxDay // Clamp day to valid range for the new month/year
//        }
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        color = MaterialTheme.colorScheme.surface
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "목록 날짜를 선택해 주세요", // "Please select a list date"
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp) // Fixed height for the picker area
//                    .background(Color.Transparent)
//            ) {
//                // Highlighted selection area (the grey bar in the image)
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(50.dp) // Height of a single item
//                        .align(Alignment.Center)
//                        .background(Color(0xFFE0E0E0), MaterialTheme.shapes.small) // Light grey background
//                )
//
//                Row(
//                    modifier = Modifier.fillMaxSize(),
//                    horizontalArrangement = Arrangement.SpaceAround,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Year Picker
//                    DatePickerColumn(
//                        items = years,
//                        selectedItem = selectedYear,
//                        onItemSelected = { year -> selectedYear = year },
//                        listState = yearListState
//                    ) { item, isSelected ->
//                        Text(
//                            text = "${item}년",
//                            fontSize = if (isSelected) 20.sp else 16.sp,
//                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
//                            color = if (isSelected) Color.Black else Color.Gray,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        )
//                    }
//
//                    // Month Picker
//                    DatePickerColumn(
//                        items = months,
//                        selectedItem = selectedMonth,
//                        onItemSelected = { month -> selectedMonth = month },
//                        listState = monthListState
//                    ) { item, isSelected ->
//                        Text(
//                            text = "${item}월",
//                            fontSize = if (isSelected) 20.sp else 16.sp,
//                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
//                            color = if (isSelected) Color.Black else Color.Gray,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        )
//                    }
//
//                    // Day Picker
//                    DatePickerColumn(
//                        items = days,
//                        selectedItem = selectedDay,
//                        onItemSelected = { day -> selectedDay = day },
//                        listState = dayListState
//                    ) { item, isSelected ->
//                        Text(
//                            text = "${item}일",
//                            fontSize = if (isSelected) 20.sp else 16.sp,
//                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
//                            color = if (isSelected) Color.Black else Color.Gray,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                Button(onClick = onCancel) {
//                    Text("취소")
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Button(onClick = {
//                    val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth, selectedDay)
//                    onDateSelected(selectedLocalDate)
//                }) {
//                    Text("저장")
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun <T> DatePickerColumn(
//    items: List<T>,
//    selectedItem: T,
//    onItemSelected: (T) -> Unit,
//    listState: LazyListState,
//    itemContent: @Composable (T, Boolean) -> Unit
//) {
//    val itemHeight = 50.dp // Must match the height of the highlighted selection area
//    val coroutineScope = rememberCoroutineScope()
//
//    // Calculate the index of the selected item to scroll to it
//    val initialIndex = items.indexOf(selectedItem)
//    val centerIndex = initialIndex // We want the initially selected item to be in the center
//    val preScrollItems = 2 // Number of items to show above the center for padding
//
//    // This effect ensures that when the items list changes (e.g., days in month),
//    // the scroll position is updated to keep the selected item visible.
//    LaunchedEffect(items) {
//        if (items.isNotEmpty()) {
//            val newIndex = items.indexOf(selectedItem).coerceIn(0, items.size -1)
//            coroutineScope.launch {
//                listState.scrollToItem(newIndex)
//            }
//        }
//    }
//
//    LazyColumn(
//        state = listState,
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .width(IntrinsicSize.Min) // Allows content to dictate width
//            .height(200.dp) // Same height as the parent Box
//            .background(Color.Transparent),
//        contentPadding = PaddingValues(vertical = itemHeight * preScrollItems), // Padding to center the selected item
//        userScrollEnabled = true
//    ) {
//        items(items.size) { index ->
//            val item = items[index]
//            val isSelected = item == selectedItem
//
//            // Calculate the distance from the center of the viewport to apply fade/scale
//            // This is a simplified approach. A more accurate one would use layout coordinates.
//            val layoutInfo = listState.layoutInfo
//            val visibleItems = layoutInfo.visibleItemsInfo
//
//            val currentItemInfo = visibleItems.find { it.index == index }
//            val centerOffset = (layoutInfo.viewportSize.height / 2f) - (itemHeight.toPx() / 2f)
//
//            val alpha = if (currentItemInfo != null) {
//                val itemCenter = currentItemInfo.offset + (currentItemInfo.size / 2f)
//                val distance = kotlin.math.abs(itemCenter - centerOffset)
//                // Normalize distance from 0 to 1 based on item height
//                val normalizedDistance = (distance / (itemHeight.toPx() * preScrollItems)).coerceIn(0f, 1f)
//                1f - (normalizedDistance * 0.7f) // Fade out more as it moves away from center
//            } else {
//                0.3f // Far away items are more faded
//            }
//
//            val scale = if (isSelected) 1.2f else 1.0f // Slightly enlarge selected item
//
//            Box(
//                modifier = Modifier
//                    .height(itemHeight)
//                    .width(IntrinsicSize.Max)
//                    .graphicsLayer {
//                        this.alpha = alpha
//                        this.scaleX = scale
//                        this.scaleY = scale
//                    }
//                    .wrapContentSize(Alignment.Center) // Center the text within the item box
//            ) {
//                itemContent(item, isSelected)
//            }
//
//            // Logic to snap to nearest item after scroll
//            // This is crucial for picker-like behavior
//            // A more robust solution would involve detecting scroll end and animating to the snap position.
//            // For simplicity, we can monitor scroll state and adjust when not scrolling.
//            LaunchedEffect(listState.isScrollInProgress) {
//                if (!listState.isScrollInProgress) {
//                    val firstVisibleItem = listState.firstVisibleItemIndex
//                    val offset = listState.firstVisibleItemScrollOffset
//
//                    if (offset != 0) {
//                        val selectedIndex = if (offset > itemHeight.toPx() / 2) {
//                            firstVisibleItem + 1
//                        } else {
//                            firstVisibleItem
//                        }
//                        if (selectedIndex >= 0 && selectedIndex < items.size) {
//                            onItemSelected(items[selectedIndex])
//                            coroutineScope.launch {
//                                listState.animateScrollToItem(selectedIndex)
//                            }
//                        }
//                    } else {
//                        // If perfectly aligned, just update the selected item
//                        if (firstVisibleItem >= 0 && firstVisibleItem < items.size) {
//                            onItemSelected(items[firstVisibleItem])
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewIOSDatePicker() {
//    MaterialTheme {
//        DatePicker(
//            onDateSelected = { date -> println("Selected date: $date") },
//            onCancel = { println("Date picker cancelled") }
//        )
//    }
//}