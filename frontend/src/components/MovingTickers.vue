<script setup>
import { ref, onMounted } from 'vue'
import * as marketApi from '../api/market'

const tickers = ref([])

onMounted(async () => {
  try {
    const r = await marketApi.getRandomQuotes()
    if (r.data.data) {
      tickers.value = r.data.data.map(q => ({
        symbol: q.symbol,
        price: q.regularMarketPrice,
        change: q.regularMarketChangePercent,
      }))
    }
  } catch { /* noop */ }
})
</script>

<template>
  <div v-if="tickers.length > 0" class="ticker-wrapper">
    <div class="ticker-track">
      <div class="ticker-item" v-for="t in tickers" :key="t.symbol">
        <span class="ticker-symbol">{{ t.symbol }}</span>
        <span class="ticker-price">${{ t.price.toFixed(2) }}</span>
        <span :class="t.change >= 0 ? 'ticker-pos' : 'ticker-neg'">
          {{ t.change >= 0 ? '+' : '' }}{{ t.change.toFixed(2) }}%
        </span>
      </div>
      <!-- duplicate for seamless loop -->
      <div class="ticker-item" v-for="t in tickers" :key="t.symbol + '-2'">
        <span class="ticker-symbol">{{ t.symbol }}</span>
        <span class="ticker-price">${{ t.price.toFixed(2) }}</span>
        <span :class="t.change >= 0 ? 'ticker-pos' : 'ticker-neg'">
          {{ t.change >= 0 ? '+' : '' }}{{ t.change.toFixed(2) }}%
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ticker-wrapper {
  background: #18181b;
  border-bottom: 1px solid #27272a;
  overflow: hidden;
  height: 36px;
}

.ticker-track {
  display: flex;
  animation: scroll 40s linear infinite;
  height: 100%;
  align-items: center;
}

@keyframes scroll {
  0%   { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.ticker-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  white-space: nowrap;
  border-right: 1px solid #27272a;
  height: 100%;
}

.ticker-symbol {
  font-weight: 700;
  font-size: 12px;
  color: #f4f4f5;
  letter-spacing: 0.03em;
}

.ticker-price {
  font-size: 12px;
  font-weight: 500;
  color: #a1a1aa;
}

.ticker-pos { font-size: 12px; font-weight: 600; color: #22c55e; }
.ticker-neg { font-size: 12px; font-weight: 600; color: #ef4444; }
</style>
