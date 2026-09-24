package com.project.pghostel.app.controller;

import com.project.pghostel.app.entity.Food;
import com.project.pghostel.app.service.FoodService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food")
@CrossOrigin
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }


    // ==================================================
    // ADMIN / WARDEN
    // TODAY'S FOOD
    // ==================================================

    @GetMapping("/today")
    public List<Food> getTodayFood() {

        return foodService.getTodayFood();
    }


    // ==================================================
    // TENANT
    // MY TODAY FOOD
    // ==================================================

    @GetMapping("/my")
    public Food getMyTodayFood(
            Authentication authentication) {

        return foodService.getMyTodayFood(
                authentication.getName()
        );
    }


    // ==================================================
    // TENANT
    // UPDATE TODAY FOOD
    // ==================================================

    @PutMapping("/my")
    public Food updateMyTodayFood(
            Authentication authentication,
            @RequestBody FoodRequest request) {

        return foodService.updateMyTodayFood(
                authentication.getName(),
                request.isBreakfast(),
                request.isLunch(),
                request.isDinner()
        );
    }


    // ==================================================
    // TENANT
    // MY FOOD HISTORY
    // ==================================================

    @GetMapping("/my/history")
    public List<Food> getMyFoodHistory(
            Authentication authentication) {

        return foodService.getMyFoodHistory(
                authentication.getName()
        );
    }


    // ==================================================
    // ADMIN / WARDEN
    // TENANT FOOD HISTORY
    // ==================================================

    @GetMapping("/tenant/{tenantId}/history")
    public List<Food> getFoodHistory(
            @PathVariable Long tenantId) {

        return foodService.getFoodHistory(tenantId);
    }


    // ==================================================
    // REQUEST DTO
    // ==================================================

    public static class FoodRequest {

        private boolean breakfast;
        private boolean lunch;
        private boolean dinner;


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
    }
}