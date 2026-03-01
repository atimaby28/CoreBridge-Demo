<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = ref({
  email: '',
  password: '',
})
const error = ref('')
const loading = ref(false)

// 데모 계정 정보
const demoAccounts = [
  { role: '구직자', icon: '👤', email: 'user@demo.com', password: 'qwer1234', color: 'blue' },
  { role: '기업', icon: '🏢', email: 'company@demo.com', password: 'qwer1234', color: 'emerald' },
  { role: '관리자', icon: '🛡️', email: 'admin@demo.com', password: 'qwer1234', color: 'purple' },
]

function fillDemoAccount(account: typeof demoAccounts[0]) {
  form.value.email = account.email
  form.value.password = account.password
}

// 리다이렉트 경로
const redirectPath = ref('/')

onMounted(() => {
  // 쿼리 파라미터에서 리다이렉트 경로 추출
  if (route.query.redirect) {
    redirectPath.value = decodeURIComponent(route.query.redirect as string)
  }
})

async function handleSubmit(): Promise<void> {
  if (!form.value.email || !form.value.password) {
    error.value = '이메일과 비밀번호를 입력해주세요.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await authStore.login(form.value)
    router.push(redirectPath.value)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
    <div class="max-w-md w-full space-y-8">
      <!-- 로고 -->
      <div class="text-center">
        <router-link to="/home" class="inline-flex items-center space-x-2">
          <div class="w-12 h-12 bg-primary-600 rounded-xl flex items-center justify-center">
            <span class="text-white font-bold text-2xl">C</span>
          </div>
        </router-link>
        <h2 class="mt-6 text-3xl font-bold text-gray-900">로그인</h2>
        <p class="mt-2 text-sm text-gray-600">
          계정이 없으신가요?
          <router-link to="/auth/signup" class="text-primary-600 hover:text-primary-500">
            회원가입
          </router-link>
        </p>
      </div>

      <!-- 폼 -->
      <form @submit.prevent="handleSubmit" class="mt-8 space-y-6">
        <!-- 에러 메시지 -->
        <div v-if="error" class="bg-red-50 text-red-600 p-4 rounded-lg text-sm">
          {{ error }}
        </div>

        <div class="space-y-4">
          <!-- 이메일 -->
          <div>
            <label for="email" class="label">이메일</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              required
              class="input"
              placeholder="example@email.com"
            />
          </div>

          <!-- 비밀번호 -->
          <div>
            <label for="password" class="label">비밀번호</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              required
              class="input"
              placeholder="••••••••"
            />
          </div>
        </div>

        <!-- 로그인 버튼 -->
        <button
          type="submit"
          :disabled="loading"
          class="w-full btn btn-primary py-3"
        >
          <span v-if="loading">로그인 중...</span>
          <span v-else>로그인</span>
        </button>
      </form>

      <!-- 데모 계정 안내 -->
      <div class="mt-6">
        <div class="relative">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-3 bg-gray-50 text-gray-500">데모 계정으로 빠른 로그인</span>
          </div>
        </div>

        <div class="mt-4 space-y-2">
          <button
            v-for="account in demoAccounts"
            :key="account.email"
            @click="fillDemoAccount(account)"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-lg border-2 transition-all duration-200 text-left group"
            :class="form.email === account.email
              ? 'border-primary-500 bg-primary-50 shadow-sm'
              : 'border-gray-200 bg-white hover:border-primary-300 hover:bg-gray-50'"
          >
            <span class="text-xl flex-shrink-0">{{ account.icon }}</span>
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-sm text-gray-900">{{ account.role }}</div>
              <div class="text-xs text-gray-500 font-mono truncate">{{ account.email }}</div>
            </div>
            <div class="flex-shrink-0 text-xs font-mono text-gray-400 bg-gray-100 px-2 py-1 rounded">
              qwer1234
            </div>
            <svg
              class="w-4 h-4 flex-shrink-0 transition-transform duration-200"
              :class="form.email === account.email ? 'text-primary-500' : 'text-gray-300 group-hover:text-gray-400 group-hover:translate-x-0.5'"
              fill="none" stroke="currentColor" viewBox="0 0 24 24"
            >
              <path v-if="form.email === account.email" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
