# TradeX

A paper trading platform with real-time market data. Practice investing with live stock quotes — no real money involved.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3, Vite, TailwindCSS, Pinia, amCharts 5 |
| Backend | Spring Boot 3 (Java 21), MySQL 8, Kafka |
| Market Data | Node.js + yahoo-finance2 |
| Live Prices | Node.js WebSocket + Redis pub/sub |
| Auth | JWT, BCrypt, email verification (Gmail SMTP) |

## Project Structure

```
TradeX/
├── frontend/              # Vue 3 SPA
├── backend-springboot/    # Spring Boot REST API
├── backend-node/          # Node.js market data + WebSocket
├── database/
│   ├── schema.sql         # Table definitions
│   └── data.sql           # Seed data (includes admin account)
├── docker-compose.yml
└── .env.example
```

## Features

- **Authentication** — Signup with email verification, login, password reset
- **Market Data** — Live quotes, historical charts, fundamentals, screeners
- **Trading** — Buy/sell orders with real-time portfolio tracking
- **Watchlist** — Track symbols with live price updates
- **Portfolio** — Holdings with P&L tracking
- **Admin Panel** — User management, order history, market stats

## Default Admin Account

| Email | Password |
|---|---|
| admin@tradex.com | admin123 |

---

## Running Locally

### Prerequisites

- Java 21
- Maven
- Node.js 20+
- MySQL 8
- Redis

### 1. Database

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/data.sql
```

### 2. Spring Boot Backend

Edit `backend-springboot/src/main/resources/application.properties` with your database and mail credentials, then:

```bash
cd backend-springboot
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`.

### 3. Node.js Services

```bash
cd backend-node
npm install
```

Start each in a separate terminal:

```bash
node src/yahoo.js      # Market data API — http://localhost:5002
node src/websocket.js  # Live price WebSocket — ws://localhost:5003
node src/poller.js     # Polls Yahoo Finance and pushes to Redis
```

> Redis must be running locally: `redis-server`

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173/TradeX`.

---

## Docker Deployment

### Prerequisites

- Docker
- Docker Compose

### 1. Configure environment

```bash
cp .env.example .env
# Fill in your secrets in .env
```

### 2. Start everything

```bash
docker-compose up -d
```

App will be available on port `80`.

### Updating after code changes

```bash
# Rebuild and push the changed image(s)
docker build --platform linux/amd64 -t haoenc/tradex-frontend:latest ./frontend
docker push haoenc/tradex-frontend:latest

# On the server
docker-compose pull
docker-compose up -d
```
