package com.checkout.payment.gateway.api.dto;

public class ErrorResponse {
  private final String message;

  public ErrorResponse(String message) {
    this.message = message.toLowerCase();
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        '}';
  }
}
