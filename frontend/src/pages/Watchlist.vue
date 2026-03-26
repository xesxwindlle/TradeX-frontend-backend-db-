<script setup>
import { ref, onMounted } from 'vue'
import { Star, Trash2, AlertCircle } from 'lucide-vue-next'
import * as watchlistApi from '../api/watchlist'
import { fmt } from '../components/ui'
const items = ref([])
const loading = ref(true)
const newSymbol = ref('')
const adding = ref(false)
const addError = ref('')
const removing = ref(null)

async function load() {
  const r = await watchlistApi.getWatchlist()
  if (r.data.data) items.value = r.data.data
  loading.value = false
}

onMounted(() => { load() })

async function handleAdd(e) {
  e.preventDefault()
  if (!newSymbol.value.trim()) return
  adding.value = true
  addError.value = ''
  try {
    await watchlistApi.addToWatchlist(newSymbol.value.trim().toUpperCase())
    newSymbol.value = ''
    await load()
  } catch (err) {
    addError.value = err.response?.data?.message || 'Failed to add symbol'
  } finally { adding.value = false }
}

async function handleRemove(symbol) {
  removing.value = symbol
  try {
    await watchlistApi.removeFromWatchlist(symbol)
    items.value = items.value.filter(x => x.symbol !== symbol)
  } finally { removing.value = null }
}
</script>

<template>
  <div class="landing-container page-enter">
    <div class="welcome-section">
      <h1 class="welcome-title">Watchlist</h1>
      <p class="welcome-subtitle">Track stocks you care about</p>
    </div>

    <!-- Add form -->
    <div class="chart-card" style="margin-bottom: 20px;">
      <form @submit="handleAdd" style="display: flex; gap: 8px;">
        <input
          placeholder="Add symbol (e.g. AAPL)"
          v-model="newSymbol"
          @input="(e) => { newSymbol = e.target.value.toUpperCase(); addError = '' }"
          style="flex: 1; background: #141922; border: 1px solid #1f2937; border-radius: 8px; padding: 10px 12px; font-size: 14px; color: #e1e4e8; outline: none; text-transform: uppercase;"
        />
        <button type="submit" :disabled="adding || !newSymbol.trim()"
          style="display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:0 16px;font-size:13px;background:#007bff;color:white;border:none;cursor:pointer;min-width:80px;"
          :style="(adding || !newSymbol.trim()) ? 'opacity:0.4;cursor:not-allowed;' : ''">
          <div v-if="adding" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,0.3);border-top-color:white;animation:spin 0.7s linear infinite;"></div>
          <span v-else>Add</span>
        </button>
      </form>
      <div v-if="addError" style="display: flex; align-items: center; gap: 8px; margin-top: 8px; font-size: 12px; color: #ef4444;">
        <AlertCircle :size="12" /> {{ addError }}
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" style="display: flex; align-items: center; justify-content: center; padding: 80px 0;">
      <div style="width: 24px; height: 24px; border-radius: 50%; border: 2px solid #1f2937; border-top-color: #007bff; animation: spin 0.7s linear infinite;"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="items.length === 0" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 64px 0; gap: 12px;">
      <div style="color: #9ca3af;"><Star :size="36" /></div>
      <div style="font-size: 14px; font-weight: 500; color: #9ca3af;">Your watchlist is empty</div>
      <div style="font-size: 12px; color: #9ca3af;">Add stocks above to start tracking them</div>
    </div>

    <!-- List -->
    <template v-else>
      <div class="page-section">
        <div style="overflow-x: auto;">
          <table class="data-table">
            <thead>
              <tr>
                <th>Symbol</th>
                <th>Name</th>
                <th style="text-align: right;">Price</th>
                <th style="text-align: right;">Change</th>
                <th style="text-align: right;"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.symbol">
                <td>
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <div style="width: 28px; height: 28px; border-radius: 8px; background: rgba(0,123,255,0.12); display: flex; align-items: center; justify-content: center;">
                      <Star :size="12" style="color: #007bff;" />
                    </div>
                    <span style="font-weight: 600; font-size: 13px; color: #e1e4e8;">{{ item.symbol }}</span>
                  </div>
                </td>
                <td style="color: #9ca3af; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ item.name }}</td>
                <td class="tabular" style="text-align: right; font-size: 14px; font-weight: 600; color: #e1e4e8;">{{ fmt(item.currentPrice) }}</td>
                <td style="text-align: right;">
                  <div class="tabular" :style="{ fontSize: '13px', fontWeight: '600', color: item.changePercent >= 0 ? '#10b981' : '#ef4444' }">
                    {{ item.changePercent >= 0 ? '+' : '' }}{{ item.changePercent.toFixed(2) }}%
                  </div>
                </td>
                <td style="text-align: right;">
                  <button @click="handleRemove(item.symbol)" :disabled="removing === item.symbol"
                    style="padding: 6px; border-radius: 8px; background: none; border: none; color: #9ca3af; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; margin-left: auto;"
                    @mouseenter="$event.currentTarget.style.color='#ef4444'"
                    @mouseleave="$event.currentTarget.style.color='#9ca3af'"
                    :style="removing === item.symbol ? 'opacity:0.5;cursor:not-allowed;' : ''">
                    <div v-if="removing === item.symbol" style="width:14px;height:14px;border-radius:50%;border:2px solid #9ca3af;border-top-color:#ef4444;animation:spin 0.7s linear infinite;"></div>
                    <Trash2 v-else :size="14" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div style="margin-top: 16px; font-size: 12px; color: #9ca3af;">
        {{ items.length }} stock{{ items.length !== 1 ? 's' : '' }} watched &bull;
        {{ items.filter(i => i.changePercent >= 0).length }} up,
        {{ items.filter(i => i.changePercent < 0).length }} down
      </div>
    </template>
  </div>
</template>
