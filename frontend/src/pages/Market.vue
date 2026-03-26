<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TrendingUp, TrendingDown, Activity } from 'lucide-vue-next'
import * as marketApi from '../api/market'
import { fmt, fmtVol } from '../components/ui'
import AmIndicesChart from '../components/charts/AmIndicesChart.vue'

const router = useRouter()

const INDEX_SYMBOLS = [
  { symbol: '^GSPC', label: 'S&P 500' },
  { symbol: '^DJI',  label: 'Dow Jones' },
  { symbol: '^IXIC', label: 'NASDAQ' },
  { symbol: '^RUT',  label: 'Russell 2000' },
]
const indices = ref([])
const indicesLoading = ref(true)

const activeTab = ref('gainers')
const gainers = ref([])
const losers = ref([])
const mostActive = ref([])
const loading = ref(true)

const tabs = [
  { key: 'gainers',  label: 'Top Gainers',  icon: TrendingUp  },
  { key: 'losers',   label: 'Top Losers',   icon: TrendingDown },
  { key: 'active',   label: 'Most Active',  icon: Activity    },
]

const currentList = () => {
  if (activeTab.value === 'gainers') return gainers.value
  if (activeTab.value === 'losers')  return losers.value
  return mostActive.value
}

onMounted(async () => {
  // Fetch indices
  const results = await Promise.allSettled(INDEX_SYMBOLS.map(i => marketApi.getQuote(i.symbol)))
  indices.value = INDEX_SYMBOLS.map((idx, i) => {
    const r = results[i]
    if (r.status === 'fulfilled' && r.value.data.data) {
      const d = r.value.data.data
      return { label: idx.label, price: d.regularMarketPrice, change: d.regularMarketChange, changePercent: d.regularMarketChangePercent }
    }
    return { label: idx.label, price: 0, change: 0, changePercent: 0 }
  })
  indicesLoading.value = false

  // Fetch movers
  try {
    const [g, l, a] = await Promise.all([
      marketApi.getWinners(),
      marketApi.getLosers(),
      marketApi.getMostActive(),
    ])
    if (g.data.data) gainers.value = g.data.data
    if (l.data.data) losers.value = l.data.data
    if (a.data.data) mostActive.value = a.data.data.slice().sort((a, b) => b.regularMarketVolume - a.regularMarketVolume)
  } finally {
    loading.value = false
  }
})

function goToTrade(symbol) {
  router.push(`/trade?symbol=${encodeURIComponent(symbol)}`)
}
</script>

<template>
  <div class="landing-container page-enter">

    <!-- Header -->
    <div class="welcome-section">
      <div class="welcome-title">Market Overview</div>
      <div class="welcome-subtitle">Track market indices and today's top movers</div>
    </div>

    <!-- Index cards -->
    <div class="stats-grid" style="margin-bottom: 24px;">
      <template v-if="indicesLoading">
        <div v-for="idx in INDEX_SYMBOLS" :key="idx.symbol" class="stat-card">
          <div class="stat-label">{{ idx.label }}</div>
          <div class="skel" style="width: 90px; height: 22px; margin: 6px 0;"></div>
          <div class="skel" style="width: 110px; height: 14px;"></div>
        </div>
      </template>
      <template v-else>
        <div v-for="idx in indices" :key="idx.label" class="stat-card">
          <div class="stat-label">{{ idx.label }}</div>
          <div class="stat-value">{{ fmt(idx.price) }}</div>
          <div class="stat-change" :style="{ color: idx.change >= 0 ? '#10b981' : '#ef4444' }">
            {{ idx.change >= 0 ? '+' : '' }}{{ fmt(idx.change) }}
            ({{ idx.changePercent >= 0 ? '+' : '' }}{{ idx.changePercent.toFixed(2) }}%)
          </div>
        </div>
      </template>
    </div>

    <!-- Indices moving chart -->
    <div class="chart-card" style="margin-bottom: 32px;">
      <div class="card-header">
        <div>
          <div class="card-title">Market Indices</div>
          <div class="card-subtitle">Historical performance</div>
        </div>
      </div>
      <AmIndicesChart />
    </div>

    <!-- Movers section -->
    <div class="chart-card">
      <!-- Tabs -->
      <div style="display: flex; gap: 0; border-bottom: 1px solid #1f2937; margin-bottom: 24px;">
        <button
          v-for="t in tabs" :key="t.key"
          @click="activeTab = t.key"
          style="display: flex; align-items: center; gap: 6px; padding: 12px 20px; background: none; border: none; cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.15s;"
          :style="activeTab === t.key
            ? 'color:#a78bfa; border-bottom: 2px solid #8b5cf6; margin-bottom: -1px;'
            : 'color:#9ca3af; border-bottom: 2px solid transparent; margin-bottom: -1px;'"
        >
          <component :is="t.icon" :size="13" />
          {{ t.label }}
        </button>
      </div>

      <!-- Card grid -->
      <div class="movers-grid">
        <!-- Skeleton cards while loading -->
        <div v-if="loading" v-for="n in 12" :key="'sk-' + n" class="mover-card skeleton-card">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
            <div>
              <div class="skel" style="width: 48px; height: 14px; margin-bottom: 6px;"></div>
              <div class="skel" style="width: 90px; height: 10px;"></div>
            </div>
            <div class="skel" style="width: 44px; height: 18px; border-radius: 6px;"></div>
          </div>
          <div class="skel" style="width: 72px; height: 18px; margin-bottom: 6px;"></div>
          <div class="skel" style="width: 56px; height: 10px;"></div>
        </div>

        <!-- Actual cards -->
        <div
          v-else
          v-for="stock in currentList()" :key="stock.symbol"
          class="mover-card"
          @click="goToTrade(stock.symbol)"
        >
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
            <div>
              <div style="font-weight: 700; font-size: 15px; color: #e1e4e8;">{{ stock.symbol }}</div>
              <div style="font-size: 11px; color: #9ca3af; margin-top: 2px; max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ stock.name }}</div>
            </div>
            <span
              :style="stock.regularMarketChangePercent >= 0
                ? 'font-size:11px;font-weight:700;color:#10b981;background:rgba(16,185,129,0.12);padding:2px 7px;border-radius:6px;'
                : 'font-size:11px;font-weight:700;color:#ef4444;background:rgba(239,68,68,0.12);padding:2px 7px;border-radius:6px;'"
            >
              {{ stock.regularMarketChangePercent >= 0 ? '+' : '' }}{{ stock.regularMarketChangePercent.toFixed(2) }}%
            </span>
          </div>
          <div style="font-size: 18px; font-weight: 700; color: #e1e4e8; margin-bottom: 4px;">${{ stock.regularMarketPrice.toFixed(2) }}</div>
          <div style="font-size: 11px; color: #9ca3af;">
            <span v-if="activeTab === 'active'">Vol: {{ fmtVol(stock.regularMarketVolume) }}</span>
            <span v-else :style="{ color: stock.regularMarketChange >= 0 ? '#10b981' : '#ef4444' }">
              {{ stock.regularMarketChange >= 0 ? '+' : '' }}{{ fmt(stock.regularMarketChange) }}
            </span>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
@keyframes spin { to { transform: rotate(360deg); } }

@keyframes shimmer {
  0% { background-position: -200px 0; }
  100% { background-position: 200px 0; }
}

.skel {
  border-radius: 4px;
  background: linear-gradient(90deg, #27272a 25%, #3f3f46 50%, #27272a 75%);
  background-size: 400px 100%;
  animation: shimmer 1.2s infinite linear;
}

.skeleton-card { cursor: default; pointer-events: none; }

.movers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.mover-card { 
  background: #09090b;
  border: 1px solid #18181b;
  border-radius: 10px;
  padding: 14px;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s;
}

.mover-card:hover {
  background: #27272a;
  transform: translateY(-2px);
}
</style>
