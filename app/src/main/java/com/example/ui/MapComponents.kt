package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusEntity
import com.example.viewmodel.BusViewModel
import kotlin.math.sqrt

@Composable
fun InteractiveMapSection(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier,
    selectedBusNumber: Int? = null,
    onBusClick: (BusEntity) -> Unit = {}
) {
    val buses by viewModel.buses.collectAsState()
    val routes by viewModel.routes.collectAsState()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var selectedBusOnMap by remember { mutableStateOf<BusEntity?>(null) }
    
    // Pulse animation for active moving buses
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Sync selectedBusNumber prop if changed from outside (dashboard clicks)
    LaunchedEffect(selectedBusNumber, buses) {
        if (selectedBusNumber != null) {
            selectedBusOnMap = buses.find { it.busNumber == selectedBusNumber }
            // Center map on selected bus coordinate (scaled coordinates)
            selectedBusOnMap?.let { bus ->
                offset = Offset(
                    x = (300f - bus.latitude.toFloat() * 6f),
                    y = (300f - bus.longitude.toFloat() * 6f)
                )
                scale = 1.2f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("interactive_map_container")
    ) {
        // Map Canvas Renderer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                        offset = offset + pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Coordinates are normalized relative to a 100x100 grid inside ViewModel.
            // Let's stretch them to fit our canvas dimension (e.g. multiply X by width/100, Y by height/100)
            fun mapX(x: Float): Float = (x / 100f) * canvasW
            fun mapY(y: Float): Float = (y / 150f) * canvasH

            // 1. Draw Map Background grid or water bodies
            drawRect(
                color = Color(0xFFF1F5F9)
            )

            // Draw simulated Karatoa River flowing on the right side of Bogura
            val riverPoints = listOf(
                Offset(mapX(85f), mapY(-10f)),
                Offset(mapX(80f), mapY(25f)),
                Offset(mapX(78f), mapY(55f)),
                Offset(mapX(75f), mapY(85f)),
                Offset(mapX(70f), mapY(120f)),
                Offset(mapX(73f), mapY(150f))
            )
            for (i in 0 until riverPoints.size - 1) {
                drawLine(
                    color = Color(0xFF93C5FD),
                    start = riverPoints[i],
                    end = riverPoints[i+1],
                    strokeWidth = 14f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            // 2. Draw Roads (Gobindaganj to Sherpur Hwy, Satmatha connections, etc.)
            // Main North-South Highway (Rangpur Road)
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(mapX(50f), mapY(-20f)),
                end = Offset(mapX(50f), mapY(140f)),
                strokeWidth = 8f
            )
            // Highway white dash lines
            drawLine(
                color = Color.White,
                start = Offset(mapX(50f), mapY(-20f)),
                end = Offset(mapX(50f), mapY(140f)),
                strokeWidth = 1.5f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Satmatha to Charmatha road
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(mapX(50f), mapY(85f)),
                end = Offset(mapX(20f), mapY(70f)),
                strokeWidth = 6f
            )

            // Satmatha to Gabtoli road
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(mapX(50f), mapY(85f)),
                end = Offset(mapX(85f), mapY(75f)),
                strokeWidth = 6f
            )

            // Matidali bypass
            drawLine(
                color = Color(0xFFcbd5e1),
                start = Offset(mapX(20f), mapY(70f)),
                end = Offset(mapX(50f), mapY(60f)),
                strokeWidth = 5f
            )

            // 3. Draw Route Lines (translucent colored traces)
            routes.forEach { route ->
                val color = when (route.id) {
                    1, 2 -> Color(0x7F2563EB) // Blue
                    3 -> Color(0x7F10B981) // Green
                    4 -> Color(0x7F8B5CF6) // Purple
                    5 -> Color(0x7FF59E0B) // Amber
                    else -> Color(0x7FE11D48) // Red
                }
                
                // Tracing coordinate approximations
                val stopsCoords = getStopsCoordsList(route.routeName)
                for (idx in 0 until stopsCoords.size - 1) {
                    drawLine(
                        color = color,
                        start = Offset(mapX(stopsCoords[idx].x), mapY(stopsCoords[idx].y)),
                        end = Offset(mapX(stopsCoords[idx+1].x), mapY(stopsCoords[idx+1].y)),
                        strokeWidth = 12f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // 4. Draw Stops/Landmarks
            val landmarks = listOf(
                Landmark("Pundra Univ Campus", 50f, 40f, true),
                Landmark("Satmatha", 50f, 85f),
                Landmark("Gobindaganj", 50f, 5f),
                Landmark("Mokamtola", 50f, 20f),
                Landmark("Charmatha", 20f, 70f),
                Landmark("Banani", 35f, 95f),
                Landmark("Sherpur", 50f, 125f),
                Landmark("Gabtoli", 85f, 75f),
                Landmark("Matidali", 50f, 60f)
            )

            landmarks.forEach { lm ->
                val xCoord = mapX(lm.x)
                val yCoord = mapY(lm.y)
                
                if (lm.isCampus) {
                    // Draw specialized Campus shield badge
                    drawCircle(Color(0xFF1A4B8F), radius = 14f, center = Offset(xCoord, yCoord))
                    drawCircle(Color.White, radius = 9f, center = Offset(xCoord, yCoord))
                    drawCircle(Color(0xFFFF9800), radius = 5f, center = Offset(xCoord, yCoord))
                } else {
                    // Draw generic stop marker
                    drawCircle(Color(0xFF1E293B), radius = 7f, center = Offset(xCoord, yCoord))
                    drawCircle(Color.White, radius = 4f, center = Offset(xCoord, yCoord))
                }
            }

            // 5. Draw Running/Delayed/Offline Buses
            buses.forEach { bus ->
                if (bus.status != "Offline") {
                    val bx = mapX(bus.latitude.toFloat())
                    val by = mapY(bus.longitude.toFloat())

                    // Draw Live pulse for running bus
                    if (bus.status == "Running") {
                        drawCircle(
                            color = Color(0x4D10B981),
                            radius = pulseRadius,
                            center = Offset(bx, by)
                        )
                    }

                    // Base color representing status
                    val busColor = when (bus.status) {
                        "Running" -> Color(0xFF10B981) // Green
                        "Delayed" -> Color(0xFFEF4444) // Red
                        "Reached Campus" -> Color(0xFF1A4B8F) // Blue
                        "Maintenance" -> Color(0xFFF59E0B) // Amber
                        else -> Color(0xFF64748B) // Slate
                    }

                    // Outer shadow/border
                    drawCircle(
                        color = Color.White,
                        radius = 16f,
                        center = Offset(bx, by)
                    )

                    // Fill Circle
                    drawCircle(
                        color = busColor,
                        radius = 13f,
                        center = Offset(bx, by)
                    )
                }
            }
        }

        // 6. Non-canvas overlay UI (interactive elements, labels and tooltip card)
        // Canvas Labels (Rendered as overlay Composables to avoid canvas text scaling blurring)
        Box(modifier = Modifier.fillMaxSize()) {
            val canvasW = 300.dp // Approximate overlay viewport width
            val canvasH = 380.dp

            fun mapOverlayX(x: Float): Float = (x / 100f) * 360f // Approximate matching scale
            fun mapOverlayY(y: Float): Float = (y / 150f) * 380f

            // Floating Campus Shield
            Box(
                modifier = Modifier
                    .offset(x = 145.dp, y = 80.dp) // Aligns with Campus grid coords
                    .background(Color(0xFF1A4B8F), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    "PUST Campus",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }

            // Tap-to-select Bus overlay buttons on exact locations
            buses.forEach { bus ->
                if (bus.status != "Offline") {
                    // Let's compute approx layout coordinates corresponding to translation
                    val xPos = (bus.latitude.toFloat() / 100f) * 320f
                    val yPos = (bus.longitude.toFloat() / 150f) * 380f

                    IconButton(
                        onClick = {
                            selectedBusOnMap = bus
                            onBusClick(bus)
                        },
                        modifier = Modifier
                            .offset(x = xPos.dp - 12.dp, y = yPos.dp - 12.dp)
                            .size(28.dp)
                            .testTag("bus_marker_${bus.busNumber}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        ) {
                            Text(
                                text = bus.busNumber.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Floating Control Panel (Reset view, Zoom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { scale = (scale + 0.2f).coerceIn(0.5f, 3.0f) },
                modifier = Modifier.size(40.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF1A4B8F)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick = { scale = (scale - 0.2f).coerceIn(0.5f, 3.0f) },
                modifier = Modifier.size(40.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF1A4B8F)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick = {
                    scale = 1.0f
                    offset = Offset.Zero
                    selectedBusOnMap = null
                },
                modifier = Modifier.size(40.dp),
                containerColor = Color(0xFF1A4B8F),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Center View", modifier = Modifier.size(20.dp))
            }
        }

        // Floating Demo Mode Indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
                Text(
                    "SIMULATOR MODE ACTIVE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        // Selected Bus Detail Card Overlay
        selectedBusOnMap?.let { bus ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .width(220.dp)
                    .testTag("map_bus_tooltip"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Bus No. ${bus.busNumber}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A4B8F),
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = { selectedBusOnMap = null },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close Detail", tint = Color.Gray, modifier = Modifier.size(12.dp))
                        }
                    }
                    Text(
                        bus.busName,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text("Route: ${bus.routeName}", fontSize = 11.sp)
                    Text("Driver: ${bus.assignedDriverName}", fontSize = 11.sp)
                    Text("Speed: ${bus.speed.toInt()} km/h", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("ETA to Campus: ${bus.estimatedArrivalTime}", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                when (bus.status) {
                                    "Running" -> Color(0xFFDCFCE7)
                                    "Delayed" -> Color(0xFFFEE2E2)
                                    "Reached Campus" -> Color(0xFFDBEAFE)
                                    else -> Color(0xFFF1F5F9)
                                }, RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            bus.status.uppercase(),
                            color = when (bus.status) {
                                "Running" -> Color(0xFF15803D)
                                "Delayed" -> Color(0xFFB91C1C)
                                "Reached Campus" -> Color(0xFF1E40AF)
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

// Coordinate Point structures for math
data class MapPoint(val x: Float, val y: Float)

fun getStopsCoordsList(routeName: String): List<MapPoint> {
    return when {
        routeName.contains("Gobindaganj") -> listOf(
            MapPoint(50f, 5f),
            MapPoint(50f, 20f),
            MapPoint(45f, 32f),
            MapPoint(50f, 40f)
        )
        routeName.contains("Satmatha") -> listOf(
            MapPoint(50f, 85f),
            MapPoint(30f, 75f),
            MapPoint(50f, 60f),
            MapPoint(50f, 48f),
            MapPoint(50f, 40f)
        )
        routeName.contains("Charmatha") -> listOf(
            MapPoint(20f, 70f),
            MapPoint(35f, 85f),
            MapPoint(50f, 60f),
            MapPoint(50f, 48f),
            MapPoint(50f, 40f)
        )
        routeName.contains("Sherpur") -> listOf(
            MapPoint(50f, 125f),
            MapPoint(35f, 95f),
            MapPoint(50f, 85f),
            MapPoint(50f, 60f),
            MapPoint(50f, 40f)
        )
        routeName.contains("Gabtoli") -> listOf(
            MapPoint(85f, 75f),
            MapPoint(70f, 80f),
            MapPoint(50f, 85f),
            MapPoint(50f, 60f),
            MapPoint(50f, 40f)
        )
        else -> listOf(
            MapPoint(50f, 85f),
            MapPoint(50f, 60f),
            MapPoint(50f, 40f)
        )
    }
}

data class Landmark(
    val name: String,
    val x: Float,
    val y: Float,
    val isCampus: Boolean = false
)
