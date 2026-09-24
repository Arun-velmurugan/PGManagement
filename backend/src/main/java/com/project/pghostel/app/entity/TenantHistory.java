package com.project.pghostel.app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tenant_history")
public class TenantHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    // Original tenant
    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // Room stayed in
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "vacate_date")
    private LocalDate vacateDate;

    @Column(name = "food_plan", nullable = false)
    private String foodPlan;

    @Column(nullable = false)
    private double rent;

    @Column(nullable = false)
    private String status;


    public TenantHistory() {
    }


    public TenantHistory(Long historyId, Tenant tenant, Room room,
            LocalDate checkInDate, LocalDate vacateDate,
            String foodPlan, double rent, String status) {

        this.historyId = historyId;
        this.tenant = tenant;
        this.room = room;
        this.checkInDate = checkInDate;
        this.vacateDate = vacateDate;
        this.foodPlan = foodPlan;
        this.rent = rent;
        this.status = status;
    }


    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }


    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }


    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }


    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }


    public LocalDate getVacateDate() {
        return vacateDate;
    }

    public void setVacateDate(LocalDate vacateDate) {
        this.vacateDate = vacateDate;
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


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}