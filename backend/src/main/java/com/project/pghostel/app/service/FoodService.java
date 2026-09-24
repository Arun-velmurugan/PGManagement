package com.project.pghostel.app.service;

import com.project.pghostel.app.entity.Food;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.FoodRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FoodService {

        private final FoodRepository foodRepository;
        private final TenantRepository tenantRepository;
        private final UserRepository userRepository;

        public FoodService(
                        FoodRepository foodRepository,
                        TenantRepository tenantRepository,
                        UserRepository userRepository) {

                this.foodRepository = foodRepository;
                this.tenantRepository = tenantRepository;
                this.userRepository = userRepository;
        }

        // ==================================================
        // CREATE TODAY'S FOOD RECORDS
        // ==================================================

        @Scheduled(cron = "0 0 0 * * *")
        public void createDailyFoodRecords() {
                createTodayFoodRecords();
        }

        public void createTodayFoodRecords() {

                LocalDate today = LocalDate.now();

                List<Tenant> tenants = tenantRepository.findByStatus("ACTIVE");

                for (Tenant tenant : tenants) {

                        if (!"WITH_FOOD".equalsIgnoreCase(
                                        tenant.getFoodPlan())) {
                                continue;
                        }

                        boolean exists = foodRepository
                                        .existsByTenant_TenantIdAndFoodDate(
                                                        tenant.getTenantId(),
                                                        today);

                        if (!exists) {

                                Food food = new Food();

                                food.setTenant(tenant);
                                food.setFoodDate(today);

                                food.setBreakfast(false);
                                food.setLunch(false);
                                food.setDinner(false);

                                food.setStatus("PENDING");

                                foodRepository.save(food);
                        }
                }
        }

        // ==================================================
        // ADMIN / WARDEN - TODAY'S FOOD
        // ==================================================

        public List<Food> getTodayFood() {

                createTodayFoodRecords();

                LocalDate today = LocalDate.now();

                List<Food> allFood = foodRepository.findByFoodDateOrderByFoodIdAsc(
                                today);

                List<Food> result = new ArrayList<>();

                for (Food food : allFood) {

                        Tenant tenant = food.getTenant();

                        if ("ACTIVE".equalsIgnoreCase(
                                        tenant.getStatus())
                                        &&
                                        "WITH_FOOD".equalsIgnoreCase(
                                                        tenant.getFoodPlan())) {

                                result.add(food);
                        }
                }

                return result;
        }

        // ==================================================
        // TENANT - TODAY'S FOOD
        // ==================================================

        public Food getMyTodayFood(String username) {

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found."));

                Tenant tenant = tenantRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Tenant details not found."));

                if (!"ACTIVE".equalsIgnoreCase(
                                tenant.getStatus())) {

                        throw new RuntimeException(
                                        "Tenant is not active.");
                }

                if (!"WITH_FOOD".equalsIgnoreCase(
                                tenant.getFoodPlan())) {

                        throw new RuntimeException(
                                        "Food facility is not available.");
                }

                LocalDate today = LocalDate.now();

                Food food = foodRepository
                                .findByTenant_TenantIdAndFoodDate(
                                                tenant.getTenantId(),
                                                today)
                                .orElse(null);

                if (food == null) {

                        food = new Food();

                        food.setTenant(tenant);
                        food.setFoodDate(today);

                        food.setBreakfast(false);
                        food.setLunch(false);
                        food.setDinner(false);

                        food.setStatus("PENDING");

                        food = foodRepository.save(food);
                }

                return food;
        }

        // ==================================================
        // TENANT - UPDATE TODAY'S FOOD
        // ==================================================

        public Food updateMyTodayFood(
                        String username,
                        boolean breakfast,
                        boolean lunch,
                        boolean dinner) {

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found."));

                Tenant tenant = tenantRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Tenant details not found."));

                if (!"ACTIVE".equalsIgnoreCase(
                                tenant.getStatus())) {

                        throw new RuntimeException(
                                        "Tenant is not active.");
                }

                if (!"WITH_FOOD".equalsIgnoreCase(
                                tenant.getFoodPlan())) {

                        throw new RuntimeException(
                                        "Food facility is not available.");
                }

                LocalDate today = LocalDate.now();

                Food food = foodRepository
                                .findByTenant_TenantIdAndFoodDate(
                                                tenant.getTenantId(),
                                                today)
                                .orElse(null);

                if (food == null) {

                        food = new Food();

                        food.setTenant(tenant);
                        food.setFoodDate(today);
                }

                food.setBreakfast(breakfast);
                food.setLunch(lunch);
                food.setDinner(dinner);

                updateStatus(food);

                return foodRepository.save(food);
        }

        // ==================================================
        // STATUS
        // ==================================================

        private void updateStatus(Food food) {

                int count = 0;

                if (food.isBreakfast()) {
                        count++;
                }

                if (food.isLunch()) {
                        count++;
                }

                if (food.isDinner()) {
                        count++;
                }

                if (count == 0) {

                        food.setStatus("PENDING");

                } else if (count == 3) {

                        food.setStatus("TAKEN");

                } else {

                        food.setStatus("PARTIAL");
                }
        }

        // ==================================================
        // FOOD HISTORY
        // ==================================================

        public List<Food> getFoodHistory(Long tenantId) {

                return foodRepository
                                .findByTenant_TenantIdOrderByFoodDateDesc(
                                                tenantId);
        }

        public List<Food> getMyFoodHistory(String username) {

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found."));

                Tenant tenant = tenantRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Tenant details not found."));

                return getFoodHistory(tenant.getTenantId());
        }

}