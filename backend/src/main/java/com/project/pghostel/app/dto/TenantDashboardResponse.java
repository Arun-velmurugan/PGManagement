package com.project.pghostel.app.dto;


import java.time.LocalDate;
import java.util.List;

public class TenantDashboardResponse {

    private String username;

    private double currentRent;
    private String currentRentPeriod;

    private String paymentStatus;
    private String paymentPeriod;

    private long openComplaintsCount;

    private RoomDetails room;
    private CurrentPayment currentPayment;

    private List<NoticeDetails> latestNotices;
    private List<ComplaintDetails> latestComplaints;


    // =========================
    // GETTERS AND SETTERS
    // =========================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getCurrentRent() {
        return currentRent;
    }

    public void setCurrentRent(double currentRent) {
        this.currentRent = currentRent;
    }

    public String getCurrentRentPeriod() {
        return currentRentPeriod;
    }

    public void setCurrentRentPeriod(String currentRentPeriod) {
        this.currentRentPeriod = currentRentPeriod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentPeriod() {
        return paymentPeriod;
    }

    public void setPaymentPeriod(String paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
    }

    public long getOpenComplaintsCount() {
        return openComplaintsCount;
    }

    public void setOpenComplaintsCount(long openComplaintsCount) {
        this.openComplaintsCount = openComplaintsCount;
    }

    public RoomDetails getRoom() {
        return room;
    }

    public void setRoom(RoomDetails room) {
        this.room = room;
    }

    public CurrentPayment getCurrentPayment() {
        return currentPayment;
    }

    public void setCurrentPayment(CurrentPayment currentPayment) {
        this.currentPayment = currentPayment;
    }

    public List<NoticeDetails> getLatestNotices() {
        return latestNotices;
    }

    public void setLatestNotices(List<NoticeDetails> latestNotices) {
        this.latestNotices = latestNotices;
    }

    public List<ComplaintDetails> getLatestComplaints() {
        return latestComplaints;
    }

    public void setLatestComplaints(List<ComplaintDetails> latestComplaints) {
        this.latestComplaints = latestComplaints;
    }


    // =========================
    // ROOM DETAILS
    // =========================

    public static class RoomDetails {

        private String roomNo;
        private String roomType;
        private String food;
        private double rent;
        private String status;


        public String getRoomNo() {
            return roomNo;
        }

        public void setRoomNo(String roomNo) {
            this.roomNo = roomNo;
        }

        public String getRoomType() {
            return roomType;
        }

        public void setRoomType(String roomType) {
            this.roomType = roomType;
        }

        public String getFood() {
            return food;
        }

        public void setFood(String food) {
            this.food = food;
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


    // =========================
    // CURRENT PAYMENT
    // =========================

    public static class CurrentPayment {

        private String rentPeriod;
        private double amount;
        private LocalDate dueDate;
        private LocalDate paymentDate;
        private String status;


        public String getRentPeriod() {
            return rentPeriod;
        }

        public void setRentPeriod(String rentPeriod) {
            this.rentPeriod = rentPeriod;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public LocalDate getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentDate(LocalDate paymentDate) {
            this.paymentDate = paymentDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    // =========================
    // NOTICE DETAILS
    // =========================

    public static class NoticeDetails {

        private String title;
        private String message;
        private LocalDate createdAt;


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDate getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDate createdAt) {
            this.createdAt = createdAt;
        }
    }


    // =========================
    // COMPLAINT DETAILS
    // =========================

    public static class ComplaintDetails {

        private String subject;
        private String description;
        private LocalDate createdAt;
        private String status;


        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDate getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDate createdAt) {
            this.createdAt = createdAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}