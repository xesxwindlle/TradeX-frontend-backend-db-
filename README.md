# TradeX

A full-stack stock trading simulation platform where users can browse market data, place buy/sell orders, manage portfolios, and track watchlists — all with real-time-style market information.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3, Vite, Tailwind CSS, DaisyUI, Pinia, amCharts 5 |
| Backend | Java 21 Servlets (WAR), JWT authentication, Google Gmail API |
| Database | MySQL 8 |
| Server | Apache Tomcat |

## Project Structure

```
TradeX/
├── frontend/          # Vue 3 SPA
│   ├── src/
│   │   ├── components/    # Vue components (30+)
│   │   ├── stores/        # Pinia stores (auth, account, admin, symbolQuote)
│   │   ├── status/        # Page-level views (Login, SignUp, Admin, AccountManagement)
│   │   ├── library/       # Axios config, constants, utilities
│   │   └── styles/        # Global styles
│   └── vite.config.js
│
├── backend/           # Java Servlet API
│   ├── src/main/java/
│   │   ├── controller/    # Servlet endpoints
│   │   └── domain/        # Domain models, DAOs, services
│   └── pom.xml
│
└── database/          # SQL scripts
    ├── schema.sql         # Table definitions
    └── data.sql           # Sample seed data
```

## Features

### User Features
- **Authentication** — Sign up, login, JWT-based sessions, password reset via email verification
- **Market Data** — Browse instruments with real-time prices, historical charts, fundamentals
- **Trading** — Place buy/sell orders with portfolio balance tracking
- **Portfolio** — View holdings with unit price and quantity, track profit/loss
- **Watchlist** — Add/remove symbols to a personal watchlist
- **Account Management** — Add funds, view order history, update phone number, change password

### Admin Features
- **User Management** — View all users/accounts, export to CSV
- **Order Management** — View all orders across accounts, export to CSV
- **Market Stats** — Most traded, most held, most watched symbols
- **Instrument Management** — Browse/search instruments, view sector/industry hierarchy

### Visualizations
- Market treemap by sector (amCharts 5)
- Winners/losers bar charts
- Index cards with moving ticker
- Historical candlestick/line charts
- Sector & industry tree view
- Ranking charts for most active, most held, most watched

## Prerequisites

- **Java** 21+
- **Apache Tomcat** 9 or 10
- **MySQL** 8.0+
- **Node.js** 20.19+ or 22.12+
- **Maven** 3.8+

## Setup

### 1. Database

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/data.sql
```

Configure a JNDI DataSource named `jdbc/TradeX` in your Tomcat server. Add to your Tomcat `context.xml`:

```xml
<Resource name="jdbc/TradeX"
          auth="Container"
          type="javax.sql.DataSource"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/TradeX"
          username="your_username"
          password="your_password"
          maxTotal="20"
          maxIdle="10"
          maxWaitMillis="10000" />
```

### 2. Backend

```bash
cd backend
mvn clean package
```

Deploy the generated `target/TradeX-0.0.1-SNAPSHOT.war` to Tomcat's `webapps/` directory (rename to `TradeX.war` for cleaner URLs).

The API will be available at `http://localhost:8080/TradeX`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend dev server runs at `http://localhost:5173` and expects the backend at `http://localhost:8080/TradeX`.

To build for production:

```bash
npm run build
```

The output will be in `frontend/dist/`.

## Database Schema

9 tables across 3 domains:

**User & Account**
- `ACCOUNTS` — Login credentials, balances, role (Regular/Admin), status (Active/Limited/Frozen/Closed)
- `USERS` — Personal info linked to an account

**Market & Instruments**
- `instruments` — Stock/index metadata with sector, industry, exchange info
- `market_data` — Real-time price data (price, volume, moving averages)
- `fundamental_metrics` — PE ratios, EPS, dividends, market cap
- `historical_quotes` — Daily OHLCV candle data

**Trading & Portfolio**
- `orders` — Buy/Sell orders with status tracking (Open/Filled/Cancelled)
- `ACCOUNT_HOLDS_SYMBOLS` — Current portfolio holdings
- `ACCOUNT_WATCHES_SYMBOLS` — Watchlist entries

## API Endpoints

Key servlet endpoints (all under `/TradeX`):

| Category | Endpoints |
|---|---|
| Auth | Login, SignUp, RequestPasswordReset, ResetPassword, SendSignupVerificationCode |
| Account | GetAccountInfo, AddFund, UpdatePassword, UpdateMobileNumber, DeleteAccount |
| Trading | CreateOrder, GetOrders, GetHoldings, UpdateHolding |
| Watchlist | AddWatching, RemoveWatching, GetWatchings |
| Market Data | SearchSymbol, GetRandomSymbolQuotes, GetTopWinners, GetTopLosers, GetMostActive |
| Admin | GetUsers, GetAllOrders, GetAllInstruments, SearchInstruments, ExportUsersCSV, ExportOrdersCSV, ExportInstrumentsCSV |
| Analytics | GetMostTraded, GetMostHeld, GetMostWatched, GetTotalProfitLoss, GetMarketTreemapData, GetSectorIndustryTree |
