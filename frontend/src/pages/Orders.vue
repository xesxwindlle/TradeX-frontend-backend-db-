<script setup>
import { ref, computed, onMounted } from 'vue'
import { ClipboardList } from 'lucide-vue-next'
import * as ordersApi from '../api/orders'
import { fmt } from '../components/ui'

const orders = ref([])
const loading = ref(true)
const filter = ref('All')

onMounted(() => {
  ordersApi.getOrders()
    .then(r => {
      if (r.data.data) {
        orders.value = r.data.data.slice().sort((a, b) => new Date(b.createdTime).getTime() - new Date(a.createdTime).getTime())
      }
    })
    .finally(() => { loading.value = false })
})

const filters = ['All', 'Open', 'Filled', 'Cancelled']

const filtered = computed(() => filter.value === 'All' ? orders.value : orders.value.filter(o => o.status === filter.value))

const counts = computed(() => ({
  All: orders.value.length,
  Open: orders.value.filter(o => o.status === 'Open').length,
  Filled: orders.value.filter(o => o.status === 'Filled').length,
  Cancelled: orders.value.filter(o => o.status === 'Cancelled').length,
}))

function badgeVariant(variant) {
  const map = {
    positive: 'bg-positive-dim text-positive',
    negative: 'bg-negative-dim text-negative',
    neutral: 'bg-border text-secondary',
  }
  return 'inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-semibold uppercase tracking-wider ' + (map[variant] || map.neutral)
}
</script>

<template>
  <div class="landing-container page-enter">
    <div class="welcome-section">
      <h1 class="welcome-title">Orders</h1>
      <p class="welcome-subtitle">Your complete order history</p>
    </div>

    <!-- Filter tabs -->
    <div style="display: flex; gap: 4px; margin-bottom: 20px;">
      <button v-for="f in filters" :key="f"
        @click="filter = f"
        :style="filter === f
          ? 'display:flex;align-items:center;gap:6px;padding:6px 12px;border-radius:8px;font-size:12px;font-weight:500;background:rgba(139,92,246,0.12);color:#a78bfa;border:none;cursor:pointer;'
          : 'display:flex;align-items:center;gap:6px;padding:6px 12px;border-radius:8px;font-size:12px;font-weight:500;background:none;color:#9ca3af;border:none;cursor:pointer;'">
        {{ f }}
        <span :style="filter === f
          ? 'font-size:10px;padding:1px 6px;border-radius:9999px;font-weight:700;background:rgba(139,92,246,0.2);color:#a78bfa;'
          : 'font-size:10px;padding:1px 6px;border-radius:9999px;font-weight:700;background:#1f2937;color:#9ca3af;'">
          {{ counts[f] }}
        </span>
      </button>
    </div>

    <!-- Table -->
    <div class="page-section chart-card">
      <div v-if="loading" style="display: flex; align-items: center; justify-content: center; padding: 80px 0;">
        <div style="width: 24px; height: 24px; border-radius: 50%; border: 2px solid #1f2937; border-top-color: #007bff; animation: spin 0.7s linear infinite;"></div>
      </div>
      <div v-else-if="filtered.length === 0" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 64px 0; gap: 12px;">
        <div style="color: #9ca3af;"><ClipboardList :size="36" /></div>
        <div style="font-size: 14px; font-weight: 500; color: #9ca3af;">No orders found</div>
        <div v-if="filter !== 'All'" style="font-size: 12px; color: #9ca3af;">No {{ filter.toLowerCase() }} orders</div>
        <div v-else style="font-size: 12px; color: #9ca3af;">Place your first order in the Trade page</div>
      </div>
      <div v-else style="overflow-x: auto;">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Symbol</th>
              <th>Action</th>
              <th style="text-align: right;">Quantity</th>
              <th style="text-align: right;">Unit Price</th>
              <th style="text-align: right;">Total Value</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in filtered" :key="o.id">
              <td class="tabular" style="color: #9ca3af;">#{{ o.id }}</td>
              <td style="font-weight: 600; color: #e1e4e8;">{{ o.symbol }}</td>
              <td>
                <span class="badge" :class="o.action === 'Buy' ? 'badge-positive' : 'badge-negative'">{{ o.action }}</span>
              </td>
              <td class="tabular" style="text-align: right; color: #9ca3af;">{{ o.quantity }}</td>
              <td class="tabular" style="text-align: right; color: #e1e4e8;">{{ fmt(o.unitPrice) }}</td>
              <td class="tabular" style="text-align: right; font-weight: 600; color: #e1e4e8;">{{ fmt(o.unitPrice * o.quantity) }}</td>
              <td>
                <span class="badge" :class="o.status === 'Filled' ? 'badge-positive' : o.status === 'Cancelled' ? 'badge-negative' : 'badge-neutral'">{{ o.status }}</span>
              </td>
              <td style="color: #9ca3af; white-space: nowrap; font-size: 12px;">
                {{ new Date(o.createdTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) }}
                <div style="font-size: 10px; color: #9ca3af; opacity: 0.7;">{{ new Date(o.createdTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }) }}</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
