package com.project.pghostel.app.service;

import com.project.pghostel.app.entity.Movement;
import com.project.pghostel.app.entity.MovementHistory;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.MovementHistoryRepository;
import com.project.pghostel.app.repository.MovementRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovementService {

    private final MovementRepository movementRepository;
    private final MovementHistoryRepository movementHistoryRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;


    public MovementService(
            MovementRepository movementRepository,
            MovementHistoryRepository movementHistoryRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository) {

        this.movementRepository = movementRepository;
        this.movementHistoryRepository = movementHistoryRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }


    // ==================================================
    // GET ALL CURRENT MOVEMENTS
    // ADMIN / WARDEN
    // ==================================================

    public List<Movement> getAllMovements() {

        return movementRepository.findAll();
    }


    // ==================================================
    // CREATE MOVEMENT WHEN TENANT IS ADDED
    // ==================================================

    @Transactional
    public void createMovementForTenant(Tenant tenant) {

        Movement movement = new Movement();

        movement.setTenant(tenant);
        movement.setStatus("IN");
        movement.setMovementTime(LocalDateTime.now());

        Movement savedMovement =
                movementRepository.save(movement);


        // Create first history record
        createHistory(
                tenant,
                "IN",
                savedMovement.getMovementTime()
        );
    }


    // ==================================================
    // MARK TENANT OUT
    // ==================================================

    @Transactional
    public void markOut(Long tenantId) {

        Movement movement =
                movementRepository
                        .findByTenant_TenantId(tenantId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Movement record not found."
                                )
                        );


        LocalDateTime now =
                LocalDateTime.now();


        movement.setStatus("OUT");
        movement.setMovementTime(now);

        movementRepository.save(movement);


        // Add OUT history
        createHistory(
                movement.getTenant(),
                "OUT",
                now
        );
    }


    // ==================================================
    // MARK TENANT IN
    // ==================================================

    @Transactional
    public void markIn(Long tenantId) {

        Movement movement =
                movementRepository
                        .findByTenant_TenantId(tenantId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Movement record not found."
                                )
                        );


        LocalDateTime now =
                LocalDateTime.now();


        movement.setStatus("IN");
        movement.setMovementTime(now);

        movementRepository.save(movement);


        // Add IN history
        createHistory(
                movement.getTenant(),
                "IN",
                now
        );
    }


    // ==================================================
    // CREATE HISTORY
    // ==================================================

    private void createHistory(
            Tenant tenant,
            String status,
            LocalDateTime movementTime) {

        MovementHistory history =
                new MovementHistory();

        history.setTenant(tenant);
        history.setStatus(status);
        history.setMovementTime(movementTime);

        movementHistoryRepository.save(history);
    }


    // ==================================================
    // GET TENANT MOVEMENT HISTORY
    // ADMIN / WARDEN
    // ==================================================

    public List<MovementHistory> getHistory(
            Long tenantId) {

        return movementHistoryRepository
                .findByTenant_TenantIdOrderByMovementTimeDesc(
                        tenantId
                );
    }


    // ==================================================
    // GET CURRENT MOVEMENT OF LOGGED-IN TENANT
    // ==================================================

    public Movement getMyMovement(String username) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );


        Tenant tenant =
                tenantRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant details not found."
                                )
                        );


        return movementRepository
                .findByTenant_TenantId(
                        tenant.getTenantId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Movement record not found."
                        )
                );
    }


    // ==================================================
    // MARK LOGGED-IN TENANT OUT
    // ==================================================

    @Transactional
    public void markMyOut(String username) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );


        Tenant tenant =
                tenantRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant details not found."
                                )
                        );


        markOut(tenant.getTenantId());
    }


    // ==================================================
    // MARK LOGGED-IN TENANT IN
    // ==================================================

    @Transactional
    public void markMyIn(String username) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );


        Tenant tenant =
                tenantRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant details not found."
                                )
                        );


        markIn(tenant.getTenantId());
    }


    // ==================================================
    // GET LOGGED-IN TENANT HISTORY
    // ==================================================

    public List<MovementHistory> getMyHistory(
            String username) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );


        Tenant tenant =
                tenantRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant details not found."
                                )
                        );


        return movementHistoryRepository
                .findByTenant_TenantIdOrderByMovementTimeDesc(
                        tenant.getTenantId()
                );
    }
}