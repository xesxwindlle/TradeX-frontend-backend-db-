<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import * as holdingsApi from '../api/holdings'
import * as marketApi from '../api/market'
import * as accountApi from '../api/account'
import { fmt } from '../components/ui'
import { usePriceStore } from '../stores/price'

const priceStore = usePriceStore()

const COLORS = ['#007bff', '#10b981', '#ef4444', '#f59e0b', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#06b6d4', '#a855f7']

const holdings = ref([])
const pnl = ref(null)
const account = ref(null)
const loading = ref(true)

const totalPortfolio = computed(() => (account.value?.cashBalance ?? 0) + totalHoldingsValue.value)
const totalHoldingsValue = computed(() => holdings.value.reduce((s, h) => s + h.totalValue, 0))
const pnlPos = computed(() => (pnl.value?.totalProfitLoss ?? 0) >= 0)

onMounted(async () => {
  try {
    const [hr, ar] = await Promise.all([holdingsApi.getHoldings(), accountApi.getAccount()])
    if (ar.data.data) account.value = ar.data.data

    if (hr.data.data) {
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
        return {
          ...h,
          currentPrice,
          totalValue: currentPrice * h.quantity,
          unrealizedPnl: (currentPrice - h.unitPrice) * h.quantity,
        }
      })

      // Compute aggregate P&L from enriched holdings
      const totalCostBasis = holdings.value.reduce((s, h) => s + h.unitPrice * h.quantity, 0)
      const totalMarketValue = holdings.value.reduce((s, h) => s + h.totalValue, 0)
      const totalProfitLoss = totalMarketValue - totalCostBasis
      pnl.value = {
        totalProfitLoss,
        totalCostBasis,
        totalMarketValue,
        percentage: totalCostBasis > 0 ? (totalProfitLoss / totalCostBasis) * 100 : 0,
      }
    }
  } finally { loading.value = false }
})

// Live price updates from WebSocket
watch(() => priceStore.prices, (prices) => {
  if (!holdings.value.length) return
  holdings.value = holdings.value.map(h => {
    const live = prices[h.symbol]
    if (!live) return h
    const currentPrice = live.effectivePrice
    return { ...h, currentPrice, totalValue: currentPrice * h.quantity, unrealizedPnl: (currentPrice - h.unitPrice) * h.quantity }
  })
  const totalCostBasis = holdings.value.reduce((s, h) => s + h.unitPrice * h.quantity, 0)
  const totalMarketValue = holdings.value.reduce((s, h) => s + h.totalValue, 0)
  const totalProfitLoss = totalMarketValue - totalCostBasis
  pnl.value = { totalProfitLoss, totalCostBasis, totalMarketValue, percentage: totalCostBasis > 0 ? (totalProfitLoss / totalCostBasis) * 100 : 0 }
}, { deep: true })

// Simple SVG donut chart
const donutPaths = computed(() => {
  if (!holdings.value.length) return []
  const total = holdings.value.reduce((s, h) => s + h.totalValue, 0)
  let cumulative = 0
  const cx = 90; const cy = 90; const r = 70; const innerR = 45
  return holdings.value.map((h, i) => {
    const fraction = h.totalValue / total
    const startAngle = cumulative * 2 * Math.PI - Math.PI / 2
    const endAngle = (cumulative + fraction) * 2 * Math.PI - Math.PI / 2
    cumulative += fraction
    const x1 = cx + r * Math.cos(startAngle)
    const y1 = cy + r * Math.sin(startAngle)
    const x2 = cx + r * Math.cos(endAngle)
    const y2 = cy + r * Math.sin(endAngle)
    const ix1 = cx + innerR * Math.cos(endAngle)
    const iy1 = cy + innerR * Math.sin(endAngle)
    const ix2 = cx + innerR * Math.cos(startAngle)
    const iy2 = cy + innerR * Math.sin(startAngle)
    const largeArc = fraction > 0.5 ? 1 : 0
    const d = `M ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} L ${ix1} ${iy1} A ${innerR} ${innerR} 0 ${largeArc} 0 ${ix2} ${iy2} Z`
    return { d, fill: COLORS[i % COLORS.length], symbol: h.symbol }
  })
})
</script>

<template>
  <div v-if="loading" style="display: flex; align-items: center; justify-content: center; height: 100%;">
    <div style="width: 32px; height: 32px; border-radius: 50%; border: 2px solid #1f2937; border-top-color: #007bff; animation: spin 0.7s linear infinite;"></div>
  </div>

  <div v-else class="landing-container page-enter">
    <div class="welcome-section">
      <h1 class="welcome-title">Portfolio</h1>
      <p class="welcome-subtitle">Your holdings and performance</p>
    </div>

    <!-- Account stats -->
    <div class="stats-grid" style="margin-bottom: 32px;">
      <div class="stat-card">
        <div class="stat-label">Cash Balance</div>
        <div class="stat-value">{{ fmt(account?.cashBalance) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Market Value</div>
        <div class="stat-value">{{ fmt(totalHoldingsValue) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Total Portfolio</div>
        <div class="stat-value">{{ fmt(totalPortfolio) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">P&amp;L</div>
        <div class="stat-value" :style="{ color: pnlPos ? '#10b981' : '#ef4444' }">
          {{ pnlPos ? '+' : '' }}{{ fmt(pnl?.totalProfitLoss) }}
        </div>
        <div v-if="pnl" class="stat-change" :style="{ color: pnlPos ? '#10b981' : '#ef4444' }">
          {{ pnlPos ? '+' : '' }}{{ pnl.percentage.toFixed(2) }}%
        </div>
      </div>
    </div>


    <div v-if="holdings.length === 0" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 64px 0; gap: 12px;">
      <div style="font-size: 14px; font-weight: 500; color: #9ca3af;">No holdings yet</div>
      <div style="font-size: 12px; color: #9ca3af;">Start trading to build your portfolio</div>
    </div>

    <div v-else style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px;">
      <!-- Holdings table -->
      <div class="page-section chart-card" style="margin-bottom: 0;">
        <div class="section-header">
          <span style="font-size: 14px; font-weight: 600; color: #e1e4e8;">Holdings ({{ holdings.length }})</span>
        </div>
        <div style="overflow-x: auto;">
          <table class="data-table">
            <thead>
              <tr>
                <th>Symbol</th>
                <th style="text-align: right;">Qty</th>
                <th style="text-align: right;">Unit Cost</th>
                <th style="text-align: right;">Current</th>
                <th style="text-align: right;">Total</th>
                <th style="text-align: right;">P&amp;L</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="h in holdings" :key="h.symbol">
                <td style="font-weight: 600; color: #e1e4e8;">{{ h.symbol }}</td>
                <td class="tabular" style="text-align: right; color: #9ca3af;">{{ h.quantity }}</td>
                <td class="tabular" style="text-align: right; color: #9ca3af;">{{ fmt(h.unitPrice) }}</td>
                <td class="tabular" style="text-align: right; color: #e1e4e8;">{{ fmt(h.currentPrice) }}</td>
                <td class="tabular" style="text-align: right; font-weight: 500; color: #e1e4e8;">{{ fmt(h.totalValue) }}</td>
                <td style="text-align: right;">
                  <div class="tabular" :style="{ fontWeight: '600', color: h.unrealizedPnl >= 0 ? '#10b981' : '#ef4444' }">
                    {{ h.unrealizedPnl >= 0 ? '+' : '' }}{{ fmt(h.unrealizedPnl) }}
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Allocation donut -->
      <div class="chart-card" style="align-self: start;">
        <div class="card-header">
          <div class="card-title" style="font-size: 14px;">Allocation</div>
        </div>
        <svg v-if="donutPaths.length" viewBox="0 0 180 180" style="width: 100%; max-height: 200px;">
          <path v-for="p in donutPaths" :key="p.symbol" :d="p.d" :fill="p.fill" />
        </svg>
        <div style="margin-top: 12px; display: flex; flex-direction: column; gap: 6px;">
          <div v-for="(h, i) in holdings" :key="h.symbol" style="display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <div style="width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0;" :style="{ background: COLORS[i % COLORS.length] }" />
              <span style="font-size: 12px; color: #9ca3af;">{{ h.symbol }}</span>
            </div>
            <span class="tabular" style="font-size: 11px; color: #9ca3af;">
              {{ totalHoldingsValue > 0 ? (h.totalValue / totalHoldingsValue * 100).toFixed(1) : '0' }}%
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
