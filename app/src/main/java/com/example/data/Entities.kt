package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // studentId, teacherId, driverId, or email/username for admin
    val role: String, // "student", "teacher", "driver", "admin"
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String,
    val department: String? = null,
    val busNumber: String? = null,
    val route: String? = null,
    val pickupStop: String? = null,
    val isApproved: Boolean = true // auto-approve for convenience but managed by admin
)

@Entity(tableName = "buses")
data class BusEntity(
    @PrimaryKey val busNumber: Int, // 1 to 8
    val busName: String,
    val assignedDriverName: String,
    val routeName: String,
    val status: String, // "Running", "Reached Campus", "Delayed", "Early", "Not Started", "Stopped", "Offline", "Maintenance"
    val latitude: Double,
    val longitude: Double,
    val estimatedArrivalTime: String, // e.g., "12 mins" or "10:30 AM"
    val delayMinutes: Int,
    val crowdLevel: String, // "Low Crowd", "Medium Crowd", "High Crowd", "Full"
    val seatAvailability: String, // "Seats Available", "Few Seats Available", "No Seats Available"
    val lastUpdatedTime: Long,
    val speed: Float, // in km/h
    val isTeacherOnly: Boolean = false
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: Int,
    val routeName: String,
    val busNumber: Int,
    val startingPoint: String,
    val destination: String,
    val stops: String, // comma-separated stops, e.g. "Satmatha, Matidali, Tinmatha, Campus"
    val estimatedTravelTime: String,
    val departureTime: String,
    val returnTime: String
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val userRole: String,
    val busNumber: String,
    val subject: String,
    val message: String,
    val status: String, // "Pending", "Under Review", "Resolved", "Rejected"
    val adminFeedback: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val targetGroup: String, // "All", "Students", "Teachers", "Drivers", or specific route name
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "maintenance_logs")
data class MaintenanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val busNumber: Int,
    val type: String, // "Fuel Refill", "Oil Change", "Tyre Change", "Brake Service", "Engine Issue", "Battery Issue", "Air Conditioner Issue", "Cleaning Status", "General Repair"
    val details: String,
    val cost: Double,
    val date: String,
    val nextServiceDate: String,
    val isResolved: Boolean = true
)

@Entity(tableName = "lost_found")
data class LostFoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val contactName: String,
    val contactPhone: String,
    val type: String, // "Lost", "Found"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "trip_history")
data class TripHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val busNumber: Int,
    val driverName: String,
    val routeName: String,
    val startTime: String,
    val endTime: String? = null,
    val status: String, // "Completed", "Active", "Cancelled"
    val date: String
)
