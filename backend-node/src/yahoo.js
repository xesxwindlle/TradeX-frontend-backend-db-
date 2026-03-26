import YahooFinance from "yahoo-finance2";
import express from "express";
import { getOrSetCache } from "./cache.js";

/*
Third party API and router setup
*/
const yahooFinance = new YahooFinance();
const app = express();
const port = 5002;
const router = express.Router();


// ── Named single-segment routes (must come before /:symbol) ──────────────────

router.get("/sector/technology", async (_req, res) => {
  try {
    const result = await getOrSetCache("screener:technology", () => yahooFinance.screener("growth_technology_stocks"));
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/gainers", async (_req, res) => {
  try {
    const result = await getOrSetCache("screener:gainers", () => yahooFinance.screener("day_gainers"));
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/losers", async (_req, res) => {
  try {
    const result = await getOrSetCache("screener:losers", () => yahooFinance.screener("day_losers"));
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/active", async (_req, res) => {
  try {
    const result = await getOrSetCache("screener:active", () => yahooFinance.screener("most_actives"));
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── Two-segment named routes ──────────────────────────────────────────────────

router.get("/trending/:country", async (req, res) => {
  const country = req.params.country;
  try {
    const result = await getOrSetCache(`trending:${country}`, async () => {
      const trending = await yahooFinance.trendingSymbols(country, { count: 30 });
      const symbols = (trending.quotes || []).map(q => q.symbol).filter(Boolean);
      if (symbols.length === 0) return { quotes: [] };
      const quotes = await yahooFinance.quote(symbols);
      return { quotes: Array.isArray(quotes) ? quotes : [quotes] };
    });
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/search/:query", async (req, res) => {
  const query = req.params.query;
  try {
    const results = await getOrSetCache(`search:${query}`, () => yahooFinance.search(query));
    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── Symbol routes ─────────────────────────────────────────────────────────────

router.get("/:symbol", async (req, res) => {
  const symbol = req.params.symbol;
  try {
    const quote = await getOrSetCache(`quote:${symbol}`, () => yahooFinance.quote(symbol));
    res.json(quote);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/details", async (req, res) => {
  const symbol = req.params.symbol;
  try {
    // const stats = await yahooFinance.quoteSummary(symbol, { modules: ['price', 'summaryDetail', 'assetProfile'] });
    const stats = await getOrSetCache(`details:${symbol}`, () => yahooFinance.quoteSummary(symbol, { modules: ['price', 'summaryDetail', 'assetProfile'] }));
    res.json(stats);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/historical", async (req, res) => {
  const symbol = req.params.symbol;
  let { period1, period2 } = req.query;
  const today = new Date().toISOString().split("T")[0];
  if (!period1) {
    try {
      const stats = await getOrSetCache(`quote:${symbol}`, () => yahooFinance.quote(symbol));
      period1 = new Date(stats.firstTradeDateMilliseconds).toISOString().split("T")[0];
    } catch (err) {
      return res.status(500).json({ error: err.message });
    }
  }
  try {
    const history = await getOrSetCache(`historical:${symbol}:${period1}:${period2 || today}`, () => yahooFinance.historical(symbol, {
      period1,
      period2: period2 || today,
    }));
    res.json(history);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/chart", async (req, res) => {
  const symbol = req.params.symbol;
  let { period1, period2, interval } = req.query;
  const today = new Date().toISOString().split("T")[0];
  if (!period1) {
    try {
      const stats = await getOrSetCache(`quote:${symbol}`, () => yahooFinance.quote(symbol));
      period1 = new Date(stats.firstTradeDateMilliseconds).toISOString().split("T")[0];
    } catch (err) {
      return res.status(500).json({ error: err.message });
    }
  }
  try {
    const history = await getOrSetCache(`chart:${symbol}:${period1}:${period2 || today}:${interval || '1d'}`, () => yahooFinance.chart(symbol, {
      period1,
      period2: period2 || today,
      interval: interval || "1d",
    }));
    res.json(history);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/fundamentals", async (req, res) => {
  const symbol = req.params.symbol;
  const period1 = req.query.period1 || "2020-01-01";
  const module = req.query.module || "financials";
  try {
    const stats = await getOrSetCache(`fundamentals:${symbol}:${period1}:${module}`, () => yahooFinance.fundamentalsTimeSeries(symbol, { period1, module }));
    res.json(stats);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/fundamentals-over-time", async (req, res) => {
  const symbol = req.params.symbol;
  const period1 = req.query.period1 || "2020-01-01";
  const module = req.query.module || "financials";
  try {
    const stats = await getOrSetCache(`fundamentals-over-time:${symbol}:${period1}:${module}`, () => yahooFinance.fundamentalsTimeSeries(symbol, { period1, module }));
    res.json(stats);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/recommendations", async (req, res) => {
  const symbol = req.params.symbol;
  try {
    const results = await getOrSetCache(`recommendations:${symbol}`, () => yahooFinance.recommendationsBySymbol(symbol));
    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/insights", async (req, res) => {
  const symbol = req.params.symbol;
  try {
    const insights = await getOrSetCache(`insights:${symbol}`, () => yahooFinance.insights(symbol));
    res.json(insights);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get("/:symbol/options", async (req, res) => {
  const symbol = req.params.symbol;
  try {
    const options = await getOrSetCache(`options:${symbol}`, () => yahooFinance.options(symbol));
    res.json(options);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────

app.use("/symbols", router);

app.listen(port, () => {
  console.log(`Yahoo Finance server running at http://localhost:${port}`);
});
