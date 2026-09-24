package com.project.pghostel.app.service;

import com.project.pghostel.app.entity.Payment;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.PaymentRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountTransactionService accountTransactionService;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RazorpayService razorpayService;

    public PaymentService(
            PaymentRepository paymentRepository,
            AccountTransactionService accountTransactionService,
            UserRepository userRepository,
            TenantRepository tenantRepository,
            RazorpayService razorpayService) {

        this.paymentRepository = paymentRepository;
        this.accountTransactionService = accountTransactionService;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.razorpayService = razorpayService;
    }

    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    public List<Payment> getAllPayments() {

        List<Payment> payments = paymentRepository.findAll();

        for (Payment payment : payments) {
            updateOverdueStatus(payment);
        }

        return payments;
    }

    // =========================================================
    // CREATE INITIAL PAYMENT
    // =========================================================

    public Payment createInitialPayment(Tenant tenant) {

        String month = YearMonth
                .from(tenant.getCheckInDate())
                .toString();

        boolean exists =
                paymentRepository.existsByTenant_TenantIdAndMonth(
                        tenant.getTenantId(),
                        month
                );

        if (exists) {
            throw new RuntimeException(
                    "Payment already exists for this month."
            );
        }

        Payment payment = new Payment();

        payment.setTenant(tenant);
        payment.setAmount(tenant.getRent());
        payment.setDueDate(tenant.getCheckInDate());
        payment.setPaymentDate(null);
        payment.setStatus("PENDING");
        payment.setMonth(month);
        payment.setPaymentMethod(null);

        return paymentRepository.save(payment);
    }

    // =========================================================
    // CREATE MONTHLY PAYMENTS
    // =========================================================

    public void createMonthlyPayments() {

        List<Tenant> tenants =
                tenantRepository.findByStatus("ACTIVE");

        LocalDate today = LocalDate.now();

        for (Tenant tenant : tenants) {

            LocalDate checkInDate =
                    tenant.getCheckInDate();

            int day = checkInDate.getDayOfMonth();

            YearMonth currentMonth =
                    YearMonth.from(today);

            int lastDay =
                    currentMonth.lengthOfMonth();

            int dueDay =
                    Math.min(day, lastDay);

            LocalDate dueDate =
                    currentMonth.atDay(dueDay);

            if (!today.isBefore(dueDate)) {

                String month =
                        currentMonth.toString();

                boolean exists =
                        paymentRepository
                                .existsByTenant_TenantIdAndMonth(
                                        tenant.getTenantId(),
                                        month
                                );

                if (!exists) {

                    Payment payment =
                            new Payment();

                    payment.setTenant(tenant);
                    payment.setAmount(tenant.getRent());
                    payment.setDueDate(dueDate);
                    payment.setPaymentDate(null);
                    payment.setStatus("PENDING");
                    payment.setMonth(month);
                    payment.setPaymentMethod(null);

                    paymentRepository.save(payment);
                }
            }
        }
    }

    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    public Payment getPaymentById(Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found."
                                )
                        );

        updateOverdueStatus(payment);

        return payment;
    }

    // =========================================================
    // CASH PAYMENT REQUEST
    // TENANT -> WAITING_FOR_CONFIRMATION
    // =========================================================

    public Payment requestCashPayment(Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found."
                                )
                        );

        String status = payment.getStatus();

        // Already paid
        if ("PAID".equalsIgnoreCase(status)) {

            throw new RuntimeException(
                    "Payment is already completed."
            );
        }

        // Already requested
        if ("WAITING_FOR_CONFIRMATION"
                .equalsIgnoreCase(status)) {

            throw new RuntimeException(
                    "Cash payment request already submitted."
            );
        }

        // Store payment method
        payment.setPaymentMethod("CASH");

        // Waiting for Admin/Warden confirmation
        payment.setStatus(
                "WAITING_FOR_CONFIRMATION"
        );

        // Payment is not completed yet
        payment.setPaymentDate(null);

        return paymentRepository.save(payment);
    }

    // =========================================================
    // RECEIVE CASH PAYMENT
    // ADMIN / WARDEN CONFIRMATION
    // =========================================================

    public Payment receivePayment(Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found."
                                )
                        );

        // Already paid
        if ("PAID".equalsIgnoreCase(
                payment.getStatus())) {

            throw new RuntimeException(
                    "Payment is already completed."
            );
        }

        // Only CASH payments waiting for confirmation
        // can be received from this action
        if (!"WAITING_FOR_CONFIRMATION"
                .equalsIgnoreCase(
                        payment.getStatus())) {

            throw new RuntimeException(
                    "This payment is not waiting for cash confirmation."
            );
        }

        // Make payment completed
        payment.setPaymentDate(
                LocalDate.now()
        );

        payment.setStatus("PAID");

        Payment savedPayment =
                paymentRepository.save(payment);

        // Add income to Accounts
        accountTransactionService.addIncome(
                "Rent - "
                        + payment.getTenant()
                                .getUser()
                                .getFullName(),

                payment.getAmount(),

                payment.getPaymentDate(),

                payment.getPaymentId()
        );

        return savedPayment;
    }

    // =========================================================
    // VERIFY RAZORPAY PAYMENT
    // =========================================================

    public Payment verifyPayment(
            Long paymentId,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found."
                                )
                        );

        if ("PAID".equalsIgnoreCase(
                payment.getStatus())) {

            throw new RuntimeException(
                    "Payment is already completed."
            );
        }

        if (payment.getRazorpayOrderId() != null
                && !payment.getRazorpayOrderId()
                        .equals(razorpayOrderId)) {

            throw new RuntimeException(
                    "Invalid Razorpay order ID."
            );
        }

        boolean verified =
                razorpayService.verifyPaymentSignature(
                        razorpayOrderId,
                        razorpayPaymentId,
                        razorpaySignature
                );

        if (!verified) {

            throw new RuntimeException(
                    "Payment verification failed."
            );
        }

        payment.setRazorpayOrderId(
                razorpayOrderId
        );

        payment.setRazorpayPaymentId(
                razorpayPaymentId
        );

        payment.setRazorpaySignature(
                razorpaySignature
        );

        payment.setPaymentMethod("RAZORPAY");

        payment.setPaymentDate(
                LocalDate.now()
        );

        payment.setStatus("PAID");

        Payment savedPayment =
                paymentRepository.save(payment);

        // Add income to Accounts
        accountTransactionService.addIncome(
                "Rent - "
                        + payment.getTenant()
                                .getUser()
                                .getFullName(),

                payment.getAmount(),

                payment.getPaymentDate(),

                payment.getPaymentId()
        );

        return savedPayment;
    }

    // =========================================================
    // GET MY PAYMENTS
    // =========================================================

    public List<Payment> getMyPayments(
            String username) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );

        Tenant tenant =
                tenantRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant details not found."
                                )
                        );

        List<Payment> payments =
                paymentRepository
                        .findByTenant_TenantId(
                                tenant.getTenantId()
                        );

        for (Payment payment : payments) {
            updateOverdueStatus(payment);
        }

        return payments;
    }

    // =========================================================
    // PAYMENT HISTORY
    // =========================================================

    public List<Payment> getPaymentHistory(
            Long tenantId) {

        List<Payment> history =
                paymentRepository
                        .findByTenant_TenantId(
                                tenantId
                        );

        for (Payment payment : history) {
            updateOverdueStatus(payment);
        }

        return history;
    }

    // =========================================================
    // UPDATE OVERDUE STATUS
    // =========================================================

    private void updateOverdueStatus(
            Payment payment) {

        // IMPORTANT:
        // WAITING_FOR_CONFIRMATION should NOT
        // become OVERDUE.

        if ("PENDING".equalsIgnoreCase(
                payment.getStatus())) {

            if (LocalDate.now()
                    .isAfter(payment.getDueDate())) {

                payment.setStatus("OVERDUE");

                paymentRepository.save(payment);
            }
        }
    }
}