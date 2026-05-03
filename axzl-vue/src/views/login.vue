<template>
  <div class="login">
    <!-- 动态背景粒子效果 -->
    <div class="particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- 主容器 -->
    <div class="login-container">
      <!-- 左侧装饰区域 -->
      <div class="login-left">
        <div class="brand-section">
          <div class="logo-wrapper">
            <svg-icon icon-class="logo" class="logo-icon" />
          </div>
          <h1 class="brand-title">{{ title }}</h1>
          <p class="brand-subtitle">智能问诊管理系统</p>
          <div class="feature-list">
            <div class="feature-item">
              <svg-icon icon-class="guide" class="feature-icon" />
              <span>智能AI辅助诊断</span>
            </div>
            <div class="feature-item">
              <svg-icon icon-class="monitor" class="feature-icon" />
              <span>健康档案管理</span>
            </div>
            <div class="feature-item">
              <svg-icon icon-class="education" class="feature-icon" />
              <span>知识库管理</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-box">
          <div class="login-header">
            <h2 class="login-title">欢迎登录</h2>
            <p class="login-desc">请输入您的账号信息</p>
          </div>
          
          <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item prop="username">
              <div class="input-wrapper">
                <svg-icon icon-class="user" class="input-icon" />
                <el-input
                  v-model="loginForm.username"
                  type="text"
                  size="large"
                  auto-complete="off"
                  placeholder="请输入账号"
                  class="custom-input"
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-wrapper">
                <svg-icon icon-class="password" class="input-icon" />
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  size="large"
                  auto-complete="off"
                  placeholder="请输入密码"
                  @keyup.enter="handleLogin"
                  class="custom-input"
                  show-password
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="code" v-if="captchaEnabled">
              <div class="code-wrapper">
                <div class="input-wrapper code-input">
                  <svg-icon icon-class="validCode" class="input-icon" />
                  <el-input
                    v-model="loginForm.code"
                    size="large"
                    auto-complete="off"
                    placeholder="验证码"
                    @keyup.enter="handleLogin"
                    class="custom-input"
                  />
                </div>
                <div class="captcha-box" @click="getCode">
                  <img :src="codeUrl" class="captcha-img" alt="验证码" />
                </div>
              </div>
            </el-form-item>
            
            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe" class="remember-me">
                <span class="checkbox-text">记住密码</span>
              </el-checkbox>
              <router-link v-if="register" class="forgot-link" :to="'/register'">立即注册</router-link>
            </div>
            
            <el-form-item>
              <el-button
                :loading="loading"
                size="large"
                class="login-btn"
                @click.prevent="handleLogin"
              >
                <span v-if="!loading" class="btn-text">登 录</span>
                <span v-else class="btn-text">登 录 中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    
    <!-- 底部版权 -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'
import type { CaptchaInfoResult } from '@/types/api/login'
import type { LoginForm } from '@/types/api/login'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref<LoginForm>({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(true)
const redirect = ref<string | undefined>(undefined)

// 生成粒子样式
function getParticleStyle(index: number) {
  const size = Math.random() * 10 + 5
  const left = Math.random() * 100
  const delay = Math.random() * 5
  const duration = Math.random() * 10 + 10
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

watch(route, (newRoute: any) => {
    redirect.value = (newRoute.query && newRoute.query.redirect) as string | undefined
}, { immediate: true })

function handleLogin(): void {
  proxy.$refs.loginRef.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc: Record<string, any>, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode(): void {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie(): void {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

// 粒子背景
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  bottom: -20px;
  animation: float-up infinite;
}

@keyframes float-up {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) rotate(720deg);
    opacity: 0;
  }
}

// 主容器
.login-container {
  position: relative;
  z-index: 1;
  display: flex;
  width: 1100px;
  max-width: 92%;
  min-height: 600px;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 左侧品牌区域
.login-left {
  flex: 1.2;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 50px 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    width: 150%;
    height: 150%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 2px, transparent 2px);
    background-size: 30px 30px;
    animation: moveBackground 30s linear infinite;
  }
}

@keyframes moveBackground {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.brand-section {
  position: relative;
  z-index: 1;
  color: white;
  text-align: center;
}

.logo-wrapper {
  width: 90px;
  height: 90px;
  margin: 0 auto 25px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 3px solid rgba(255, 255, 255, 0.3);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.logo-icon {
  font-size: 45px;
  color: white;
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.95;
  margin-bottom: 40px;
  font-weight: 300;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 380px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 22px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateX(8px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.feature-icon {
  font-size: 22px;
  color: white;
  flex-shrink: 0;
}

.feature-item span {
  font-size: 15px;
  font-weight: 500;
  white-space: nowrap;
}

// 右侧登录区域
.login-right {
  width: 480px;
  padding: 50px 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  position: relative;
}

.login-box {
  width: 100%;
  max-width: 360px;
}

.login-header {
  margin-bottom: 35px;
  text-align: center;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}

// 表单样式
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 10px;
  padding: 0 14px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  
  &:focus-within {
    background: white;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .input-icon {
    font-size: 18px;
    color: #999;
    margin-right: 10px;
    transition: color 0.3s ease;
  }
  
  &:focus-within .input-icon {
    color: #667eea;
  }
}

.custom-input {
  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    padding: 0;
    background: transparent;
  }
  
  :deep(.el-input__inner) {
    height: 48px;
    font-size: 14px;
    color: #333;
    
    &::placeholder {
      color: #bbb;
    }
  }
}

// 验证码区域
.code-wrapper {
  display: flex;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.captcha-box {
  width: 110px;
  height: 48px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #f5f7fa;
  transition: all 0.3s ease;
  flex-shrink: 0;
  
  &:hover {
    border-color: #667eea;
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  }
}

.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

// 登录选项
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  margin-top: 4px;
}

.remember-me {
  :deep(.el-checkbox__label) {
    color: #666;
    font-size: 14px;
  }
}

.checkbox-text {
  user-select: none;
}

.forgot-link {
  color: #667eea;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
  
  &:hover {
    color: #764ba2;
    text-decoration: underline;
  }
}

// 登录按钮
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.btn-text {
  color: white;
}

// 底部版权
.el-login-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  z-index: 10;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

// 响应式设计
@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
    min-height: auto;
    max-height: 95vh;
  }
  
  .login-left {
    padding: 35px 30px;
    min-height: 280px;
  }
  
  .login-right {
    width: 100%;
    padding: 35px 30px;
  }
  
  .brand-title {
    font-size: 26px;
  }
  
  .brand-subtitle {
    margin-bottom: 30px;
  }
  
  .feature-list {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
  }
  
  .feature-item {
    flex: 1;
    min-width: 180px;
    max-width: 240px;
    padding: 12px 18px;
  }
}

@media (max-width: 768px) {
  .login-container {
    width: 100%;
    border-radius: 0;
    max-height: 100vh;
  }
  
  .login-left {
    display: none;
  }
  
  .login-right {
    padding: 35px 25px;
  }
  
  .login-box {
    max-width: 100%;
  }
  
  .login-title {
    font-size: 26px;
  }
  
  .login-header {
    margin-bottom: 30px;
  }
}
</style>
