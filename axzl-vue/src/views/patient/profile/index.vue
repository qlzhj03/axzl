<template>
  <div class="patient-profile" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div>
          <h2 class="page-title">个人信息</h2>
          <p class="page-subtitle">管理您的账户和个人资料</p>
        </div>
      </div>
    </div>

    <div class="profile-layout">
      <!-- 左侧：个人信息卡片 -->
      <div class="profile-sidebar">
        <div class="info-card">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <el-avatar :size="96" :src="displayAvatar" class="user-avatar">
                <el-icon :size="40"><User /></el-icon>
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="20"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
            <h3 class="user-name">{{ user.nickName || user.userName }}</h3>
            <span class="user-role">{{ roleGroup || '患者' }}</span>
          </div>

          <div class="info-list">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <label>用户名</label>
              <span>{{ user.userName }}</span>
            </div>
            <div class="info-item">
              <el-icon><Iphone /></el-icon>
              <label>手机号</label>
              <span>{{ user.phonenumber || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <label>邮箱</label>
              <span>{{ user.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Male /></el-icon>
              <label>性别</label>
              <span>{{ sexText }}</span>
            </div>
            <div class="info-item">
              <el-icon><Timer /></el-icon>
              <label>注册时间</label>
              <span>{{ user.createTime }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：编辑区域 -->
      <div class="profile-main">
        <div class="info-card">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <!-- 基本资料 -->
            <el-tab-pane label="基本资料" name="info">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="80px"
                label-position="left"
                class="profile-form"
              >
                <el-form-item label="昵称" prop="nickName">
                  <el-input v-model="profileForm.nickName" placeholder="请输入昵称" maxlength="30" show-word-limit />
                </el-form-item>
                <el-form-item label="手机号" prop="phonenumber">
                  <el-input v-model="profileForm.phonenumber" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" maxlength="50" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="profileForm.sex">
                    <el-radio value="0">男</el-radio>
                    <el-radio value="1">女</el-radio>
                    <el-radio value="2">未知</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="saving" @click="saveProfile" round>
                    <el-icon><Check /></el-icon>保存修改
                  </el-button>
                  <el-button @click="resetProfileForm" round>重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="100px"
                label-position="left"
                class="profile-form"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="changingPwd" @click="changePassword" round>
                    <el-icon><Key /></el-icon>修改密码
                  </el-button>
                  <el-button @click="resetPwdForm" round>重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, User, Iphone, Message, Male, Timer, Check, Key } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import { getUserProfile, updateUserProfile, updateUserPwd, uploadAvatar } from '@/api/system/user'

defineOptions({ name: 'PatientProfile' })

const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('info')
const loading = ref(false)
const saving = ref(false)
const changingPwd = ref(false)
const user = ref<any>({})
const roleGroup = ref('')
const profileFormRef = ref()
const pwdFormRef = ref()

const BASE_API = import.meta.env.VITE_APP_BASE_API

const displayAvatar = computed(() => {
  const av = user.value.avatar || userStore.avatar || ''
  if (!av) return ''
  if (av.startsWith('http')) return av
  return BASE_API + av
})

const sexText = computed(() => {
  const map: Record<string, string> = { '0': '男', '1': '女', '2': '未知' }
  return map[user.value.sex] || '未知'
})

const profileForm = reactive({
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '2'
})

const profileRules = {
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phonenumber: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function loadProfile() {
  loading.value = true
  try {
    const res = await getUserProfile()
    const u = res.data || {}
    user.value = u
    roleGroup.value = res.roleGroup || ''
    Object.assign(profileForm, {
      nickName: u.nickName || '',
      phonenumber: u.phonenumber || '',
      email: u.email || '',
      sex: u.sex || '2'
    })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function fillFormFromUser() {
  Object.assign(profileForm, {
    nickName: user.value.nickName || '',
    phonenumber: user.value.phonenumber || '',
    email: user.value.email || '',
    sex: user.value.sex || '2'
  })
}

async function saveProfile() {
  await profileFormRef.value?.validate()
  saving.value = true
  try {
    await updateUserProfile(profileForm)
    ElMessage.success('保存成功')
    userStore.nickName = profileForm.nickName
    await loadProfile()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

function resetProfileForm() {
  fillFormFromUser()
  profileFormRef.value?.clearValidate()
}

async function changePassword() {
  await pwdFormRef.value?.validate()
  changingPwd.value = true
  try {
    await updateUserPwd(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    resetPwdForm()
  } catch (e) {
    console.error(e)
  } finally {
    changingPwd.value = false
  }
}

function resetPwdForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

function beforeAvatarUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB')
  return isImage && isLt2M
}

async function handleAvatarUpload(options: any) {
  const formData = new FormData()
  formData.append('avatarfile', options.file)
  try {
    const res = await uploadAvatar(formData)
    const imgUrl = res.imgUrl || ''
    const fullUrl = imgUrl.startsWith('http') ? imgUrl : BASE_API + imgUrl
    user.value.avatar = imgUrl
    userStore.avatar = fullUrl
    ElMessage.success('头像更新成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('头像上传失败')
  }
}

onMounted(() => {
  loadProfile()
  if (route.query.tab === 'password') activeTab.value = 'password'
})
</script>

<style scoped lang="scss">
.patient-profile {
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 28px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--el-color-primary) 0%, #6366f1 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--patient-text);
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--patient-text-secondary);
  margin: 4px 0 0;
}

// 布局
.profile-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

// 信息卡片
.info-card {
  background: var(--patient-card-bg);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--patient-border);
}

// 头像区域
.avatar-section {
  text-align: center;
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--patient-border);
}

.avatar-uploader {
  display: inline-block;
  position: relative;
  cursor: pointer;
  margin-bottom: 16px;

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.user-avatar {
  border: 3px solid var(--el-fill-color-light);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.user-name {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: var(--patient-text);
}

.user-role {
  font-size: 13px;
  color: var(--patient-text-secondary);
}

// 信息列表
.info-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  font-size: 14px;
  transition: background 0.2s;

  &:hover {
    background: var(--patient-bg);
  }

  .el-icon {
    color: var(--el-color-primary);
    font-size: 18px;
    flex-shrink: 0;
  }

  label {
    flex: 0 0 60px;
    color: var(--patient-text-secondary);
    font-size: 13px;
  }

  span {
    flex: 1;
    color: var(--patient-text);
    text-align: right;
    word-break: break-all;
  }
}

// 标签页
.profile-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
  }
}

// 表单
.profile-form {
  max-width: 480px;

  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--patient-text-secondary);
  }

  :deep(.el-input__wrapper) {
    border-radius: 10px;
  }

  :deep(.el-radio-group) {
    display: flex;
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}
</style>
