package com.checkout.payment.gateway.exception;

public class InvalidCreditCardNumberException extends RuntimeException{
  public InvalidCreditCardNumberException(String message){
    super(message);
  }
}
