<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { LogOut, Shield, Search, X } from 'lucide-vue-next'
import MovingTickers from './MovingTickers.vue'
import * as marketApi from '../api/market'
import * as watchlistApi from '../api/watchlist'
import * as holdingsApi from '../api/holdings'
import { usePriceStore } from '../stores/price'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const nav = [
  { to: '/market',    label: 'Market'    },
  { to: '/trade',     label: 'Trade'     },
  { to: '/orders',    label: 'Orders'    },
  { to: '/portfolio', label: 'Portfolio' },
  { to: '/account',   label: 'Account'   },
]

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

// ── Global stock search ────────────────────────────────────────────────────
const searchQuery = ref('')
const searchResults = ref([])
const searching = ref(false)

let searchTimer = null
watch(searchQuery, (q) => {
  if (!q.trim()) { searchResults.value = []; return }
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    searching.value = true
    try {
      const r = await marketApi.searchInstruments(q)
      if (r.data.data) searchResults.value = r.data.data
    } finally { searching.value = false }
  }, 400)
})

function selectSymbol(symbol) {
  searchQuery.value = ''
  searchResults.value = []
  router.push(`/trade?symbol=${encodeURIComponent(symbol)}`)
}

function clearSearch() {
  searchQuery.value = ''
  searchResults.value = []
}

// ── WebSocket live prices ──────────────────────────────────────────────────
const priceStore = usePriceStore()
let ws = null

async function connectWebSocket() {
  const [wlRes, hdRes] = await Promise.allSettled([
    watchlistApi.getWatchlist(),
    holdingsApi.getHoldings()
  ])

  const symbols = new Set()
  if (wlRes.status === 'fulfilled' && wlRes.value.data.data)
    wlRes.value.data.data.forEach(w => symbols.add(w.symbol))
  if (hdRes.status === 'fulfilled' && hdRes.value.data.data)
    hdRes.value.data.data.forEach(h => symbols.add(h.symbol))

  ws = new WebSocket(`ws://${window.location.hostname}:5003`)

  ws.onopen = () => {
    ws.send(JSON.stringify({ type: 'subscribe', symbols: [...symbols] }))
    console.log('[ws] subscribed to:', [...symbols])
  }

  ws.onmessage = (event) => {
    const update = JSON.parse(event.data)
    priceStore.updatePrice(update)
  }

  ws.onclose = () => {
    console.log('[ws] disconnected, reconnecting in 3s...')
    setTimeout(connectWebSocket, 3000)
  }
}

onMounted(connectWebSocket)
onUnmounted(() => ws?.close())
</script>

<template>
  <div style="display:flex; flex-direction:column; height:100vh; overflow:hidden;">

    <!-- Header -->
    <header class="header">
      <div class="header-brand">
        Trade<span id="X">X</span>
      </div>

      <nav class="nav-links">
        <a
          v-for="item in nav"
          :key="item.to"
          :class="['nav-link', isActive(item.to) ? 'active' : '']"
          @click="router.push(item.to)"
        >{{ item.label }}</a>

        <a
          v-if="auth.isAdmin"
          :class="['nav-link', isActive('/admin') ? 'active' : '']"
          @click="router.push('/admin')"
        >
          <Shield :size="13" /> Admin
        </a>
      </nav>

      <!-- Global stock search -->
      <div style="position: relative; margin: 0 16px;">
        <Search :size="13" style="position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #9ca3af; pointer-events: none;" />
        <input
          v-model="searchQuery"
          placeholder="Search symbol..."
          style="width: 200px; background: #18181b; border: 1px solid var(--color1--); border-radius: 8px; padding: 6px 28px 6px 30px; font-size: 12px; color: #e1e4e8; outline: none;"
        />
        <button v-if="searchQuery" @click="clearSearch"
          style="position: absolute; right: 8px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #9ca3af; cursor: pointer; display: flex; align-items: center;">
          <X :size="11" />
        </button>
        <!-- Dropdown -->
        <div v-if="searchResults.length > 0 || searching"
          style="position: absolute; top: calc(100% + 6px); left: 0; right: 0; min-width: 260px; background: #18181b; border: 1px solid #27272a; border-radius: 10px; box-shadow: 0 8px 32px rgba(0,0,0,0.6); z-index: 100; overflow: hidden;">
          <div v-if="searching" style="padding: 10px 14px; font-size: 12px; color: #9ca3af;">Searching...</div>
          <button v-for="r in searchResults.slice(0, 8)" :key="r.symbol"
            @click="selectSymbol(r.symbol)"
            style="width: 100%; display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 8px 14px; background: none; border: none; text-align: left; cursor: pointer; transition: background 0.15s;"
            @mouseenter="$event.currentTarget.style.background='rgba(39,39,42,0.8)'"
            @mouseleave="$event.currentTarget.style.background='none'">
            <div>
              <div style="font-size: 13px; font-weight: 600; color: #e1e4e8;">{{ r.symbol }}</div>
              <div style="font-size: 11px; color: #9ca3af; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px;">{{ r.longName || r.shortName }}</div>
            </div>
            <div style="font-size: 10px; color: #9ca3af; flex-shrink: 0;">{{ r.exchange }}</div>
          </button>
        </div>
      </div>

      <div class="icons-set">
        <div v-if="auth.isAdmin" class="admin-label">ADMIN</div>
        <span style="font-size:12px; color:#9ca3af;">{{ auth.user?.email }}</span>
        <button class="signout-btn" @click="handleLogout">
          <LogOut :size="14" /> Sign out
        </button>
      </div>
    </header>

    <!-- Moving ticker (below header) -->
    <div style="position:fixed; top:57px; left:0; right:0; z-index:2;">
      <MovingTickers />
    </div>

    <!-- Main content -->
    <main style="flex:1; overflow-y:auto; margin-top:93px;">
      <router-view />
    </main>


  </div>
</template>
