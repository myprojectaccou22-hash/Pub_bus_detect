package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.repository.BusRepository
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class BusViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = BusRepository(database)

    // UI States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow("welcome") // welcome, login, signup, student_dashboard, driver_dashboard, admin_dashboard
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Scanned QR code Bus
    private val _scannedBus = MutableStateFlow<BusEntity?>(null)
    val scannedBus: StateFlow<BusEntity?> = _scannedBus.asStateFlow()

    // Flows from database
    val buses: StateFlow<List<BusEntity>> = repository.allBuses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routes: StateFlow<List<RouteEntity>> = repository.allRoutes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notices: StateFlow<List<NoticeEntity>> = repository.allNotices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val complaints: StateFlow<List<ComplaintEntity>> = repository.allComplaints.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val maintenanceLogs: StateFlow<List<MaintenanceLogEntity>> = repository.allMaintenanceLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lostFoundItems: StateFlow<List<LostFoundEntity>> = repository.allLostFoundItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val trips: StateFlow<List<TripHistoryEntity>> = repository.allTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search and Filter states
    var searchQuery = MutableStateFlow("")
        private set
    var selectedFilter = MutableStateFlow("All") // All, Running, Delayed, Reached Campus, Offline, Teacher Bus, Student Buses
        private set

    // Notification list (local runtime notifications)
    private val _notifications = MutableStateFlow<List<String>>(
        listOf(
            "Welcome to Pundra University Bus Tracking System!",
            "Bus No. 2 has started its trip from Satmatha."
        )
    )
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    // Favourites (Set of bus numbers)
    private val _favouriteBuses = MutableStateFlow<Set<Int>>(setOf(2, 3))
    val favouriteBuses: StateFlow<Set<Int>> = _favouriteBuses.asStateFlow()

    // Offline mode support flag
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // GPS Simulation progress map (Bus Number -> Route completion percent 0.0f to 1.0f)
    val busSimProgress = mutableStateMapOf<Int, Float>()

    // Current active trip for driver (Trip ID)
    private val _activeTripId = MutableStateFlow<Long?>(null)
    val activeTripId: StateFlow<Long?> = _activeTripId.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database
            repository.seedDatabaseIfEmpty()
            
            // Set up initial positions for running buses in simulation
            busSimProgress[1] = 0.3f
            busSimProgress[2] = 0.6f
            busSimProgress[3] = 0.1f
            busSimProgress[6] = 0.8f

            // Start GPS live tracking simulation loop
            startGpsSimulation()
        }
    }

    // Toggle offline mode and sync if needed
    fun setOfflineMode(enabled: Boolean) {
        _isOffline.value = enabled
        if (!enabled) {
            addNotification("Online: All local data synchronized successfully.")
        } else {
            addNotification("Offline Mode: Using locally cached bus routes and schedule.")
        }
    }

    fun addNotification(text: String) {
        _notifications.value = listOf(text) + _notifications.value
    }

    fun toggleFavourite(busNumber: Int) {
        val current = _favouriteBuses.value
        if (current.contains(busNumber)) {
            _favouriteBuses.value = current - busNumber
            addNotification("Removed Bus No. $busNumber from your favourites.")
        } else {
            _favouriteBuses.value = current + busNumber
            addNotification("Added Bus No. $busNumber to your favourites.")
        }
    }

    // Screens navigation
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setScannedBus(bus: BusEntity?) {
        _scannedBus.value = bus
    }

    // Authentication
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && user.password == password) {
                if (!user.isApproved) {
                    onResult(false, "Account is pending approval from admin.")
                } else {
                    _currentUser.value = user
                    // Redirect to corresponding dashboard
                    when (user.role) {
                        "admin" -> _currentScreen.value = "admin_dashboard"
                        "driver" -> _currentScreen.value = "driver_dashboard"
                        else -> _currentScreen.value = "student_dashboard"
                    }
                    addNotification("Logged in as ${user.fullName} (${user.role.uppercase()})")
                    onResult(true, "Success")
                }
            } else {
                onResult(false, "Invalid email or password.")
            }
        }
    }

    fun signUp(user: UserEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserById(user.id)
            if (existing != null) {
                onResult(false, "ID already registered.")
                return@launch
            }
            val existingEmail = repository.getUserByEmail(user.email)
            if (existingEmail != null) {
                onResult(false, "Email already registered.")
                return@launch
            }
            repository.insertUser(user)
            addNotification("Account created for ${user.fullName}. Welcome!")
            _currentUser.value = user
            when (user.role) {
                "admin" -> _currentScreen.value = "admin_dashboard"
                "driver" -> _currentScreen.value = "driver_dashboard"
                else -> _currentScreen.value = "student_dashboard"
            }
            onResult(true, "Success")
        }
    }

    fun logout() {
        val name = _currentUser.value?.fullName ?: "User"
        _currentUser.value = null
        _currentScreen.value = "welcome"
        addNotification("Logged out successfully.")
    }

    // Feedback & Complaints
    fun getComplaintsByUser(userId: String): Flow<List<ComplaintEntity>> {
        return repository.getComplaintsByUser(userId)
    }

    fun submitComplaint(busNumber: String, subject: String, message: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val complaint = ComplaintEntity(
                userId = user.id,
                userName = user.fullName,
                userRole = user.role,
                busNumber = busNumber,
                subject = subject,
                message = message,
                status = "Pending"
            )
            repository.insertComplaint(complaint)
            addNotification("Feedback submitted successfully. Status: Pending.")
        }
    }

    fun updateComplaintStatus(complaint: ComplaintEntity, status: String, feedback: String?) {
        viewModelScope.launch {
            val updated = complaint.copy(status = status, adminFeedback = feedback)
            repository.updateComplaint(updated)
            addNotification("Updated complaint status to $status.")
        }
    }

    // Notices / Announcements
    fun postNotice(title: String, content: String, targetGroup: String) {
        viewModelScope.launch {
            val notice = NoticeEntity(title = title, content = content, targetGroup = targetGroup)
            repository.insertNotice(notice)
            addNotification("Broadcast notice sent to: $targetGroup")
        }
    }

    fun deleteNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
            addNotification("Announcement removed.")
        }
    }

    // Lost & Found
    fun postLostFound(title: String, description: String, contactName: String, contactPhone: String, type: String) {
        viewModelScope.launch {
            val item = LostFoundEntity(
                title = title,
                description = description,
                contactName = contactName,
                contactPhone = contactPhone,
                type = type
            )
            repository.insertLostFound(item)
            addNotification("Posted lost/found notice for: $title")
        }
    }

    fun deleteLostFound(item: LostFoundEntity) {
        viewModelScope.launch {
            repository.deleteLostFound(item)
            addNotification("Lost/found post deleted.")
        }
    }

    // Driver Operations
    fun driverStartTrip(busNumber: Int, routeName: String, driverName: String) {
        viewModelScope.launch {
            val trip = TripHistoryEntity(
                busNumber = busNumber,
                driverName = driverName,
                routeName = routeName,
                startTime = "07:30 AM", // Simplified current time representation
                status = "Active",
                date = "2026-06-26"
            )
            val id = repository.insertTrip(trip)
            _activeTripId.value = id

            // Set bus status to Running
            val bus = repository.allBuses.firstOrNull()?.find { it.busNumber == busNumber }
            if (bus != null) {
                repository.updateBus(bus.copy(status = "Running", speed = 40f, lastUpdatedTime = System.currentTimeMillis()))
            }
            addNotification("Trip started for Bus No. $busNumber ($routeName).")
        }
    }

    fun driverEndTrip(busNumber: Int) {
        val tripId = _activeTripId.value
        viewModelScope.launch {
            if (tripId != null) {
                val tripsList = repository.allTrips.firstOrNull() ?: emptyList()
                val activeTrip = tripsList.find { it.id == tripId.toInt() }
                if (activeTrip != null) {
                    repository.updateTrip(activeTrip.copy(endTime = "08:15 AM", status = "Completed"))
                }
            }
            _activeTripId.value = null

            // Set bus status to Reached Campus
            val bus = repository.allBuses.firstOrNull()?.find { it.busNumber == busNumber }
            if (bus != null) {
                repository.updateBus(bus.copy(status = "Reached Campus", speed = 0f, estimatedArrivalTime = "Reached", lastUpdatedTime = System.currentTimeMillis()))
            }
            addNotification("Trip completed for Bus No. $busNumber. Reached Campus.")
        }
    }

    fun driverUpdateStatus(busNumber: Int, status: String, delayMinutes: Int) {
        viewModelScope.launch {
            val bus = repository.allBuses.firstOrNull()?.find { it.busNumber == busNumber }
            if (bus != null) {
                repository.updateBus(
                    bus.copy(
                        status = status,
                        delayMinutes = delayMinutes,
                        lastUpdatedTime = System.currentTimeMillis()
                    )
                )
                addNotification("Status updated for Bus No. $busNumber: $status ($delayMinutes mins late)")
            }
        }
    }

    fun driverUpdateCrowdSeats(busNumber: Int, crowd: String, seats: String) {
        viewModelScope.launch {
            repository.updateBusCrowdAndSeats(busNumber, crowd, seats)
            addNotification("Updated Bus No. $busNumber crowd: $crowd, seats: $seats.")
        }
    }

    // Admin Operations
    fun adminAddBus(bus: BusEntity) {
        viewModelScope.launch {
            repository.insertBus(bus)
            addNotification("Bus No. ${bus.busNumber} added successfully.")
        }
    }

    fun adminEditBus(bus: BusEntity) {
        viewModelScope.launch {
            repository.updateBus(bus)
            addNotification("Bus No. ${bus.busNumber} updated successfully.")
        }
    }

    fun adminDeleteBus(bus: BusEntity) {
        viewModelScope.launch {
            repository.deleteBus(bus)
            addNotification("Bus No. ${bus.busNumber} deleted.")
        }
    }

    fun adminAddRoute(route: RouteEntity) {
        viewModelScope.launch {
            repository.insertRoute(route)
            addNotification("Route added: ${route.routeName}")
        }
    }

    fun adminDeleteRoute(route: RouteEntity) {
        viewModelScope.launch {
            repository.deleteRoute(route)
            addNotification("Route deleted: ${route.routeName}")
        }
    }

    fun addMaintenanceLog(log: MaintenanceLogEntity) {
        viewModelScope.launch {
            repository.insertMaintenanceLog(log)
            addNotification("Maintenance log created for Bus No. ${log.busNumber}.")
        }
    }

    fun resolveMaintenanceLog(log: MaintenanceLogEntity) {
        viewModelScope.launch {
            repository.updateMaintenanceLog(log.copy(isResolved = true))
            addNotification("Maintenance issue resolved for Bus No. ${log.busNumber}.")
        }
    }

    // Emergency Features
    fun sendSosAlert(busNumber: Int, type: String, description: String) {
        viewModelScope.launch {
            // Create a critical Notice
            val title = "🚨 EMERGENCY: Bus No. $busNumber Alert!"
            val content = "Alert Level: $type. Details: $description. Direct transport authorities have been notified."
            val notice = NoticeEntity(title = title, content = content, targetGroup = "All")
            repository.insertNotice(notice)

            // Update bus status to Stopped or Maintenance
            val bus = repository.allBuses.firstOrNull()?.find { it.busNumber == busNumber }
            if (bus != null) {
                repository.updateBus(
                    bus.copy(
                        status = "Stopped",
                        speed = 0f,
                        estimatedArrivalTime = "Delayed (Emergency)",
                        lastUpdatedTime = System.currentTimeMillis()
                    )
                )
            }

            addNotification("🚨 SOS Alert Sent for Bus No. $busNumber: $type")
        }
    }

    // GPS Live Tracking Simulation
    private fun startGpsSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(3000) // Update every 3 seconds
                if (_isOffline.value) continue // Do not update live locations in offline mode

                val currentBuses = buses.value
                for (bus in currentBuses) {
                    if (bus.status == "Running") {
                        val progress = busSimProgress[bus.busNumber] ?: 0f
                        val newProgress = progress + 0.02f
                        
                        if (newProgress >= 1.0f) {
                            // Reached campus or end of route!
                            busSimProgress[bus.busNumber] = 0.0f
                            
                            viewModelScope.launch {
                                repository.updateBus(
                                    bus.copy(
                                        status = "Reached Campus",
                                        speed = 0f,
                                        estimatedArrivalTime = "Reached",
                                        lastUpdatedTime = System.currentTimeMillis()
                                    )
                                )
                                addNotification("Bus No. ${bus.busNumber} has reached its destination.")
                            }
                        } else {
                            busSimProgress[bus.busNumber] = newProgress
                            
                            // Simulate coordinates based on route completion
                            val (lat, lng) = getRouteSimCoords(bus.routeName, newProgress)
                            val simulatedSpeed = Random.nextInt(35, 60).toFloat()
                            
                            // Calculate simple ETA in minutes based on remaining progress
                            val totalTripMinutes = when {
                                bus.routeName.contains("Gobindaganj") -> 45
                                bus.routeName.contains("Sherpur") -> 50
                                bus.routeName.contains("Satmatha") -> 30
                                else -> 35
                            }
                            val etaMins = ((1.0f - newProgress) * totalTripMinutes).toInt()
                            val etaText = if (etaMins <= 0) "1 min" else "$etaMins mins"

                            // Trigger near-stop notification randomly
                            if (etaMins == 5 && Random.nextInt(0, 3) == 1) {
                                viewModelScope.launch {
                                    addNotification("🔔 Notification: Bus No. ${bus.busNumber} is near your selected pickup stop!")
                                }
                            }

                            viewModelScope.launch {
                                repository.updateBusLiveLocation(
                                    busNumber = bus.busNumber,
                                    status = "Running",
                                    lat = lat,
                                    lng = lng,
                                    speed = simulatedSpeed,
                                    eta = etaText,
                                    delay = bus.delayMinutes
                                )
                            }
                        }
                    } else if (bus.status == "Not Started" && Random.nextInt(0, 20) == 5) {
                        // Start some buses automatically in the demo
                        viewModelScope.launch {
                            repository.updateBus(
                                bus.copy(
                                    status = "Running",
                                    speed = 40f,
                                    lastUpdatedTime = System.currentTimeMillis()
                                )
                            )
                            busSimProgress[bus.busNumber] = 0.05f
                            addNotification("Bus No. ${bus.busNumber} has started its trip on ${bus.routeName}.")
                        }
                    }
                }
            }
        }
    }

    // Helper to calculate coordinates along routes (simulating 100x100 canvas grid)
    private fun getRouteSimCoords(routeName: String, progress: Float): Pair<Double, Double> {
        val startX: Double
        val startY: Double
        val endX = 50.0 // Campus X is always 50 (center)
        val endY = 40.0 // Campus Y is 40

        when {
            routeName.contains("Gobindaganj") -> {
                startX = 50.0
                startY = 0.0
            }
            routeName.contains("Satmatha") -> {
                startX = 50.0
                startY = 85.0
            }
            routeName.contains("Charmatha") -> {
                startX = 20.0
                startY = 70.0
            }
            routeName.contains("Sherpur") -> {
                startX = 50.0
                startY = 110.0
            }
            routeName.contains("Gabtoli") -> {
                startX = 85.0
                startY = 75.0
            }
            else -> {
                startX = 50.0
                startY = 85.0
            }
        }

        // Interpolate between start and end
        val currentX = startX + (endX - startX) * progress
        val currentY = startY + (endY - startY) * progress

        return Pair(currentX, currentY)
    }
}
