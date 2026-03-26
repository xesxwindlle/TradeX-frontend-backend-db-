import client from './client'
import * as yahoo from './yahoo'

// DB-backed — stay on Spring Boot
export const getInstruments = (limit) =>
  client.get('/api/market/instruments', { params: { limit } })

export const getTreemap = (limit) =>
  client.get('/api/market/treemap', { params: { limit } })

export const getSectors = () =>
  client.get('/api/market/sectors')

// Real-time — Yahoo Finance via Node server
export const getQuote = (symbol) => yahoo.getQuote(symbol)
export const getChart = (symbol, period) => yahoo.getChart(symbol, period)
export const getRecommendations = (symbol) => yahoo.getRecommendations(symbol)
export const getInsights = (symbol) => yahoo.getInsights(symbol)
export const getWinners = () => yahoo.getWinners()
export const getLosers = () => yahoo.getLosers()
export const getMostActive = () => yahoo.getMostActive()
export const getRandomQuotes = () => yahoo.getRandomQuotes()
export const searchInstruments = (term) => yahoo.searchSymbols(term)
