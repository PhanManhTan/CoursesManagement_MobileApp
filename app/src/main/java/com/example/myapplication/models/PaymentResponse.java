package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("paymentUrl")
    private String paymentUrl;

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}