package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.bank.BankClient;
import com.checkout.payment.gateway.client.bank.BankPaymentResponse;
import com.checkout.payment.gateway.exception.InvalidCreditCardNumberException;
import com.checkout.payment.gateway.exception.InvalidExpiryDateException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.PaymentStatus;
import com.checkout.payment.gateway.api.dto.PostPaymentRequest;
import com.checkout.payment.gateway.api.dto.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PaymentService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentService.class);
  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    PostPaymentResponse payment = paymentsRepository.get(id)
        .orElseThrow(() -> {
          LOG.warn("Payment with ID {} was not found", id);
          return new PaymentNotFoundException("Payment not found");
        });

    LOG.debug("Payment with ID {} retrieved successfully", id);

    return payment;
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    boolean expiryDateValid = isExpiryDateValid(paymentRequest);
    if(!expiryDateValid){
      throw new InvalidExpiryDateException("Expiry date must be in the future");
    }

    LOG.info("Processing payment for card ending {}", getLastFourDigits(paymentRequest.getCardNumber()));

    BankPaymentResponse bankResponse = bankClient.processPayment(paymentRequest);

    UUID paymentId = UUID.randomUUID();

    PostPaymentResponse paymentResponse = PostPaymentResponse.builder()
        .id(paymentId)
        .status(bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED)
        .cardNumberLastFour(getLastFourDigits(paymentRequest.getCardNumber()))
        .expiryMonth(paymentRequest.getExpiryMonth())
        .expiryYear(paymentRequest.getExpiryYear())
        .currency(paymentRequest.getCurrency())
        .amount(paymentRequest.getAmount())
        .build();

    paymentsRepository.add(paymentResponse);

    LOG.info("Payment {} processed with status {}", paymentId, paymentResponse.getStatus());

    return paymentResponse;
  }

  private String getLastFourDigits(String cardNumber) {
    if (cardNumber == null || cardNumber.length() < 4) {
      throw new InvalidCreditCardNumberException("Invalid card number");
    }

    return cardNumber.substring(cardNumber.length() - 4);
  }

  private boolean isExpiryDateValid(PostPaymentRequest request) {

    YearMonth expiryDate =
        YearMonth.of(
            request.getExpiryYear(),
            request.getExpiryMonth());

    if (!expiryDate.isAfter(YearMonth.now())) {
      return false;
    }

    return true;
  }
}
