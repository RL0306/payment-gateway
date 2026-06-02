package com.checkout.payment.gateway.client.bank;

import com.checkout.payment.gateway.api.dto.PostPaymentRequest;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class HttpBankClient implements BankClient {
    private final RestTemplate restTemplate;

    @Value("${bank.url}")
    private String bankUrl;

    @Override
    public BankPaymentResponse processPayment(PostPaymentRequest request) {
        BankPaymentRequest bankRequest = BankPaymentRequest.builder()
                .cardNumber(request.getCardNumber())
                .expiryDate(formatExpiryDate(request))
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .cvv(request.getCvv())
                .build();

        try{
          return restTemplate.postForObject(
              bankUrl + "/payments",
              bankRequest,
              BankPaymentResponse.class
          );
        } catch (ServiceUnavailable exception) {
          throw new BankUnavailableException("Bank is unavailable");
        }
    }

    private String formatExpiryDate(PostPaymentRequest request) {
        return String.format("%02d/%d", request.getExpiryMonth(), request.getExpiryYear());
    }
}