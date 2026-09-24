package com.project.pghostel.app.controller;

import com.project.pghostel.app.entity.Movement;
import com.project.pghostel.app.entity.MovementHistory;
import com.project.pghostel.app.service.MovementService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movements")
@CrossOrigin
public class MovementController {

    private final MovementService movementService;


    public MovementController(
            MovementService movementService) {

        this.movementService = movementService;
    }


    // ==================================================
    // ADMIN / WARDEN
    // GET ALL CURRENT MOVEMENTS
    // ==================================================

    @GetMapping
    public List<Movement> getAllMovements() {

        return movementService.getAllMovements();
    }


    // ==================================================
    // ADMIN / WARDEN
    // GET TENANT HISTORY
    // ==================================================

    @GetMapping("/{tenantId}/history")
    public List<MovementHistory> getHistory(
            @PathVariable Long tenantId) {

        return movementService.getHistory(tenantId);
    }


    // ==================================================
    // ADMIN / WARDEN
    // MARK TENANT OUT
    // ==================================================

    @PutMapping("/tenant/{tenantId}/out")
    public ResponseEntity<?> markOut(
            @PathVariable Long tenantId) {

        try {

            movementService.markOut(tenantId);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Tenant marked as OUT."
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // ==================================================
    // TENANT
    // GET MY CURRENT MOVEMENT
    // ==================================================

    @GetMapping("/my")
    public ResponseEntity<?> getMyMovement(
            Authentication authentication) {

        try {

            String username =
                    authentication.getName();


            Movement movement =
                    movementService
                            .getMyMovement(username);


            return ResponseEntity.ok(movement);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // ==================================================
    // TENANT
    // MARK MYSELF OUT
    // ==================================================

    @PutMapping("/my/out")
    public ResponseEntity<?> markMyOut(
            Authentication authentication) {

        try {

            String username =
                    authentication.getName();


            movementService
                    .markMyOut(username);


            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Marked OUT successfully."
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // ==================================================
    // TENANT
    // MARK MYSELF IN
    // ==================================================

    @PutMapping("/my/in")
    public ResponseEntity<?> markMyIn(
            Authentication authentication) {

        try {

            String username =
                    authentication.getName();


            movementService
                    .markMyIn(username);


            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Marked IN successfully."
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // ==================================================
    // TENANT
    // GET MY MOVEMENT HISTORY
    // ==================================================

    @GetMapping("/my/history")
    public ResponseEntity<?> getMyHistory(
            Authentication authentication) {

        try {

            String username =
                    authentication.getName();


            List<MovementHistory> history =
                    movementService
                            .getMyHistory(username);


            return ResponseEntity.ok(history);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}