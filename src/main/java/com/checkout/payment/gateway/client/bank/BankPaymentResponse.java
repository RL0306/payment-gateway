package com.checkout.payment.gateway.client.bank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BankPaymentResponse {

    private boolean authorized;

    @JsonProperty("authorization_code")
    private String authorizationCode;
}