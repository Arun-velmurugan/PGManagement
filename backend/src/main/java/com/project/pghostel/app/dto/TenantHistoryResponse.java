package com.project.pghostel.app.dto;

import java.time.LocalDate;

public class TenantHistoryResponse {

    private Long historyId;
    private String roomNo;
    private LocalDate checkInDate;
    private LocalDate vacateDate;
    private String foodPlan;
    private double rent;
    private String status;

    public TenantHistoryResponse() {
    }

    public TenantHistoryResponse(
            Long historyId,
            String roomNo,
            LocalDate checkInDate,
            LocalDate vacateDate,
            String foodPlan,
            double rent,
            String status) {

        this.historyId = historyId;
        this.roomNo = roomNo;
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

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
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