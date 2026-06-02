package com.checkout.payment.gateway.api;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.checkout.payment.gateway.api.dto.PostPaymentRequest;
import com.checkout.payment.gateway.model.PaymentStatus;
import com.checkout.payment.gateway.api.dto.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;
  @Autowired
  private ObjectMapper objectMapper;

  final String PAYMENT_API = "/api/v1/payments";

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment =
        PostPaymentResponse
            .builder()
            .id(UUID.randomUUID())
            .amount(10)
            .currency("USD")
            .status(PaymentStatus.AUTHORIZED)
            .expiryMonth(12)
            .expiryYear(2024)
            .cardNumberLastFour("4321")
            .build();

    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get(PAYMENT_API + "/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.card_number_last_four").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiry_month").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiry_year").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get(PAYMENT_API + "/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("payment not found"));
  }

  @Test
  void whenCardNumberIsInvalidThen400IsReturned() throws Exception {

    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("123");
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv(123);

    String jsonRequest = objectMapper.writeValueAsString(request);

    mvc.perform(post(PAYMENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
            .value("card_number must be between 14 and 19 digits"));
  }

  @Test
  void whenExpiryDateIsInPastThen400IsReturned() throws Exception {

    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248877");
    request.setExpiryMonth(1);
    request.setExpiryYear(2020);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv(123);

    String jsonRequest = objectMapper.writeValueAsString(request);

    mvc.perform(post(PAYMENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
            .value("expiry date must be in the future"));
  }

  @Test
  void whenValidPaymentRequestThenPaymentIsProcessedSuccessfully() throws Exception {

    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248877");
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv(123);

    String jsonRequest = objectMapper.writeValueAsString(request);

    mvc.perform(post(PAYMENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.card_number_last_four").value("8877"))
        .andExpect(jsonPath("$.expiry_month").value(12))
        .andExpect(jsonPath("$.expiry_year").value(2030))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void whenValidPaymentRequestIsDeclinedThenDeclinedPaymentIsReturned() throws Exception {

    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248872");
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv(123);

    String jsonRequest = objectMapper.writeValueAsString(request);

    mvc.perform(post(PAYMENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value(PaymentStatus.DECLINED.getName()));
  }

  @Test
  void whenBankIsUnavailableThen503IsReturned() throws Exception {

    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248870");
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv(123);

    String jsonRequest = objectMapper.writeValueAsString(request);

    mvc.perform(post(PAYMENT_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.message")
            .value("bank is unavailable"));
  }


}
