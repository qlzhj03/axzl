<template>
  <div class="register">
    <!-- 动态背景粒子效果 -->
    <div class="particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- 主容器 -->
    <div class="register-container">
      <!-- 左侧装饰区域 -->
      <div class="register-left">
        <div class="brand-section">
          <div class="logo-wrapper">
            <svg-icon icon-class="logo" class="logo-icon" />
          </div>
          <h1 class="brand-title">{{ title }}</h1>
          <p class="brand-subtitle">智能问诊管理系统</p>
          <div class="benefits-list">
            <div class="benefit-item">
              <svg-icon icon-class="guide" class="benefit-icon" />
              <div class="benefit-content">
                <h4>智能诊断</h4>
                <p>AI辅助医疗诊断</p>
              </div>
            </div>
            <div class="benefit-item">
              <svg-icon icon-class="monitor" class="benefit-icon" />
              <div class="benefit-content">
                <h4>健康管理</h4>
                <p>个人健康档案</p>
              </div>
            </div>
            <div class="benefit-item">
              <svg-icon icon-class="education" class="benefit-icon" />
              <div class="benefit-content">
                <h4>知识库</h4>
                <p>专业医学知识</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧注册表单 -->
      <div class="register-right">
        <div class="register-box">
          <div class="register-header">
            <h2 class="register-title">创建账号</h2>
            <p class="register-desc">填写以下信息完成注册</p>
          </div>
          
          <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form">
            <el-form-item prop="username">
              <div class="input-wrapper">
                <svg-icon icon-class="user" class="input-icon" />
                <el-input 
                  v-model="registerForm.username" 
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
                  v-model="registerForm.password"
                  type="password"
                  size="large" 
                  auto-complete="off"
                  placeholder="请输入密码"
                  @keyup.enter="handleRegister"
                  class="custom-input"
                  show-password
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <div class="input-wrapper">
                <svg-icon icon-class="password" class="input-icon" />
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  size="large" 
                  auto-complete="off"
                  placeholder="请确认密码"
                  @keyup.enter="handleRegister"
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
                    size="large" 
                    v-model="registerForm.code"
                    auto-complete="off"
                    placeholder="验证码"
                    @keyup.enter="handleRegister"
                    class="custom-input"
                  />
                </div>
                <div class="captcha-box" @click="getCode">
                  <img :src="codeUrl" class="captcha-img" alt="验证码" />
                </div>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button
                :loading="loading"
                size="large" 
                class="register-btn"
                @click.prevent="handleRegister"
              >
                <span v-if="!loading" class="btn-text">注 册</span>
                <span v-else class="btn-text">注 册 中...</span>
              </el-button>
            </el-form-item>
            
            <div class="login-link">
              <span>已有账号？</span>
              <router-link :to="'/login'">立即登录</router-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
    
    <!-- 底部版权 -->
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'
import type { RegisterForm } from '@/types/api/login'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()

const registerForm = ref<RegisterForm>({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: ""
})

const equalToPassword = (rule: any, value: string, callback: (error?: Error) => void): void => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref<string>("")
const loading = ref<boolean>(false)
const captchaEnabled = ref<boolean>(true)

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

function handleRegister(): void {
  proxy.$refs.registerRef.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(() => {
        const username = registerForm.value.username
        ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success",
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled) {
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
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang='scss' scoped>
.register {
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
.register-container {
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
.register-left {
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

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 380px;
  margin: 0 auto;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 22px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  text-align: left;
  
  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateX(8px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.benefit-icon {
  font-size: 26px;
  color: white;
  flex-shrink: 0;
}

.benefit-content {
  h4 {
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 4px 0;
  }
  
  p {
    font-size: 13px;
    opacity: 0.9;
    margin: 0;
  }
}

// 右侧注册区域
.register-right {
  width: 480px;
  padding: 50px 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  position: relative;
}

.register-box {
  width: 100%;
  max-width: 360px;
}

.register-header {
  margin-bottom: 35px;
  text-align: center;
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}

// 表单样式
.register-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
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

// 注册按钮
.register-btn {
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
  margin-top: 8px;
  
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

// 登录链接
.login-link {
  text-align: center;
  margin-top: 18px;
  font-size: 14px;
  color: #666;
  
  a {
    color: #667eea;
    text-decoration: none;
    font-weight: 600;
    margin-left: 5px;
    transition: all 0.3s ease;
    
    &:hover {
      color: #764ba2;
      text-decoration: underline;
    }
  }
}

// 底部版权
.el-register-footer {
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
  .register-container {
    flex-direction: column;
    min-height: auto;
    max-height: 95vh;
  }
  
  .register-left {
    padding: 35px 30px;
    min-height: 280px;
  }
  
  .register-right {
    width: 100%;
    padding: 35px 30px;
  }
  
  .brand-title {
    font-size: 26px;
  }
  
  .brand-subtitle {
    margin-bottom: 30px;
  }
  
  .benefits-list {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
  }
  
  .benefit-item {
    flex: 1;
    min-width: 180px;
    max-width: 240px;
    padding: 14px 18px;
  }
}

@media (max-width: 768px) {
  .register-container {
    width: 100%;
    border-radius: 0;
    max-height: 100vh;
  }
  
  .register-left {
    display: none;
  }
  
  .register-right {
    padding: 35px 25px;
  }
  
  .register-box {
    max-width: 100%;
  }
  
  .register-title {
    font-size: 26px;
  }
  
  .register-header {
    margin-bottom: 30px;
  }
}
</style>
