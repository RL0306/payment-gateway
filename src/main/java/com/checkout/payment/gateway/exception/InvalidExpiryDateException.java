package com.checkout.payment.gateway.exception;

public class InvalidExpiryDateException extends RuntimeException{
  public InvalidExpiryDateException(String message) {
    super(message);
  }
}
