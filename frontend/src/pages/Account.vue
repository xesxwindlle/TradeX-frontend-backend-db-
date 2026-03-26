<script setup>
import { ref, onMounted } from 'vue'
import { User, DollarSign, Lock, Phone, AlertTriangle, CheckCircle2 } from 'lucide-vue-next'
import * as accountApi from '../api/account'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { fmt } from '../components/ui'

const auth = useAuthStore()
const router = useRouter()
const account = ref(null)
const loading = ref(true)

// Funds
const fundsAmount = ref('')
const fundsLoading = ref(false)
const fundsMsg = ref('')
const fundsErr = ref('')

// Password
const pwCurrent = ref('')
const pwNew = ref('')
const pwNew2 = ref('')
const pwLoading = ref(false)
const pwMsg = ref('')
const pwErr = ref('')

// Phone
const phoneNew = ref('')
const phoneCode = ref('')
const phoneSent = ref(false)
const phoneLoading = ref(false)
const phoneMsg = ref('')
const phoneErr = ref('')

// Reset
const resetOpen = ref(false)
const resetLoading = ref(false)

// Delete
const deleteOpen = ref(false)
const deleteLoading = ref(false)

onMounted(async () => {
  try {
    const r = await accountApi.getAccount()
    if (r.data.data) account.value = r.data.data
  } finally { loading.value = false }
})

async function handleAddFunds(e) {
  e.preventDefault()
  fundsErr.value = ''; fundsMsg.value = ''
  const amount = parseFloat(fundsAmount.value)
  if (isNaN(amount) || amount <= 0) { fundsErr.value = 'Enter a valid amount'; return }
  fundsLoading.value = true
  try {
    const r = await accountApi.addFunds(amount)
    if (r.data.success) {
      fundsMsg.value = `Added ${fmt(amount)} — new balance: ${fmt(r.data.data ?? 0)}`
      fundsAmount.value = ''
      const ar = await accountApi.getAccount()
      if (ar.data.data) account.value = ar.data.data
    } else { fundsErr.value = r.data.message || 'Failed' }
  } catch (err) {
    fundsErr.value = err.response?.data?.message || 'Failed'
  } finally { fundsLoading.value = false }
}

async function handleChangePassword(e) {
  e.preventDefault()
  pwErr.value = ''; pwMsg.value = ''
  if (pwNew.value !== pwNew2.value) { pwErr.value = 'New passwords do not match'; return }
  if (pwNew.value.length < 8) { pwErr.value = 'Password must be at least 8 characters'; return }
  pwLoading.value = true
  try {
    const r = await accountApi.changePassword(pwCurrent.value, pwNew.value)
    if (r.data.success) {
      pwMsg.value = 'Password changed successfully'
      pwCurrent.value = ''; pwNew.value = ''; pwNew2.value = ''
    } else { pwErr.value = r.data.message || 'Failed' }
  } catch (err) {
    pwErr.value = err.response?.data?.message || 'Failed'
  } finally { pwLoading.value = false }
}

async function handleSendPhoneCode() {
  phoneErr.value = ''; phoneMsg.value = ''
  if (!phoneNew.value.trim()) { phoneErr.value = 'Enter new phone number'; return }
  phoneLoading.value = true
  try {
    await accountApi.sendMobileCode(phoneNew.value)
    phoneSent.value = true
  } catch (err) {
    phoneErr.value = err.response?.data?.message || 'Failed to send code'
  } finally { phoneLoading.value = false }
}

async function handleChangePhone(e) {
  e.preventDefault()
  phoneErr.value = ''; phoneMsg.value = ''
  if (!account.value?.phoneNumber || !phoneNew.value || !phoneCode.value) { phoneErr.value = 'Fill all fields'; return }
  phoneLoading.value = true
  try {
    const r = await accountApi.changeMobile(account.value.phoneNumber, phoneNew.value, phoneCode.value)
    if (r.data.success) {
      phoneMsg.value = 'Phone number updated successfully'
      phoneNew.value = ''; phoneCode.value = ''; phoneSent.value = false
      const ar = await accountApi.getAccount()
      if (ar.data.data) account.value = ar.data.data
    } else { phoneErr.value = r.data.message || 'Failed' }
  } catch (err) {
    phoneErr.value = err.response?.data?.message || 'Failed'
  } finally { phoneLoading.value = false }
}

async function handleResetAccount() {
  resetLoading.value = true
  try {
    await accountApi.resetAccount()
    resetOpen.value = false
    const ar = await accountApi.getAccount()
    if (ar.data.data) account.value = ar.data.data
  } finally { resetLoading.value = false }
}

async function handleDeleteAccount() {
  deleteLoading.value = true
  try {
    await accountApi.deleteAccount()
    auth.logout()
    router.push('/login')
  } finally { deleteLoading.value = false }
}

const profileFields = (a) => [
  { label: 'Account Number', value: a.accountNumber },
  { label: 'Email', value: a.email },
  { label: 'Full Name', value: [a.firstName, a.middleName, a.lastName].filter(Boolean).join(' ') },
  { label: 'Phone', value: a.phoneNumber },
  { label: 'Date of Birth', value: a.birthDate },
  { label: 'Member Since', value: a.createdDate ? new Date(a.createdDate).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' }) : '—' },
  { label: 'Role', value: a.role },
  { label: 'Status', value: a.status },
]
</script>

<template>
  <div v-if="loading" style="display: flex; align-items: center; justify-content: center; height: 100%;">
    <div style="width: 32px; height: 32px; border-radius: 50%; border: 2px solid #1f2937; border-top-color: #007bff; animation: spin 0.7s linear infinite;"></div>
  </div>

  <div v-else class="landing-container page-enter">
    <div class="welcome-section">
      <div class="welcome-title">Account</div>
      <div class="welcome-subtitle">Manage your profile and settings</div>
    </div>

    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px; align-items: start;">

      <!-- Left column: Profile + Danger Zone -->
      <div>
        <!-- Profile Info -->
        <div class="chart-card" style="margin-bottom: 24px;">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 12px;">
              <div style="width: 36px; height: 36px; border-radius: 10px; background: rgba(0,123,255,0.12); display: flex; align-items: center; justify-content: center; color: #007bff;">
                <User :size="16" />
              </div>
              <div>
                <div style="font-size: 14px; font-weight: 600; color: #e1e4e8;">Profile Information</div>
                <div style="font-size: 12px; color: #9ca3af;">Your account details</div>
              </div>
            </div>
          </div>
          <div v-if="account" style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div v-for="field in profileFields(account)" :key="field.label"
              style="background: #141922; border-radius: 8px; padding: 10px 12px;">
              <div style="font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 2px;">{{ field.label }}</div>
              <div style="font-size: 13px; color: #e1e4e8; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ field.value ?? '—' }}</div>
            </div>
          </div>
          <div v-if="account?.reasonForSignup" style="background: #141922; border-radius: 8px; padding: 10px 12px; margin-top: 12px;">
            <div style="font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 2px;">Reason for Signup</div>
            <div style="font-size: 13px; color: #9ca3af;">{{ account.reasonForSignup }}</div>
          </div>
        </div>

        <!-- Danger Zone -->
        <div class="chart-card danger-zone" style="border-color: rgba(239,68,68,0.2);">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 12px;">
              <div style="width: 36px; height: 36px; border-radius: 10px; background: rgba(239,68,68,0.12); display: flex; align-items: center; justify-content: center; color: #ef4444;">
                <AlertTriangle :size="16" />
              </div>
              <div>
                <div style="font-size: 14px; font-weight: 600; color: #ef4444;">Danger Zone</div>
                <div style="font-size: 12px; color: #9ca3af;">Irreversible actions</div>
              </div>
            </div>
          </div>
          <div style="border-top: 1px solid #1f2937; margin-bottom: 16px;"></div>
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;">
            <div>
              <div style="font-size: 13px; font-weight: 500; color: #e1e4e8;">Reset Account</div>
              <div style="font-size: 12px; color: #9ca3af;">Clear all holdings, watchlist, and order history</div>
            </div>
            <button @click="resetOpen = true"
              style="display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.3);color:#ef4444;cursor:pointer;transition:background 0.2s;"
              @mouseenter="$event.currentTarget.style.background='rgba(239,68,68,0.2)'"
              @mouseleave="$event.currentTarget.style.background='rgba(239,68,68,0.1)'">
              Reset Account
            </button>
          </div>
          <div style="border-top: 1px solid #1f2937; margin-bottom: 16px;"></div>
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <div>
              <div style="font-size: 13px; font-weight: 500; color: #e1e4e8;">Delete Account</div>
              <div style="font-size: 12px; color: #9ca3af;">Permanently delete your account and all data</div>
            </div>
            <button @click="deleteOpen = true"
              style="display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.3);color:#ef4444;cursor:pointer;transition:background 0.2s;"
              @mouseenter="$event.currentTarget.style.background='rgba(239,68,68,0.2)'"
              @mouseleave="$event.currentTarget.style.background='rgba(239,68,68,0.1)'">
              Delete Account
            </button>
          </div>
        </div>
      </div>

      <!-- Right column: Add Funds + Change Password + Change Phone -->
      <div>
        <!-- Add Funds -->
        <div class="chart-card" style="margin-bottom: 24px;">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 12px;">
              <div style="width: 36px; height: 36px; border-radius: 10px; background: rgba(0,123,255,0.12); display: flex; align-items: center; justify-content: center; color: #007bff;">
                <DollarSign :size="16" />
              </div>
              <div>
                <div style="font-size: 14px; font-weight: 600; color: #e1e4e8;">Add Funds</div>
                <div style="font-size: 12px; color: #9ca3af;">Current balance: {{ fmt(account?.cashBalance) }}</div>
              </div>
            </div>
          </div>
          <form @submit="handleAddFunds" style="display: flex; flex-direction: column; gap: 12px;">
            <div>
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 6px;">Amount (USD)</div>
              <div style="position: relative;">
                <span style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); font-size: 14px; color: #9ca3af;">$</span>
                <input type="number" min="1" step="0.01" placeholder="1000.00" v-model="fundsAmount"
                  @input="fundsMsg = ''; fundsErr = ''"
                  style="width: 100%; background: #141922; border: 1px solid #1f2937; border-radius: 8px; padding: 10px 12px 10px 24px; font-size: 14px; color: #e1e4e8; outline: none; box-sizing: border-box;" />
              </div>
            </div>
            <div v-if="fundsMsg" style="display:flex;align-items:center;gap:8px;font-size:13px;color:#10b981;background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.2);border-radius:8px;padding:8px 12px;">
              <CheckCircle2 :size="14" /> {{ fundsMsg }}
            </div>
            <div v-if="fundsErr" style="display:flex;align-items:center;gap:8px;font-size:13px;color:#ef4444;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.2);border-radius:8px;padding:8px 12px;">
              <AlertTriangle :size="14" /> {{ fundsErr }}
            </div>
            <button type="submit" :disabled="fundsLoading || !fundsAmount"
              style="display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:#007bff;color:white;border:none;cursor:pointer;transition:opacity 0.2s;"
              :style="(fundsLoading || !fundsAmount) ? 'opacity:0.4;cursor:not-allowed;' : ''">
              <div v-if="fundsLoading" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,0.3);border-top-color:white;animation:spin 0.7s linear infinite;"></div>
              <span v-else>Add Funds</span>
            </button>
          </form>
        </div>

        <!-- Change Password -->
        <div class="chart-card" style="margin-bottom: 24px;">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 12px;">
              <div style="width: 36px; height: 36px; border-radius: 10px; background: rgba(0,123,255,0.12); display: flex; align-items: center; justify-content: center; color: #007bff;">
                <Lock :size="16" />
              </div>
              <div style="font-size: 14px; font-weight: 600; color: #e1e4e8;">Change Password</div>
            </div>
          </div>
          <form @submit="handleChangePassword" style="display: flex; flex-direction: column; gap: 12px;">
            <div>
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 6px;">Current Password</div>
              <input type="password" placeholder="••••••••" v-model="pwCurrent" @input="pwMsg = ''; pwErr = ''"
                style="width: 100%; background: #141922; border: 1px solid #1f2937; border-radius: 8px; padding: 10px 12px; font-size: 14px; color: #e1e4e8; outline: none;" />
            </div>
            <div>
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 6px;">New Password</div>
              <input type="password" placeholder="Min 8 characters" v-model="pwNew"
                style="width: 100%; background: #141922; border: 1px solid #1f2937; border-radius: 8px; padding: 10px 12px; font-size: 14px; color: #e1e4e8; outline: none;" />
            </div>
            <div>
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 6px;">Confirm New Password</div>
              <input type="password" placeholder="Repeat new password" v-model="pwNew2"
                style="width: 100%; background: #141922; border: 1px solid #1f2937; border-radius: 8px; padding: 10px 12px; font-size: 14px; color: #e1e4e8; outline: none;" />
            </div>
            <div v-if="pwMsg" style="display:flex;align-items:center;gap:8px;font-size:13px;color:#10b981;background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.2);border-radius:8px;padding:8px 12px;">
              <CheckCircle2 :size="14" /> {{ pwMsg }}
            </div>
            <div v-if="pwErr" style="display:flex;align-items:center;gap:8px;font-size:13px;color:#ef4444;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.2);border-radius:8px;padding:8px 12px;">
              <AlertTriangle :size="14" /> {{ pwErr }}
            </div>
            <button type="submit" :disabled="pwLoading || !pwCurrent || !pwNew || !pwNew2"
              style="display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:#007bff;color:white;border:none;cursor:pointer;transition:opacity 0.2s;"
              :style="(pwLoading || !pwCurrent || !pwNew || !pwNew2) ? 'opacity:0.4;cursor:not-allowed;' : ''">
              <div v-if="pwLoading" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,0.3);border-top-color:white;animation:spin 0.7s linear infinite;"></div>
              <span v-else>Change Password</span>
            </button>
          </form>
        </div>

        <!-- Change Phone -->
        <div class="chart-card">
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 12px;">
              <div style="width: 36px; height: 36px; border-radius: 10px; background: rgba(0,123,255,0.12); display: flex; align-items: center; justify-content: center; color: #007bff;">
                <Phone :size="16" />
              </div>
              <div>
                <div style="font-size: 14px; font-weight: 600; color: #e1e4e8;">Change Phone Number</div>
                <div style="font-size: 12px; color: #9ca3af;">Current: {{ account?.phoneNumber }}</div>
              </div>
            </div>
          </div>
          <form @submit="handleChangePhone" style="display: flex; flex-direction: column; gap: 12px;">
            <div>
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 6px;">New Phone Number</div>
              <div style="display: flex; gap: 8px;">
                <input placeholder="10-digit number" v-model="phoneNew" :disabled="phoneSent"
                  @input="phoneMsg = ''; phoneErr = ''"
                  style="flex:1;background:#141922;border:1px solid #1f2937;border-radius:8px;padding:10px 12px;font-size:14px;color:#e1e4e8;outline:none;"
                  :style="phoneSent ? 'opacity:0.5;' : ''" />
                <button type="button" @click="handleSendPhoneCode" :disabled="phoneLoading || phoneSent || !phoneNew"
                  :style="phoneSent
                    ? 'flex-shrink:0;display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:0 16px;font-size:13px;background:transparent;border:1px solid #1f2937;color:#9ca3af;cursor:not-allowed;'
                    : (phoneLoading || !phoneNew)
                      ? 'flex-shrink:0;display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:0 16px;font-size:13px;background:#007bff;color:white;border:none;cursor:not-allowed;opacity:0.4;'
                      : 'flex-shrink:0;display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:0 16px;font-size:13px;background:#007bff;color:white;border:none;cursor:pointer;'">
                  <span v-if="phoneSent">&#10003; Sent</span>
                  <div v-else-if="phoneLoading" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,0.3);border-top-color:white;animation:spin 0.7s linear infinite;"></div>
                  <span v-else>Send Code</span>
                </button>
              </div>
            </div>
            <div v-if="phoneSent">
              <div style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #9ca3af; margin-bottom: 6px;">Verification Code</div>
              <input placeholder="6-digit code" v-model="phoneCode"
                style="width: 100%; background: #141922; border: 1px solid #1f2937; border-radius: 8px; padding: 10px 12px; font-size: 14px; color: #e1e4e8; outline: none;" />
            </div>
            <div v-if="phoneMsg" style="display:flex;align-items:center;gap:8px;font-size:13px;color:#10b981;background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.2);border-radius:8px;padding:8px 12px;">
              <CheckCircle2 :size="14" /> {{ phoneMsg }}
            </div>
            <div v-if="phoneErr" style="display:flex;align-items:center;gap:8px;font-size:13px;color:#ef4444;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.2);border-radius:8px;padding:8px 12px;">
              <AlertTriangle :size="14" /> {{ phoneErr }}
            </div>
            <button v-if="phoneSent" type="submit" :disabled="phoneLoading || !phoneCode"
              style="display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:#007bff;color:white;border:none;cursor:pointer;transition:opacity 0.2s;"
              :style="(phoneLoading || !phoneCode) ? 'opacity:0.4;cursor:not-allowed;' : ''">
              <div v-if="phoneLoading" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,0.3);border-top-color:white;animation:spin 0.7s linear infinite;"></div>
              <span v-else>Update Phone</span>
            </button>
          </form>
        </div>
      </div>

    </div>

    <!-- Reset Modal -->
    <div v-if="resetOpen" style="position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center;">
      <div style="position: absolute; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px);" @click="resetOpen = false" />
      <div style="position: relative; z-index: 10; width: 100%; max-width: 420px; margin: 0 16px; background: #1f2937; border: 1px solid #374151; border-radius: 16px; box-shadow: 0 24px 48px rgba(0,0,0,0.4);">
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-bottom: 1px solid #374151;">
          <h3 style="font-size: 15px; font-weight: 600; color: #e1e4e8;">Reset Account</h3>
          <button @click="resetOpen = false" style="background: none; border: none; color: #9ca3af; cursor: pointer; font-size: 16px;">&#x2715;</button>
        </div>
        <div style="padding: 24px; display: flex; flex-direction: column; gap: 16px;">
          <p style="font-size: 14px; color: #9ca3af; line-height: 1.6;">
            Are you sure you want to reset your account? This action <strong style="color: #ef4444;">cannot be undone</strong>. All your holdings, watchlist, and order history will be permanently deleted. Your cash balance will be restored to the default.
          </p>
          <div style="display: flex; gap: 8px;">
            <button @click="resetOpen = false"
              style="flex:1;display:inline-flex;align-items:center;justify-content:center;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:transparent;border:1px solid #1f2937;color:#9ca3af;cursor:pointer;">
              Cancel
            </button>
            <button @click="handleResetAccount" :disabled="resetLoading"
              style="flex:1;display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.3);color:#ef4444;cursor:pointer;transition:opacity 0.2s;"
              :style="resetLoading ? 'opacity:0.4;cursor:not-allowed;' : ''">
              <div v-if="resetLoading" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(239,68,68,0.3);border-top-color:#ef4444;animation:spin 0.7s linear infinite;"></div>
              <span v-else>Yes, Reset</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div v-if="deleteOpen" style="position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center;">
      <div style="position: absolute; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px);" @click="deleteOpen = false" />
      <div style="position: relative; z-index: 10; width: 100%; max-width: 420px; margin: 0 16px; background: #1f2937; border: 1px solid #374151; border-radius: 16px; box-shadow: 0 24px 48px rgba(0,0,0,0.4);">
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-bottom: 1px solid #374151;">
          <h3 style="font-size: 15px; font-weight: 600; color: #e1e4e8;">Delete Account</h3>
          <button @click="deleteOpen = false" style="background: none; border: none; color: #9ca3af; cursor: pointer; font-size: 16px;">&#x2715;</button>
        </div>
        <div style="padding: 24px; display: flex; flex-direction: column; gap: 16px;">
          <p style="font-size: 14px; color: #9ca3af; line-height: 1.6;">
            Are you sure you want to permanently delete your account? This action <strong style="color: #ef4444;">cannot be undone</strong>. All your data, holdings, and order history will be lost.
          </p>
          <div style="display: flex; gap: 8px;">
            <button @click="deleteOpen = false"
              style="flex:1;display:inline-flex;align-items:center;justify-content:center;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:transparent;border:1px solid #1f2937;color:#9ca3af;cursor:pointer;">
              Cancel
            </button>
            <button @click="handleDeleteAccount" :disabled="deleteLoading"
              style="flex:1;display:inline-flex;align-items:center;justify-content:center;gap:8px;font-weight:600;border-radius:8px;padding:10px 16px;font-size:13px;background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.3);color:#ef4444;cursor:pointer;transition:opacity 0.2s;"
              :style="deleteLoading ? 'opacity:0.4;cursor:not-allowed;' : ''">
              <div v-if="deleteLoading" style="width:14px;height:14px;border-radius:50%;border:2px solid rgba(239,68,68,0.3);border-top-color:#ef4444;animation:spin 0.7s linear infinite;"></div>
              <span v-else>Yes, Delete</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes spin { to { transform: rotate(360deg); } }
.danger-zone { transition: border-color 0.3s, box-shadow 0.3s; }
.danger-zone:hover { border-color: rgba(239,68,68,0.6) !important; box-shadow: 0 0 0 1px rgba(239,68,68,0.15); }
</style>
