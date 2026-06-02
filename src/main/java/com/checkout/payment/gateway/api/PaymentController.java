package com.checkout.payment.gateway.api;

import com.checkout.payment.gateway.api.dto.PostPaymentRequest;
import com.checkout.payment.gateway.api.dto.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentService;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @GetMapping("/{id}")
  public ResponseEntity<PostPaymentResponse> getPaymentById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentService.getPaymentById(id), HttpStatus.OK);
  }

  @Operation(
      summary = "Process a payment",
      description = "Validates a payment before sending it to the acquiring bank",
      responses = {
          @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid payment details"),
          @ApiResponse(responseCode = "503", description = "Server error")
      }
  )
  @PostMapping
  public ResponseEntity<PostPaymentResponse> processPayment(
      @Valid @RequestBody PostPaymentRequest request) {

    PostPaymentResponse response = paymentService.processPayment(request);

    return ResponseEntity.ok(response);
  }
}
