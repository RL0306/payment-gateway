package com.checkout.payment.gateway.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class PostPaymentRequest implements Serializable {

  @JsonProperty("card_number")
  @NotBlank(message = "card_number is required")
  @Size(min = 14, max = 19, message = "card_number must be between 14 and 19 digits")
  @Pattern(regexp = "\\d+", message = "card_number must only contain numeric characters")
  private String cardNumber;

  @JsonProperty("expiry_month")
  @Min(value = 1, message = "expiry_month must be between 1 and 12")
  @Max(value = 12, message = "expiry_month must be between 1 and 12")
  private int expiryMonth;

  @JsonProperty("expiry_year")
  private int expiryYear;

  @NotBlank(message = "currency is required")
  @Size(min = 3, max = 3, message = "currency must be 3 characters")
  @Pattern(
      regexp = "GBP|USD|EUR",
      message = "currency must be one of GBP, USD or EUR"
  )
  private String currency;

  @Positive(message = "amount must be greater than 0")
  private Integer amount;

  @Min(value = 100, message = "cvv must be 3 or 4 digits")
  @Max(value = 9999, message = "cvv must be 3 or 4 digits")
  private int cvv;

}
