<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { useRoute, useRouter } from 'vue-router'
import { CheckCircle2, AlertCircle, Star, StarOff, Search } from 'lucide-vue-next'
import * as marketApi from '../api/market'
import * as ordersApi from '../api/orders'
import * as holdingsApi from '../api/holdings'
import * as watchlistApi from '../api/watchlist'
import * as accountApi from '../api/account'
import { getInsights } from '../api/market'
import { fmt, fmtCompact, fmtVol } from '../components/ui'
import AmStockChart from '../components/charts/AmStockChart.vue'
import { usePriceStore } from '../stores/price'

const priceStore = usePriceStore()

const route = useRoute()
const router = useRouter()

// ── State ──────────────────────────────────────────────────────────────────
const quoteLoading = ref(false)
const quote = ref(null)

const account = ref(null)
const holdings = ref([])
const watchlist = ref([])
const wlLoading = ref(false)
const activeTab = ref('Watchlist')

const action = ref('Buy')
const quantity = ref('')
const submitting = ref(false)
const success = ref('')
const error = ref('')

const recommendations = ref([])
const stockInsights = ref(null)

// ── Helpers ────────────────────────────────────────────────────────────────
async function loadHoldings() {
  const hr = await holdingsApi.getHoldings()
  if (!hr.data.data) return
  const raw = hr.data.data
  const quotes = await Promise.allSettled(raw.map(h => marketApi.getQuote(h.symbol)))
  holdings.value = raw.map((h, i) => {
    const q = quotes[i]
    const qd = q.status === 'fulfilled' && q.value.data.data ? q.value.data.data : null
    const currentPrice = qd
      ? qd.marketState === 'PRE' ? (qd.preMarketPrice || qd.regularMarketPrice)
      : (qd.marketState === 'POSTPOST' || qd.marketState === 'POST') ? (qd.postMarketPrice || qd.regularMarketPrice)
      : qd.regularMarketPrice
      : h.unitPrice
    const qData = q.status === 'fulfilled' && q.value.data.data ? q.value.data.data : null
    const name = qData ? (qData.name || '') : ''
    return { ...h, currentPrice, totalValue: currentPrice * h.quantity, unrealizedPnl: (currentPrice - h.unitPrice) * h.quantity, name }
  })
}

async function loadWatchlist() {
  const r = await watchlistApi.getWatchlist()
  if (!r.data.data) return
  const raw = r.data.data
  const quotes = await Promise.allSettled(raw.map(w => marketApi.getQuote(w.symbol)))
  watchlist.value = raw.map((w, i) => {
    const q = quotes[i]
    const qData = q.status === 'fulfilled' && q.value.data.data ? q.value.data.data : null
    return {
      ...w,
      name: qData?.name || '',
      currentPrice: qData?.regularMarketPrice ?? 0,
      changePercent: qData?.regularMarketChangePercent ?? 0,
      change: qData?.regularMarketChange ?? 0,
    }
  })
}

// Live updates from WebSocket
watch(() => priceStore.prices, (prices) => {
  holdings.value = holdings.value.map(h => {
    const live = prices[h.symbol]
    if (!live) return h
    const currentPrice = live.effectivePrice
    return { ...h, currentPrice, totalValue: currentPrice * h.quantity, unrealizedPnl: (currentPrice - h.unitPrice) * h.quantity }
  })
  watchlist.value = watchlist.value.map(w => {
    const live = prices[w.symbol]
    if (!live) return w
    return { ...w, currentPrice: live.regularMarketPrice, changePercent: live.regularMarketChangePercent }
  })
}, { deep: true })

// ── Spring Boot WebSocket (order notifications) ────────────────────────────
let stompClient = null

function connectOrderNotifications(accountNumber) {
  stompClient = new Client({
    webSocketFactory: () => new SockJS(`${window.location.origin}/TradeX/ws`),
    onConnect: () => {
      stompClient.subscribe(`/topic/order-executed/${accountNumber}`, () => {
        loadHoldings()
        accountApi.getAccount().then(r => { if (r.data.data) account.value = r.data.data })
      })
    }
  })
  stompClient.activate()
}

// ── Init ───────────────────────────────────────────────────────────────────
onMounted(async () => {
  accountApi.getAccount().then(r => {
    if (r.data.data) {
      account.value = r.data.data
      connectOrderNotifications(r.data.data.accountNumber)
    }
  })
  loadHoldings()
  loadWatchlist()
  if (route.query.symbol) await loadQuote(route.query.symbol)
})

onUnmounted(() => stompClient?.deactivate())


watch(() => route.query.symbol, (sym) => {
  if (sym) loadQuote(sym)
})

// Live price updates for current symbol
watch(() => quote.value && priceStore.prices[quote.value.symbol], (live) => {
  if (!live || !quote.value) return
  quote.value.regularMarketPrice = live.regularMarketPrice
  quote.value.regularMarketChange = live.regularMarketChange
  quote.value.regularMarketChangePercent = live.regularMarketChangePercent
  quote.value.postMarketPrice = live.postMarketPrice
  quote.value.postMarketChange = live.postMarketChange
  quote.value.postMarketChangePercent = live.postMarketChangePercent
})

// ── Quote loading ──────────────────────────────────────────────────────────
async function loadQuote(symbol) {
  if (!symbol) return
  quoteLoading.value = true
  quote.value = null
  error.value = ''
  success.value = ''
  quantity.value = ''
  recommendations.value = []
  stockInsights.value = null
  try {
    const r = await marketApi.getQuote(symbol.trim().toUpperCase())
    if (r.data.data) {
      quote.value = r.data.data
      // Fetch recommendations and insights in background
      marketApi.getRecommendations(symbol.trim().toUpperCase())
        .then(rec => { recommendations.value = (rec.data.data || []).slice(0, 6) })
        .catch(() => {})
      getInsights(symbol.trim().toUpperCase())
        .then(res => {
          console.log('[insights]', res.data.data)
          stockInsights.value = res.data.data || null
        })
        .catch((e) => { console.error('[insights error]', e) })
    } else {
      error.value = 'Symbol not found'
    }
  } catch {
    error.value = 'Failed to fetch quote for ' + symbol.trim().toUpperCase()
  } finally {
    quoteLoading.value = false
  }
}

function selectSearchResult(symbol) {
  router.replace(`/trade?symbol=${encodeURIComponent(symbol)}`)
  loadQuote(symbol)
}

// ── Watchlist ──────────────────────────────────────────────────────────────
function isWatched(symbol) {
  return watchlist.value.some(w => w.symbol === symbol)
}

async function toggleWatchlist(symbol) {
  wlLoading.value = true
  try {
    if (isWatched(symbol)) {
      await watchlistApi.removeFromWatchlist(symbol)
      watchlist.value = watchlist.value.filter(x => x.symbol !== symbol)
    } else {
      await watchlistApi.addToWatchlist(symbol)
      await loadWatchlist()
    }
  } finally { wlLoading.value = false }
}

// ── Order form ─────────────────────────────────────────────────────────────
let successTimer = null
watch(success, (v) => {
  if (v) {
    if (successTimer) clearTimeout(successTimer)
    successTimer = setTimeout(() => { success.value = '' }, 5000)
  }
})

async function handleSubmit(e) {
  e.preventDefault()
  if (!quote.value) return
  const qty = parseInt(quantity.value)
  if (isNaN(qty) || qty <= 0) { error.value = 'Enter a valid quantity'; return }
  error.value = ''
  submitting.value = true
  try {
    const res = await ordersApi.placeOrder({
      symbol: quote.value.symbol,
      unitPrice: effectivePrice.value,
      quantity: qty,
      action: action.value,
    })
    if (res.data.success) {
      success.value = `${action.value} order placed for ${qty} share${qty !== 1 ? 's' : ''} of ${quote.value.symbol} at ${fmt(effectivePrice.value)}`
      quantity.value = ''
    } else {
      error.value = res.data.message || 'Order failed'
    }
  } catch (err) {
    error.value = err?.response?.data?.message || 'Order failed'
  } finally { submitting.value = false }
}

const qty = computed(() => parseInt(quantity.value) || 0)
function deriveEffectivePrice(q) {
  if (!q) return 0
  if (q.marketState === 'PRE') return q.preMarketPrice || q.regularMarketPrice
  if (q.marketState === 'POSTPOST' || q.marketState === 'POST') return q.postMarketPrice || q.regularMarketPrice
  return q.regularMarketPrice
}

const effectivePrice = computed(() => {
  if (!quote.value) return 0
  const live = priceStore.prices[quote.value.symbol]
  if (live) return live.effectivePrice
  return deriveEffectivePrice(quote.value)
})
const estimatedTotal = computed(() => quote.value ? qty.value * effectivePrice.value : 0)
const pos = computed(() => quote.value ? quote.value.regularMarketChangePercent >= 0 : false)

function setMax() {
  if (!quote.value) return
  if (action.value === 'Buy') {
    const cash = account.value?.cashBalance ?? 0
    quantity.value = String(Math.floor(cash / effectivePrice.value))
  } else {
    quantity.value = String(currentHolding.value?.quantity ?? 0)
  }
}

const currentHolding = computed(() =>
  quote.value ? holdings.value.find(h => h.symbol === quote.value.symbol) ?? null : null
)

const holdingsSummary = computed(() => {
  const total = holdings.value.length
  if (total === 0) return ''
  const up = holdings.value.filter(h => h.unrealizedPnl >= 0).length
  const down = total - up
  return `${total} stock${total !== 1 ? 's' : ''} held • ${up} up, ${down} down`
})

const watchlistSummary = computed(() => {
  const total = watchlist.value.length
  if (total === 0) return ''
  const loaded = watchlist.value.map(w => priceStore.prices[w.symbol]).filter(Boolean)
  if (loaded.length === 0) return `${total} stock${total !== 1 ? 's' : ''} watched`
  const up = loaded.filter(q => q.regularMarketChangePercent >= 0).length
  const down = loaded.length - up
  return `${total} stock${total !== 1 ? 's' : ''} watched • ${up} up, ${down} down`
})

</script>

<template>
  <div style="display: flex; height: 100%; overflow: hidden;">

    <!-- ── Left panel: Holdings / Watchlist tab switch ──────────────────── -->
    <div style="width: 240px; flex-shrink: 0; border-right: 1px solid #1f2937; display: flex; flex-direction: column; overflow: hidden;">

      <!-- Tab switch -->
      <div style="flex-shrink: 0; border-bottom: 1px solid #1f2937;">
        <div style="display: flex;">
          <button v-for="tab in ['Watchlist', 'Holdings']" :key="tab"
            @click="activeTab = tab"
            :style="activeTab === tab
              ? 'flex:1;padding:12px;background:none;border:none;border-bottom:2px solid #8b5cf6;color:#a78bfa;font-size:14px;font-weight:700;cursor:pointer;transition:all 0.2s;'
              : 'flex:1;padding:12px;background:none;border:none;border-bottom:2px solid transparent;color:#9ca3af;font-size:14px;font-weight:600;cursor:pointer;transition:all 0.2s;'">
            {{ tab }}
          </button>
        </div>
      </div>

      <!-- Scrollable list -->
      <div style="flex: 1; overflow-y: auto; padding: 8px; display: flex; flex-direction: column; gap: 6px;">

        <!-- Watchlist -->
        <template v-if="activeTab === 'Watchlist'">
          <div v-if="watchlist.length === 0" style="padding: 24px 16px; font-size: 12px; color: #9ca3af; text-align: center;">No symbols</div>
          <button v-for="w in watchlist" :key="w.symbol"
            @click="selectSearchResult(w.symbol)"
            class="symbol-card"
            :style="[
              'width:100%;display:block;padding:12px;background:#18181b;border-radius:10px;border:1px solid;cursor:pointer;text-align:left;',
              quote?.symbol === w.symbol ? 'border-color:#8b5cf6;box-shadow:0 0 0 1px rgba(139,92,246,0.2);' : 'border-color:#27272a;'
            ].join('')">
            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2px;">
              <span style="font-size: 13px; font-weight: 700; color: #e1e4e8;">{{ w.symbol }}</span>
              <span class="tabular" style="font-size: 12px; color: #e1e4e8; flex-shrink: 0;">{{ w.currentPrice ? fmt(w.currentPrice) : '—' }}</span>
            </div>
            <div style="font-size: 11px; color: #71717a; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ w.name }}</div>
            <div style="display: flex; justify-content: flex-end; gap: 4px;">
              <template v-if="w.changePercent != null">
                <span class="tabular" :style="{ fontSize: '11px', fontWeight: '700', padding: '2px 6px', borderRadius: '4px', color: w.changePercent >= 0 ? '#10b981' : '#ef4444', background: w.changePercent >= 0 ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)' }">
                  {{ w.change >= 0 ? '+' : '' }}{{ fmt(w.change) }}
                </span>
                <span class="tabular" :style="{ fontSize: '11px', fontWeight: '700', padding: '2px 6px', borderRadius: '4px', color: w.changePercent >= 0 ? '#10b981' : '#ef4444', background: w.changePercent >= 0 ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)' }">
                  {{ w.changePercent >= 0 ? '+' : '' }}{{ w.changePercent.toFixed(2) }}%
                </span>
              </template>
            </div>
          </button>
        </template>

        <!-- Holdings -->
        <template v-else>
          <div v-if="holdings.length === 0" style="padding: 24px 16px; font-size: 12px; color: #9ca3af; text-align: center;">No holdings</div>
          <button v-for="h in holdings" :key="h.symbol"
            @click="selectSearchResult(h.symbol)"
            class="symbol-card"
            :style="[
              'width:100%;display:block;padding:12px;background:#18181b;border-radius:10px;border:1px solid;cursor:pointer;text-align:left;',
              quote?.symbol === h.symbol ? 'border-color:#8b5cf6;box-shadow:0 0 0 1px rgba(139,92,246,0.2);' : 'border-color:#27272a;'
            ].join('')">
            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 2px;">
              <span style="font-size: 13px; font-weight: 700; color: #e1e4e8;">{{ h.symbol }}</span>
              <span class="tabular" style="font-size: 12px; color: #e1e4e8;">{{ fmt(h.unitPrice) }}</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <div style="font-size: 11px; color: #71717a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 140px;">{{ h.name || '' }}</div>
              <span style="font-size: 11px; color: #71717a; flex-shrink: 0;">unit cost</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-size: 11px; color: #9ca3af;">{{ h.quantity }} shares</span>
              <div style="display: flex; gap: 4px;">
                <span class="tabular" :style="{ fontSize: '11px', fontWeight: '700', padding: '2px 6px', borderRadius: '4px', color: h.unrealizedPnl >= 0 ? '#10b981' : '#ef4444', background: h.unrealizedPnl >= 0 ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)' }">
                  {{ h.unrealizedPnl >= 0 ? '+' : '' }}{{ fmt(h.unrealizedPnl) }}
                </span>
                <span class="tabular" :style="{ fontSize: '11px', fontWeight: '700', padding: '2px 6px', borderRadius: '4px', color: h.unrealizedPnl >= 0 ? '#10b981' : '#ef4444', background: h.unrealizedPnl >= 0 ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)' }">
                  {{ h.unrealizedPnl >= 0 ? '+' : '' }}{{ h.unitPrice > 0 ? ((h.unrealizedPnl / (h.unitPrice * h.quantity)) * 100).toFixed(2) : '0.00' }}%
                </span>
              </div>
            </div>
          </button>
        </template>

      </div>

      <!-- Summary pinned at bottom -->
      <div v-if="activeTab === 'Holdings' && holdingsSummary" style="flex-shrink: 0;">
        <div style="margin: 0 16px; border-top: 1px solid #27272a;"></div>
        <div style="padding: 10px 16px; font-size: 11px; color: #71717a; line-height: 1.5;">{{ holdingsSummary }}</div>
      </div>
      <div v-else-if="activeTab === 'Watchlist' && watchlistSummary" style="flex-shrink: 0;">
        <div style="margin: 0 16px; border-top: 1px solid #27272a;"></div>
        <div style="padding: 10px 16px; font-size: 11px; color: #71717a; line-height: 1.5;">{{ watchlistSummary }}</div>
      </div>
    </div>

    <!-- ── Middle panel: Stock detail ──────────────────────────────────── -->
    <div style="flex: 1; display: flex; flex-direction: column; border-right: 1px solid #1f2937; overflow: hidden;">

      <!-- Scrollable content -->
      <div style="flex: 1; overflow-y: auto;">

      <!-- Loading -->
      <div v-if="quoteLoading" style="display: flex; align-items: center; justify-content: center; padding: 120px 0;">
        <div style="width: 24px; height: 24px; border-radius: 50%; border: 2px solid #1f2937; border-top-color: #8b5cf6; animation: spin 0.7s linear infinite;"></div>
      </div>

      <!-- Empty state -->
      <div v-else-if="!quote && !error" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120px 0; color: #9ca3af;">
        <Search :size="36" style="margin-bottom: 12px; opacity: 0.3;" />
        <p style="font-size: 13px;">Search for a stock to view details and trade</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error && !quote" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120px 0;">
        <AlertCircle :size="36" style="margin-bottom: 12px; color: #ef4444; opacity: 0.5;" />
        <p style="font-size: 13px; color: #ef4444;">{{ error }}</p>
      </div>

      <!-- Quote detail -->
      <div v-else-if="quote" style="padding: 18px 20px;">

        <!-- Header: symbol + name + watchlist | similar stocks -->
        <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 10px;">
          <!-- Left: symbol info -->
          <div>
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 2px;">
              <h2 style="font-size: 22px; font-weight: 700; color: #e1e4e8;">{{ quote.symbol }}</h2>
              <button
                @click="toggleWatchlist(quote.symbol)"
                :disabled="wlLoading"
                :style="isWatched(quote.symbol)
                  ? 'padding: 6px; border-radius: 8px; background: rgba(139,92,246,0.12); color: #8b5cf6; border: none; cursor: pointer;'
                  : 'padding: 6px; border-radius: 8px; background: none; color: #9ca3af; border: none; cursor: pointer;'">
                <Star v-if="isWatched(quote.symbol)" :size="15" fill="currentColor" />
                <StarOff v-else :size="15" />
              </button>
            </div>
            <div style="font-size: 13px; color: #9ca3af; margin-bottom: 6px;">{{ quote.name }}</div>
            <div style="display: flex; gap: 6px; flex-wrap: wrap;">
              <span v-if="quote.sector" style="font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 4px; background: rgba(139,92,246,0.1); color: #a78bfa; border: 1px solid rgba(139,92,246,0.2);">{{ quote.sector }}</span>
              <span v-if="quote.industry" style="font-size: 11px; padding: 2px 8px; border-radius: 4px; background: rgba(39,39,42,0.8); color: #9ca3af; border: 1px solid #27272a;">{{ quote.industry }}</span>
            </div>
          </div>
          <!-- Right: similar stocks -->
          <div v-if="recommendations.length > 0" style="flex-shrink: 0; text-align: right;">
            <div style="font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #6b7280; margin-bottom: 6px;">Similar</div>
            <div style="display: flex; flex-wrap: wrap; gap: 4px; justify-content: flex-end; max-width: 220px;">
              <button
                v-for="rec in recommendations"
                :key="rec.symbol"
                @click="selectSearchResult(rec.symbol)"
                style="padding: 3px 10px; border-radius: 20px; border: 1px solid #27272a; background: #18181b; color: #a78bfa; font-size: 11px; font-weight: 600; cursor: pointer; transition: all 0.15s;"
                @mouseenter="$event.currentTarget.style.background='rgba(139,92,246,0.12)'; $event.currentTarget.style.borderColor='rgba(139,92,246,0.3)'"
                @mouseleave="$event.currentTarget.style.background='#18181b'; $event.currentTarget.style.borderColor='#27272a'">
                {{ rec.symbol }}
              </button>
            </div>
          </div>
        </div>

        <!-- Price row + 52W range side by side -->
        <div style="display: flex; gap: 20px; align-items: center; margin-bottom: 14px;">
          <!-- Price -->
          <div style="flex: 0 0 auto;">
            <div class="tabular" style="font-size: 32px; font-weight: 700; color: #e1e4e8; line-height: 1;">{{ fmt(quote.regularMarketPrice) }}</div>
            <div class="tabular" :style="{ fontSize: '14px', fontWeight: '600', color: pos ? '#10b981' : '#ef4444', marginTop: '4px' }">
              {{ pos ? '+' : '' }}{{ fmt(quote.regularMarketChange) }}
              <span style="margin-left: 4px;">{{ pos ? '+' : '' }}{{ quote.regularMarketChangePercent.toFixed(2) }}%</span>
            </div>
            <div v-if="quote.marketState === 'PRE' && quote.preMarketPrice != null" class="tabular" style="font-size: 11px; color: #9ca3af; margin-top: 3px;">
              Pre: {{ fmt(quote.preMarketPrice) }}
              <span :style="{ color: (quote.preMarketChange ?? 0) >= 0 ? '#10b981' : '#ef4444' }">
                {{ (quote.preMarketChange ?? 0) >= 0 ? '+' : '' }}{{ fmt(quote.preMarketChange ?? 0) }}
                ({{ (quote.preMarketChangePercent ?? 0) >= 0 ? '+' : '' }}{{ (quote.preMarketChangePercent ?? 0).toFixed(2) }}%)
              </span>
            </div>
            <div v-else-if="(quote.marketState === 'POSTPOST' || quote.marketState === 'POST') && quote.postMarketPrice != null" class="tabular" style="font-size: 11px; color: #9ca3af; margin-top: 3px;">
              After: {{ fmt(quote.postMarketPrice) }}
              <span :style="{ color: (quote.postMarketChange ?? 0) >= 0 ? '#10b981' : '#ef4444' }">
                {{ (quote.postMarketChange ?? 0) >= 0 ? '+' : '' }}{{ fmt(quote.postMarketChange ?? 0) }}
                ({{ (quote.postMarketChangePercent ?? 0) >= 0 ? '+' : '' }}{{ (quote.postMarketChangePercent ?? 0).toFixed(2) }}%)
              </span>
            </div>
          </div>
        </div>

        <!-- Stock Chart (self-fetching, symbol-driven) -->
        <div class="chart-card" style="padding: 0; overflow: hidden; margin-bottom: 14px;">
          <AmStockChart :symbol="quote.symbol" />
        </div>

        <!-- Price Stats BELOW chart -->
        <div class="chart-card" style="margin-bottom: 14px;">
          <div style="font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 10px;">Price Stats</div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0;">
            <div v-for="stat in [
              { label: 'Open',        value: fmt(quote.regularMarketOpen) },
              { label: 'Prev Close',  value: fmt(quote.regularMarketPreviousClose) },
              { label: 'Day High',    value: fmt(quote.regularMarketDayHigh) },
              { label: 'Day Low',     value: fmt(quote.regularMarketDayLow) },
              { label: 'Bid',         value: quote.bid ? fmt(quote.bid) : '—' },
              { label: 'Ask',         value: quote.ask ? fmt(quote.ask) : '—' },
              { label: 'Volume',      value: quote.regularMarketVolume ? fmtVol(quote.regularMarketVolume) : '—' },
              { label: 'Beta',        value: quote.beta ? quote.beta.toFixed(2) : '—' },
              { label: 'Avg Vol 10D', value: quote.averageDailyVolume10d ? fmtVol(quote.averageDailyVolume10d) : '—' },
              { label: 'Avg Vol 3M',  value: quote.averageDailyVolume3m ? fmtVol(quote.averageDailyVolume3m) : '—' },
              { label: '50D Avg',     value: fmt(quote.fiftyDayAverage) },
              { label: '200D Avg',    value: fmt(quote.twoHundredDayAverage) },
            ]" :key="stat.label"
              style="padding: 5px 8px; border-bottom: 1px solid rgba(39,39,42,0.6); display: flex; justify-content: space-between; align-items: center;">
              <span style="font-size: 11px; color: #9ca3af;">{{ stat.label }}</span>
              <span class="tabular" style="font-size: 12px; color: #e1e4e8; font-weight: 500;">{{ stat.value }}</span>
            </div>
          </div>
        </div>

         <!-- 52W Range bar -->
        <div class="chart-card" style="margin-bottom: 14px;">
          <div style="font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 10px;">52-Week Range</div>
          <div style="display: flex; align-items: center; gap: 10px;">
            <span class="tabular" style="font-size: 11px; color: #ef4444; min-width: 60px;">{{ fmt(quote.fiftyTwoWeekLow) }}</span>
            <div style="flex: 1; height: 6px; background: #27272a; border-radius: 3px; position: relative; overflow: hidden;">
              <div :style="{
                position: 'absolute', left: 0, top: 0, bottom: 0,
                width: quote.fiftyTwoWeekHigh > quote.fiftyTwoWeekLow
                  ? ((quote.regularMarketPrice - quote.fiftyTwoWeekLow) / (quote.fiftyTwoWeekHigh - quote.fiftyTwoWeekLow) * 100).toFixed(1) + '%'
                  : '50%',
                background: 'linear-gradient(90deg, #ef4444, #10b981)',
                borderRadius: '3px'
              }"></div>
            </div>
            <span class="tabular" style="font-size: 11px; color: #10b981; min-width: 60px; text-align: right;">{{ fmt(quote.fiftyTwoWeekHigh) }}</span>
          </div>
        </div>

        <!-- Valuation BELOW chart -->
        <div class="chart-card" style="margin-bottom: 14px;">
          <div style="font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 10px;">Valuation</div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0;">
            <div v-for="stat in [
              { label: 'Market Cap',  value: quote.marketCap ? fmtCompact(quote.marketCap) : '—' },
              { label: 'Shares Out.', value: quote.sharesOutstanding ? fmtCompact(quote.sharesOutstanding) : '—' },
              { label: 'P/E (TTM)',   value: quote.trailingPe ? quote.trailingPe.toFixed(2) : '—' },
              { label: 'Fwd P/E',    value: quote.forwardPe ? quote.forwardPe.toFixed(2) : '—' },
              { label: 'P/S (TTM)',  value: quote.priceToSalesTrailing12Months ? quote.priceToSalesTrailing12Months.toFixed(2) : '—' },
              { label: 'P/B',        value: quote.priceToBook ? quote.priceToBook.toFixed(2) : '—' },
              { label: 'Book Value', value: quote.bookValue ? fmt(quote.bookValue) : '—' },
              { label: 'EPS (TTM)',  value: quote.trailingEps ? fmt(quote.trailingEps) : '—' },
              { label: 'EPS Fwd',   value: quote.forwardEps ? fmt(quote.forwardEps) : '—' },
              { label: 'EPS (CY)',  value: quote.epsCurrentYear ? fmt(quote.epsCurrentYear) : '—' },
              { label: 'Next Earn.',value: quote.nextEarningsDate || '—' },
              { label: 'Div Rate',  value: quote.dividendRate ? fmt(quote.dividendRate) : '—' },
              { label: 'Div Yield', value: quote.dividendYield ? (quote.dividendYield * 100).toFixed(2) + '%' : '—' },
            ]" :key="stat.label"
              style="padding: 5px 8px; border-bottom: 1px solid rgba(39,39,42,0.6); display: flex; justify-content: space-between; align-items: center;">
              <span style="font-size: 11px; color: #9ca3af;">{{ stat.label }}</span>
              <span class="tabular" style="font-size: 12px; color: #e1e4e8; font-weight: 500;">{{ stat.value }}</span>
            </div>
          </div>
        </div>


        <!-- About -->
        <div v-if="quote.companyDescription" class="chart-card">
          <div style="font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 8px;">About</div>
          <p style="font-size: 12px; color: #9ca3af; line-height: 1.65; display: -webkit-box; -webkit-line-clamp: 5; -webkit-box-orient: vertical; overflow: hidden;">{{ quote.companyDescription }}</p>
        </div>

      </div>

      </div> <!-- end scrollable content -->
    </div>

    <!-- ── Right panel: Order form ───────────────────────────────────────── -->
    <div style="width: 290px; flex-shrink: 0; display: flex; flex-direction: column; overflow: hidden;">

      <!-- Buy / Sell toggle — flush top -->
      <div v-if="quote" style="display: flex; border-bottom: 1px solid #27272a; flex-shrink: 0;">
        <button @click="action = 'Buy'" style="cursor:pointer;"
          :style="action === 'Buy'
            ? 'flex:1;padding:12px;font-size:14px;font-weight:700;background:none;border:none;border-bottom:2px solid #10b981;color:#10b981;margin-bottom:-1px;'
            : 'flex:1;padding:12px;font-size:14px;font-weight:600;background:none;border:none;border-bottom:2px solid transparent;color:#9ca3af;margin-bottom:-1px;'">
          Buy
        </button>
        <button @click="action = 'Sell'" style="cursor:pointer;"
          :style="action === 'Sell'
            ? 'flex:1;padding:12px;font-size:14px;font-weight:700;background:none;border:none;border-bottom:2px solid #ef4444;color:#ef4444;margin-bottom:-1px;'
            : 'flex:1;padding:12px;font-size:14px;font-weight:600;background:none;border:none;border-bottom:2px solid transparent;color:#9ca3af;margin-bottom:-1px;'">
          Sell
        </button>
      </div>

      <div style="flex: 1; overflow-y: auto;">
        <div style="padding: 18px;">
        <!-- <div style="font-size: 13px; font-weight: 700; color: #e1e4e8; margin-bottom: 18px;">Place Order</div> -->

        <div v-if="!quote" style="font-size: 12px; color: #9ca3af; text-align: center; padding: 40px 0;">
          Select a stock to place an order
        </div>

        <template v-else>

          <form @submit="handleSubmit" style="display: flex; flex-direction: column; gap: 14px;">
            <div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                <div style="font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #9ca3af;">Quantity (shares)</div>
                <button type="button" @click="setMax"
                  style="font-size: 10px; font-weight: 700; color: #a78bfa; background: rgba(139,92,246,0.1); border: 1px solid rgba(139,92,246,0.2); border-radius: 4px; padding: 2px 8px; cursor: pointer; transition: background 0.15s;"
                  @mouseenter="$event.currentTarget.style.background='rgba(139,92,246,0.2)'"
                  @mouseleave="$event.currentTarget.style.background='rgba(139,92,246,0.1)'">MAX</button>
              </div>
              <input type="number" min="1" placeholder="0" v-model="quantity"
                style="width: 100%; background: #18181b; border: 1px solid #27272a; border-radius: 8px; padding: 10px 12px; font-size: 14px; color: #e1e4e8; outline: none; cursor: text; box-sizing: border-box;" />
            </div>

            <!-- Info card -->
            <div style="background: #18181b; border-radius: 10px; padding: 12px; border: 1px solid #27272a;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                <span style="font-size: 11px; color: #9ca3af;">Price per share</span>
                <span class="tabular" style="font-size: 12px; color: #e1e4e8;">{{ fmt(effectivePrice) }}</span>
              </div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                <span style="font-size: 11px; color: #9ca3af;">Quantity</span>
                <span class="tabular" style="font-size: 12px; color: #e1e4e8;">{{ qty > 0 ? qty : '—' }}</span>
              </div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                <span style="font-size: 11px; color: #9ca3af;">Cash Balance</span>
                <span class="tabular" style="font-size: 12px; color: #e1e4e8;">{{ fmt(account?.cashBalance) }}</span>
              </div>
              <div style="border-top: 1px solid #27272a; padding-top: 8px; display: flex; justify-content: space-between;">
                <span style="font-size: 12px; font-weight: 600; color: #e1e4e8;">Estimated Total</span>
                <span class="tabular" style="font-size: 14px; font-weight: 700; color: #e1e4e8;">{{ qty > 0 ? fmt(estimatedTotal) : '—' }}</span>
              </div>
            </div>

          <!-- Current position -->
          <div v-if="currentHolding" style="background: rgba(139,92,246,0.06); border: 1px solid rgba(139,92,246,0.2); border-radius: 10px; padding: 14px; margin-bottom: 16px;">
            <div style="font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #8b5cf6; margin-bottom: 10px;">Your Position</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
              <div>
                <div style="font-size: 10px; color: #9ca3af; margin-bottom: 2px;">Shares</div>
                <div class="tabular" style="font-size: 14px; font-weight: 700; color: #e1e4e8;">{{ currentHolding.quantity }}</div>
              </div>
              <div>
                <div style="font-size: 10px; color: #9ca3af; margin-bottom: 2px;">Avg Cost</div>
                <div class="tabular" style="font-size: 14px; font-weight: 700; color: #e1e4e8;">{{ fmt(currentHolding.unitPrice) }}</div>
              </div>
              <div style="grid-column: span 2;">
                <div style="font-size: 10px; color: #9ca3af; margin-bottom: 2px;">Unrealized P&amp;L</div>
                <div style="display: flex; gap: 8px; align-items: baseline; flex-wrap: wrap;">
                  <div class="tabular" :style="{ fontSize: '14px', fontWeight: '700', color: currentHolding.unrealizedPnl >= 0 ? '#10b981' : '#ef4444' }">
                    {{ currentHolding.unrealizedPnl >= 0 ? '+' : '' }}{{ fmt(currentHolding.unrealizedPnl) }}
                  </div>
                  <div class="tabular" :style="{ fontSize: '12px', fontWeight: '600', color: currentHolding.unrealizedPnl >= 0 ? '#10b981' : '#ef4444' }">
                    {{ currentHolding.unrealizedPnl >= 0 ? '+' : '' }}{{ currentHolding.unitPrice > 0 ? ((currentHolding.unrealizedPnl / (currentHolding.unitPrice * currentHolding.quantity)) * 100).toFixed(2) : '0.00' }}%
                  </div>
                </div>
              </div>
            </div>
          </div>

            <!-- Feedback -->
            <div v-if="error" style="display:flex;align-items:center;gap:8px;font-size:12px;color:#ef4444;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.2);border-radius:8px;padding:8px 12px;">
              <AlertCircle :size="13" /> {{ error }}
            </div>
            <div v-if="success" style="display:flex;align-items:center;gap:8px;font-size:12px;color:#10b981;background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.2);border-radius:8px;padding:8px 12px;">
              <CheckCircle2 :size="13" /> {{ success }}
            </div>

            <button
              class="execute-btn"
              :class="action === 'Buy' ? 'execute-btn-buy' : 'execute-btn-sell'"
              type="submit"
              :disabled="submitting || !quantity || (action === 'Buy' && estimatedTotal > (account?.cashBalance ?? 0)) || (action === 'Sell' && !currentHolding) || (action === 'Sell' && qty > (currentHolding?.quantity ?? 0))"
              :style="submitting || !quantity || (action === 'Buy' && estimatedTotal > (account?.cashBalance ?? 0)) || (action === 'Sell' && !currentHolding) || (action === 'Sell' && qty > (currentHolding?.quantity ?? 0)) ? 'opacity:0.4;' : ''">
              <div v-if="submitting" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,0.3);border-top-color:white;animation:spin 0.7s linear infinite;"></div>
              <span v-else>{{ action }} {{ qty > 0 ? qty + ' share' + (qty !== 1 ? 's' : '') : '' }} of {{ quote.symbol }}</span>
            </button>
          </form>
        </template>

        <!-- ── Insights ──────────────────────────────────────────────────── -->
        <div v-if="stockInsights" style="margin-top: 20px; border-top: 1px solid #27272a; padding-top: 16px;">
          <div style="font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #9ca3af; margin-bottom: 12px;">Insights</div>

          <!-- Analyst Recommendation -->
          <div v-if="stockInsights.recommendation" style="display: flex; align-items: center; justify-content: space-between; background: #18181b; border: 1px solid #27272a; border-radius: 10px; padding: 12px; margin-bottom: 12px;">
            <div>
              <div style="font-size: 10px; color: #6b7280; margin-bottom: 4px;">{{ stockInsights.recommendation.provider }}</div>
              <div :style="{
                fontSize: '18px', fontWeight: '800', letterSpacing: '0.02em',
                color: stockInsights.recommendation.rating === 'BUY' ? '#10b981' : stockInsights.recommendation.rating === 'SELL' ? '#ef4444' : '#f59e0b'
              }">{{ stockInsights.recommendation.rating }}</div>
            </div>
            <div v-if="stockInsights.recommendation.targetPrice" style="text-align: right;">
              <div style="font-size: 10px; color: #6b7280; margin-bottom: 4px;">Target Price</div>
              <div class="tabular" style="font-size: 18px; font-weight: 800; color: #a78bfa;">{{ fmt(stockInsights.recommendation.targetPrice) }}</div>
            </div>
          </div>

          <!-- Technical Outlook -->
          <div v-if="stockInsights.instrumentInfo?.technicalEvents" style="margin-bottom: 12px;">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">
              Technical Outlook
              <span style="font-weight: 400; color: #4b5563; margin-left: 4px;">· {{ stockInsights.instrumentInfo.technicalEvents.provider }}</span>
            </div>
            <div v-for="(outlook, key) in {
              'Short Term':  stockInsights.instrumentInfo.technicalEvents.shortTermOutlook,
              'Mid Term':    stockInsights.instrumentInfo.technicalEvents.intermediateTermOutlook,
              'Long Term':   stockInsights.instrumentInfo.technicalEvents.longTermOutlook,
            }" :key="key" style="padding: 7px 0; border-bottom: 1px solid rgba(39,39,42,0.6);">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 3px;">
                <span style="font-size: 11px; color: #9ca3af;">{{ key }}</span>
                <span v-if="outlook" :style="{
                  fontSize: '10px', fontWeight: '700', padding: '2px 7px', borderRadius: '4px',
                  color: outlook.direction === 'Bullish' ? '#10b981' : outlook.direction === 'Bearish' ? '#ef4444' : '#f59e0b',
                  background: outlook.direction === 'Bullish' ? 'rgba(16,185,129,0.1)' : outlook.direction === 'Bearish' ? 'rgba(239,68,68,0.1)' : 'rgba(245,158,11,0.1)'
                }">{{ outlook.direction }}</span>
              </div>
              <div v-if="outlook?.scoreDescription" style="font-size: 10px; color: #6b7280;">{{ outlook.scoreDescription }}</div>
            </div>
          </div>

          <!-- Key Levels -->
          <div v-if="stockInsights.instrumentInfo?.keyTechnicals" style="margin-bottom: 12px;">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">Key Levels</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 6px;">
              <div v-for="stat in [
                { label: 'Support',    value: stockInsights.instrumentInfo.keyTechnicals.support,    color: '#10b981' },
                { label: 'Resistance', value: stockInsights.instrumentInfo.keyTechnicals.resistance, color: '#ef4444' },
                { label: 'Stop Loss',  value: stockInsights.instrumentInfo.keyTechnicals.stopLoss,   color: '#f59e0b' },
              ]" :key="stat.label" style="background: #18181b; border: 1px solid #27272a; border-radius: 8px; padding: 8px; text-align: center;">
                <div style="font-size: 9px; color: #6b7280; text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 4px;">{{ stat.label }}</div>
                <div class="tabular" :style="{ fontSize: '12px', fontWeight: '700', color: stat.color }">{{ stat.value ? fmt(stat.value) : '—' }}</div>
              </div>
            </div>
          </div>

          <!-- Valuation -->
          <div v-if="stockInsights.instrumentInfo?.valuation?.description" style="margin-bottom: 12px;">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">Valuation</div>
            <div style="display: flex; justify-content: space-between; align-items: center; background: #18181b; border: 1px solid #27272a; border-radius: 8px; padding: 10px 12px;">
              <span style="font-size: 12px; color: #e1e4e8;">{{ stockInsights.instrumentInfo.valuation.description }}</span>
              <span v-if="stockInsights.instrumentInfo.valuation.discount" style="font-size: 13px; font-weight: 700; color: #a78bfa;">{{ stockInsights.instrumentInfo.valuation.discount }}</span>
            </div>
          </div>

          <!-- Company Snapshot -->
          <div v-if="stockInsights.companySnapshot?.company" style="margin-bottom: 12px;">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">Company vs Sector</div>
            <div v-for="metric in [
              { label: 'Innovativeness', company: stockInsights.companySnapshot.company.innovativeness, sector: stockInsights.companySnapshot.sector?.innovativeness },
              { label: 'Hiring',         company: stockInsights.companySnapshot.company.hiring,         sector: stockInsights.companySnapshot.sector?.hiring },
              { label: 'Insider Sentiment', company: stockInsights.companySnapshot.company.insiderSentiments, sector: stockInsights.companySnapshot.sector?.insiderSentiments },
              { label: 'Earnings',       company: stockInsights.companySnapshot.company.earningsReports, sector: stockInsights.companySnapshot.sector?.earningsReports },
            ]" :key="metric.label" style="margin-bottom: 7px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 3px;">
                <span style="font-size: 10px; color: #9ca3af;">{{ metric.label }}</span>
                <span style="font-size: 10px; color: #6b7280;">{{ metric.company != null ? (metric.company * 100).toFixed(0) + '%' : '—' }}</span>
              </div>
              <div style="position: relative; height: 4px; background: #27272a; border-radius: 2px;">
                <div :style="{ position: 'absolute', left: 0, top: 0, bottom: 0, width: (metric.company * 100).toFixed(0) + '%', background: '#8b5cf6', borderRadius: '2px' }"></div>
                <div v-if="metric.sector != null" :style="{ position: 'absolute', top: '-2px', bottom: '-2px', width: '2px', left: (metric.sector * 100).toFixed(0) + '%', background: '#4b5563', borderRadius: '1px' }"></div>
              </div>
            </div>
            <div style="display: flex; gap: 12px; margin-top: 4px;">
              <div style="display: flex; align-items: center; gap: 4px;"><div style="width: 8px; height: 3px; background: #8b5cf6; border-radius: 1px;"></div><span style="font-size: 9px; color: #6b7280;">Company</span></div>
              <div style="display: flex; align-items: center; gap: 4px;"><div style="width: 2px; height: 8px; background: #4b5563; border-radius: 1px;"></div><span style="font-size: 9px; color: #6b7280;">Sector avg</span></div>
            </div>
          </div>

          <!-- Bull / Bear Summary -->
          <div v-if="stockInsights.upsell?.msBullishSummary?.length || stockInsights.upsell?.msBearishSummary?.length" style="margin-bottom: 12px;">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">Bull / Bear Case</div>
            <div v-for="(pt, i) in stockInsights.upsell.msBullishSummary?.slice(0, 2)" :key="'bull' + i"
              style="display: flex; gap: 8px; margin-bottom: 6px;">
              <span style="color: #10b981; font-size: 12px; flex-shrink: 0; margin-top: 1px;">↑</span>
              <span style="font-size: 11px; color: #9ca3af; line-height: 1.5;">{{ pt }}</span>
            </div>
            <div v-for="(pt, i) in stockInsights.upsell.msBearishSummary?.slice(0, 2)" :key="'bear' + i"
              style="display: flex; gap: 8px; margin-bottom: 6px;">
              <span style="color: #ef4444; font-size: 12px; flex-shrink: 0; margin-top: 1px;">↓</span>
              <span style="font-size: 11px; color: #9ca3af; line-height: 1.5;">{{ pt }}</span>
            </div>
          </div>

          <!-- Research Report (upsellSearchDD) -->
          <div v-if="stockInsights.upsellSearchDD?.researchReports" style="margin-bottom: 12px;">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">Research Report</div>
            <div style="background: #18181b; border: 1px solid #27272a; border-radius: 10px; padding: 12px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                <span style="font-size: 10px; color: #6b7280;">{{ stockInsights.upsellSearchDD.researchReports.provider }}</span>
                <span v-if="stockInsights.upsellSearchDD.researchReports.investmentRating" :style="{
                  fontSize: '10px', fontWeight: '700', padding: '1px 6px', borderRadius: '4px',
                  color: stockInsights.upsellSearchDD.researchReports.investmentRating === 'Bullish' ? '#10b981' : stockInsights.upsellSearchDD.researchReports.investmentRating === 'Bearish' ? '#ef4444' : '#f59e0b',
                  background: stockInsights.upsellSearchDD.researchReports.investmentRating === 'Bullish' ? 'rgba(16,185,129,0.1)' : stockInsights.upsellSearchDD.researchReports.investmentRating === 'Bearish' ? 'rgba(239,68,68,0.1)' : 'rgba(245,158,11,0.1)'
                }">{{ stockInsights.upsellSearchDD.researchReports.investmentRating }}</span>
              </div>
              <div style="font-size: 11px; font-weight: 600; color: #e1e4e8; line-height: 1.4; margin-bottom: 6px;">{{ stockInsights.upsellSearchDD.researchReports.title }}</div>
              <p style="font-size: 10px; color: #6b7280; line-height: 1.5; margin-bottom: 6px;">{{ stockInsights.upsellSearchDD.researchReports.summary }}</p>
              <span style="font-size: 10px; color: #4b5563;">{{ new Date(stockInsights.upsellSearchDD.researchReports.reportDate).toLocaleDateString() }}</span>
            </div>
          </div>

          <!-- Analyst Reports -->
          <div v-if="stockInsights.reports?.some(r => r.investmentRating)" style="margin-bottom: 12px;">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">Analyst Reports</div>
            <div v-for="report in stockInsights.reports.filter(r => r.investmentRating).slice(0, 2)" :key="report.id"
              style="background: #18181b; border: 1px solid #27272a; border-radius: 10px; padding: 12px; margin-bottom: 8px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                <span style="font-size: 10px; color: #6b7280;">{{ report.provider }}</span>
                <span :style="{
                  fontSize: '10px', fontWeight: '700', padding: '1px 6px', borderRadius: '4px',
                  color: report.investmentRating === 'Bullish' ? '#10b981' : report.investmentRating === 'Bearish' ? '#ef4444' : '#f59e0b',
                  background: report.investmentRating === 'Bullish' ? 'rgba(16,185,129,0.1)' : report.investmentRating === 'Bearish' ? 'rgba(239,68,68,0.1)' : 'rgba(245,158,11,0.1)'
                }">{{ report.investmentRating }}</span>
              </div>
              <div style="font-size: 11px; font-weight: 600; color: #e1e4e8; line-height: 1.4; margin-bottom: 6px;">{{ report.title }}</div>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 10px; color: #4b5563;">{{ new Date(report.reportDate).toLocaleDateString() }}</span>
                <span v-if="report.targetPrice" class="tabular" style="font-size: 11px; font-weight: 700; color: #a78bfa;">Target: {{ fmt(report.targetPrice) }}</span>
              </div>
            </div>
          </div>

          <!-- Significant Developments -->
          <div v-if="stockInsights.sigDevs?.length">
            <div style="font-size: 11px; font-weight: 600; color: #71717a; margin-bottom: 8px;">Significant Developments</div>
            <div v-for="(dev, i) in stockInsights.sigDevs.slice(0, 3)" :key="i"
              style="padding: 8px 10px; background: #18181b; border: 1px solid #27272a; border-radius: 8px; margin-bottom: 6px;">
              <div style="font-size: 11px; color: #e1e4e8; line-height: 1.5;">{{ dev.headline }}</div>
              <div v-if="dev.date" style="font-size: 10px; color: #6b7280; margin-top: 4px;">{{ new Date(dev.date).toLocaleDateString() }}</div>
            </div>
          </div>

        </div>

        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
@keyframes spin { to { transform: rotate(360deg); } }

.symbol-card { transition: background 0.15s; }
.symbol-card:hover { background: #27272a !important; }


.execute-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 700;
  border-radius: 8px;
  padding: 14px;
  font-size: 14px;
  width: 100%;
  color: white;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
}
.execute-btn:disabled { cursor: not-allowed; }
.execute-btn:not(:disabled):hover { opacity: 0.85; }
.execute-btn-buy  { background: #10b981; }
.execute-btn-sell { background: #ef4444; }
</style>
