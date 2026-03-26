import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePriceStore = defineStore('price', () => {
  const prices = ref({})

  function updatePrice(update) {
    prices.value[update.symbol] = {
      ...update,
      effectivePrice: update.marketState === 'PRE'
        ? (update.preMarketPrice || update.regularMarketPrice)
        : (update.marketState === 'POSTPOST' || update.marketState === 'POST')
          ? (update.postMarketPrice || update.regularMarketPrice)
          : update.regularMarketPrice
    }
  }

  return { prices, updatePrice }
})
