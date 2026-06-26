package com.example.repository

import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class BusRepository(private val db: AppDatabase) {

    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsersFlow()
    val allBuses: Flow<List<BusEntity>> = db.busDao().getAllBusesFlow()
    val allRoutes: Flow<List<RouteEntity>> = db.routeDao().getAllRoutesFlow()
    val allComplaints: Flow<List<ComplaintEntity>> = db.complaintDao().getAllComplaintsFlow()
    val allNotices: Flow<List<NoticeEntity>> = db.noticeDao().getAllNoticesFlow()
    val allMaintenanceLogs: Flow<List<MaintenanceLogEntity>> = db.maintenanceLogDao().getAllLogsFlow()
    val allLostFoundItems: Flow<List<LostFoundEntity>> = db.lostFoundDao().getAllItemsFlow()
    val allTrips: Flow<List<TripHistoryEntity>> = db.tripHistoryDao().getAllTripsFlow()

    fun getComplaintsByUser(userId: String): Flow<List<ComplaintEntity>> {
        return db.complaintDao().getComplaintsByUserFlow(userId)
    }

    suspend fun getUserById(id: String): UserEntity? = withContext(Dispatchers.IO) {
        db.userDao().getUserById(id)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        db.userDao().getUserByEmail(email)
    }

    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        db.userDao().insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        db.userDao().updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) = withContext(Dispatchers.IO) {
        db.userDao().deleteUser(user)
    }

    suspend fun approveUser(id: String, approved: Boolean) = withContext(Dispatchers.IO) {
        db.userDao().updateUserApproval(id, approved)
    }

    suspend fun insertBus(bus: BusEntity) = withContext(Dispatchers.IO) {
        db.busDao().insertBus(bus)
    }

    suspend fun updateBus(bus: BusEntity) = withContext(Dispatchers.IO) {
        db.busDao().updateBus(bus)
    }

    suspend fun deleteBus(bus: BusEntity) = withContext(Dispatchers.IO) {
        db.busDao().deleteBus(bus)
    }

    suspend fun updateBusLiveLocation(
        busNumber: Int,
        status: String,
        lat: Double,
        lng: Double,
        speed: Float,
        eta: String,
        delay: Int
    ) = withContext(Dispatchers.IO) {
        db.busDao().updateBusLiveLocation(busNumber, status, lat, lng, speed, eta, delay, System.currentTimeMillis())
    }

    suspend fun updateBusCrowdAndSeats(busNumber: Int, crowd: String, seats: String) = withContext(Dispatchers.IO) {
        db.busDao().updateBusCrowdAndSeats(busNumber, crowd, seats)
    }

    suspend fun insertRoute(route: RouteEntity) = withContext(Dispatchers.IO) {
        db.routeDao().insertRoute(route)
    }

    suspend fun updateRoute(route: RouteEntity) = withContext(Dispatchers.IO) {
        db.routeDao().updateRoute(route)
    }

    suspend fun deleteRoute(route: RouteEntity) = withContext(Dispatchers.IO) {
        db.routeDao().deleteRoute(route)
    }

    suspend fun insertComplaint(complaint: ComplaintEntity) = withContext(Dispatchers.IO) {
        db.complaintDao().insertComplaint(complaint)
    }

    suspend fun updateComplaint(complaint: ComplaintEntity) = withContext(Dispatchers.IO) {
        db.complaintDao().updateComplaint(complaint)
    }

    suspend fun insertNotice(notice: NoticeEntity) = withContext(Dispatchers.IO) {
        db.noticeDao().insertNotice(notice)
    }

    suspend fun deleteNotice(notice: NoticeEntity) = withContext(Dispatchers.IO) {
        db.noticeDao().deleteNotice(notice)
    }

    suspend fun insertMaintenanceLog(log: MaintenanceLogEntity) = withContext(Dispatchers.IO) {
        db.maintenanceLogDao().insertLog(log)
    }

    suspend fun updateMaintenanceLog(log: MaintenanceLogEntity) = withContext(Dispatchers.IO) {
        db.maintenanceLogDao().updateLog(log)
    }

    suspend fun deleteMaintenanceLog(log: MaintenanceLogEntity) = withContext(Dispatchers.IO) {
        db.maintenanceLogDao().deleteLog(log)
    }

    suspend fun insertLostFound(item: LostFoundEntity) = withContext(Dispatchers.IO) {
        db.lostFoundDao().insertItem(item)
    }

    suspend fun deleteLostFound(item: LostFoundEntity) = withContext(Dispatchers.IO) {
        db.lostFoundDao().deleteItem(item)
    }

    suspend fun insertTrip(trip: TripHistoryEntity): Long = withContext(Dispatchers.IO) {
        db.tripHistoryDao().insertTrip(trip)
    }

    suspend fun updateTrip(trip: TripHistoryEntity) = withContext(Dispatchers.IO) {
        db.tripHistoryDao().updateTrip(trip)
    }

    // Seeding function to make demo fully operational out of the box
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingBuses = db.busDao().getAllBusesFlow().firstOrNull()
        if (existingBuses.isNullOrEmpty()) {
            // Seed Routes
            val defaultRoutes = listOf(
                RouteEntity(1, "Gobindaganj to Campus", 1, "Gobindaganj", "Campus", "Gobindaganj, Mokamtola, Shibganj, Gokul, Campus", "45 mins", "07:15 AM", "02:15 PM"),
                RouteEntity(2, "Campus to Gobindaganj", 7, "Campus", "Gobindaganj", "Campus, Gokul, Shibganj, Mokamtola, Gobindaganj", "45 mins", "02:15 PM", "07:15 AM"),
                RouteEntity(3, "Satmatha to Campus", 2, "Satmatha", "Campus", "Satmatha, Tinmatha, Matidali, Gokul, Campus", "30 mins", "07:30 AM", "01:30 PM"),
                RouteEntity(4, "Charmatha to Campus", 4, "Charmatha", "Campus", "Charmatha, Banani, Matidali, Gokul, Campus", "35 mins", "07:40 AM", "02:00 PM"),
                RouteEntity(5, "Sherpur to Campus", 3, "Sherpur", "Campus", "Sherpur, Banani, Satmatha, Matidali, Campus", "50 mins", "07:00 AM", "01:45 PM"),
                RouteEntity(6, "Gabtoli to Campus", 5, "Gabtoli", "Campus", "Gabtoli, Chelopara, Satmatha, Matidali, Campus", "40 mins", "07:10 AM", "02:10 PM"),
                RouteEntity(7, "Satmatha to Campus (Trip B)", 6, "Satmatha", "Campus", "Satmatha, Matidali, Gokul, Campus", "25 mins", "08:15 AM", "03:15 PM")
            )
            for (route in defaultRoutes) {
                db.routeDao().insertRoute(route)
            }

            // Seed Buses
            val defaultBuses = listOf(
                BusEntity(1, "Pundra Teacher Express", "Abul Kalam", "Gobindaganj to Campus", "Running", 50.0, 10.0, "15 mins", 3, "Low Crowd", "Seats Available", System.currentTimeMillis(), 42f, isTeacherOnly = true),
                BusEntity(2, "Pundra Student Wing 1", "Jalal Uddin", "Satmatha to Campus", "Running", 50.0, 55.0, "8 mins", 0, "Medium Crowd", "Few Seats Available", System.currentTimeMillis(), 48f),
                BusEntity(3, "Pundra Student Wing 2", "Anisur Rahman", "Sherpur to Campus", "Running", 50.0, 95.0, "12 mins", 4, "High Crowd", "No Seats Available", System.currentTimeMillis(), 35f),
                BusEntity(4, "Pundra Student Wing 3", "Rabiul Islam", "Charmatha to Campus", "Not Started", 30.0, 65.0, "Scheduled", 0, "Low Crowd", "Seats Available", System.currentTimeMillis(), 0f),
                BusEntity(5, "Pundra Student Wing 4", "Kamrul Hasan", "Gabtoli to Campus", "Offline", 80.0, 60.0, "--", 0, "Low Crowd", "Seats Available", System.currentTimeMillis(), 0f),
                BusEntity(6, "Pundra Student Wing 5", "Milon Mia", "Satmatha to Campus", "Delayed", 50.0, 75.0, "22 mins", 12, "Medium Crowd", "Few Seats Available", System.currentTimeMillis(), 15f),
                BusEntity(7, "Pundra Student Wing 6", "Rafiqul Islam", "Campus to Gobindaganj", "Not Started", 50.0, 25.0, "Scheduled", 0, "Low Crowd", "Seats Available", System.currentTimeMillis(), 0f),
                BusEntity(8, "Pundra Student Wing 7", "Selim Reza", "Sherpur to Campus", "Maintenance", 50.0, 120.0, "--", 0, "Low Crowd", "Seats Available", System.currentTimeMillis(), 0f)
            )
            for (bus in defaultBuses) {
                db.busDao().insertBus(bus)
            }

            // Seed Accounts
            val defaultUsers = listOf(
                UserEntity("student@pundra.edu", "student", "Tanvir Ahmed", "01712345678", "student@pundra.edu", "student123", "CSE", "Bus No. 3", "Sherpur to Campus", "Banani"),
                UserEntity("teacher@pundra.edu", "teacher", "Prof. Dr. Mizanur Rahman", "01812345678", "teacher@pundra.edu", "teacher123", "EEE", "Bus No. 1", "Gobindaganj to Campus", "Mokamtola"),
                UserEntity("driver@pundra.edu", "driver", "Jalal Uddin", "01912345678", "driver@pundra.edu", "driver123", null, "2", "Satmatha to Campus", null),
                UserEntity("admin@pundra.edu", "admin", "Engr. Alimul Razi", "01512345678", "admin@pundra.edu", "admin123")
            )
            for (user in defaultUsers) {
                db.userDao().insertUser(user)
            }

            // Seed Notices
            val defaultNotices = listOf(
                NoticeEntity(0, "Bus No. 8 Under Maintenance", "Please note that Bus No. 8 is undergoing brake serving today. Students assigned to Bus No. 8 please board Bus No. 3 instead.", "All", System.currentTimeMillis() - 7200000),
                NoticeEntity(0, "Route Change Alert for Gobindaganj", "Due to road work near Mokamtola, Teacher Bus No. 1 will detour through bypass route. Expect a 10 minutes delay.", "Route: Gobindaganj to Campus", System.currentTimeMillis() - 3600000),
                NoticeEntity(0, "Notice for Drivers: Pre-trip Checklist", "All drivers are requested to complete and submit the daily inspection checklist inside driver dashboard before starting any trip.", "Drivers", System.currentTimeMillis())
            )
            for (notice in defaultNotices) {
                db.noticeDao().insertNotice(notice)
            }

            // Seed Complaints
            val defaultComplaints = listOf(
                ComplaintEntity(0, "student@pundra.edu", "Tanvir Ahmed", "student", "Bus No. 3", "Extreme Overcrowding", "Bus No. 3 has been extremely crowded during morning trips. Please allocate another bus for Sherpur route.", "Pending"),
                ComplaintEntity(0, "teacher@pundra.edu", "Prof. Dr. Mizanur Rahman", "teacher", "Bus No. 1", "AC cooling issue", "The AC in Bus No. 1 was not cooling properly yesterday.", "Resolved", "AC compressor has been fully serviced. It is working now.")
            )
            for (complaint in defaultComplaints) {
                db.complaintDao().insertComplaint(complaint)
            }

            // Seed Maintenance Logs
            val defaultLogs = listOf(
                MaintenanceLogEntity(0, 1, "Air Conditioner Issue", "AC Compressor replacement & Freon refill", 12500.0, "2026-06-25", "2026-09-25"),
                MaintenanceLogEntity(0, 8, "Brake Service", "Brake pads replacement & fluid refill", 4500.0, "2026-06-26", "2026-08-26"),
                MaintenanceLogEntity(0, 3, "Fuel Refill", "80 Litres Octane Refill", 10400.0, "2026-06-26", "N/A")
            )
            for (log in defaultLogs) {
                db.maintenanceLogDao().insertLog(log)
            }

            // Seed Lost & Found
            val defaultLostFound = listOf(
                LostFoundEntity(0, "Calculus 2 Textbook", "Found a calculus textbook on Bus No. 3 Satmatha route yesterday afternoon.", "Anika Tahsin", "01788889999", "Found"),
                LostFoundEntity(0, "Cased Sunglasses", "Lost a black Ray-Ban sunglasses case in Bus No. 2 around 8 AM.", "Abrar Kabir", "01322221111", "Lost")
            )
            for (item in defaultLostFound) {
                db.lostFoundDao().insertItem(item)
            }

            // Seed some default completed trips for reporting
            val defaultTrips = listOf(
                TripHistoryEntity(0, 2, "Jalal Uddin", "Satmatha to Campus", "07:30 AM", "08:05 AM", "Completed", "2026-06-25"),
                TripHistoryEntity(0, 1, "Abul Kalam", "Gobindaganj to Campus", "07:15 AM", "08:02 AM", "Completed", "2026-06-25"),
                TripHistoryEntity(0, 3, "Anisur Rahman", "Sherpur to Campus", "07:00 AM", "07:54 AM", "Completed", "2026-06-25"),
                TripHistoryEntity(0, 6, "Milon Mia", "Satmatha to Campus", "08:15 AM", "08:55 AM", "Completed", "2026-06-25")
            )
            for (trip in defaultTrips) {
                db.tripHistoryDao().insertTrip(trip)
            }
        }
    }
}
