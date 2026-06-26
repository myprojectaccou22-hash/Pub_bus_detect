package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET isApproved = :approved WHERE id = :id")
    suspend fun updateUserApproval(id: String, approved: Boolean)
}

@Dao
interface BusDao {
    @Query("SELECT * FROM buses ORDER BY busNumber ASC")
    fun getAllBusesFlow(): Flow<List<BusEntity>>

    @Query("SELECT * FROM buses WHERE busNumber = :busNumber LIMIT 1")
    suspend fun getBusByNumber(busNumber: Int): BusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBus(bus: BusEntity)

    @Update
    suspend fun updateBus(bus: BusEntity)

    @Delete
    suspend fun deleteBus(bus: BusEntity)

    @Query("UPDATE buses SET status = :status, latitude = :lat, longitude = :lng, speed = :speed, estimatedArrivalTime = :eta, delayMinutes = :delay, lastUpdatedTime = :timestamp WHERE busNumber = :busNumber")
    suspend fun updateBusLiveLocation(busNumber: Int, status: String, lat: Double, lng: Double, speed: Float, eta: String, delay: Int, timestamp: Long)

    @Query("UPDATE buses SET crowdLevel = :crowd, seatAvailability = :seats WHERE busNumber = :busNumber")
    suspend fun updateBusCrowdAndSeats(busNumber: Int, crowd: String, seats: String)
}

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY id ASC")
    fun getAllRoutesFlow(): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Update
    suspend fun updateRoute(route: RouteEntity)

    @Delete
    suspend fun deleteRoute(route: RouteEntity)
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY timestamp DESC")
    fun getAllComplaintsFlow(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE userId = :userId ORDER BY timestamp DESC")
    fun getComplaintsByUserFlow(userId: String): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    @Delete
    suspend fun deleteComplaint(complaint: ComplaintEntity)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY timestamp DESC")
    fun getAllNoticesFlow(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Update
    suspend fun updateNotice(notice: NoticeEntity)

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity)
}

@Dao
interface MaintenanceLogDao {
    @Query("SELECT * FROM maintenance_logs ORDER BY date DESC")
    fun getAllLogsFlow(): Flow<List<MaintenanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MaintenanceLogEntity)

    @Update
    suspend fun updateLog(log: MaintenanceLogEntity)

    @Delete
    suspend fun deleteLog(log: MaintenanceLogEntity)
}

@Dao
interface LostFoundDao {
    @Query("SELECT * FROM lost_found ORDER BY timestamp DESC")
    fun getAllItemsFlow(): Flow<List<LostFoundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LostFoundEntity)

    @Delete
    suspend fun deleteItem(item: LostFoundEntity)
}

@Dao
interface TripHistoryDao {
    @Query("SELECT * FROM trip_history ORDER BY id DESC")
    fun getAllTripsFlow(): Flow<List<TripHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripHistoryEntity): Long

    @Update
    suspend fun updateTrip(trip: TripHistoryEntity)
}
