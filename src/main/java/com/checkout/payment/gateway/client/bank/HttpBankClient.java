package com.checkout.payment.gateway.client.bank;

import com.checkout.payment.gateway.api.dto.PostPaymentRequest;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class HttpBankClient implements BankClient {
    private final RestTemplate restTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(HttpBankClient.class);

    @Value("${bank.url}")
    private String bankUrl;

    @Override
    public BankPaymentResponse processPayment(PostPaymentRequest request) {
      LOG.info("Sending payment request to acquiring bank for card ending {}",
          getLastFourDigits(request.getCardNumber()));

      BankPaymentRequest bankRequest = BankPaymentRequest.builder()
                .cardNumber(request.getCardNumber())
                .expiryDate(formatExpiryDate(request))
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .cvv(request.getCvv())
                .build();

        try{
          BankPaymentResponse response = restTemplate.postForObject(
              bankUrl + "/payments",
              bankRequest,
              BankPaymentResponse.class
          );

          LOG.info("Received response from acquiring bank. authorized={}",
              response != null && response.isAuthorized());

          return response;

        } catch (Exception exception) {
          LOG.warn("Acquiring bank is unavailable", exception);
          throw new BankUnavailableException("Bank is unavailable");
        }
    }

    private String formatExpiryDate(PostPaymentRequest request) {
        return String.format("%02d/%d", request.getExpiryMonth(), request.getExpiryYear());
    }

  private String getLastFourDigits(String cardNumber) {
    return cardNumber.substring(cardNumber.length() - 4);
  }
}