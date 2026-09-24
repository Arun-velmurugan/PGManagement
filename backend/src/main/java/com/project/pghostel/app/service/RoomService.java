package com.project.pghostel.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.pghostel.app.entity.Room;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.repository.RoomRepository;
import com.project.pghostel.app.repository.TenantRepository;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;

    public RoomService(RoomRepository roomRepository,
            TenantRepository tenantRepository) {

        this.roomRepository = roomRepository;
        this.tenantRepository = tenantRepository;
    }

    // ==========================================
    // GET ALL ROOMS
    // ==========================================

    public List<Room> getAllRooms() {

        List<Room> rooms = roomRepository.findAllByOrderByIdAsc();

        for (Room room : rooms) {

            updateRoomStatus(room);

        }

        return rooms;
    }

    // ==========================================
    // GET ROOM BY ID
    // ==========================================

    public Room getRoomById(Long id) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        updateRoomStatus(room);

        return room;
    }

    // ==========================================
    // CREATE ROOM
    // ==========================================

    public Room createRoom(Room room) {

        if (roomRepository.existsByRoomNo(room.getRoomNo())) {

            throw new RuntimeException(
                    "Room number already exists.");
        }

        setRent(room);

        room.setStatus("AVAILABLE");

        return roomRepository.save(room);
    }

    // ==========================================
    // UPDATE ROOM
    // ==========================================

    public Room updateRoom(Long id, Room newRoom) {

        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found"));

        // Count ONLY ACTIVE tenants
        long tenantCount =
                tenantRepository.countByRoomIdAndStatus(
                        id, "ACTIVE");

        String newType =
                newRoom.getRoomType().toUpperCase();

        // ======================================
        // 4 SHARE -> 2 SHARE
        // ======================================

        if (newType.equals("2 SHARE")
                && tenantCount > 2) {

            throw new RuntimeException(
                    "Already this room has above 2 tenants, " +
                    "so the room type can't change from " +
                    "4 SHARE to 2 SHARE.");
        }

        // ======================================
        // ROOM NUMBER
        // ======================================

        existingRoom.setRoomNo(
                newRoom.getRoomNo());

        // ======================================
        // ROOM TYPE
        // ======================================

        existingRoom.setRoomType(newType);

        // ======================================
        // AUTOMATIC RENT
        // ======================================

        setRent(existingRoom);

        // Update active tenant rents
        // according to new room type
        updateTenantRents(existingRoom);

        // ======================================
        // STATUS
        // ======================================

        updateRoomStatus(existingRoom);

        return roomRepository.save(existingRoom);
    }

    // ==========================================
    // DELETE ROOM
    // ==========================================

    public void deleteRoom(Long id) {

        // Only ACTIVE tenants should block deletion
        long tenantCount =
                tenantRepository.countByRoomIdAndStatus(
                        id, "ACTIVE");

        if (tenantCount > 0) {

            throw new RuntimeException(
                    "Cannot delete room because tenants are assigned to it.");
        }

        roomRepository.deleteById(id);
    }

    // ==========================================
    // SET RENT
    // ==========================================

    private void setRent(Room room) {

        if (room.getRoomType()
                .equalsIgnoreCase("2 SHARE")) {

            room.setRent(5000.0);

        } else if (room.getRoomType()
                .equalsIgnoreCase("4 SHARE")) {

            room.setRent(4000.0);

        } else {

            throw new RuntimeException(
                    "Invalid room type.");
        }
    }

    // ==========================================
    // UPDATE ROOM STATUS
    // ==========================================

    private void updateRoomStatus(Room room) {

        // IMPORTANT:
        // Count ONLY ACTIVE tenants
        long tenantCount =
                tenantRepository.countByRoomIdAndStatus(
                        room.getId(), "ACTIVE");

        int capacity =
                getCapacity(room.getRoomType());

        if (tenantCount >= capacity) {

            room.setStatus("OCCUPIED");

        } else {

            room.setStatus("AVAILABLE");
        }
    }

    // ==========================================
    // GET CAPACITY
    // ==========================================

    public int getCapacity(String roomType) {

        if (roomType.equalsIgnoreCase("2 SHARE")) {

            return 2;
        }

        if (roomType.equalsIgnoreCase("4 SHARE")) {

            return 4;
        }

        throw new RuntimeException(
                "Invalid room type.");
    }

    // ==========================================
    // UPDATE ROOM STATUS AFTER TENANT CHANGE
    // ==========================================

    public void updateRoomStatusAfterTenantChange(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found."));

        updateRoomStatus(room);

        roomRepository.save(room);
    }

    // ==========================================
    // CHECK ROOM CAPACITY
    // ==========================================

    public void checkRoomCapacity(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found"));

        // Count ONLY ACTIVE tenants
        long tenantCount =
                tenantRepository.countByRoomIdAndStatus(
                        roomId, "ACTIVE");

        int capacity =
                getCapacity(room.getRoomType());

        if (tenantCount >= capacity) {

            throw new RuntimeException(
                    "Room already occupied. " +
                    room.getRoomType() +
                    " room can have maximum " +
                    capacity +
                    " tenants.");
        }
    }

    // ==========================================
    // UPDATE ACTIVE TENANT RENTS
    // ==========================================

    private void updateTenantRents(Room room) {

        List<Tenant> tenants =
                tenantRepository.findByRoomId(
                        room.getId());

        for (Tenant tenant : tenants) {

            // Only active tenants
            if (!"ACTIVE".equalsIgnoreCase(
                    tenant.getStatus())) {

                continue;
            }

            double roomRent;

            if ("2 SHARE".equalsIgnoreCase(
                    room.getRoomType())) {

                roomRent = 5000.0;

            } else if ("4 SHARE".equalsIgnoreCase(
                    room.getRoomType())) {

                roomRent = 4000.0;

            } else {

                throw new RuntimeException(
                        "Invalid room type.");
            }

            double foodCharge = 0.0;

            if ("WITH_FOOD".equalsIgnoreCase(
                    tenant.getFoodPlan())) {

                foodCharge = 2500.0;
            }

            tenant.setRent(
                    roomRent + foodCharge);

            tenantRepository.save(tenant);
        }
    }
}