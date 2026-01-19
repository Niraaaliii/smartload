# SmartLoad Optimization API

REST API that finds the best combination of orders for a truck to maximize revenue.

## How to run

```bash
git clone <your-repo>
cd smartload
docker compose up --build
```

Service will be available at http://localhost:8080

## Health check

```bash
curl http://localhost:8080/actuator/health
```

## Example request

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request.json
```

Example response:

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

## Running tests

```bash
./mvnw test
```

## What it does

The API picks orders that maximize profit while respecting:
- Weight and volume limits
- Route compatibility (same origin/destination)
- Hazmat rules (can't mix hazmat with regular cargo)
- Date constraints (pickup before delivery)

## Tech choices

I went with Java 17 and Spring Boot because:
- Java's type system helps catch errors early when dealing with business rules
- Spring Boot has built-in validation and health checks
- Actuator gives us the /actuator/health endpoint for free

For the algorithm, I used backtracking with pruning. Since we're limited to 22 orders max, this approach guarantees finding the best solution while being easier to understand than dynamic programming. I added some optimizations like sorting orders by value density and tracking suffix sums to skip branches that can't improve the result.

One thing to note - I'm using `long` for money (in cents) instead of `double` to avoid floating point rounding issues. That's pretty standard for financial calculations.
