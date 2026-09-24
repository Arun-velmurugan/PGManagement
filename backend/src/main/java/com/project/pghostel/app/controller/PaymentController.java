package com.project.pghostel.app.controller;

import com.project.pghostel.app.dto.CreateOrderResponse;
import com.project.pghostel.app.entity.Payment;
import com.project.pghostel.app.service.PaymentService;
import com.project.pghostel.app.service.RazorpayService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.razorpay.Order;
import com.razorpay.QrCode;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayService razorpayService;

    public PaymentController(
            PaymentService paymentService,
            RazorpayService razorpayService) {

        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
    }

    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    @GetMapping
    public List<Payment> getAllPayments() {

        return paymentService.getAllPayments();
    }

    // =========================================================
    // GET MY PAYMENTS
    // =========================================================

    @GetMapping("/my")
    public List<Payment> getMyPayments(
            Authentication authentication) {

        String username = authentication.getName();

        return paymentService.getMyPayments(username);
    }

    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    @GetMapping("/{paymentId}")
    public Payment getPaymentById(
            @PathVariable Long paymentId) {

        return paymentService.getPaymentById(paymentId);
    }

    // =========================================================
    // CREATE RAZORPAY ORDER
    // =========================================================

    @PostMapping("/{paymentId}/create-order")
    public CreateOrderResponse createOrder(
            @PathVariable Long paymentId) throws Exception {

        Payment payment =
                paymentService.getPaymentById(paymentId);

        if ("PAID".equalsIgnoreCase(
                payment.getStatus())) {

            throw new RuntimeException(
                    "Payment is already completed."
            );
        }

        Order order =
                razorpayService.createOrder(
                        payment.getAmount(),
                        payment.getPaymentId()
                );

        return new CreateOrderResponse(
                order.get("id"),
                order.get("amount"),
                order.get("currency"),
                payment.getPaymentId(),
                razorpayService.getKeyId()
        );
    }

    // =========================================================
    // CREATE RAZORPAY QR
    // =========================================================

    @PostMapping("/{paymentId}/create-qr")
    public Map<String, Object> createQrCode(
            @PathVariable Long paymentId) throws Exception {

        Payment payment =
                paymentService.getPaymentById(paymentId);

        if ("PAID".equalsIgnoreCase(
                payment.getStatus())) {

            throw new RuntimeException(
                    "Payment is already completed."
            );
        }

        QrCode qrCode =
                razorpayService.createQrCode(
                        payment.getAmount(),
                        payment.getPaymentId()
                );

        Map<String, Object> response =
                new java.util.HashMap<>();

        response.put(
                "qrId",
                qrCode.get("id")
        );

        response.put(
                "imageUrl",
                qrCode.get("image_url")
        );

        response.put(
                "amount",
                payment.getAmount()
        );

        response.put(
                "paymentId",
                payment.getPaymentId()
        );

        return response;
    }

    // =========================================================
    // CASH PAYMENT REQUEST
    // TENANT -> WAITING_FOR_CONFIRMATION
    // =========================================================

    @PutMapping("/{paymentId}/cash-request")
    public Payment requestCashPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        return paymentService.requestCashPayment(
                paymentId
        );
    }

    // =========================================================
    // RECEIVE CASH PAYMENT
    // ADMIN / WARDEN ONLY
    // =========================================================

    @PutMapping("/{paymentId}/receive")
    public Payment receivePayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        checkAdminOrWarden(authentication);

        return paymentService.receivePayment(
                paymentId
        );
    }

    // =========================================================
    // PAYMENT HISTORY
    // =========================================================

    @GetMapping("/tenant/{tenantId}/history")
    public List<Payment> getPaymentHistory(
            @PathVariable Long tenantId) {

        return paymentService.getPaymentHistory(
                tenantId
        );
    }

    // =========================================================
    // RAZORPAY PAYMENT VERIFY
    // =========================================================

    @PostMapping("/{paymentId}/verify")
    public Payment verifyPayment(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> body) {

        return paymentService.verifyPayment(
                paymentId,
                body.get("razorpay_payment_id"),
                body.get("razorpay_order_id"),
                body.get("razorpay_signature")
        );
    }

    // =========================================================
    // ADMIN / WARDEN CHECK
    // =========================================================

    private void checkAdminOrWarden(
            Authentication authentication) {

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );

        boolean isWarden =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_WARDEN".equals(
                                        authority.getAuthority()
                                )
                        );

        if (!isAdmin && !isWarden) {

            throw new RuntimeException(
                    "Only Admin or Warden can receive payment."
            );
        }
    }
}