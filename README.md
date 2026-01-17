# SmartLoad Optimization API

A simple REST API that figures out the best combination of orders for a truck to maximize profit.

## Quick Start

```bash
git clone <your-repo>
cd smartload
docker compose up --build
```

The service runs on `http://localhost:8080`

## Check if it's running

```bash
curl http://localhost:8080/actuator/health
```

## How to use

### POST /api/v1/load-optimizer/optimize

Send a truck and a list of orders, get back the optimal combination.

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

## What it checks

- **Weight & Volume** - won't exceed truck limits
- **Same Route** - all orders must go to the same destination
- **Hazmat Rules** - no mixing hazmat with regular cargo
- **Valid Dates** - pickup must be before delivery

## Built with

- Java 17
- Spring Boot 4.0
- Maven
- Docker
