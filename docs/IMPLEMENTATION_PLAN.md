# SmartLoad Optimization API - Implementation Plan

Build a REST API that returns the optimal combination of orders to maximize revenue while respecting truck constraints.

---

## Algorithm Choice

**Recursive Backtracking with Pruning** - Simple, readable, and performant for n ≤ 22 orders.

---

## File Structure

```
src/main/java/com/teleport/smartload/
├── model/
│   ├── Truck.java
│   └── Order.java
├── dto/
│   ├── OptimizeRequest.java
│   └── OptimizeResponse.java
├── service/
│   ├── LoadOptimizerService.java
│   └── LoadOptimizerServiceImpl.java
├── controller/
│   └── LoadOptimizerController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── SmartloadApplication.java
```

---

## Components

### 1. Models
- **Truck**: id, maxWeightLbs, maxVolumeCuft
- **Order**: id, payoutCents (Long), weightLbs, volumeCuft, origin, destination, pickupDate, deliveryDate, isHazmat

### 2. DTOs
- **OptimizeRequest**: truck + list of orders (with validation)
- **OptimizeResponse**: truckId, selectedOrderIds, totalPayoutCents, totalWeightLbs, totalVolumeCuft, utilization percentages

### 3. Service (Core Algorithm)
- Filter compatible orders (same route, valid dates)
- Recursive backtracking to find max revenue combination
- Constraints: weight, volume, hazmat isolation

### 4. Controller
- `POST /api/v1/load-optimizer/optimize`
- Returns 200 on success, 400 on invalid input

### 5. Docker
- Multi-stage Dockerfile
- docker-compose.yml (port 8080)

---

## Constraints Checklist
- [x] Stateless (no database)
- [x] Port 8080
- [x] Money in cents (Long, not float)
- [x] Hazmat isolation
- [x] Route compatibility (same origin/destination)
- [x] Weight/volume limits
