import { WebSocketServer } from 'ws'
import { createClient } from 'redis'

const wss = new WebSocketServer({ port: 5003 })
const redisUrl = `redis://${process.env.REDIS_HOST || 'localhost'}:6379`
const subscriber = createClient({ url: redisUrl })
const redis = createClient({ url: redisUrl })
await subscriber.connect()
await redis.connect()

// Track connected clients and their symbols
const clients = new Map() // ws -> Set of symbols

wss.on('connection', (ws) => {
  clients.set(ws, new Set())
  console.log('[ws] client connected')

  // Client sends { type: 'subscribe', symbols: ['AAPL', 'TSLA'] }
  ws.on('message', async (msg) => {
    try {
      const { type, symbols } = JSON.parse(msg)
      if (type === 'subscribe') {
        clients.set(ws, new Set(symbols))
        console.log(`[ws] subscribed to: ${symbols.join(', ')}`)
        // Push cached quotes immediately on subscribe
        for (const symbol of symbols) {
          const cached = await redis.get(`quote:${symbol}`)
          if (cached && ws.readyState === 1) ws.send(cached)
        }
      }
    } catch (e) {
      console.error('[ws] bad message:', e.message)
    }
  })

  ws.on('close', () => {
    clients.delete(ws)
    console.log('[ws] client disconnected')
  })
})

// Subscribe to Redis and push to relevant clients
await subscriber.subscribe('quote-updates', (message) => {
  const update = JSON.parse(message)
  console.log('[ws] received from Redis:', update.symbol)
  for (const [ws, symbols] of clients) {
    if (symbols.has(update.symbol) && ws.readyState === 1) {
      ws.send(message)
    }
  }
})

console.log('[ws] WebSocket server running on port 5003')
