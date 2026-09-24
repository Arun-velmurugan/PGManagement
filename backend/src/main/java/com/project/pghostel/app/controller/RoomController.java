package com.project.pghostel.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.pghostel.app.entity.Room;
import com.project.pghostel.app.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }


    @GetMapping
    public List<Room> getAllRooms() {

        return roomService.getAllRooms();
    }


    @GetMapping("/{id}")
    public Room getRoom(@PathVariable Long id) {

        return roomService.getRoomById(id);
    }

    


    @PostMapping
    public ResponseEntity<?> createRoom(
            @RequestBody Room room) {

        try {

            return ResponseEntity.ok(
                    roomService.createRoom(room)
            );

        }
        catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long id,
            @RequestBody Room room) {

        try {

            return ResponseEntity.ok(
                    roomService.updateRoom(id, room)
            );

        }
        catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(
            @PathVariable Long id) {

        try {

            roomService.deleteRoom(id);

            return ResponseEntity.ok(
                    "Room deleted successfully."
            );

        }
        catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}