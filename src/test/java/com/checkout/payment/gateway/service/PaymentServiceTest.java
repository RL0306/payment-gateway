package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.api.dto.PostPaymentRequest;
import com.checkout.payment.gateway.api.dto.PostPaymentResponse;
import com.checkout.payment.gateway.client.bank.BankClient;
import com.checkout.payment.gateway.client.bank.BankPaymentResponse;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.PaymentStatus;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private BankClient bankClient;

  @InjectMocks
  private PaymentService paymentService;

  @Test
  void whenBankAuthorizesPaymentThenAuthorizedPaymentIsReturned() {
    PostPaymentRequest request = validPaymentRequest("2222405343248877");

    BankPaymentResponse bankResponse = new BankPaymentResponse(true, "auth-code");

    when(bankClient.processPayment(request)).thenReturn(bankResponse);

    PostPaymentResponse response = paymentService.processPayment(request);

    assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
    assertEquals("8877", response.getCardNumberLastFour());
    assertEquals(request.getExpiryMonth(), response.getExpiryMonth());
    assertEquals(request.getExpiryYear(), response.getExpiryYear());
    assertEquals(request.getCurrency(), response.getCurrency());
    assertEquals(request.getAmount(), response.getAmount());

    verify(paymentsRepository).add(any(PostPaymentResponse.class));
  }

  @Test
  void whenBankDeclinesPaymentThenDeclinedPaymentIsReturned() {
    PostPaymentRequest request = validPaymentRequest("2222405343248872");

    BankPaymentResponse bankResponse = new BankPaymentResponse(false, null);

    when(bankClient.processPayment(request)).thenReturn(bankResponse);

    PostPaymentResponse response = paymentService.processPayment(request);

    assertEquals(PaymentStatus.DECLINED, response.getStatus());
    assertEquals("8872", response.getCardNumberLastFour());

    verify(paymentsRepository).add(any(PostPaymentResponse.class));
  }

  @Test
  void whenPaymentExistsThenPaymentIsReturned() {
    UUID paymentId = UUID.randomUUID();

    PostPaymentResponse payment = PostPaymentResponse.builder()
        .id(paymentId)
        .status(PaymentStatus.AUTHORIZED)
        .cardNumberLastFour("8877")
        .expiryMonth(12)
        .expiryYear(2030)
        .currency("GBP")
        .amount(100)
        .build();

    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(payment));

    PostPaymentResponse response = paymentService.getPaymentById(paymentId);

    assertEquals(payment, response);
  }

  @Test
  void whenPaymentDoesNotExistThenExceptionIsThrown() {
    UUID paymentId = UUID.randomUUID();

    when(paymentsRepository.get(paymentId)).thenReturn(Optional.empty());

    PaymentNotFoundException exception = assertThrows(
        PaymentNotFoundException.class,
        () -> paymentService.getPaymentById(paymentId)
    );

    assertEquals("Payment not found", exception.getMessage());
  }

  private PostPaymentRequest validPaymentRequest(String cardNumber) {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber(cardNumber);
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv(123);
    return request;
  }
}