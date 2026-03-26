<script setup>
import { ref, onMounted } from 'vue'
import clsx from 'clsx'
import { Shield, Users, ClipboardList, BarChart2, TrendingUp, Plus, RefreshCw, Trash2, AlertTriangle } from 'lucide-vue-next'
import * as adminApi from '../api/admin'
import { fmt } from '../components/ui'
import AmRankingChart from '../components/charts/AmRankingChart.vue'
const tab = ref('users')

const tabs = [
  { key: 'users', label: 'Users' },
  { key: 'orders', label: 'Orders' },
  { key: 'instruments', label: 'Instruments' },
  { key: 'stats', label: 'Stats' },
]

// ── USERS ──
const users = ref([])
const usersLoading = ref(true)
const resetting = ref(null)
const deleting = ref(null)
const confirmDelete = ref(null)
const resetMsg = ref('')

async function loadUsers() {
  const r = await adminApi.getUsers()
  if (r.data.data) users.value = r.data.data
  usersLoading.value = false
}

async function handleReset(accountNumber) {
  resetting.value = accountNumber
  try {
    await adminApi.resetUserPassword(accountNumber)
    resetMsg.value = `Password reset sent for account #${accountNumber}`
    setTimeout(() => { resetMsg.value = '' }, 4000)
  } finally { resetting.value = null }
}

async function handleDelete() {
  if (!confirmDelete.value) return
  deleting.value = confirmDelete.value.accountNumber
  try {
    await adminApi.deleteUser(confirmDelete.value.accountNumber)
    users.value = users.value.filter(u => u.accountNumber !== confirmDelete.value.accountNumber)
    confirmDelete.value = null
  } finally { deleting.value = null }
}

// ── ORDERS ──
const adminOrders = ref([])
const ordersLoading = ref(false)
const fromDate = ref('')
const toDate = ref('')

async function loadOrders(f, t) {
  ordersLoading.value = true
  try {
    const r = await adminApi.getAdminOrders(f, t)
    if (r.data.data) adminOrders.value = r.data.data.slice().sort((a, b) => new Date(b.createdTime).getTime() - new Date(a.createdTime).getTime())
  } finally { ordersLoading.value = false }
}

// ── INSTRUMENTS ──
const instruments = ref([])
const instrLoading = ref(true)
const newSymbol = ref('')
const adding = ref(false)
const addMsg = ref('')
const addErr = ref('')

async function loadInstruments() {
  const r = await adminApi.getAdminInstruments(50)
  if (r.data.data) instruments.value = r.data.data
  instrLoading.value = false
}

async function handleAddSymbol(e) {
  e.preventDefault()
  if (!newSymbol.value.trim()) return
  adding.value = true; addMsg.value = ''; addErr.value = ''
  try {
    const r = await adminApi.addSymbol(newSymbol.value.trim().toUpperCase())
    if (r.data.success) {
      addMsg.value = `Symbol ${newSymbol.value.toUpperCase()} added`
      newSymbol.value = ''
      await loadInstruments()
    } else { addErr.value = r.data.message || 'Failed' }
  } catch (err) {
    addErr.value = err.response?.data?.message || 'Failed'
  } finally { adding.value = false }
}

// ── STATS ──
const traded = ref([])
const held = ref([])
const watched = ref([])
const statsLoading = ref(true)

const STAT_COLORS = ['#8b5cf6', '#7c3aed', '#6d28d9', '#5b21b6', '#4c1d95', '#6366f1', '#4f46e5', '#4338ca', '#3730a3', '#312e81']

async function loadStats() {
  try {
    const [tr, he, wa] = await Promise.all([
      adminApi.getMostTraded(10),
      adminApi.getMostHeld(10),
      adminApi.getMostWatched(10),
    ])
    if (tr.data.data) traded.value = tr.data.data
    if (he.data.data) held.value = he.data.data
    if (wa.data.data) watched.value = wa.data.data
  } finally { statsLoading.value = false }
}

onMounted(async () => {
  await loadUsers()
  loadOrders()
  loadInstruments()
  loadStats()
})

function badgeVariant(variant) {
  const map = {
    positive: 'bg-positive-dim text-positive',
    negative: 'bg-negative-dim text-negative',
    neutral: 'bg-border text-secondary',
    accent: 'bg-accent-dim text-accent',
  }
  return 'inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-semibold uppercase tracking-wider ' + (map[variant] || map.neutral)
}

const statDatasets = [
  { key: 'traded', label: 'Most Traded' },
  { key: 'held', label: 'Most Held' },
  { key: 'watched', label: 'Most Watched' },
]

function getStatData(key) {
  if (key === 'traded') return traded.value
  if (key === 'held') return held.value
  return watched.value
}
</script>

<template>
  <div class="p-6 max-w-6xl mx-auto page-enter">
    <div class="mb-6 flex items-center gap-3">
      <div class="w-9 h-9 rounded-xl bg-accent-dim flex items-center justify-center">
        <Shield :size="16" class="text-accent" />
      </div>
      <div>
        <h1 class="text-[22px] font-bold text-primary">Admin Panel</h1>
        <p class="text-[13px] text-secondary">Platform management and analytics</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 mb-6 border-b border-border pb-2">
      <button v-for="t in tabs" :key="t.key"
        @click="tab = t.key"
        :class="clsx('flex items-center gap-1.5 px-4 py-2 rounded-lg text-[13px] font-medium transition-all', tab === t.key ? 'bg-accent-dim text-accent' : 'text-secondary hover:text-primary hover:bg-border')">
        <Users v-if="t.key === 'users'" :size="13" />
        <ClipboardList v-else-if="t.key === 'orders'" :size="13" />
        <BarChart2 v-else-if="t.key === 'instruments'" :size="13" />
        <TrendingUp v-else :size="13" />
        {{ t.label }}
      </button>
    </div>

    <!-- USERS TAB -->
    <div v-if="tab === 'users'">
      <div v-if="resetMsg" class="mb-4 flex items-center gap-2 text-[13px] text-positive bg-positive-dim border border-positive/20 rounded-lg px-3 py-2">
        {{ resetMsg }}
      </div>
      <div v-if="usersLoading" class="flex justify-center py-20">
        <div class="w-6 h-6 rounded-full border-2 border-border border-t-accent animate-spin"></div>
      </div>
      <div v-else class="rounded-xl border border-border bg-card overflow-hidden">
        <div class="px-5 py-4 border-b border-border">
          <span class="text-[14px] font-semibold text-primary">{{ users.length }} Users</span>
        </div>
        <div class="overflow-x-auto">
          <table>
            <thead class="border-b border-border bg-surface">
              <tr>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">#</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Email</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Name</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Role</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Status</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted text-right">Balance</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted text-right">Orders</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.accountNumber" class="border-b border-border/50 hover:bg-surface/50 transition-colors">
                <td class="px-5 py-3 text-[12px] tabular text-muted">{{ u.accountNumber }}</td>
                <td class="px-5 py-3 text-[13px] text-primary max-w-[180px] truncate">{{ u.email }}</td>
                <td class="px-5 py-3 text-[13px] text-secondary whitespace-nowrap">{{ [u.firstName, u.lastName].filter(Boolean).join(' ') }}</td>
                <td class="px-5 py-3">
                  <span :class="badgeVariant(u.role === 'Admin' ? 'accent' : 'neutral')">{{ u.role }}</span>
                </td>
                <td class="px-5 py-3">
                  <span :class="badgeVariant(u.status === 'Active' ? 'positive' : u.status === 'Closed' ? 'negative' : 'neutral')">{{ u.status }}</span>
                </td>
                <td class="px-5 py-3 text-right text-[13px] tabular text-primary">{{ fmt(u.cashBalance) }}</td>
                <td class="px-5 py-3 text-right text-[13px] tabular text-secondary">{{ u.orderCount }}</td>
                <td class="px-5 py-3">
                  <div class="flex items-center gap-1">
                    <button @click="handleReset(u.accountNumber)" :disabled="resetting === u.accountNumber"
                      class="p-1.5 rounded-lg text-muted hover:text-accent hover:bg-accent-dim transition-colors" title="Reset Password">
                      <div v-if="resetting === u.accountNumber" class="w-3 h-3 rounded-full border-2 border-muted border-t-accent animate-spin"></div>
                      <RefreshCw v-else :size="13" />
                    </button>
                    <button @click="confirmDelete = u" class="p-1.5 rounded-lg text-muted hover:text-negative hover:bg-negative-dim transition-colors" title="Delete User">
                      <Trash2 :size="13" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Delete User Modal -->
      <div v-if="confirmDelete" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="confirmDelete = null" />
        <div class="relative z-10 w-full max-w-md mx-4 bg-card border border-border rounded-2xl shadow-2xl">
          <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="text-[15px] font-semibold text-primary">Delete User</h3>
            <button @click="confirmDelete = null" class="text-muted hover:text-primary">✕</button>
          </div>
          <div class="p-6 flex flex-col gap-4">
            <div class="flex items-center gap-3 p-3 bg-negative-dim rounded-lg border border-negative/20">
              <AlertTriangle :size="16" class="text-negative shrink-0" />
              <p class="text-[13px] text-secondary">
                Delete account <strong class="text-primary">#{{ confirmDelete.accountNumber }}</strong> ({{ confirmDelete.email }})? This cannot be undone.
              </p>
            </div>
            <div class="flex gap-2">
              <button @click="confirmDelete = null"
                class="flex-1 inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all px-4 py-2.5 text-[13px] bg-transparent border border-border text-secondary hover:text-primary">
                Cancel
              </button>
              <button @click="handleDelete" :disabled="!!deleting"
                class="flex-1 inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2.5 text-[13px] bg-negative/10 border border-negative/30 text-negative hover:bg-negative/20">
                <div v-if="deleting" class="w-3.5 h-3.5 rounded-full border-2 border-negative/30 border-t-negative animate-spin"></div>
                <span v-else>Delete</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ORDERS TAB -->
    <div v-else-if="tab === 'orders'">
      <div class="flex gap-2 mb-4">
        <input type="date" v-model="fromDate" class="max-w-[160px] bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
        <input type="date" v-model="toDate" class="max-w-[160px] bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
        <button @click="loadOrders(fromDate || undefined, toDate || undefined)"
          class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all px-4 py-2.5 text-[13px] bg-transparent border border-border text-secondary hover:text-primary hover:border-zinc-500">
          Filter
        </button>
        <button @click="fromDate = ''; toDate = ''; loadOrders()"
          class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all px-4 py-2.5 text-[13px] bg-transparent border border-border text-secondary hover:text-primary hover:border-zinc-500">
          Clear
        </button>
      </div>

      <div class="rounded-xl border border-border bg-card overflow-hidden">
        <div class="px-5 py-4 border-b border-border">
          <span class="text-[14px] font-semibold text-primary">{{ adminOrders.length }} Orders</span>
        </div>
        <div v-if="ordersLoading" class="flex justify-center py-20">
          <div class="w-6 h-6 rounded-full border-2 border-border border-t-accent animate-spin"></div>
        </div>
        <div v-else-if="adminOrders.length === 0" class="flex flex-col items-center justify-center py-16 gap-3">
          <ClipboardList :size="32" class="text-muted" />
          <span class="text-[14px] text-secondary">No orders found</span>
        </div>
        <div v-else class="overflow-x-auto">
          <table>
            <thead class="border-b border-border bg-surface">
              <tr>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">ID</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Symbol</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Action</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted text-right">Qty</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted text-right">Price</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Status</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Date</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="o in adminOrders" :key="o.id" class="border-b border-border/50 hover:bg-surface/50 transition-colors">
                <td class="px-5 py-3 text-[12px] tabular text-muted">#{{ o.id }}</td>
                <td class="px-5 py-3 text-[13px] font-semibold text-primary">{{ o.symbol }}</td>
                <td class="px-5 py-3"><span :class="badgeVariant(o.action === 'Buy' ? 'positive' : 'negative')">{{ o.action }}</span></td>
                <td class="px-5 py-3 text-right text-[13px] tabular text-secondary">{{ o.quantity }}</td>
                <td class="px-5 py-3 text-right text-[13px] tabular text-primary">{{ fmt(o.unitPrice) }}</td>
                <td class="px-5 py-3"><span :class="badgeVariant(o.status === 'Filled' ? 'positive' : o.status === 'Cancelled' ? 'negative' : 'neutral')">{{ o.status }}</span></td>
                <td class="px-5 py-3 text-[12px] text-muted whitespace-nowrap">
                  {{ new Date(o.createdTime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- INSTRUMENTS TAB -->
    <div v-else-if="tab === 'instruments'">
      <div class="rounded-xl border border-border bg-card p-4 mb-4">
        <form @submit="handleAddSymbol" class="flex gap-2">
          <div class="relative flex-1">
            <Plus :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted" />
            <input placeholder="Add new symbol (e.g. NVDA)" v-model="newSymbol"
              @input="(e) => { newSymbol = e.target.value.toUpperCase(); addMsg = ''; addErr = '' }"
              class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all pl-9 uppercase" />
          </div>
          <button type="submit" :disabled="adding || !newSymbol.trim()"
            class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2.5 text-[13px] bg-accent text-white hover:bg-accent-hover">
            <div v-if="adding" class="w-3.5 h-3.5 rounded-full border-2 border-white/30 border-t-white animate-spin"></div>
            <span v-else>Add Symbol</span>
          </button>
        </form>
        <div v-if="addMsg" class="mt-2 text-[12px] text-positive">{{ addMsg }}</div>
        <div v-if="addErr" class="mt-2 text-[12px] text-negative">{{ addErr }}</div>
      </div>

      <div class="rounded-xl border border-border bg-card overflow-hidden">
        <div class="px-5 py-4 border-b border-border">
          <span class="text-[14px] font-semibold text-primary">{{ instruments.length }} Instruments</span>
        </div>
        <div v-if="instrLoading" class="flex justify-center py-20">
          <div class="w-6 h-6 rounded-full border-2 border-border border-t-accent animate-spin"></div>
        </div>
        <div v-else class="overflow-x-auto">
          <table>
            <thead class="border-b border-border bg-surface">
              <tr>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Symbol</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Name</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Sector</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted">Exchange</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted text-right">Price</th>
                <th class="px-5 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-muted text-right">Change</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="i in instruments" :key="i.symbol" class="border-b border-border/50 hover:bg-surface/50 transition-colors">
                <td class="px-5 py-3 font-semibold text-[13px] text-primary">{{ i.symbol }}</td>
                <td class="px-5 py-3 text-[13px] text-secondary max-w-[180px] truncate">{{ i.longName || i.shortName }}</td>
                <td class="px-5 py-3 text-[12px] text-muted">{{ i.sector || '—' }}</td>
                <td class="px-5 py-3 text-[12px] text-muted">{{ i.exchange }}</td>
                <td class="px-5 py-3 text-right text-[13px] tabular text-primary">{{ fmt(i.regularMarketPrice) }}</td>
                <td class="px-5 py-3 text-right">
                  <span :class="clsx('text-[12px] tabular font-semibold', i.regularMarketChangePercent >= 0 ? 'text-positive' : 'text-negative')">
                    {{ i.regularMarketChangePercent >= 0 ? '+' : '' }}{{ i.regularMarketChangePercent.toFixed(2) }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- STATS TAB -->
    <div v-else-if="tab === 'stats'" class="flex flex-col gap-6">
      <div class="rounded-xl border border-border bg-card p-5">
        <div class="text-[15px] font-semibold text-primary mb-1">Market Activity</div>
        <div class="text-[12px] text-muted mb-5">Top 10 symbols by trading activity</div>
        <AmRankingChart :traded="traded" :held="held" :watched="watched" :loading="statsLoading" />
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
        <div v-for="ds in statDatasets" :key="ds.key" class="rounded-xl border border-border bg-card overflow-hidden">
          <div class="px-5 py-3 border-b border-border">
            <span class="text-[13px] font-semibold text-primary">{{ ds.label }}</span>
          </div>
          <div v-if="statsLoading" class="flex justify-center py-6">
            <div class="w-4 h-4 rounded-full border-2 border-border border-t-accent animate-spin"></div>
          </div>
          <div v-else-if="getStatData(ds.key).length === 0" class="px-5 py-6 text-center text-[12px] text-muted">No data</div>
          <div v-else class="divide-y divide-border/30">
            <div v-for="(s, i) in getStatData(ds.key)" :key="s.symbol"
              class="flex items-center justify-between px-5 py-2.5 hover:bg-surface/50 transition-colors">
              <div class="flex items-center gap-2.5">
                <div class="w-5 h-5 rounded-full flex items-center justify-center text-[9px] font-bold text-white"
                  :style="{ background: STAT_COLORS[i % STAT_COLORS.length] }">
                  {{ i + 1 }}
                </div>
                <span class="text-[12px] font-semibold text-primary">{{ s.symbol }}</span>
              </div>
              <span class="text-[12px] tabular text-secondary">{{ s.count }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
