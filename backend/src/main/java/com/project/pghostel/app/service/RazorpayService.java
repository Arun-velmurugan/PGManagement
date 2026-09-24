package com.project.pghostel.app.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.QrCode;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;


    // ==========================================
    // GET RAZORPAY KEY ID
    // ==========================================

    public String getKeyId() {
        return keyId;
    }


    // ==========================================
    // CREATE RAZORPAY ORDER
    // ==========================================

    public Order createOrder(
            double amount,
            Long paymentId) throws Exception {

        RazorpayClient razorpayClient =
                new RazorpayClient(
                        keyId,
                        keySecret
                );

        int amountInPaise =
                (int) Math.round(amount * 100);

        JSONObject orderRequest =
                new JSONObject();

        orderRequest.put(
                "amount",
                amountInPaise
        );

        orderRequest.put(
                "currency",
                "INR"
        );

        orderRequest.put(
                "receipt",
                "payment_" + paymentId
        );

        return razorpayClient.orders.create(
                orderRequest
        );
    }


    // ==========================================
    // CREATE RAZORPAY QR CODE
    // ==========================================

    public QrCode createQrCode(
            double amount,
            Long paymentId) throws Exception {

        RazorpayClient razorpayClient =
                new RazorpayClient(
                        keyId,
                        keySecret
                );

        int amountInPaise =
                (int) Math.round(amount * 100);

        JSONObject qrRequest =
                new JSONObject();

        // Fixed amount QR
        qrRequest.put(
                "type",
                "upi_qr"
        );

        qrRequest.put(
                "name",
                "Aadhya Mens PG"
        );

        qrRequest.put(
                "usage",
                "single_use"
        );

        qrRequest.put(
                "fixed_amount",
                true
        );

        qrRequest.put(
                "payment_amount",
                amountInPaise
        );

        qrRequest.put(
                "description",
                "Rent Payment - Payment ID " + paymentId
        );

        qrRequest.put(
                "customer_id",
                ""
        );

        return razorpayClient.qrCode.create(
                qrRequest
        );
    }


    // ==========================================
    // VERIFY RAZORPAY PAYMENT SIGNATURE
    // ==========================================

    public boolean verifyPaymentSignature(
            String orderId,
            String paymentId,
            String signature) {

        try {

            String generatedSignature =
                    hmacSHA256(
                            orderId + "|" + paymentId,
                            keySecret
                    );

            return generatedSignature.equals(
                    signature
            );

        } catch (Exception e) {

            return false;
        }
    }


    // ==========================================
    // HMAC SHA256
    // ==========================================

    private String hmacSHA256(
            String data,
            String secret) throws Exception {

        javax.crypto.Mac mac =
                javax.crypto.Mac.getInstance(
                        "HmacSHA256"
                );

        javax.crypto.spec.SecretKeySpec secretKey =
                new javax.crypto.spec.SecretKeySpec(
                        secret.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                );

        mac.init(secretKey);

        byte[] hash =
                mac.doFinal(
                        data.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8
                        )
                );

        StringBuilder hexString =
                new StringBuilder();

        for (byte b : hash) {

            String hex =
                    Integer.toHexString(
                            0xff & b
                    );

            if (hex.length() == 1) {

                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }
}