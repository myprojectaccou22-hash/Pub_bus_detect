package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.viewmodel.BusViewModel

@Composable
fun WelcomeScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Pundra University Emblem Mock
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DirectionsBus,
                contentDescription = "Pundra Bus Emblem",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PUNDRA UNIVERSITY",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Science & Technology, Bogura",
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = "TRANSPORT TRACKER",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f, fill = false),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Track, Plan, and Ride Safely",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Real-time schedule countdown, active routes tracking, and emergency alerts management for Bogura campuses.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.navigateTo("login") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("welcome_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("LOG IN", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo("signup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("welcome_signup_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("REGISTER NEW ACCOUNT", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Guest shortcut access for evaluator
                Text(
                    "Quick Access Demo Credentials:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CredentialChip("Student", "student@pundra.edu") {
                        viewModel.login("student@pundra.edu", "student123") { _, _ -> }
                    }
                    CredentialChip("Driver", "driver@pundra.edu") {
                        viewModel.login("driver@pundra.edu", "driver123") { _, _ -> }
                    }
                    CredentialChip("Admin", "admin@pundra.edu") {
                        viewModel.login("admin@pundra.edu", "admin123") { _, _ -> }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun CredentialChip(label: String, email: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Tap to Login", fontSize = 8.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("welcome") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            Icons.Default.LockOpen,
            contentDescription = "Login Key",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Account Login",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Access your customized transport tracker",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Please enter email and password."
                } else {
                    isLoading = true
                    viewModel.login(email, password) { success, msg ->
                        isLoading = false
                        if (!success) {
                            errorMessage = msg
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_submit_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("LOG IN", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Don't have an account? Sign Up",
            modifier = Modifier
                .clickable { viewModel.navigateTo("signup") }
                .padding(8.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SignUpScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    var selectedRoleTab by remember { mutableStateOf(0) } // 0: Student, 1: Teacher, 2: Driver
    val roles = listOf("Student", "Teacher", "Driver")

    // Form inputs
    var fullName by remember { mutableStateOf("") }
    var uniqueId by remember { mutableStateOf("") } // student id, teacher id, driver id
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Dropdowns selection
    var selectedBus by remember { mutableStateOf("Bus No. 2") }
    var selectedRoute by remember { mutableStateOf("Satmatha to Campus") }
    var selectedPickup by remember { mutableStateOf("Satmatha") }

    val buses = listOf("Bus No. 1", "Bus No. 2", "Bus No. 3", "Bus No. 4", "Bus No. 5", "Bus No. 6", "Bus No. 7", "Bus No. 8")
    val routes = listOf("Gobindaganj to Campus", "Satmatha to Campus", "Charmatha to Campus", "Sherpur to Campus", "Gabtoli to Campus")
    val stops = listOf("Campus", "Satmatha", "Gobindaganj", "Mokamtola", "Charmatha", "Banani", "Sherpur", "Gabtoli", "Matidali")

    var busExpanded by remember { mutableStateOf(false) }
    var routeExpanded by remember { mutableStateOf(false) }
    var stopExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("welcome") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role select Tabs
        TabRow(selectedTabIndex = selectedRoleTab) {
            roles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedRoleTab == index,
                    onClick = { selectedRoleTab = index; errorMessage = "" },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uniqueId,
            onValueChange = { uniqueId = it },
            label = { Text(if (selectedRoleTab == 0) "Student ID" else if (selectedRoleTab == 1) "Teacher ID" else "Driver ID") },
            modifier = Modifier.fillMaxWidth().testTag("signup_id_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth().testTag("signup_phone_input"),
            singleLine = true
        )

        if (selectedRoleTab < 2) { // Student or Teacher
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth().testTag("signup_dept_input"),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role-based specific selectors
        if (selectedRoleTab < 2) { // Student / Teacher Bus Details
            Text("Assigned Pickup Bus Preferences:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Bus Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { busExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedBus, fontSize = 11.sp)
                    }
                    DropdownMenu(expanded = busExpanded, onDismissRequest = { busExpanded = false }) {
                        buses.forEach { bus ->
                            DropdownMenuItem(text = { Text(bus) }, onClick = { selectedBus = bus; busExpanded = false })
                        }
                    }
                }
                // Route Dropdown
                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedButton(onClick = { routeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedRoute, fontSize = 11.sp)
                    }
                    DropdownMenu(expanded = routeExpanded, onDismissRequest = { routeExpanded = false }) {
                        routes.forEach { route ->
                            DropdownMenuItem(text = { Text(route) }, onClick = { selectedRoute = route; routeExpanded = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Selected Pickup Stop:", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { stopExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedPickup)
                }
                DropdownMenu(expanded = stopExpanded, onDismissRequest = { stopExpanded = false }) {
                    stops.forEach { stop ->
                        DropdownMenuItem(text = { Text(stop) }, onClick = { selectedPickup = stop; stopExpanded = false })
                    }
                }
            }
        } else { // Driver details
            Text("Assigned Vehicle Details:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Bus Number
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { busExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedBus)
                    }
                    DropdownMenu(expanded = busExpanded, onDismissRequest = { busExpanded = false }) {
                        buses.forEach { bus ->
                            DropdownMenuItem(text = { Text(bus) }, onClick = { selectedBus = bus; busExpanded = false })
                        }
                    }
                }
                // Route Number
                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedButton(onClick = { routeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedRoute)
                    }
                    DropdownMenu(expanded = routeExpanded, onDismissRequest = { routeExpanded = false }) {
                        routes.forEach { route ->
                            DropdownMenuItem(text = { Text(route) }, onClick = { selectedRoute = route; routeExpanded = false })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (fullName.isEmpty() || uniqueId.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Please fill in all standard credentials."
                } else {
                    val entity = UserEntity(
                        id = uniqueId,
                        role = roles[selectedRoleTab].lowercase(),
                        fullName = fullName,
                        phone = phone,
                        email = email,
                        password = password,
                        department = if (selectedRoleTab < 2) department else null,
                        busNumber = selectedBus.replace("Bus No. ", ""),
                        route = selectedRoute,
                        pickupStop = if (selectedRoleTab < 2) selectedPickup else null,
                        isApproved = true // Auto-approved for demo comfort
                    )
                    viewModel.signUp(entity) { success, msg ->
                        if (!success) {
                            errorMessage = msg
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("signup_submit_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("REGISTER & LOG IN", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Already have an account? Login",
            modifier = Modifier
                .clickable { viewModel.navigateTo("login") }
                .padding(8.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
