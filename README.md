# retail-platform

A production-ready retail e-commerce backend built with Spring Boot, MongoDB, Kafka, and GCP.

Live API: https://retail-platform-73450871659.europe-west1.run.app/swagger-ui/index.html

## Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21 + Spring Boot 3.5.14 |
| Database | MongoDB Atlas |
| Messaging | Apache Kafka (Confluent Cloud) |
| Deployment | GCP Cloud Run |
| Build | Docker + Cloud Build |

## Architecture


```
POST /api/v1/orders
         │
         ▼
   OrderService ──publish──► Kafka: order.placed
         │                           │
         ▼                           ▼
   Order{PENDING}         StockEventConsumer
   → MongoDB                         │
                          ┌──────────┴──────────┐
                          ▼                     ▼
                  Order{CONFIRMED}       Order{CANCELLED}
                  stock decremented      insufficient stock
                          │
                          └──publish──► Kafka: stock.updated
```



## Endpoints

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | List all products (paginated, filterable by category) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products/sku/{sku}` | Get product by SKU |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{id}` | Update a product |
| DELETE | `/api/v1/products/{id}` | Delete a product |

### Stock
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/stock/{productId}` | Get stock for a product |
| POST | `/api/v1/stock` | Initialize stock |
| PATCH | `/api/v1/stock/{productId}/adjust` | Adjust stock (IN / OUT) |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Place an order |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| GET | `/api/v1/orders?customerId={id}` | Get orders by customer |

## Key design decisions

**Why separate `stock` from `product`?**
Stock changes on every order. The catalog changes rarely. Two aggregates with different write frequencies — merging them would cause unnecessary write conflicts at scale.

**Why Kafka over direct HTTP calls?**
Decoupling. The order service publishes an event and returns immediately. Stock, email, warehouse — each consumer reacts independently at its own pace. If a consumer is down, events persist in the topic and are processed on restart. No lost orders.

**Why `PENDING` → `CONFIRMED` status flow?**
The order is recorded instantly (fast response to client), stock is decremented asynchronously by the Kafka consumer. This is eventual consistency — the final state is always coherent, just not instantaneous.

**Why immutable SKU?**
The SKU is the business identifier of a product — it appears on labels, in orders, in third-party systems. Changing it after creation would cause data inconsistency across the board.

**Why Java records for DTOs?**
Immutable, concise, zero boilerplate. The Mongo document and the API response are always separate types — if the persistence layer changes, the API contract doesn't break.

## Running locally

```bash
# Start MongoDB + Kafka
docker-compose up -d

# Run the API
./mvnw spring-boot:run

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Deployment

Built and deployed on GCP Cloud Run via Cloud Build:

```bash
gcloud builds submit --tag gcr.io/retail-platform-496417/retail-platform
gcloud run deploy retail-platform --image gcr.io/retail-platform-496417/retail-platform --platform managed --region europe-west1 --allow-unauthenticated
```