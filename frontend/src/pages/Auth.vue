<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Loader2, Eye, EyeOff, CheckCircle2 } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import * as authApi from '../api/auth'
import clsx from 'clsx'
function convertDate(htmlDate) {
  if (!htmlDate) return ''
  const [year, month, day] = htmlDate.split('-')
  return `${month}/${day}/${year}`
}

const mode = ref('login')
const step = ref(1)
const loading = ref(false)
const error = ref('')
const showPw = ref(false)
const codeSent = ref(false)

// Login
const email = ref('')
const password = ref('')

// Signup
const su = ref({
  email: '', phoneNumber: '', firstName: '', middleName: '',
  lastName: '', birthDate: '', reasonForSignup: '', password: '', verificationCode: '',
})

// Reset
const resetEmail = ref('')
const resetCode = ref('')
const newPassword = ref('')
const resetDone = ref(false)

const auth = useAuthStore()
const router = useRouter()

async function handleLogin(e) {
  e.preventDefault()
  error.value = ''; loading.value = true
  try {
    const res = await authApi.login(email.value, password.value)
    if (res.data.success && res.data.data) {
      auth.login(res.data.data)
      router.push('/market')
    } else {
      error.value = res.data.message || 'Login failed'
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Invalid credentials'
  } finally { loading.value = false }
}

async function sendCode() {
  error.value = ''; loading.value = true
  try {
    await authApi.sendSignupCode(su.value.email)
    codeSent.value = true; step.value = 3
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to send code'
  } finally { loading.value = false }
}

async function handleSignup(e) {
  e.preventDefault()
  error.value = ''; loading.value = true
  try {
    const payload = { ...su.value, birthDate: convertDate(su.value.birthDate) }
    const res = await authApi.signup(payload)
    if (res.data.success) {
      mode.value = 'login'; error.value = ''; step.value = 1
    } else {
      error.value = res.data.message || 'Signup failed'
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Signup failed'
  } finally { loading.value = false }
}

async function handleSendResetCode() {
  error.value = ''; loading.value = true
  try {
    await authApi.requestPasswordReset(resetEmail.value)
    codeSent.value = true
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to send code'
  } finally { loading.value = false }
}

async function handleConfirmReset(e) {
  e.preventDefault()
  error.value = ''; loading.value = true
  try {
    await authApi.confirmPasswordReset(resetEmail.value, resetCode.value, newPassword.value)
    resetDone.value = true
  } catch (err) {
    error.value = err.response?.data?.message || 'Reset failed'
  } finally { loading.value = false }
}

const features = [
  'Live market quotes & historical charts',
  'Virtual paper trading with full order history',
  'Portfolio analytics & P&L tracking',
]
</script>

<template>
  <div class="min-h-screen bg-base flex">
    <!-- Left panel -->
    <div class="hidden lg:flex flex-col w-[480px] shrink-0 relative overflow-hidden border-r border-border" style="background: #8b5cf6;">
      <div class="cube cube1"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube2"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube4"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube5"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube6"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube4"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube5"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube6"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="cube cube7"><div class="front"></div><div class="bottom"></div><div class="after"></div><div class="over"></div><div class="left"></div><div class="right"></div></div>
      <div class="relative z-10 flex flex-col h-full p-12">
        <div class="flex items-center gap-2.5 mb-auto">
          <div style="font-size: 2rem; font-weight: bold; color: var(--color1--); font-style: italic; text-shadow: 4px 4px 10px black, 2px 2px 2px black;">
            Trade<span style="color: white; font-size: 42px; font-style: italic; vertical-align: middle;">X</span>
          </div>
        </div>

        <div class="mb-auto">
          <h1 class="text-4xl font-bold text-primary leading-tight mb-4">
            Paper trading,<br />real market data.
          </h1>
          <p class="text-[15px] leading-relaxed mb-10" style="color: #A1A1AA;">
            Practice investing with real-time stock data, no real money. Build your strategy, track your portfolio, and learn to trade.
          </p>
          <div class="flex flex-col gap-4">
            <div v-for="item in features" :key="item" class="flex items-center gap-3 text-[14px]" style="color: #A1A1AA;">
              <CheckCircle2 :size="16" class="text-accent shrink-0" />
              {{ item }}
            </div>
          </div>
        </div>

        <div class="text-[12px] text-muted">© 2025 TradeX. Paper trading platform.</div>
      </div>
    </div>

    <!-- Right panel -->
    <div class="flex-1 flex items-center justify-center px-6 py-12">
      <div class="w-full max-w-[360px]">

        <!-- LOGIN -->
        <div v-if="mode === 'login'" class="page-enter">
          <div class="mb-8">
            <h2 class="text-2xl font-bold text-primary mb-1">Welcome back</h2>
            <p class="text-secondary text-[14px]">Sign in to your account</p>
          </div>
          <form @submit="handleLogin" class="flex flex-col gap-4">
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Email</div>
              <input type="email" placeholder="you@example.com" v-model="email" required
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Password</div>
              <div class="relative">
                <input :type="showPw ? 'text' : 'password'" placeholder="••••••••" v-model="password" required
                  class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all pr-10" />
                <button type="button" @click="showPw = !showPw" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-secondary">
                  <EyeOff v-if="showPw" :size="14" />
                  <Eye v-else :size="14" />
                </button>
              </div>
            </div>
            <p v-if="error" class="text-[13px] text-negative bg-negative-dim border border-negative/20 rounded-lg px-3 py-2">{{ error }}</p>
            <button type="submit" :disabled="loading"
              class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed px-5 py-3 text-[14px] bg-accent text-white hover:bg-accent-hover active:scale-[0.98] w-full mt-1">
              <Loader2 v-if="loading" :size="16" class="animate-spin" />
              <template v-else><span>Sign in</span><ArrowRight :size="15" /></template>
            </button>
          </form>
          <div class="mt-6 flex items-center justify-between text-[13px]">
            <button @click="mode = 'reset'; error = ''; codeSent = false" class="text-accent hover:underline">Forgot password?</button>
            <button @click="mode = 'signup'; error = ''; step = 1" class="text-secondary hover:text-primary">Create account →</button>
          </div>
        </div>

        <!-- SIGNUP -->
        <div v-else-if="mode === 'signup'" class="page-enter">
          <div class="mb-8">
            <h2 class="text-2xl font-bold text-primary mb-1">Create account</h2>
            <div class="flex gap-1.5 mt-3">
              <div v-for="s in [1, 2, 3]" :key="s"
                :class="clsx('h-1 rounded-full flex-1 transition-all', s <= step ? 'bg-accent' : 'bg-border')" />
            </div>
            <p class="text-muted text-[12px] mt-2">Step {{ step }} of 3</p>
          </div>

          <!-- Step 1 -->
          <div v-if="step === 1" class="flex flex-col gap-4">
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Email</div>
              <input type="email" placeholder="you@example.com" v-model="su.email"
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Phone Number</div>
              <input placeholder="10 digits" v-model="su.phoneNumber"
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Password</div>
              <input type="password" placeholder="Min 8 characters" v-model="su.password"
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <p v-if="error" class="text-[13px] text-negative bg-negative-dim border border-negative/20 rounded-lg px-3 py-2">{{ error }}</p>
            <button
              class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all px-5 py-3 text-[14px] bg-accent text-white hover:bg-accent-hover active:scale-[0.98] w-full"
              @click="() => { if (su.email && su.phoneNumber && su.password.length >= 8) { error = ''; step = 2 } else error = 'Please fill all fields. Password min 8 chars.' }">
              Continue <ArrowRight :size="15" />
            </button>
          </div>

          <!-- Step 2 -->
          <div v-else-if="step === 2" class="flex flex-col gap-4">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">First Name</div>
                <input placeholder="John" v-model="su.firstName"
                  class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
              </div>
              <div>
                <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Last Name</div>
                <input placeholder="Doe" v-model="su.lastName"
                  class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
              </div>
            </div>
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Middle Name <span class="normal-case text-muted font-normal">(optional)</span></div>
              <input placeholder="M." v-model="su.middleName"
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Date of Birth</div>
              <input type="date" v-model="su.birthDate"
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Why TradeX? <span class="normal-case font-normal text-muted">(optional)</span></div>
              <input placeholder="Learn to invest..." v-model="su.reasonForSignup"
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <p v-if="error" class="text-[13px] text-negative bg-negative-dim border border-negative/20 rounded-lg px-3 py-2">{{ error }}</p>
            <div class="flex gap-2">
              <button @click="step = 1"
                class="flex-1 inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all px-5 py-3 text-[14px] bg-transparent border border-border text-secondary hover:text-primary hover:border-zinc-500">
                Back
              </button>
              <button @click="sendCode" :disabled="loading"
                class="flex-1 inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed px-5 py-3 text-[14px] bg-accent text-white hover:bg-accent-hover">
                <Loader2 v-if="loading" :size="16" class="animate-spin" />
                <span v-else>Send Code</span>
              </button>
            </div>
          </div>

          <!-- Step 3 -->
          <form v-else-if="step === 3" @submit="handleSignup" class="flex flex-col gap-4">
            <p class="text-[13px] text-secondary bg-accent-dim border border-accent/20 rounded-lg px-3 py-2">
              A 6-digit verification code was sent to <strong class="text-primary">{{ su.email }}</strong>
            </p>

            <div>
              <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Verification Code</div>
              <input placeholder="000000" v-model="su.verificationCode"
                class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
            </div>
            <p v-if="error" class="text-[13px] text-negative bg-negative-dim border border-negative/20 rounded-lg px-3 py-2">{{ error }}</p>
            <button type="submit" :disabled="loading"
              class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed px-5 py-3 text-[14px] bg-accent text-white hover:bg-accent-hover w-full">
              <Loader2 v-if="loading" :size="16" class="animate-spin" />
              <span v-else>Create Account</span>
            </button>
          </form>

          <button @click="mode = 'login'; error = ''; step = 1" class="mt-5 text-[13px] text-secondary hover:text-primary">← Back to sign in</button>
        </div>

        <!-- RESET -->
        <div v-else-if="mode === 'reset'" class="page-enter">
          <div v-if="resetDone" class="text-center">
            <CheckCircle2 :size="48" class="text-positive mx-auto mb-4" />
            <h2 class="text-xl font-bold text-primary mb-2">Password reset!</h2>
            <p class="text-secondary text-[14px] mb-6">You can now sign in with your new password.</p>
            <button @click="mode = 'login'; email = resetEmail; resetDone = false; codeSent = false"
              class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all px-5 py-3 text-[14px] bg-accent text-white hover:bg-accent-hover w-full">
              Sign in
            </button>
          </div>
          <template v-else>
            <div class="mb-8">
              <h2 class="text-2xl font-bold text-primary mb-1">Reset password</h2>
              <p class="text-secondary text-[14px]">Enter your email to receive a reset code</p>
            </div>
            <form @submit="handleConfirmReset" class="flex flex-col gap-4">
              <div>
                <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Email</div>
                <div class="flex gap-2">
                  <input type="email" placeholder="you@example.com" v-model="resetEmail" :disabled="codeSent"
                    class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all disabled:opacity-50" />
                  <button type="button" @click="handleSendResetCode" :disabled="loading || codeSent"
                    :class="clsx('shrink-0 inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2.5 text-[13px]', codeSent ? 'bg-transparent border border-border text-secondary' : 'bg-accent text-white hover:bg-accent-hover')">
                    <span v-if="codeSent">✓</span>
                    <Loader2 v-else-if="loading" :size="14" class="animate-spin" />
                    <span v-else>Send</span>
                  </button>
                </div>
              </div>
              <template v-if="codeSent">
                <div>
                  <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">Code</div>
                  <input placeholder="6-digit code" v-model="resetCode"
                    class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
                </div>
                <div>
                  <div class="text-[12px] font-medium text-muted mb-1.5 uppercase tracking-wider">New Password</div>
                  <input type="password" placeholder="Min 8 characters" v-model="newPassword"
                    class="w-full bg-surface border border-border rounded-lg px-3 py-2.5 text-[14px] text-primary placeholder-muted outline-none focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all" />
                </div>
                <button type="submit" :disabled="loading"
                  class="inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed px-5 py-3 text-[14px] bg-accent text-white hover:bg-accent-hover w-full">
                  <Loader2 v-if="loading" :size="16" class="animate-spin" />
                  <span v-else>Reset Password</span>
                </button>
              </template>
              <p v-if="error" class="text-[13px] text-negative bg-negative-dim border border-negative/20 rounded-lg px-3 py-2">{{ error }}</p>
            </form>
            <button @click="mode = 'login'; error = ''" class="mt-5 text-[13px] text-secondary hover:text-primary">← Back to sign in</button>
          </template>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.cube {
  width: 0;
  height: 0;
  transform: rotateX(-45deg) rotateY(45deg);
  transform-style: preserve-3d;
  position: absolute;
}
.cube1  { animation: cube1  3s ease infinite backwards; }
.cube2  { animation: cube2  3s ease infinite backwards; }
.cube3  { animation: cube3  3s ease infinite backwards; }
.cube4  { animation: cube4  3s ease infinite backwards; }
.cube5  { animation: cube5  3s ease infinite backwards; }
.cube6  { animation: cube6  4s ease infinite backwards; }
.cube7  { animation: cube7  3.5s ease infinite backwards; }
.cube8  { animation: cube8  4.5s ease infinite backwards; }
.cube9  { animation: cube9  3.2s ease infinite backwards; }
.cube10 { animation: cube10 5s ease infinite backwards; }

.front, .bottom, .after, .over, .left, .right {
  width: 200px;
  height: 200px;
  opacity: 0.5;
  position: absolute;
  right: 500px;
  bottom: 300px;
}
.front  { background: #7c3aed; transform: translateZ(200px); }
.bottom { background: #8b5cf6; transform: rotateX(90deg) translateY(100px) translateZ(-100px); }
.over   { background: #6d28d9; transform: rotateX(90deg) translateY(100px) translateZ(100px); }
.after  { background: #8b5cf6; }
.left   { background: #7c3aed; transform: translateZ(100px) translateX(-100px) rotateY(90deg); }
.right  { background: #8b5cf6; transform: rotateY(90deg); }

@keyframes cube1 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(0, 0, 0); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(0, -400px, 0); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(400px, -400px, 0); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, -200px, 200px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(0, 0, 0); }
}
@keyframes cube2 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(900px, -500px, 0px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(1100px, -700px, -200px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(1200px, -500px, 400px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(800px, -300px, -200px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(900px, -500px, 0px); }
}
@keyframes cube3 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(0px, -1100px, 0px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, -700px, 0px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(400px, -700px, 200px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(0px, -700px, 0px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(0px, -1100px, 0px); }
}
@keyframes cube4 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(1300px, 20px, 0px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(1000px, 400px, 200px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(400px, 200px, -200px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(1300px, 200px, -300px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(1300px, 20px, 0px); }
}
@keyframes cube5 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, -400px, 200px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, -400px, 800px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, 100px, 800px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, 100px, 200px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, -400px, 200px); }
}
@keyframes cube6 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(0px, 900px, 100px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(350px, 1250px, 450px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(550px, 1000px, -200px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(150px, 650px, 350px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(0px, 900px, 100px); }
}
@keyframes cube7 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(700px, 1000px, 200px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(950px, 1200px, 400px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(600px, 1300px, 100px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(850px, 1100px, 350px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(700px, 1000px, 200px); }
}
@keyframes cube8 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(100px, -800px, 400px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(400px, -1000px, 200px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(600px, -600px, 600px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(200px, -600px, 800px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(100px, -800px, 400px); }
}
@keyframes cube9 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(700px, 200px, -200px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(1000px, -200px, -400px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(800px, -400px, 200px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(500px, 0px, 400px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(700px, 200px, -200px); }
}
@keyframes cube10 {
  0%   { transform: rotateX(-45deg) rotateY(45deg) translate3d(300px, -600px, -300px); }
  25%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(100px, -900px, 100px); }
  50%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(500px, -1000px, 500px); }
  75%  { transform: rotateX(-45deg) rotateY(45deg) translate3d(700px, -700px, -100px); }
  100% { transform: rotateX(-45deg) rotateY(45deg) translate3d(300px, -600px, -300px); }
}
</style>
