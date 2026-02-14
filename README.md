# Smart Valuator API

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-blue?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-7.0-green?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue?logo=postgresql&logoColor=white)
![Build](https://img.shields.io/badge/Build-Maven-red?logo=apachemaven&logoColor=white)
![AI](https://img.shields.io/badge/AI-Llama%203.3-orange?logo=meta&logoColor=white)
![Hugging Face](https://img.shields.io/badge/🤗-Hugging%20Face-yellow)

</div>

---

## Description

AI-powered REST API for electronic item valuation built with **Spring Boot 7** for the **European market**. It integrates **Llama 3.3-70B** via Hugging Face for intelligent price estimation in **EUR (€)** with market-based fallback pricing. Demonstrates clean layered architecture (**Controller** → **Service** → **Repository** → **Entity/DTO**), AI integration, error handling, and REST best practices.

---

## Features

- **AI-Powered Valuation**
    - Natural language descriptions via Llama 3.3-70B
    - Intelligent price estimation based on market data
    - Fallback pricing when AI is unavailable
    - Condition-based price adjustment (1-10 scale)

- **Complete CRUD operations**
    - Create, Read, Update, Delete estimations
    - Historical tracking with timestamps
    - Price and description storage

- **Architecture & Quality**
    - Clean layered architecture
    - DTOs for API communication
    - Centralized error handling
    - BigDecimal for precise currency values
    - Proper JSON serialization (tools.jackson)
    - CORS enabled for frontend integration

---

## Technologies

| Technology | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 7.0+ |
| Spring Data JPA | - |
| PostgreSQL | - |
| Hugging Face API | Llama 3.3-70B |
| Lombok | - |
| tools.jackson | 3.0+ |

---

## Project Structure
```
com.yann.smart_valuator_api
│
├── config/           → Spring configuration (JacksonConfig, CORS)
├── controller/       → REST endpoints (EstimationController)
├── service/          → Business logic (EstimationService, HuggingFaceService)
├── repository/       → JPA interfaces for persistence
├── entity/           → JPA entities (Estimation)
└── DTO/              → API communication objects (AiEstimationResult, ChatCompletionRequest)
```

---

## AI Valuation Flow
```
Client                          Server                          Hugging Face
  │                                │                                │
  │──── POST /api/estimations ────>│                                │
  │                                │──── AI Request ───────────────>│
  │                                │                                │ Llama 3.3-70B
  │                                │<─── JSON Response ─────────────│ processes item
  │                                │                                │
  │                                │  Parse & Validate              │
  │                                │  (or use fallback pricing)     │
  │                                │                                │
  │<─── 200 OK + Estimation ───────│  Save to database             │
  │                                │                                │
  │──── GET /api/estimations ─────>│                                │
  │<─── List of estimations ───────│                                │
```

1. **Submit Item** — Client sends item details (name, brand, category, year, condition)
2. **AI Processing** — API calls Llama 3.3-70B with structured prompt
3. **Price Calculation** — AI estimates market value or fallback applies condition-based pricing
4. **Storage** — Estimation saved with description, price, and timestamp
5. **Retrieval** — Historical estimations can be queried and managed

---

## Endpoints

### Estimations

| Method | Endpoint | Description | Body Required |
|---|---|---|---|
| POST | `/api/estimations` | Create new estimation | Yes |
| GET | `/api/estimations` | List all estimations | No |
| GET | `/api/estimations/{id}` | Get estimation by ID | No |
| PUT | `/api/estimations/{id}` | Update estimation | Yes |
| DELETE | `/api/estimations/{id}` | Delete estimation | No |

---

## Examples

### Create Estimation
```http
POST /api/estimations
Content-Type: application/json

{
  "itemName": "iPhone 14 Pro",
  "brand": "Apple",
  "category": "Smartphone",
  "year": 2022,
  "conditionRating": 8
}
```

### Response
```json
{
  "id": 1,
  "itemName": "iPhone 14 Pro",
  "brand": "Apple",
  "category": "Smartphone",
  "year": 2022,
  "conditionRating": 8,
  "estimatedPrice": 400.00,
  "aiDescription": "iPhone 14 Pro from 2022 in very good condition (8/10). This model retains strong resale value in the European market with its A16 chip and advanced camera system.",
  "createdAt": "2026-02-13T08:30:15.123456"
}
```

### Get All Estimations
```http
GET /api/estimations
```

### Response
```json
[
  {
    "id": 1,
    "itemName": "iPhone 14 Pro",
    "estimatedPrice": 400.00,
    "createdAt": "2026-02-13T08:30:15.123456",
    ...
  },
  {
    "id": 2,
    "itemName": "MacBook Pro",
    "estimatedPrice": 680.00,
    "createdAt": "2026-02-13T08:25:10.654321",
    ...
  }
]
```

### Update Estimation
```http
PUT /api/estimations/1
Content-Type: application/json

{
  "itemName": "iPhone 14 Pro Max",
  "brand": "Apple",
  "category": "Smartphone",
  "year": 2022,
  "conditionRating": 9,
  "estimatedPrice": 500.00,
  "aiDescription": "Updated description"
}
```

### Delete Estimation
```http
DELETE /api/estimations/1
```

---

## AI Pricing Logic

### Llama 3.3-70B Integration
- Natural language prompts with European market context
- Structured JSON output parsing
- Markdown cleanup and validation
- Timeout handling (15s connection, 30s read)
- Pricing in EUR (€) for European market

### Fallback Pricing (when AI unavailable)

**Base Prices by Category (EUR):**

| Product Category | Base Price (EUR) |
|---|---|
| iPhone 15/16 | €650 |
| iPhone 14 | €500 |
| iPhone 13 | €380 |
| iPhone 12 | €280 |
| iPhone 11 | €200 |
| iPhone X/10 | €170 |
| Samsung Galaxy S23/24 | €470 |
| Samsung Galaxy S22/21 | €320 |
| MacBook Pro | €850 |
| MacBook Air | €550 |
| iPad Pro | €470 |
| iPad Air | €280 |
| Generic Laptop | €370 |
| Generic Tablet | €180 |
| Smartwatch | €230 |
| AirPods | €90 |
| Gaming Console | €320 |

**Condition Multiplier:**
```
Final Price = Base Price × (Condition Rating / 10)

Example:
iPhone 14 (base: €500) with condition 8/10
= €500 × 0.8 = €400.00
```

---

## Installation

### Prerequisites
```bash
# Java 17+
java -version

# Maven
mvn -version

# PostgreSQL running on localhost:5432
```

### Setup
```bash
# Clone the project
git clone <repo-url>
cd smart-valuator-api

# Configure database
# Edit src/main/resources/application.properties:
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_valuator
spring.datasource.username=your_username
spring.datasource.password=your_password

# Add Hugging Face API key
hf.api.key=your_huggingface_api_key

# Build
mvn clean compile

# Run tests
mvn test

# Run the project
mvn spring-boot:run
```

API available at: `http://localhost:8080`

---

## Configuration

### application.properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_valuator
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# Hugging Face API
hf.api.key=hf_your_api_key_here

# Server
server.port=8080

# CORS
cors.allowed-origins=http://localhost:4200
```

### Database Schema
```sql
CREATE TABLE estimations (
    id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    category VARCHAR(255),
    year INTEGER NOT NULL,
    condition_rating INTEGER,
    estimated_price DECIMAL(10,2),
    ai_description TEXT,
    created_at TIMESTAMP NOT NULL
);
```

---

## Error Handling

### AI Service Failures
```java
// Automatic fallback to market-based pricing
if (aiResponse.equals("API_ERROR")) {
    return createFallbackResult(productDetails);
}
```

### Network Timeouts
```java
// Configured timeouts prevent hanging
factory.setConnectTimeout(15000); // 15s
factory.setReadTimeout(30000);    // 30s
```

### Price Validation
```java
// Ensures price is never null or zero
if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
    price = estimateFallbackPrice(productDetails);
}
```

---

## Developer Notes

- **BigDecimal** is used for all monetary values to ensure precision
- **EUR (€)** is the currency for all prices (European market)
- **LocalDateTime** is serialized in ISO-8601 format for frontend compatibility
- **tools.jackson** (Jackson 3.x) is used for JSON processing in Spring Boot 7
- **Fallback pricing** ensures the system works even without AI connectivity
- **Condition rating** (1-10) directly affects final price estimation
- AI descriptions provide natural language context for valuations in European market context
- All timestamps are automatically generated via `@PrePersist`
- CORS is configured to allow frontend access from `localhost:4200`
- Prices are adjusted for European market (approx. 0.92x USD → EUR conversion)

---

## Testing Flow

1. **Start PostgreSQL** database
2. **Configure** API key in `application.properties`
3. **Run** the application: `mvn spring-boot:run`
4. **Test** with Postman or integrated frontend:
    - Create estimation → Verify AI description and price
    - List estimations → Check historical data
    - Update estimation → Modify values
    - Delete estimation → Clean up data

---

## Future Enhancements

- [ ] User authentication and authorization
- [ ] Multiple AI model support
- [ ] Image upload for item photos
- [ ] Price history tracking over time
- [ ] Market trend analysis
- [ ] Export estimations to PDF/Excel
- [ ] Email notifications for price changes
- [ ] Mobile app integration

---

*Personal project — Educational purpose — No restrictive licenses*