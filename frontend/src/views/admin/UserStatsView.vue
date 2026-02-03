<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { adminUserService } from '@/api/user'
import type { UserStatsResponse } from '@/types'

const stats = ref<UserStatsResponse | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    stats.value = await adminUserService.getStats()
  } catch (e) {
    error.value = '통계를 불러오는데 실패했습니다.'
    console.error(e)
  } finally {
    loading.value = false
  }
})

// 역할별 분포 비율 계산
const roleDistribution = computed(() => {
  if (!stats.value || stats.value.totalUsers === 0) return []
  const total = stats.value.totalUsers
  return [
    {
      label: '일반회원',
      count: stats.value.userCount,
      percent: Math.round((stats.value.userCount / total) * 100),
      color: 'bg-blue-500',
      lightColor: 'bg-blue-100',
      textColor: 'text-blue-700',
    },
    {
      label: '기업회원',
      count: stats.value.companyCount,
      percent: Math.round((stats.value.companyCount / total) * 100),
      color: 'bg-emerald-500',
      lightColor: 'bg-emerald-100',
      textColor: 'text-emerald-700',
    },
    {
      label: '관리자',
      count: stats.value.adminCount,
      percent: Math.round((stats.value.adminCount / total) * 100),
      color: 'bg-purple-500',
      lightColor: 'bg-purple-100',
      textColor: 'text-purple-700',
    },
  ]
})

// 상태별 분포 계산
const statusDistribution = computed(() => {
  if (!stats.value || stats.value.totalUsers === 0) return []
  const total = stats.value.totalUsers
  const activePercent = Math.round((stats.value.activeUsers / total) * 100)
  const blockedPercent = Math.round((stats.value.blockedUsers / total) * 100)
  return [
    {
      label: '활성',
      count: stats.value.activeUsers,
      percent: activePercent,
      color: 'bg-green-500',
      lightColor: 'bg-green-100',
      textColor: 'text-green-700',
    },
    {
      label: '차단',
      count: stats.value.blockedUsers,
      percent: blockedPercent,
      color: 'bg-red-500',
      lightColor: 'bg-red-100',
      textColor: 'text-red-700',
    },
  ]
})
</script>

<template>
  <div class="space-y-6">
    <!-- 헤더 -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">사용자 통계</h1>
      <p class="text-gray-500">사용자 가입 현황, 역할별 분포, 상태별 분석을 확인할 수 있습니다.</p>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="card py-16 text-center text-gray-400">
      통계를 불러오는 중...
    </div>

    <!-- 에러 -->
    <div v-else-if="error" class="card py-16 text-center text-red-500">
      {{ error }}
    </div>

    <!-- 통계 콘텐츠 -->
    <template v-else-if="stats">
      <!-- 요약 카드 -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <div class="card text-center p-5">
          <p class="text-3xl font-bold text-gray-900">{{ stats.totalUsers }}</p>
          <p class="text-sm text-gray-500 mt-1">전체 사용자</p>
        </div>
        <div class="card text-center p-5">
          <p class="text-3xl font-bold text-green-600">{{ stats.activeUsers }}</p>
          <p class="text-sm text-gray-500 mt-1">활성 사용자</p>
        </div>
        <div class="card text-center p-5">
          <p class="text-3xl font-bold text-red-600">{{ stats.blockedUsers }}</p>
          <p class="text-sm text-gray-500 mt-1">차단 사용자</p>
        </div>
        <div class="card text-center p-5">
          <p class="text-3xl font-bold text-blue-600">{{ stats.userCount }}</p>
          <p class="text-sm text-gray-500 mt-1">일반회원</p>
        </div>
        <div class="card text-center p-5">
          <p class="text-3xl font-bold text-emerald-600">{{ stats.companyCount }}</p>
          <p class="text-sm text-gray-500 mt-1">기업회원</p>
        </div>
        <div class="card text-center p-5">
          <p class="text-3xl font-bold text-purple-600">{{ stats.adminCount }}</p>
          <p class="text-sm text-gray-500 mt-1">관리자</p>
        </div>
      </div>

      <!-- 역할별 분포 -->
      <div class="card p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">역할별 분포</h2>
        <div class="space-y-4">
          <div v-for="role in roleDistribution" :key="role.label">
            <div class="flex items-center justify-between mb-1">
              <div class="flex items-center gap-2">
                <span :class="[role.lightColor, role.textColor]" class="px-2 py-0.5 rounded text-xs font-medium">
                  {{ role.label }}
                </span>
                <span class="text-sm text-gray-600">{{ role.count }}명</span>
              </div>
              <span class="text-sm font-medium text-gray-700">{{ role.percent }}%</span>
            </div>
            <div class="w-full bg-gray-100 rounded-full h-3">
              <div
                :class="role.color"
                class="h-3 rounded-full transition-all duration-500"
                :style="{ width: role.percent + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 상태별 분포 -->
      <div class="card p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">계정 상태 분포</h2>
        <div class="space-y-4">
          <div v-for="status in statusDistribution" :key="status.label">
            <div class="flex items-center justify-between mb-1">
              <div class="flex items-center gap-2">
                <span :class="[status.lightColor, status.textColor]" class="px-2 py-0.5 rounded text-xs font-medium">
                  {{ status.label }}
                </span>
                <span class="text-sm text-gray-600">{{ status.count }}명</span>
              </div>
              <span class="text-sm font-medium text-gray-700">{{ status.percent }}%</span>
            </div>
            <div class="w-full bg-gray-100 rounded-full h-3">
              <div
                :class="status.color"
                class="h-3 rounded-full transition-all duration-500"
                :style="{ width: status.percent + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 요약 테이블 -->
      <div class="card p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">상세 현황</h2>
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-3 px-4 text-sm font-medium text-gray-500">구분</th>
              <th class="text-right py-3 px-4 text-sm font-medium text-gray-500">인원</th>
              <th class="text-right py-3 px-4 text-sm font-medium text-gray-500">비율</th>
            </tr>
          </thead>
          <tbody>
            <tr class="border-b border-gray-100">
              <td class="py-3 px-4 text-sm text-gray-900">전체 사용자</td>
              <td class="py-3 px-4 text-sm text-right font-medium text-gray-900">{{ stats.totalUsers }}명</td>
              <td class="py-3 px-4 text-sm text-right text-gray-500">100%</td>
            </tr>
            <tr v-for="role in roleDistribution" :key="'table-' + role.label" class="border-b border-gray-100">
              <td class="py-3 px-4 text-sm text-gray-700 pl-8">└ {{ role.label }}</td>
              <td class="py-3 px-4 text-sm text-right text-gray-700">{{ role.count }}명</td>
              <td class="py-3 px-4 text-sm text-right text-gray-500">{{ role.percent }}%</td>
            </tr>
            <tr class="border-b border-gray-100">
              <td class="py-3 px-4 text-sm text-green-700 font-medium">활성 사용자</td>
              <td class="py-3 px-4 text-sm text-right font-medium text-green-700">{{ stats.activeUsers }}명</td>
              <td class="py-3 px-4 text-sm text-right text-gray-500">
                {{ stats.totalUsers > 0 ? Math.round((stats.activeUsers / stats.totalUsers) * 100) : 0 }}%
              </td>
            </tr>
            <tr>
              <td class="py-3 px-4 text-sm text-red-700 font-medium">차단 사용자</td>
              <td class="py-3 px-4 text-sm text-right font-medium text-red-700">{{ stats.blockedUsers }}명</td>
              <td class="py-3 px-4 text-sm text-right text-gray-500">
                {{ stats.totalUsers > 0 ? Math.round((stats.blockedUsers / stats.totalUsers) * 100) : 0 }}%
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
