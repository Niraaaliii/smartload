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
