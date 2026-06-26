package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.BusViewModel
import androidx.compose.ui.window.Dialog

@Composable
fun CustomFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9))
            .clickable(onClick = onClick)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun StudentDashboardScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val buses by viewModel.buses.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val favourites by viewModel.favouriteBuses.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val lostFound by viewModel.lostFoundItems.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Dialog flags
    var showQrScanner by remember { mutableStateOf(false) }
    var showComplaintDialog by remember { mutableStateOf(false) }
    var showAddLostFoundDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var selectedBusDetails by remember { mutableStateOf<BusEntity?>(null) }

    // Selected pickup countdown stop details
    val userBusNum = currentUser?.busNumber?.toIntOrNull() ?: 2
    val userBus = buses.find { it.busNumber == userBusNum }
    val userPickup = currentUser?.pickupStop ?: "Satmatha"

    // Filtered buses list
    val filteredBuses = remember(buses, searchQuery, selectedFilter) {
        buses.filter { bus ->
            val matchesSearch = bus.busName.contains(searchQuery, ignoreCase = true) ||
                    bus.busNumber.toString().contains(searchQuery) ||
                    bus.routeName.contains(searchQuery, ignoreCase = true) ||
                    bus.assignedDriverName.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = when (selectedFilter) {
                "Running" -> bus.status == "Running"
                "Delayed" -> bus.status == "Delayed"
                "Reached Campus" -> bus.status == "Reached Campus"
                "Offline" -> bus.status == "Offline"
                "Teacher Bus" -> bus.isTeacherOnly
                "Student Buses" -> !bus.isTeacherOnly
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "PUST",
                                color = Color(0xFF1A4B8F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Column {
                            Text(
                                "Pundra University",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "TRANSPORT SYSTEM",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (isOffline) "OFFLINE" else "ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Switch(
                            checked = !isOffline,
                            onCheckedChange = { viewModel.setOfflineMode(!it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Green,
                                checkedTrackColor = Color.DarkGray,
                                uncheckedThumbColor = Color.Red,
                                uncheckedTrackColor = Color.LightGray
                            ),
                            modifier = Modifier.scale(0.8f).testTag("offline_toggle")
                        )
                        IconButton(onClick = { viewModel.logout() }, modifier = Modifier.testTag("student_logout_btn")) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showQrScanner = true },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("fab_qr_scan")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                }
                FloatingActionButton(
                    onClick = { showSosDialog = true },
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_sos_panic")
                ) {
                    Text("SOS", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("student_dashboard_root")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // User Info Welcome Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Assalamu Alaikum,", fontSize = 12.sp, color = Color.Gray)
                            Text(currentUser?.fullName ?: "Member", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                            Text("${currentUser?.department} Department | ${currentUser?.role?.uppercase()}", fontSize = 11.sp, color = Color.DarkGray)
                        }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentUser?.fullName?.take(1)?.uppercase() ?: "P", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Real-time Bus Countdown Alert (Primary requested feature!)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = "Countdown", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Live Tracking Panel", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (userBus != null) {
                            // Active Bus Highlight
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFDBEAFE), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                "BUS NO. ${userBus.busNumber}",
                                                color = Color(0xFF1E40AF),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (userBus.status == "Running") Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                userBus.status.uppercase(),
                                                color = if (userBus.status == "Running") Color(0xFF065F46) else Color(0xFF991B1B),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        userBus.busName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E293B),
                                        lineHeight = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Driver: ${userBus.assignedDriverName} • V-9241",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = if (userBus.status == "Running") userBus.estimatedArrivalTime.replace(" mins", "").replace(" min", "") else "--",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 28.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            lineHeight = 28.sp
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            "min",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (userBus.delayMinutes > 0) {
                                        Text(
                                            "${userBus.delayMinutes} min Delay",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFF97316)
                                        )
                                    } else {
                                        Text(
                                            "On Time",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stats Chips (Grid of 2 columns)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Chip 1: Crowd level
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFFDBEAFE), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.People,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "CROWD LEVEL",
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            userBus.crowdLevel.replace(" Crowd", ""),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }

                                // Chip 2: Seats Availability
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "SEATS",
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            userBus.seatAvailability.replace("Available", "Avail"),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Progress Route Timeline
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                // Draw vertical timeline line
                                Column(
                                    modifier = Modifier
                                        .padding(start = 7.dp, top = 8.dp, bottom = 8.dp)
                                        .width(2.dp)
                                        .height(110.dp)
                                        .background(Color(0xFFE2E8F0))
                                ) {}

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Stop 1: Start Stop
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Outer ring, inner circle
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(Color.White, CircleShape)
                                                .border(2.dp, Color(0xFF94A3B8), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF94A3B8), CircleShape))
                                        }
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Satmatha (Start)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF475569)
                                            )
                                            Text(
                                                text = "07:30 AM",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }

                                    // Stop 2: Current Stop (Sherpur/Banani)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Active circle
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(Color(0xFFDBEAFE), CircleShape)
                                                .border(2.dp, Color(0xFF2563EB), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF2563EB), CircleShape))
                                        }
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$userPickup (Current)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D4ED8)
                                            )
                                            Text(
                                                text = "Near Stop",
                                                fontSize = 10.sp,
                                                color = Color(0xFF2563EB),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Stop 3: End Stop (Campus)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(Color.White, CircleShape)
                                                .border(2.dp, Color(0xFF94A3B8), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF94A3B8), CircleShape))
                                        }
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Campus (End)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF475569)
                                            )
                                            Text(
                                                text = "ETA 08:45 AM",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("No preferred bus assigned. Update preference in Register/Settings.", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Interactive Map Section (Canvas simulator)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Bus Tracking Map", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = { showQrScanner = true }) {
                            Text("Scan QR Code", fontSize = 12.sp)
                        }
                    }
                    InteractiveMapSection(
                        viewModel = viewModel,
                        selectedBusNumber = selectedBusDetails?.busNumber,
                        onBusClick = { bus -> selectedBusDetails = bus }
                    )
                }
            }

            // Quick Notifications log
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("Recent System Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        notifications.take(3).forEach { note ->
                            Text("• $note", fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            // Bus Search & Filter System
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Search University Bus Fleet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            label = { Text("Search by Bus Name, Route, Driver...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("search_buses_input"),
                            singleLine = true
                        )

                        // Filters Chips Row using our safe custom chip implementation
                        val filtersList = listOf("All", "Running", "Delayed", "Reached Campus")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filtersList.forEach { filter ->
                                CustomFilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { viewModel.selectedFilter.value = filter },
                                    label = filter
                                )
                            }
                        }

                        // Buses result list
                        Text("Search Results (${filteredBuses.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        
                        filteredBuses.forEach { bus ->
                            val isFav = favourites.contains(bus.busNumber)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .clickable { selectedBusDetails = bus }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (bus.isTeacherOnly) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(bus.busNumber.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(bus.busName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(bus.routeName, fontSize = 11.sp, color = Color.Gray)
                                        Text("Driver: ${bus.assignedDriverName}", fontSize = 10.sp)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { viewModel.toggleFavourite(bus.busNumber) }, modifier = Modifier.size(24.dp)) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Fav",
                                            tint = if (isFav) Color.Red else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                when (bus.status) {
                                                    "Running" -> Color(0xFFDCFCE7)
                                                    "Delayed" -> Color(0xFFFEE2E2)
                                                    else -> Color(0xFFF1F5F9)
                                                }, RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            bus.status,
                                            color = when (bus.status) {
                                                "Running" -> Color(0xFF15803D)
                                                "Delayed" -> Color(0xFFB91C1C)
                                                else -> Color(0xFF475569)
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Route Schedule Calendar / offline view
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Official Bus Route Schedules", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        
                        routes.forEach { r ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(r.routeName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Departs: ${r.departureTime}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Return: ${r.returnTime}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Duration: ${r.estimatedTravelTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Text("Stops: ${r.stops}", fontSize = 10.sp, color = Color.DarkGray)
                                Divider(modifier = Modifier.padding(top = 4.dp), color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }

            // Announcements feed
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notice Board", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    if (notices.isEmpty()) {
                        Text("No active transport notices.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        notices.forEach { notice ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notice.content, fontSize = 12.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Audience: ${notice.targetGroup}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Lost & Found Items Feed
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Lost & Found Desk", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            OutlinedButton(
                                onClick = { showAddLostFoundDialog = true },
                                modifier = Modifier.scale(0.85f).testTag("post_lost_btn")
                            ) {
                                Text("Post Item")
                            }
                        }

                        if (lostFound.isEmpty()) {
                            Text("No posts reported.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            lostFound.forEach { lf ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (lf.type == "Lost") Color(0xFFFEF2F2) else Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(lf.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(
                                                lf.type.uppercase(),
                                                color = if (lf.type == "Lost") Color.Red else Color(0xFF10B981),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Text(lf.description, fontSize = 11.sp, color = Color.DarkGray)
                                        Text("Contact: ${lf.contactName} (${lf.contactPhone})", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Complaints history & submit form
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Feedback & Complaints", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Button(onClick = { showComplaintDialog = true }, modifier = Modifier.scale(0.85f).testTag("open_complaint_btn")) {
                                Text("New Feedback")
                            }
                        }

                        val userComplaintsFlow = viewModel.getComplaintsByUser(currentUser?.id ?: "")
                        val userComplaints by userComplaintsFlow.collectAsState(initial = emptyList())

                        if (userComplaints.isEmpty()) {
                            Text("You haven't submitted any complaints yet.", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            userComplaints.forEach { cmp ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(cmp.subject, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(
                                                cmp.status,
                                                color = when (cmp.status) {
                                                    "Resolved" -> Color(0xFF10B981)
                                                    "Pending" -> Color(0xFFF59E0B)
                                                    else -> Color.Red
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Text(cmp.message, fontSize = 11.sp, color = Color.DarkGray)
                                        if (cmp.adminFeedback != null) {
                                            Text("Admin Response: ${cmp.adminFeedback}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Emergency Contacts list (Direct requested feature)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier.border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color.Red)
                            Text("Emergency Help Desk Contacts", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Red)
                        }
                        
                        EmergencyContactRow("Transport Director Office", "01755554444", context)
                        EmergencyContactRow("Transport Sub-station (Bogura)", "01933332222", context)
                        EmergencyContactRow("University Medical Unit", "01512121212", context)
                        EmergencyContactRow("Emergency Mechanics Service", "01844445555", context)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // Modal dialog setups
    if (showQrScanner) {
        QRScannerSimulationDialog(
            buses = buses,
            onDismiss = { showQrScanner = false },
            onBusResolved = { bus ->
                selectedBusDetails = bus
                showQrScanner = false
            }
        )
    }

    if (showComplaintDialog) {
        SubmitComplaintDialog(
            buses = buses,
            onDismiss = { showComplaintDialog = false },
            onSubmit = { busNo, subject, msg ->
                viewModel.submitComplaint(busNo, subject, msg)
            }
        )
    }

    if (showSosDialog) {
        SosAlertDialog(
            busNumber = userBusNum,
            onDismiss = { showSosDialog = false },
            onSubmitSos = { type, desc ->
                viewModel.sendSosAlert(userBusNum, type, desc)
            }
        )
    }

    // Bus Detail Card overlay
    selectedBusDetails?.let { bus ->
        val isFav = favourites.contains(bus.busNumber)
        Dialog(onDismissRequest = { selectedBusDetails = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp).testTag("bus_detail_modal")
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Bus No. ${bus.busNumber}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { viewModel.toggleFavourite(bus.busNumber) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Fav",
                                tint = if (isFav) Color.Red else Color.Gray
                            )
                        }
                    }
                    Text(bus.busName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Driver Name: ${bus.assignedDriverName}", fontSize = 12.sp)
                    Text("Assigned Route: ${bus.routeName}", fontSize = 12.sp)

                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Current Speed", fontSize = 10.sp, color = Color.Gray)
                            Text("${bus.speed.toInt()} km/h", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("ETA to Campus", fontSize = 10.sp, color = Color.Gray)
                            Text(bus.estimatedArrivalTime, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Crowd Level", fontSize = 10.sp, color = Color.Gray)
                            Text(bus.crowdLevel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Seat Status", fontSize = 10.sp, color = Color.Gray)
                            Text(bus.seatAvailability, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Divider()

                    Button(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Track my Pundra University Bus No. ${bus.busNumber} (${bus.busName}) on ${bus.routeName}. Currently at: lat=${bus.latitude}, lng=${bus.longitude}. Status: ${bus.status}. ETA: ${bus.estimatedArrivalTime}!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Location with Family"))
                        },
                        modifier = Modifier.fillMaxWidth().testTag("share_location_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Location details")
                    }
                }
            }
        }
    }

    if (showAddLostFoundDialog) {
        Dialog(onDismissRequest = { showAddLostFoundDialog = false }) {
            var title by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }
            var phoneNum by remember { mutableStateOf("") }
            var type by remember { mutableStateOf("Lost") }

            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Report Lost / Found Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = type == "Lost", onClick = { type = "Lost" })
                            Text("Lost")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = type == "Found", onClick = { type = "Found" })
                            Text("Found")
                        }
                    }

                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Item Name") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Details (where, when)") })
                    OutlinedTextField(value = phoneNum, onValueChange = { phoneNum = it }, label = { Text("Your Contact Phone") })

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && desc.isNotEmpty() && phoneNum.isNotEmpty()) {
                                viewModel.postLostFound(title, desc, currentUser?.fullName ?: "Member", phoneNum, type)
                                showAddLostFoundDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Post Announcement")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DRIVER DASHBOARD
// ---------------------------------------------------------------------------
@Composable
fun DriverDashboardScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val buses by viewModel.buses.collectAsState()
    val activeTripId by viewModel.activeTripId.collectAsState()

    val assignedBusNumber = currentUser?.busNumber?.toIntOrNull() ?: 2
    val busInfo = buses.find { it.busNumber == assignedBusNumber }

    // Pre-trip checklist states
    val checklistItems = remember {
        mutableStateMapOf(
            "Fuel Checked" to false,
            "Brake Checked" to false,
            "Tyre Checked" to false,
            "Lights Checked" to false,
            "First Aid Box Available" to false,
            "Fire Extinguisher Available" to false,
            "GPS/Phone Location Enabled" to false
        )
    }

    var showDelayReasonDialog by remember { mutableStateOf(false) }
    var selectedDelayReason by remember { mutableStateOf("") }
    var delayMinutes by remember { mutableStateOf("5") }

    var manualStopSelected by remember { mutableStateOf("Satmatha") }
    var manualStopExpanded by remember { mutableStateOf(false) }

    var showSosDialog by remember { mutableStateOf(false) }

    val allChecklistPassed = checklistItems.values.all { it }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Driver Dashboard - Bus No. $assignedBusNumber", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("driver_dashboard_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Driver Welcome Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Welcome, Driver", fontSize = 12.sp, color = Color.Gray)
                        Text(currentUser?.fullName ?: "Driver Member", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Vehicle: Bus No. $assignedBusNumber (${busInfo?.busName ?: "Student Express"})", fontSize = 11.sp, color = Color.DarkGray)
                        Text("Assigned Route: ${currentUser?.route ?: "Satmatha to Campus"}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }

            // Checklist section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Daily Pre-trip Safety Checklist", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text("Safety mandates require verifying all items before commencing trip.", fontSize = 11.sp, color = Color.Gray)
                    
                    checklistItems.keys.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checklistItems[item] == true,
                                onCheckedChange = { checklistItems[item] = it }
                            )
                            Text(item, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Trip controls (Start, End)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Trip Control Panel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    
                    if (activeTripId == null) {
                        Button(
                            onClick = {
                                viewModel.driverStartTrip(
                                    busNumber = assignedBusNumber,
                                    routeName = currentUser?.route ?: "Satmatha to Campus",
                                    driverName = currentUser?.fullName ?: "Jalal"
                                )
                            },
                            enabled = allChecklistPassed,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_trip_btn")
                        ) {
                            Text("START TRIP ON ASSIGNED ROUTE", fontWeight = FontWeight.Bold)
                        }
                        if (!allChecklistPassed) {
                            Text("⚠️ You must pass all pre-trip checklist items to start the trip.", color = Color.Red, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.driverEndTrip(assignedBusNumber) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("end_trip_btn")
                        ) {
                            Text("ARRIVED: COMPLETED TRIP", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("Trip is active. Simulated GPS location updates live automatically.", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Manual controls overlay (Crowd / Seat status / Stop)
            if (activeTripId != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Active Trip Live Updates", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        
                        // Select current manual stop
                        Text("Select Current Stop Manually:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(onClick = { manualStopExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(manualStopSelected)
                            }
                            DropdownMenu(expanded = manualStopExpanded, onDismissRequest = { manualStopExpanded = false }) {
                                listOf("Satmatha", "Tinmatha", "Matidali", "Gokul", "Campus").forEach { stop ->
                                    DropdownMenuItem(
                                        text = { Text(stop) },
                                        onClick = {
                                            manualStopSelected = stop
                                            manualStopExpanded = false
                                            viewModel.addNotification("Driver Update: Bus No. $assignedBusNumber is now at $stop.")
                                        }
                                    )
                                }
                            }
                        }

                        Divider()

                        // Crowd status selectors
                        Text("Update Passenger Occupancy:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Low", "Medium", "High", "Full").forEach { crowd ->
                                val fullCrowdString = when (crowd) {
                                    "Low" -> "Low Crowd"
                                    "Medium" -> "Medium Crowd"
                                    "High" -> "High Crowd"
                                    else -> "Full"
                                }
                                OutlinedButton(
                                    onClick = { viewModel.driverUpdateCrowdSeats(assignedBusNumber, fullCrowdString, busInfo?.seatAvailability ?: "Seats Available") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (busInfo?.crowdLevel == fullCrowdString) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (busInfo?.crowdLevel == fullCrowdString) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(crowd, fontSize = 10.sp)
                                }
                            }
                        }

                        // Seat availability selectors
                        Text("Update Seats Availability:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Seats Available", "Few Seats Available", "No Seats Available").forEach { seat ->
                                OutlinedButton(
                                    onClick = { viewModel.driverUpdateCrowdSeats(assignedBusNumber, busInfo?.crowdLevel ?: "Low Crowd", seat) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (busInfo?.seatAvailability == seat) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (busInfo?.seatAvailability == seat) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(seat.take(10) + "...", fontSize = 9.sp)
                                }
                            }
                        }

                        Divider()

                        // Log delayed reasons
                        Button(
                            onClick = { showDelayReasonDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("REPORT BUS DELAY / ISSUES")
                        }
                    }
                }
            }

            // EMERGENCY SOS BUTTON
            Button(
                onClick = { showSosDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sos_trigger_btn")
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TRIGGER SOS PANIC ALERT", fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showDelayReasonDialog) {
        Dialog(onDismissRequest = { showDelayReasonDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Report Trip Delay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = delayMinutes,
                        onValueChange = { delayMinutes = it },
                        label = { Text("Delay Minutes") }
                    )

                    Text("Delay Reason:")
                    val reasons = listOf("Traffic Jam", "Rain", "Road Block", "Mechanical Problem", "Fuel Issue", "Accident", "Driver Break")
                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDelayReason = reason }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedDelayReason == reason, onClick = { selectedDelayReason = reason })
                            Text(reason, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val mins = delayMinutes.toIntOrNull() ?: 5
                            viewModel.driverUpdateStatus(assignedBusNumber, "Delayed", mins)
                            viewModel.addNotification("Alert: Bus No. $assignedBusNumber is delayed by $mins mins due to $selectedDelayReason.")
                            showDelayReasonDialog = false
                        },
                        enabled = selectedDelayReason.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Delay")
                    }
                }
            }
        }
    }

    if (showSosDialog) {
        SosAlertDialog(
            busNumber = assignedBusNumber,
            onDismiss = { showSosDialog = false },
            onSubmitSos = { type, desc ->
                viewModel.sendSosAlert(assignedBusNumber, type, desc)
            }
        )
    }
}

// ---------------------------------------------------------------------------
// ADMIN DASHBOARD
// ---------------------------------------------------------------------------
@Composable
fun AdminDashboardScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    val buses by viewModel.buses.collectAsState()
    val complaints by viewModel.complaints.collectAsState()
    val maintenanceLogs by viewModel.maintenanceLogs.collectAsState()
    val users by viewModel.repository.allUsers.collectAsState(initial = emptyList())
    val trips by viewModel.trips.collectAsState()

    var activeAdminTab by remember { mutableStateOf(0) } // 0: Map monitor, 1: Notice board, 2: Complaints, 3: Fleet/Routes, 4: Analytics

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showAddBusDialog by remember { mutableStateOf(false) }

    // Aggregate statistics
    val runningCount = buses.count { it.status == "Running" }
    val delayedCount = buses.count { it.status == "Delayed" }
    val offlineCount = buses.count { it.status == "Offline" }
    val pendingComplaints = complaints.count { cmp -> cmp.status == "Pending" }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Admin Console - PUST Transport", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = activeAdminTab == 0,
                    onClick = { activeAdminTab = 0 },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text("Monitor", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeAdminTab == 1,
                    onClick = { activeAdminTab = 1 },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    label = { Text("Notice", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeAdminTab == 2,
                    onClick = { activeAdminTab = 2 },
                    icon = { Icon(Icons.Default.Feedback, contentDescription = null) },
                    label = { Text("Feedback", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeAdminTab == 3,
                    onClick = { activeAdminTab = 3 },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Fleet", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeAdminTab == 4,
                    onClick = { activeAdminTab = 4 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Reports", fontSize = 10.sp) }
                )
            }
        },
        modifier = modifier.fillMaxSize().testTag("admin_dashboard_root")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Dynamic Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdminStatCard("Running", runningCount.toString(), Color(0xFF10B981), modifier = Modifier.weight(1f))
                    AdminStatCard("Delayed", delayedCount.toString(), Color.Red, modifier = Modifier.weight(1f))
                    AdminStatCard("Offline", offlineCount.toString(), Color.Gray, modifier = Modifier.weight(1f))
                    AdminStatCard("Feedback", pendingComplaints.toString(), Color(0xFFFF9800), modifier = Modifier.weight(1f))
                }
            }

            // Rendering tab widgets based on active tab
            when (activeAdminTab) {
                0 -> { // LIVE GPS MONITOR
                    item {
                        Text("Real-time GPS Simulator Monitor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    item {
                        InteractiveMapSection(
                            viewModel = viewModel,
                            modifier = Modifier.testTag("admin_map_view")
                        )
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Active Buses Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                buses.forEach { bus ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Bus No. ${bus.busNumber} (${bus.busName})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("${bus.status} | Speed: ${bus.speed.toInt()} km/h | ETA: ${bus.estimatedArrivalTime}", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // BROADCAST NOTICE BOARD
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("System Notices Coordinator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Button(onClick = { showBroadcastDialog = true }, modifier = Modifier.testTag("admin_open_broadcast_btn")) {
                                Text("Broadcast")
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val noticeList by viewModel.notices.collectAsState()
                            if (noticeList.isEmpty()) {
                                Text("No notice broadcast logs.")
                            } else {
                                noticeList.forEach { notice ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                                Text(notice.content, fontSize = 12.sp, color = Color.DarkGray)
                                                Text("Target: ${notice.targetGroup}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            IconButton(onClick = { viewModel.deleteNotice(notice) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Notice", tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // FEEDBACK & COMPLAINTS MODERATION
                    item {
                        Text("Moderation Queue (${complaints.size} reports)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    if (complaints.isEmpty()) {
                        item { Text("No student/teacher complaints logged.") }
                    } else {
                        items(complaints) { cmp ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(cmp.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("By: ${cmp.userName} (${cmp.userRole.uppercase()}) | Ref: ${cmp.busNumber}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Box(modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(4.dp)) {
                                            Text(cmp.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                        }
                                    }
                                    Text(cmp.message, fontSize = 12.sp, color = Color.DarkGray)
                                    
                                    if (cmp.status == "Pending") {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.updateComplaintStatus(cmp, "Resolved", "Thank you, extra Bus 6 has been deployed to help handle high loads.") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                modifier = Modifier.weight(1f).scale(0.85f)
                                            ) {
                                                Text("Resolve")
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.updateComplaintStatus(cmp, "Rejected", "Under evaluation") },
                                                modifier = Modifier.weight(1f).scale(0.85f)
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    } else {
                                        Text("Admin Response: ${cmp.adminFeedback}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> { // FLEET & VEHICLE MAINTENANCE MANAGER
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Fleet & Maintenance logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(onClick = { showAddBusDialog = true }, modifier = Modifier.scale(0.8f)) {
                                    Text("+ Bus")
                                }
                                Button(onClick = { showMaintenanceDialog = true }, modifier = Modifier.scale(0.8f)) {
                                    Text("+ Log")
                                }
                            }
                        }
                    }
                    item {
                        Text("Fleet Registry:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    items(buses) { bus ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Bus No. ${bus.busNumber} - ${bus.busName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Route: ${bus.routeName}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Driver: ${bus.assignedDriverName} | Status: ${bus.status}", fontSize = 11.sp)
                                }
                                IconButton(onClick = { viewModel.adminDeleteBus(bus) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                }
                            }
                        }
                    }
                    item {
                        Text("Active Servicing Records:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    items(maintenanceLogs) { log ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Bus No. ${log.busNumber} [${log.type}]", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Cost: ${log.cost.toInt()} BDT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(log.details, fontSize = 11.sp, color = Color.DarkGray)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Date: ${log.date}", fontSize = 10.sp)
                                    Text("Next Due: ${log.nextServiceDate}", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                4 -> { // ANALYTICAL REPORTS
                    item {
                        Text("Transport Performance & Usage", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Campus Occupancy & Efficiency Statistics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Most Delayed Route: Gobindaganj (Avg 12 mins delay)", fontSize = 12.sp)
                                Text("• Most Crowded Route: Sherpur to Campus (High Occupancy)", fontSize = 12.sp)
                                Text("• Completeness Factor: 98.4% (Daily completed trips)", fontSize = 12.sp)
                                Text("• Active Drivers Count: ${users.count { it.role == "driver" }}", fontSize = 12.sp)
                                Text("• Student Bus Card Registrants: ${users.count { it.role == "student" }}", fontSize = 12.sp)
                            }
                        }
                    }
                    item {
                        Text("Completed Trips Log History", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    items(trips) { trip ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Bus No. ${trip.busNumber} (${trip.driverName})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(trip.status, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text("Route: ${trip.routeName}", fontSize = 11.sp, color = Color.DarkGray)
                                Text("Departed: ${trip.startTime} | Arrived: ${trip.endTime ?: "N/A"}", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showBroadcastDialog) {
        BroadcastNoticeDialog(
            onDismiss = { showBroadcastDialog = false },
            onBroadcast = { title, content, target ->
                viewModel.postNotice(title, content, target)
            }
        )
    }

    if (showMaintenanceDialog) {
        MaintenanceLoggerDialog(
            buses = buses,
            onDismiss = { showMaintenanceDialog = false },
            onLog = { log ->
                viewModel.addMaintenanceLog(log)
            }
        )
    }

    if (showAddBusDialog) {
        Dialog(onDismissRequest = { showAddBusDialog = false }) {
            var num by remember { mutableStateOf("") }
            var name by remember { mutableStateOf("") }
            var driver by remember { mutableStateOf("") }
            val routeName = "Satmatha to Campus"

            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Bus to Registry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    OutlinedTextField(value = num, onValueChange = { num = it }, label = { Text("Bus Number (e.g. 9)") })
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Bus Name (e.g. Eagle Wings)") })
                    OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Driver Name") })
                    
                    Button(
                        onClick = {
                            val busNo = num.toIntOrNull() ?: 9
                            val bus = BusEntity(
                                busNumber = busNo,
                                busName = name,
                                assignedDriverName = driver,
                                routeName = routeName,
                                status = "Not Started",
                                latitude = 50.0,
                                longitude = 85.0,
                                estimatedArrivalTime = "--",
                                delayMinutes = 0,
                                crowdLevel = "Low Crowd",
                                seatAvailability = "Seats Available",
                                lastUpdatedTime = System.currentTimeMillis(),
                                speed = 0f
                            )
                            viewModel.adminAddBus(bus)
                            showAddBusDialog = false
                        },
                        enabled = num.isNotEmpty() && name.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Bus")
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyContactRow(name: String, phone: String, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(intent)
            }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
            Text(phone, fontSize = 11.sp, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "Call",
            tint = Color.Red,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AdminStatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
