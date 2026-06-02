# Payment Gateway Challenge

A simple payment gateway implementation built using Spring Boot.

The application accepts card payment requests, forwards them to the provided acquiring bank simulator, stores the result and allows previously processed payments to be retrieved.

---

## Features

- Process card payments
- Retrieve previously processed payments
- Request validation using Jakarta Bean Validation
- Expiry date validation
- Integration with acquiring bank simulator
- Consistent error responses
- Swagger/OpenAPI documentation
- Unit and integration test coverage

---

## Technology Stack

- Java 17
- Spring Boot 3
- Gradle
- JUnit 5
- Mockito
- Lombok
- SpringDoc OpenAPI

---

## Running the Application

### Start the bank simulator

```bash
docker run --rm -p 8080:8080 checkout/banksimulator
```

### Start the application

```bash
./gradlew bootRun
```

The application will start on:

```text
http://localhost:8090
```

---

## Swagger

Swagger UI is available at:

```text
http://localhost:8090/swagger-ui/index.html
```

## Postman Collection

A Postman collection is included in the repository for testing the API.

```text
payment-gateway.postman_collection.json
```

---

## API Examples

### Process an authorised payment

#### Request

```http
POST /api/v1/payments
Content-Type: application/json
```

```json
{
  "card_number": "2222405343248877",
  "expiry_month": 12,
  "expiry_year": 2030,
  "currency": "GBP",
  "amount": 1050,
  "cvv": 123
}
```

#### Response

```json
{
  "id": "5221566d-d4d7-4689-80f7-28207dbc09dc",
  "status": "Authorized",
  "currency": "GBP",
  "amount": 1050,
  "card_number_last_four": "8877",
  "expiry_month": 12,
  "expiry_year": 2030
}
```

---

### Process a declined payment

#### Request

```json
{
  "card_number": "2222405343248872",
  "expiry_month": 12,
  "expiry_year": 2030,
  "currency": "GBP",
  "amount": 1050,
  "cvv": 123
}
```

#### Response

```json
{
  "id": "22e1ea27-90aa-4d37-9106-9ab6d23e8f36",
  "status": "Declined",
  "currency": "GBP",
  "amount": 1050,
  "card_number_last_four": "8872",
  "expiry_month": 12,
  "expiry_year": 2030
}
```

---

### Retrieve a payment

#### Request

```http
GET /api/v1/payments/{id}
```

#### Response

```json
{
  "id": "5221566d-d4d7-4689-80f7-28207dbc09dc",
  "status": "Authorized",
  "currency": "GBP",
  "amount": 1050,
  "card_number_last_four": "8877",
  "expiry_month": 12,
  "expiry_year": 2030
}
```

---

### Validation Error

#### Response

```json
{
  "message": "card_number must be between 14 and 19 digits"
}
```

---

## Design Decisions

### Architecture

The application is separated into four responsibilities:

- API layer (`PaymentController`)
- Service layer (`PaymentService`)
- Repository layer (`PaymentsRepository`)
- Bank client layer (`BankClient`)

This keeps HTTP concerns, business logic, persistence and external integrations separated while avoiding unnecessary complexity.

### Validation

Validation is performed using Jakarta Bean Validation annotations where possible.

Additional business validation ensures expired cards cannot be processed.

### Storage

Payments are stored in-memory using a repository backed by a Java `Map`.

For a production system this would likely be replaced with a persistent database such as PostgreSQL.

### Security

The full card number and CVV are never returned by the API.

Only the last four digits of the card number are exposed when processing or retrieving a payment.

Sensitive card information is excluded from application logs.

### Error Handling

The API returns the following status codes:

| Status | Description |
|----------|-------------|
| 200 | Payment processed successfully |
| 400 | Validation failure |
| 404 | Payment not found |
| 503 | Acquiring bank unavailable |

### Testing

The solution contains both integration and unit tests.

#### Controller Integration Tests

- Successful payment processing
- Declined payment processing
- Payment retrieval
- Validation failures
- Bank unavailable scenarios

#### Service Unit Tests

- Authorized payment processing
- Declined payment processing
- Payment retrieval
- Payment not found scenarios

---

## Assumptions

The following assumptions were made while implementing the solution:

- Amounts are provided in minor currency units (for example £10.50 is represented as `1050`)
- Duplicate payment protection and idempotency are outside the scope of this exercise

---

## Future Improvements

If the application were developed further, I would consider:

- Persistent storage
- Idempotency support
- Metrics
- Retry and resilience patterns around bank communication
- Correlation IDs and distributed tracing