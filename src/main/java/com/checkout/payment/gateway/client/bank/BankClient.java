package com.checkout.payment.gateway.client.bank;

import com.checkout.payment.gateway.api.dto.PostPaymentRequest;

public interface BankClient {

    BankPaymentResponse processPayment(PostPaymentRequest request);
}