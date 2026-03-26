import { createClient } from 'redis'

export const redis = createClient({ url: `redis://${process.env.REDIS_HOST || 'localhost'}:6379` })
await redis.connect()

const DEFAULT_EXPIRATION = 30

export async function getOrSetCache(key, cb) {
  const cached = await redis.get(key)
  if (cached) return JSON.parse(cached)
  const data = await cb()
  await redis.set(key, JSON.stringify(data), { EX: DEFAULT_EXPIRATION })
  return data
}
