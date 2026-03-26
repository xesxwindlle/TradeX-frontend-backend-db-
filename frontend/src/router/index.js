import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import Auth from '../pages/Auth.vue'
import Market from '../pages/Market.vue'
import Trade from '../pages/Trade.vue'
import Orders from '../pages/Orders.vue'
import Watchlist from '../pages/Watchlist.vue'
import Portfolio from '../pages/Portfolio.vue'
import Account from '../pages/Account.vue'
import Admin from '../pages/Admin.vue'

const routes = [
  {
    path: '/login',
    component: Auth,
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/market' },
      { path: 'market', component: Market },
      { path: 'trade', component: Trade },
      { path: 'orders', component: Orders },
      { path: 'watchlist', component: Watchlist },
      { path: 'portfolio', component: Portfolio },
      { path: 'account', component: Account },
      { path: 'admin', component: Admin, meta: { requiresAdmin: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory('/TradeX'),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('tx_token')
  if (to.meta.requiresAuth && !token) return '/login'
  if (to.path === '/login' && token) return '/market'
})

export default router
