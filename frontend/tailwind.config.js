/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx,vue}'],
  theme: {
    extend: {
      colors: {
        base: '#09090b',
        surface: '#111113',
        card: '#18181b',
        border: '#27272a',
        'border-subtle': '#1c1c1f',
        primary: '#f4f4f5',
        secondary: '#a1a1aa',
        muted: '#52525b',
        accent: '#8b5cf6',
        'accent-hover': '#7c3aed',
        'accent-dim': 'rgba(139,92,246,0.12)',
        positive: '#22c55e',
        negative: '#ef4444',
        'positive-dim': 'rgba(34,197,94,0.12)',
        'negative-dim': 'rgba(239,68,68,0.12)',
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Inter', 'Segoe UI', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'Cascadia Code', 'monospace'],
      },
    },
  },
  plugins: [],
}
