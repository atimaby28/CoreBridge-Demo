<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/common/AppHeader.vue'
import AppSidebar from '@/components/common/AppSidebar.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const route = useRoute()
const authStore = useAuthStore()

// 인증 페이지 여부 (레이아웃 분기)
const isAuthPage = computed(() => route.path.startsWith('/auth'))
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 인증 페이지 (헤더/사이드바 없음) -->
    <template v-if="isAuthPage">
      <router-view />
    </template>
    
    <!-- 메인 레이아웃 -->
    <template v-else>
      <AppHeader />
      <div class="flex">
        <AppSidebar />
        <div class="flex-1 ml-64 mt-16 flex flex-col min-h-[calc(100vh-4rem)]">
          <main class="flex-1 p-6">
            <router-view />
          </main>
          <AppFooter />
        </div>
      </div>
    </template>
  </div>
</template>
