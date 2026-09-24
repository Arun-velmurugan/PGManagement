package com.project.pghostel.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_no", nullable = false, unique = true)
    private String roomNo;

    @Column(name = "room_type", nullable = false)
    private String roomType;

    @Column(nullable = false)
    private double rent;

    @Column(nullable = false)
    private String status;


    public Room(Long id, String roomNo, String roomType, double rent, String status) {
        this.id = id;
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.rent = rent;
        this.status = status;
    }

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getRent() {
        return rent;
    }

    public String getStatus() {
        return status;
    }


    // ==========================================
    // SETTERS
    // ==========================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}