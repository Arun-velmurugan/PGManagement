package com.project.pghostel.app.service;

import com.project.pghostel.app.dto.TenantHistoryResponse;
import com.project.pghostel.app.entity.Room;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.PaymentRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;
import com.project.pghostel.app.entity.TenantHistory;
import com.project.pghostel.app.repository.TenantHistoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TenantService {

        private final TenantRepository tenantRepository;
        private final UserRepository userRepository;
        private final PaymentRepository paymentRepository;
        private final RoomService roomService;
        private final PaymentService paymentService;
        private final MovementService movementService;
        private final TenantHistoryRepository tenantHistoryRepository;

        public TenantService(
                        TenantRepository tenantRepository,
                        UserRepository userRepository,
                        PaymentRepository paymentRepository,
                        RoomService roomService,
                        PaymentService paymentService,
                        MovementService movementService,
                        TenantHistoryRepository tenantHistoryRepository) {
                // FoodRecordService foodRecordService) {

                this.tenantRepository = tenantRepository;
                this.userRepository = userRepository;
                this.paymentRepository = paymentRepository;
                this.roomService = roomService;
                this.paymentService = paymentService;
                this.movementService = movementService;
                this.tenantHistoryRepository = tenantHistoryRepository;
                // this.foodRecordService = foodRecordService;
        }

        // ==================================================
        // GET ALL TENANTS
        // ==================================================

        public List<Tenant> getAllTenants() {

                return tenantRepository.findAllByOrderByTenantIdAsc();
        }

        // ==================================================
        // GET TENANT BY ID
        // ==================================================

        public Tenant getTenantById(Long id) {

                return tenantRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Tenant not found."));
        }

        // ==================================================
        // GET AVAILABLE TENANT USERS
        // ==================================================
        public List<User> getAvailableTenantUsers() {

                List<User> users = userRepository.findByRoleAndStatus("TENANT", "ACTIVE");

                return users.stream()
                                .filter(user -> !tenantRepository.existsByUserId(user.getId()))
                                .toList();
        }

        // ==================================================
        // CREATE TENANT
        // ==================================================

        @Transactional
        public Tenant createTenant(
                        Long userId,
                        Long roomId,
                        String foodPlan,
                        LocalDate checkInDate) {

                // ------------------------------------------
                // GET USER
                // ------------------------------------------

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found."));

                // ------------------------------------------
                // CHECK ROLE
                // ------------------------------------------

                if (!"TENANT".equalsIgnoreCase(user.getRole())) {

                        throw new RuntimeException(
                                        "Selected user is not a tenant.");
                }

                // ------------------------------------------
                // CHECK USER STATUS
                // ------------------------------------------

                if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {

                        throw new RuntimeException(
                                        "Selected user is inactive.");
                }

                // ------------------------------------------
                // CHECK ALREADY ADDED
                // ------------------------------------------

                if (tenantRepository.existsByUserId(userId)) {

                        throw new RuntimeException(
                                        "This tenant is already added.");
                }

                // ------------------------------------------
                // CHECK ROOM CAPACITY
                // ------------------------------------------

                roomService.checkRoomCapacity(roomId);

                // ------------------------------------------
                // GET ROOM
                // ------------------------------------------

                Room room = roomService.getRoomById(roomId);

                // ------------------------------------------
                // CHECK FOOD PLAN
                // ------------------------------------------

                if (!foodPlan.equalsIgnoreCase("WITH_FOOD")
                                && !foodPlan.equalsIgnoreCase("WITHOUT_FOOD")) {

                        throw new RuntimeException(
                                        "Invalid food plan.");
                }

                // ------------------------------------------
                // CALCULATE RENT
                // ------------------------------------------

                double roomRent = room.getRent();

                double foodCharge = 0.0;

                if (foodPlan.equalsIgnoreCase("WITH_FOOD")) {

                        foodCharge = 2500.0;
                }

                double finalRent = roomRent + foodCharge;

                // ------------------------------------------
                // CREATE TENANT
                // ------------------------------------------

                Tenant tenant = new Tenant();

                tenant.setUser(user);
                tenant.setRoom(room);
                tenant.setFoodPlan(foodPlan);
                tenant.setRent(finalRent);

                // Current date if frontend doesn't send date
                if (checkInDate == null) {
                        checkInDate = LocalDate.now();
                }

                tenant.setCheckInDate(checkInDate);

                tenant.setStatus("ACTIVE");

                // ------------------------------------------
                // SAVE
                // ------------------------------------------

                Tenant savedTenant = tenantRepository.save(tenant);

                // ------------------------------------------
                // CREATE TENANT HISTORY
                // ------------------------------------------

                TenantHistory history = new TenantHistory();

                history.setTenant(savedTenant);
                history.setRoom(room);
                history.setCheckInDate(savedTenant.getCheckInDate());
                history.setVacateDate(null);
                history.setFoodPlan(savedTenant.getFoodPlan());
                history.setRent(savedTenant.getRent());
                history.setStatus("ACTIVE");

                tenantHistoryRepository.save(history);

                paymentService.createInitialPayment(savedTenant);

                movementService.createMovementForTenant(savedTenant);

                // ------------------------------------------
                // UPDATE ROOM STATUS
                // ------------------------------------------

                roomService.updateRoomStatusAfterTenantChange(roomId);

                return savedTenant;
        }

        // ==================================================
        // INACTIVE TENANT
        // ==================================================

        @Transactional
        public void makeTenantInactive(Long tenantId) {

                Tenant tenant = tenantRepository.findById(tenantId)
                                .orElseThrow(() -> new RuntimeException("Tenant not found."));

                // ------------------------------------------
                // CHECK PAYMENT
                // ------------------------------------------

                boolean pendingPayment = paymentRepository
                                .existsByTenant_TenantIdAndStatusIn(
                                                tenantId,
                                                List.of("PENDING", "OVERDUE"));

                if (pendingPayment) {

                        throw new RuntimeException(
                                        "Tenant cannot be made inactive. " +
                                                        "Please clear all pending or overdue payments first.");
                }

                // ------------------------------------------
                // TENANT INACTIVE
                // ------------------------------------------

                tenant.setStatus("INACTIVE");
                tenant.setVacateDate(LocalDate.now());

                tenantRepository.save(tenant);

                List<TenantHistory> histories = tenantHistoryRepository
                                .findByTenantTenantIdOrderByHistoryIdAsc(tenantId);

                for (TenantHistory history : histories) {

                        if ("ACTIVE".equalsIgnoreCase(history.getStatus())
                                        && history.getVacateDate() == null) {

                                history.setVacateDate(LocalDate.now());
                                history.setStatus("COMPLETED");

                                tenantHistoryRepository.save(history);

                                break;
                        }
                }

                // ------------------------------------------
                // USER INACTIVE
                // ------------------------------------------

                User user = tenant.getUser();

                user.setStatus("INACTIVE");

                userRepository.save(user);

                // ------------------------------------------
                // MOVEMENT OUT
                // ------------------------------------------

                movementService.markOut(tenantId);

                // ------------------------------------------
                // ROOM STATUS
                // ------------------------------------------

                if (tenant.getRoom() != null) {

                        roomService.updateRoomStatusAfterTenantChange(
                                        tenant.getRoom().getId());
                }
        }

        // ==================================================
        // REJOIN / ACTIVE TENANT
        // ==================================================

        @Transactional
        public void makeTenantActive(
                        Long tenantId,
                        Long roomId,
                        String foodPlan) {

                Tenant tenant = tenantRepository.findById(tenantId)
                                .orElseThrow(() -> new RuntimeException("Tenant not found."));

                // ------------------------------------------
                // CHECK ROOM
                // ------------------------------------------

                Room newRoom = roomService.getRoomById(roomId);

                roomService.checkRoomCapacity(roomId);

                // ------------------------------------------
                // CHECK FOOD PLAN
                // ------------------------------------------

                if (!"WITH_FOOD".equalsIgnoreCase(foodPlan)
                                && !"WITHOUT_FOOD".equalsIgnoreCase(foodPlan)) {

                        throw new RuntimeException("Invalid food plan.");
                }

                // ------------------------------------------
                // CALCULATE RENT
                // ------------------------------------------

                double roomRent = newRoom.getRent();

                double foodCharge = 0.0;

                if ("WITH_FOOD".equalsIgnoreCase(foodPlan)) {

                        foodCharge = 2500.0;
                }

                double finalRent = roomRent + foodCharge;

                // ------------------------------------------
                // UPDATE TENANT
                // ------------------------------------------

                tenant.setRoom(newRoom);
                tenant.setFoodPlan(foodPlan);
                tenant.setRent(finalRent);

                tenant.setStatus("ACTIVE");
                tenant.setCheckInDate(LocalDate.now());
                tenant.setVacateDate(null);

                tenantRepository.save(tenant);

                // ------------------------------------------
                // CREATE NEW TENANT HISTORY
                // ------------------------------------------

                TenantHistory history = new TenantHistory();

                history.setTenant(tenant);
                history.setRoom(newRoom);
                history.setCheckInDate(tenant.getCheckInDate());
                history.setVacateDate(null);
                history.setFoodPlan(tenant.getFoodPlan());
                history.setRent(tenant.getRent());
                history.setStatus("ACTIVE");

                tenantHistoryRepository.save(history);

                // ------------------------------------------
                // CREATE INITIAL PAYMENT
                // ------------------------------------------

                paymentService.createInitialPayment(tenant);

                // ------------------------------------------
                // USER ACTIVE
                // ------------------------------------------

                User user = tenant.getUser();

                if (user != null) {

                        user.setStatus("ACTIVE");

                        userRepository.save(user);
                }

                // ------------------------------------------
                // MOVEMENT IN
                // ------------------------------------------

                movementService.markIn(tenantId);

                // ------------------------------------------
                // ROOM STATUS
                // ------------------------------------------

                if (tenant.getRoom() != null) {

                        roomService.updateRoomStatusAfterTenantChange(
                                        tenant.getRoom().getId());
                }
        }

        public Tenant getTenantByUsername(String username) {

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found."));

                return tenantRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException("Tenant details not found."));
        }

        // ==================================================
        // UPDATE TENANT ROOM + FOOD PLAN
        // ==================================================

        @Transactional
        public Tenant updateTenantRoom(
                        Long tenantId,
                        Long newRoomId,
                        String foodPlan) {

                // ------------------------------------------
                // GET TENANT
                // ------------------------------------------

                Tenant tenant = tenantRepository.findById(tenantId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Tenant not found."));

                // ------------------------------------------
                // GET OLD ROOM
                // ------------------------------------------

                Room oldRoom = tenant.getRoom();

                // ------------------------------------------
                // GET NEW ROOM
                // ------------------------------------------

                Room newRoom = roomService.getRoomById(newRoomId);

                // ------------------------------------------
                // CHECK FOOD PLAN
                // ------------------------------------------

                if (!"WITH_FOOD".equalsIgnoreCase(foodPlan)
                                && !"WITHOUT_FOOD".equalsIgnoreCase(foodPlan)) {

                        throw new RuntimeException(
                                        "Invalid food plan.");
                }

                // ------------------------------------------
                // CHECK ROOM CAPACITY
                // ------------------------------------------

                if (oldRoom == null
                                || !oldRoom.getId().equals(newRoomId)) {

                        roomService.checkRoomCapacity(newRoomId);
                }

                // ------------------------------------------
                // UPDATE ROOM
                // ------------------------------------------

                tenant.setRoom(newRoom);

                // ------------------------------------------
                // UPDATE FOOD PLAN
                // ------------------------------------------

                tenant.setFoodPlan(foodPlan);

                // ------------------------------------------
                // CALCULATE RENT
                // ------------------------------------------

                double roomRent = newRoom.getRent();

                double foodCharge = 0.0;

                if ("WITH_FOOD".equalsIgnoreCase(foodPlan)) {

                        foodCharge = 2500.0;
                }

                double finalRent = roomRent + foodCharge;

                tenant.setRent(finalRent);

                // ------------------------------------------
                // SAVE TENANT
                // ------------------------------------------

                Tenant savedTenant = tenantRepository.save(tenant);

                // ------------------------------------------
                // UPDATE OLD ROOM STATUS
                // ------------------------------------------

                if (oldRoom != null
                                && !oldRoom.getId().equals(newRoomId)) {

                        roomService.updateRoomStatusAfterTenantChange(
                                        oldRoom.getId());
                }

                // ------------------------------------------
                // UPDATE NEW ROOM STATUS
                // ------------------------------------------

                roomService.updateRoomStatusAfterTenantChange(
                                newRoomId);

                return savedTenant;
        }
        

        // ==================================================
// GET TENANT HISTORY
// ==================================================

public List<TenantHistoryResponse> getTenantHistory(Long tenantId) {

        tenantRepository.findById(tenantId)
                        .orElseThrow(() ->
                                        new RuntimeException("Tenant not found."));

        List<TenantHistory> histories =
                        tenantHistoryRepository
                                        .findByTenantTenantIdOrderByHistoryIdAsc(tenantId);

        return histories.stream()
                        .map(history -> new TenantHistoryResponse(
                                        history.getHistoryId(),
                                        history.getRoom().getRoomNo(),
                                        history.getCheckInDate(),
                                        history.getVacateDate(),
                                        history.getFoodPlan(),
                                        history.getRent(),
                                        history.getStatus()
                        ))
                        .toList();
}
}