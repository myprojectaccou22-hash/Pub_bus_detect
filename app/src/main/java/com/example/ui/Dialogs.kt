package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BusEntity
import com.example.data.MaintenanceLogEntity
import com.example.viewmodel.BusViewModel

@Composable
fun QRScannerSimulationDialog(
    buses: List<BusEntity>,
    onDismiss: () -> Unit,
    onBusResolved: (BusEntity) -> Unit
) {
    var scanning by remember { mutableStateOf(true) }
    var selectedScanBusNo by remember { mutableStateOf(2) }

    LaunchedEffect(scanning) {
        if (scanning) {
            kotlinx.coroutines.delay(2000) // Simulate scan progress
            scanning = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp).testTag("qr_scanner_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Scan Bus QR Code",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Scanner")
                    }
                }

                if (scanning) {
                    Text("Align QR Code on Bus door inside the scanner frame", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .background(Color(0x2210B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner Frame",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(120.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Accessing back camera...", fontSize = 11.sp, color = Color.Gray)
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(50.dp),
                        tint = Color(0xFF10B981)
                    )
                    Text("QR Code Scanned Successfully!", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))

                    Text("Select Bus to Scan (Demo):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..4).forEach { num ->
                            OutlinedButton(
                                onClick = { selectedScanBusNo = num },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedScanBusNo == num) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (selectedScanBusNo == num) Color.White else MaterialTheme.colorScheme.primary
                                ),
                                shape = CircleShape,
                                modifier = Modifier.size(42.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(num.toString(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val resolved = buses.find { it.busNumber == selectedScanBusNo }
                            if (resolved != null) {
                                onBusResolved(resolved)
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("resolve_scan_button")
                    ) {
                        Text("View Bus No. $selectedScanBusNo Details")
                    }
                }
            }
        }
    }
}

@Composable
fun SubmitComplaintDialog(
    buses: List<BusEntity>,
    onDismiss: () -> Unit,
    onSubmit: (busNo: String, subject: String, message: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedBusNo by remember { mutableStateOf("Bus No. 1") }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(12.dp).testTag("complaint_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Submit Complaint / Feedback", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                Text("Select Bus Number:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedBusNo)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        buses.forEach { bus ->
                            DropdownMenuItem(
                                text = { Text("Bus No. ${bus.busNumber} (${bus.busName})") },
                                onClick = {
                                    selectedBusNo = "Bus No. ${bus.busNumber}"
                                    expanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("General Transport Office") },
                            onClick = {
                                selectedBusNo = "General Office"
                                expanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject / Issue") },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_subject_input")
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Describe details...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("complaint_msg_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (subject.isNotEmpty() && message.isNotEmpty()) {
                                onSubmit(selectedBusNo, subject, message)
                                onDismiss()
                            }
                        },
                        enabled = subject.isNotEmpty() && message.isNotEmpty(),
                        modifier = Modifier.weight(1f).testTag("complaint_submit_btn")
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun BroadcastNoticeDialog(
    onDismiss: () -> Unit,
    onBroadcast: (title: String, content: String, target: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    var selectedTarget by remember { mutableStateOf("All") }
    val targets = listOf("All", "Students", "Teachers", "Drivers", "Gobindaganj Route", "Satmatha Route", "Sherpur Route")
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(12.dp).testTag("notice_broadcast_dialog")) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Broadcast Route/Role Notice", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                Text("Select Recipient Audience:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedTarget)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        targets.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { selectedTarget = t; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Header / Title") },
                    modifier = Modifier.fillMaxWidth().testTag("notice_title_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Detailed description...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("notice_desc_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && content.isNotEmpty()) {
                                onBroadcast(title, content, selectedTarget)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotEmpty() && content.isNotEmpty(),
                        modifier = Modifier.weight(1f).testTag("notice_send_btn")
                    ) {
                        Text("Send Broadcast")
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenanceLoggerDialog(
    buses: List<BusEntity>,
    onDismiss: () -> Unit,
    onLog: (MaintenanceLogEntity) -> Unit
) {
    var selectedBusNo by remember { mutableStateOf(1) }
    var busExpanded by remember { mutableStateOf(false) }

    var type by remember { mutableStateOf("Fuel Refill") }
    val types = listOf("Fuel Refill", "Oil Change", "Tyre Change", "Brake Service", "Engine Issue", "Battery Issue", "Air Conditioner Issue", "Cleaning Status", "General Repair")
    var typeExpanded by remember { mutableStateOf(false) }

    var details by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var serviceDate by remember { mutableStateOf("2026-06-26") }
    var nextServiceDate by remember { mutableStateOf("2026-08-26") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(12.dp).testTag("maintenance_dialog")) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Log Vehicle Maintenance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Select Bus:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(onClick = { busExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Bus No. $selectedBusNo")
                            }
                            DropdownMenu(expanded = busExpanded, onDismissRequest = { busExpanded = false }) {
                                buses.forEach { bus ->
                                    DropdownMenuItem(text = { Text("Bus No. ${bus.busNumber}") }, onClick = { selectedBusNo = bus.busNumber; busExpanded = false })
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("Log Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(onClick = { typeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(type)
                            }
                            DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                types.forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Repair / Refill Details") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Service Cost (BDT)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serviceDate,
                    onValueChange = { serviceDate = it },
                    label = { Text("Service Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextServiceDate,
                    onValueChange = { nextServiceDate = it },
                    label = { Text("Next Maintenance Due Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val finalCost = cost.toDoubleOrNull() ?: 0.0
                            val log = MaintenanceLogEntity(
                                busNumber = selectedBusNo,
                                type = type,
                                details = details,
                                cost = finalCost,
                                date = serviceDate,
                                nextServiceDate = nextServiceDate,
                                isResolved = true
                            )
                            onLog(log)
                            onDismiss()
                        },
                        enabled = details.isNotEmpty(),
                        modifier = Modifier.weight(1f).testTag("maintenance_save_btn")
                    ) {
                        Text("Log Entry")
                    }
                }
            }
        }
    }
}

@Composable
fun SosAlertDialog(
    busNumber: Int,
    onDismiss: () -> Unit,
    onSubmitSos: (type: String, details: String) -> Unit
) {
    var emergencyType by remember { mutableStateOf("Mechanical Breakdown Alert") }
    val emergencies = listOf("Mechanical Breakdown Alert", "Accident Alert", "Medical Emergency Alert", "Road Block Alert")
    var expanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            modifier = Modifier.padding(12.dp).border(2.dp, Color.Red, RoundedCornerShape(16.dp)).testTag("sos_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = "SOS Alert", tint = Color.Red, modifier = Modifier.size(32.dp))
                    Text("Trigger SOS Emergency Alert", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Text(
                    "This triggers an immediate broadcast notification to all university authorities and students on this route. Please use carefully.",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )

                Text("Emergency Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(emergencyType)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        emergencies.forEach { em ->
                            DropdownMenuItem(text = { Text(em) }, onClick = { emergencyType = em; expanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe situation (e.g. tyre blowout near Matidali)") },
                    modifier = Modifier.fillMaxWidth().testTag("sos_desc_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Back")
                    }
                    Button(
                        onClick = {
                            onSubmitSos(emergencyType, description)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                        modifier = Modifier.weight(1.2f).testTag("sos_confirm_btn")
                    ) {
                        Text("BROADCAST SOS")
                    }
                }
            }
        }
    }
}
