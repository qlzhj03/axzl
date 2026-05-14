<template>
  <div class="patient-app-wrapper">
    <header class="patient-top-nav">
      <div class="nav-left">
        <router-link to="/patient/home" class="logo">安心诊疗</router-link>
        <ul class="nav-menu">
          <li v-for="item in menuItems" :key="item.path">
            <router-link :to="item.path" :class="['menu-item', { active: isActive(item.path) }]">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </router-link>
          </li>
        </ul>
      </div>

      <div class="nav-right">
        <el-icon class="theme-toggle" @click="settingsStore.toggleTheme()">
          <Moon v-if="!settingsStore.isDark" />
          <Sunny v-else />
        </el-icon>

        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile">
            <el-avatar :size="36" :src="userStore.avatar" />
            <span class="user-name">{{ userStore.nickName || userStore.name }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人信息
              </el-dropdown-item>
              <el-dropdown-item command="password">
                <el-icon><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="patient-main-container">
      <router-view v-slot="{ Component }">
        <transition name="patient-slide" mode="out-in">
          <keep-alive>
            <component :is="Component" :key="$route.path" />
          </keep-alive>
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import useSettingsStore from '@/store/modules/settings'
import useUserStore from '@/store/modules/user'
import { Moon, Sunny, ArrowDown, User, Lock, SwitchButton, HomeFilled, ChatDotRound, FirstAidKit, AlarmClock } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const settingsStore = useSettingsStore()
const userStore = useUserStore()

const menuItems = [
  { path: '/patient/home', label: '首页', icon: HomeFilled },
  { path: '/patient/consultation', label: '智能问诊', icon: ChatDotRound },
  { path: '/patient/health', label: '健康档案', icon: FirstAidKit },
  { path: '/patient/medication', label: '用药提醒', icon: AlarmClock }
]

function isActive(path: string) {
  return route.path.startsWith(path)
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/patient/profile')
      break
    case 'password':
      router.push('/patient/profile?tab=password')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logOut().then(() => {
          router.push('/login')
        })
      }).catch(() => {})
      break
  }
}
</script>

<style scoped lang="scss">
.patient-app-wrapper {
  min-height: 100vh;
  background: var(--patient-bg);
  color: var(--patient-text);
}

.patient-top-nav {
  height: 60px;
  background: var(--patient-card-bg);
  border-bottom: 1px solid var(--patient-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--el-color-primary);
  cursor: pointer;
  white-space: nowrap;
  text-decoration: none;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.8;
  }
}

.nav-menu {
  display: flex;
  list-style: none;
  gap: 4px;
  margin: 0;
  padding: 0;

  li {
    list-style: none;
  }
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  color: var(--patient-text-secondary);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  text-decoration: none;

  &:hover {
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    transform: translateY(-1px);
  }

  &.active {
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(var(--el-color-primary-rgb), 0.15);
  }
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-toggle {
  font-size: 18px;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  color: var(--patient-text-secondary);
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.patient-main-container {
  margin-top: 60px;
  padding: 24px 32px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  min-height: calc(100vh - 60px);
}

// 平滑滑动过渡动画
.patient-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.patient-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.patient-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.patient-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 768px) {
  .patient-top-nav { padding: 0 16px; }
  .nav-left { gap: 12px; }
  .menu-item span { display: none; }
  .patient-main-container { padding: 16px; }
  .user-name { display: none; }
}
</style>
