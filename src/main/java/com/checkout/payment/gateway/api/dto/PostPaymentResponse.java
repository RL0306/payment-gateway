package com.checkout.payment.gateway.api.dto;

import com.checkout.payment.gateway.model.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class PostPaymentResponse {
  private UUID id;
  private PaymentStatus status;
  @JsonProperty("card_number_last_four")
  private String cardNumberLastFour;
  @JsonProperty("expiry_month")
  private int expiryMonth;
  @JsonProperty("expiry_year")
  private int expiryYear;
  private String currency;
  private Integer amount;

}
