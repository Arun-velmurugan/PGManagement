package com.project.pghostel.app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tenantId;

    // User Management user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Assigned room
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "food_plan", nullable = false)
    private String foodPlan;

    @Column(nullable = false)
    private double rent;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "vacate_date")
    private LocalDate vacateDate;

    @Column(nullable = false)
    private String status;

    public Tenant() {
    }

    public Tenant(Long tenantId, User user, Room room, String foodPlan, double rent, LocalDate checkInDate,
            String status) {
        this.tenantId = tenantId;
        this.user = user;
        this.room = room;
        this.foodPlan = foodPlan;
        this.rent = rent;
        this.checkInDate = checkInDate;
        this.status = status;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getFoodPlan() {
        return foodPlan;
    }

    public void setFoodPlan(String foodPlan) {
        this.foodPlan = foodPlan;
    }

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getVacateDate() {
        return vacateDate;
    }

    public void setVacateDate(LocalDate vacateDate) {
        this.vacateDate = vacateDate;
    }
}