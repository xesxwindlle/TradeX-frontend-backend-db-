import axios from 'axios'

const yahoo = axios.create({ baseURL: '/yahoo/symbols' })

function oneYearAgo() {
  const d = new Date()
  d.setFullYear(d.getFullYear() - 1)
  return d.toISOString().split('T')[0]
}

function nDaysAgo(n) {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return d.toISOString().split('T')[0]
}

function normalizeQuote(q) {
  return {
    symbol: q.symbol,
    name: q.shortName || q.longName || q.symbol,
    companyDescription: '',
    regularMarketPrice: q.regularMarketPrice ?? 0,
    regularMarketChange: q.regularMarketChange ?? 0,
    regularMarketChangePercent: q.regularMarketChangePercent ?? 0,
    regularMarketOpen: q.regularMarketOpen ?? 0,
    regularMarketDayHigh: q.regularMarketDayHigh ?? 0,
    regularMarketDayLow: q.regularMarketDayLow ?? 0,
    regularMarketPreviousClose: q.regularMarketPreviousClose ?? 0,
    regularMarketVolume: q.regularMarketVolume ?? 0,
    postMarketPrice: q.postMarketPrice ?? null,
    postMarketChange: q.postMarketChange ?? null,
    postMarketChangePercent: q.postMarketChangePercent ?? null,
    fiftyTwoWeekLow: q.fiftyTwoWeekLow ?? 0,
    fiftyTwoWeekHigh: q.fiftyTwoWeekHigh ?? 0,
    fiftyDayAverage: q.fiftyDayAverage ?? 0,
    twoHundredDayAverage: q.twoHundredDayAverage ?? 0,
    averageDailyVolume3m: q.averageDailyVolume3Month ?? 0,
    averageDailyVolume10d: q.averageDailyVolume10Day ?? 0,
    marketCap: q.marketCap ?? 0,
    trailingPe: q.trailingPE ?? 0,
    forwardPe: q.forwardPE ?? 0,
    priceToBook: q.priceToBook ?? 0,
    trailingEps: q.epsTrailingTwelveMonths ?? 0,
    forwardEps: q.epsForward ?? 0,
    bookValue: q.bookValue ?? 0,
    trailingAnnualDividendYield: q.trailingAnnualDividendYield ?? 0,
    averageAnalystRating: q.averageAnalystRating ?? '',
    bid: q.bid ?? 0,
    ask: q.ask ?? 0,
    preMarketPrice: q.preMarketPrice ?? null,
    preMarketChange: q.preMarketChange ?? null,
    preMarketChangePercent: q.preMarketChangePercent ?? null,
    sharesOutstanding: q.sharesOutstanding ?? 0,
    marketState: q.marketState || 'REGULAR',
    historicalQuotes: [],
  }
}

function wrap(data) {
  return { data: { success: true, message: null, data } }
}

export async function getQuote(symbol) {
  const [quoteRes, histRes, detailRes] = await Promise.all([
    yahoo.get(`/${symbol}`),
    yahoo.get(`/${symbol}/historical`, { params: { period1: oneYearAgo() } }).catch(() => ({ data: [] })),
    yahoo.get(`/${symbol}/details`).catch(() => ({ data: {} })),
  ])
  const md = normalizeQuote(quoteRes.data)

  const profile = detailRes.data?.assetProfile || {}
  const summary = detailRes.data?.summaryDetail || {}

  md.sector = profile.sector || ''
  md.industry = profile.industry || ''
  md.companyDescription = profile.longBusinessSummary || ''
  md.beta = summary.beta ?? quoteRes.data.beta ?? 0
  md.priceToSalesTrailing12Months = summary.priceToSalesTrailing12Months ?? 0
  md.averageDailyVolume10d = summary.averageDailyVolume10Day ?? quoteRes.data.averageDailyVolume10Day ?? 0
  md.averageDailyVolume3m = summary.averageDailyVolume3Month ?? quoteRes.data.averageDailyVolume3Month ?? 0
  md.dividendRate = summary.dividendRate ?? 0
  md.dividendYield = summary.dividendYield ?? 0

  const keyStats = detailRes.data?.defaultKeyStatistics || {}
  md.sharesOutstanding = keyStats.sharesOutstanding?.raw ?? quoteRes.data.sharesOutstanding ?? 0
  md.epsCurrentYear = keyStats.forwardEps ?? 0
  md.nextEarningsDate = keyStats.nextFiscalYearEnd?.fmt ?? ''
  md.allTimeHigh = keyStats['52WeekHigh'] ?? quoteRes.data.fiftyTwoWeekHigh ?? 0
  md.allTimeLow = keyStats['52WeekLow'] ?? quoteRes.data.fiftyTwoWeekLow ?? 0
  md.nonDilutedMarketCap = keyStats.enterpriseValue?.raw ?? 0

  md.historicalQuotes = (histRes.data || []).map(q => ({
    date: new Date(q.date).getTime(),
    open: q.open ?? 0,
    high: q.high ?? 0,
    low: q.low ?? 0,
    close: q.close ?? 0,
    volume: q.volume ?? 0,
  }))
  return wrap(md)
}

// Map period name → { period1, interval } for the Yahoo chart API
function periodToParams(period) {
  const today = new Date().toISOString().split('T')[0]
  const map = {
    '1M':  { period1: nDaysAgo(30),  interval: '1d'  },
    '3M':  { period1: nDaysAgo(90),  interval: '1d'  },
    '6M':  { period1: nDaysAgo(180), interval: '1d'  },
    '1Y':  { period1: nDaysAgo(365), interval: '1d' },
    'Max': { period1: '2000-01-01',  interval: '1d' },
  }
  return { ...(map[period] || map['1Y']), period2: today }
}

export async function getChart(symbol, period = '1Y') {
  const { period1, period2, interval } = periodToParams(period)
  const res = await yahoo.get(`/${symbol}/chart`, { params: { period1, period2, interval } })
  const quotes = (res.data?.quotes || [])
    .map(q => ({
      date: new Date(q.date).getTime(),
      open: q.open ?? 0,
      high: q.high ?? 0,
      low: q.low ?? 0,
      close: q.close ?? 0,
      volume: q.volume ?? 0,
    }))
    .filter(q => q.close > 0)
  return wrap(quotes)
}

export async function getRecommendations(symbol) {
  const res = await yahoo.get(`/${symbol}/recommendations`).catch(() => ({ data: {} }))
  return wrap(res.data?.recommendedSymbols || [])
}

export async function getInsights(symbol) {
  const res = await yahoo.get(`/${symbol}/insights`).catch(() => ({ data: {} }))
  return wrap(res.data || {})
}

export async function getWinners() {
  const res = await yahoo.get('/gainers')
  return wrap((res.data.quotes || []).map(normalizeQuote))
}

export async function getLosers() {
  const res = await yahoo.get('/losers')
  return wrap((res.data.quotes || []).map(normalizeQuote))
}

export async function getMostActive() {
  const res = await yahoo.get('/active')
  return wrap((res.data.quotes || []).map(normalizeQuote))
}

export async function getRandomQuotes() {
  const res = await yahoo.get('/trending/US').catch(() => ({ data: { quotes: [] } }))
  return wrap((res.data.quotes || []).slice(0, 12).map(normalizeQuote))
}

export async function searchSymbols(term) {
  const res = await yahoo.get(`/search/${encodeURIComponent(term)}`)
  const results = (res.data?.quotes || [])
    .filter(r => r.isYahooFinance && r.quoteType === 'EQUITY')
    .slice(0, 8)
    .map(r => ({
      symbol: r.symbol,
      longName: r.longname || r.shortname || r.symbol,
      shortName: r.shortname || r.longname || r.symbol,
      sector: r.sector || '',
      industry: r.industry || '',
      exchange: r.exchDisp || r.exchange || '',
    }))
  return wrap(results)
}
