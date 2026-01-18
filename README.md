# SmartLoad Optimization API

A REST API that determines the optimal combination of orders for a truck to maximize profit while violating no constraints.

## 🚀 Quick Start

### Run with Docker

```bash
docker compose up --build
```

The service will be available at `http://localhost:8080`.

### Check Health

```bash
curl http://localhost:8080/actuator/health
# Output: {"status":"UP"}
```

---

## 🛠 Usage

**Endpoint:** `POST /api/v1/load-optimizer/optimize`

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request.json
```

**Response:**

```json
{
  "truck_id": "truck-123",
  "selected_order_ids": ["ord-001", "ord-002"],
  "total_payout_cents": 430000,
  "total_weight_lbs": 30000,
  "total_volume_cuft": 2100,
  "utilization_weight_percent": 68.18,
  "utilization_volume_percent": 70.0
}
```

---

## 🧪 Testing

Run unit and performance tests:

```bash
./mvnw test
```

## ✅ Constraints Handled

- **Capacity**: Weight & Volume limits.
- **Rules**: Hazmat isolation & Route compatibility.
- **Timing**: Pickup must be before delivery.

---

## 💡 Design & Thought Process

### Why Java & Spring Boot?
- **Java 17 (LTS)**: I chose Java for its strong typing and reliability in handling complex business rules. Using the latest LTS version ensures we have access to modern language features and performance improvements.
- **Spring Boot**: It allows for rapid development of production-ready applications. Is features like `Actuator` (health checks) and built-in validation meant I could focus entirely on the optimization logic rather than infrastructure code.

### Approach to the Solution
- **The Algorithm**: This is multi-dimensional Knapsack problem variation with added constraints (hazmat, routes). Given the constraint of N <= 22 orders, I opted for a **Recursive Backtracking** approach. This guarantees finding the global maximum payout while remaining highly readable. I implemented **pruning** (cutting off search branches that exceed capacity or dates) to ensure it performs efficiently.
- **Precision Matters**: You'll notice I used `long` for `payoutCents`. In financial software, floating-point math (`$2500.00`) can introduce rounding errors. Integer math with cents (`250000`) is the standard pattern to ensure 100% accuracy.
- **Clean Architecture**: I separated the request/response DTOs from the internal Domain Models. This decouples the API contract from the internal logic, making the code easier to maintain and test.
