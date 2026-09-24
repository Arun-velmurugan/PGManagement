package com.project.pghostel.app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "food",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"tenant_id", "food_date"}
        )
    }
)
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "food_date", nullable = false)
    private LocalDate foodDate;

    @Column(nullable = false)
    private boolean breakfast = false;

    @Column(nullable = false)
    private boolean lunch = false;

    @Column(nullable = false)
    private boolean dinner = false;

    @Column(nullable = false)
    private String status = "PENDING";


    // Getters and Setters

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public LocalDate getFoodDate() {
        return foodDate;
    }

    public void setFoodDate(LocalDate foodDate) {
        this.foodDate = foodDate;
    }

    public boolean isBreakfast() {
        return breakfast;
    }

    public void setBreakfast(boolean breakfast) {
        this.breakfast = breakfast;
    }

    public boolean isLunch() {
        return lunch;
    }

    public void setLunch(boolean lunch) {
        this.lunch = lunch;
    }

    public boolean isDinner() {
        return dinner;
    }

    public void setDinner(boolean dinner) {
        this.dinner = dinner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}