package com.project.pghostel.app.dto;

public class CreateOrderResponse {

    private String orderId;
    private int amount;
    private String currency;
    private Long paymentId;
    private String keyId;

    public CreateOrderResponse(
            String orderId,
            int amount,
            String currency,
            Long paymentId,
            String keyId) {

        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentId = paymentId;
        this.keyId = keyId;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getKeyId() {
        return keyId;
    }
}