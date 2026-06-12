package com.buhlergroup.pepper.action.navigation.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NavigationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScan(RoomScanEntity scan);

    @Query("SELECT * FROM room_scans ORDER BY created_at DESC")
    List<RoomScanEntity> getScans();

    @Query("SELECT * FROM room_scans WHERE id = :id LIMIT 1")
    RoomScanEntity getScan(String id);

    @Query("DELETE FROM room_scans WHERE id = :id")
    void deleteScan(String id);

    @Insert
    long insertWaypoint(WaypointEntity waypoint);

    @Query("SELECT * FROM waypoints WHERE scan_id = :scanId ORDER BY created_at ASC")
    List<WaypointEntity> getWaypoints(String scanId);

    @Query("SELECT * FROM waypoints WHERE scan_id = :scanId AND type = :type ORDER BY created_at DESC LIMIT 1")
    WaypointEntity getWaypointByType(String scanId, String type);

    @Query("DELETE FROM waypoints WHERE id = :id")
    void deleteWaypoint(long id);

    @Query("DELETE FROM waypoints WHERE scan_id = :scanId")
    void deleteWaypointsForScan(String scanId);
}
