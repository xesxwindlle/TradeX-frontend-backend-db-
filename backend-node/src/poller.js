import axios from 'axios';
import YahooFinance from "yahoo-finance2";
import { redis } from './cache.js';

const yahooFinance = new YahooFinance();

const POLL_INTERVAL = 15000; //15s

const ENDPOINT_URL = `${process.env.JAVA_BACKEND_URL || 'http://localhost:8080'}/TradeX/api/active-symbols`;


async function getActiveSymbols() {
    const res = await axios.get(ENDPOINT_URL);
    return res.data; 
}

async function poll() {
    try {
        const symbols = await getActiveSymbols();
        if (!symbols.length) return;

        for (let symbol of symbols) {
            try {
                // const quote = await yahooFinance.quote(symbol);

                // Write to Redis cache
                const quote = await yahooFinance.quote(symbol);

                // Update cache with fresh data, then publish
                await redis.set(`quote:${symbol}`, JSON.stringify(quote), { EX: 30 });
                await redis.publish('quote-updates', JSON.stringify(quote));
                console.log(`[poller] ${symbol}: $${quote.regularMarketPrice}`);
            } catch (err) {
                console.error(`[poller] failed for ${symbol}:`, err.message);
            }
        }
    }
    catch (err) {
        console.error('[poller] cycle failed:', err.message)
    }
}


async function run() {
    console.log('[poller] cycle starting...');
    await poll();
    console.log('[poller] cycle done, next in 15s');
    setTimeout(run, POLL_INTERVAL);
}

process.on('uncaughtException', (err) => console.error('[poller] uncaught:', err))
process.on('unhandledRejection', (err) => console.error('[poller] unhandled rejection:', err))

console.log('[poller] starting...')
run();
