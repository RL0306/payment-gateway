package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.api.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException pe) {
    LOG.error("Exception happened", pe);
    return new ResponseEntity<>(new ErrorResponse(pe.getMessage()),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InvalidExpiryDateException.class)
  public ResponseEntity<ErrorResponse> handleInvalidExpiryDateException(InvalidExpiryDateException ie){
    LOG.error("Exception happened", ie);
    return new ResponseEntity<>(new ErrorResponse(ie.getMessage()),
        HttpStatus.BAD_REQUEST);
  }


  @ExceptionHandler(InvalidCreditCardNumberException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCreditCardNumberException(InvalidCreditCardNumberException ie){
    LOG.error("Exception happened", ie);
    return new ResponseEntity<>(new ErrorResponse(ie.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailableException(
      BankUnavailableException ex) {

    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

    return new ResponseEntity<>(new ErrorResponse(errorResponse.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException exception) {

    String message = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(FieldError::getDefaultMessage)
        .findFirst()
        .orElse("Invalid request");

    ErrorResponse errorResponse = new ErrorResponse(message);

    return new ResponseEntity<>(new ErrorResponse(errorResponse.getMessage()), HttpStatus.BAD_REQUEST);
  }

}
