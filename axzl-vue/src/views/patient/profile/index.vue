<template>
  <div class="patient-profile" v-loading="loading">
    <el-row :gutter="20" class="profile-container">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="7" :xs="24">
        <el-card shadow="never" class="info-card">
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
        </el-card>
      </el-col>

      <!-- 右侧：编辑区域 -->
      <el-col :span="17" :xs="24">
        <el-card shadow="never" class="edit-card">
          <el-tabs v-model="activeTab">
            <!-- 基本资料 -->
            <el-tab-pane label="基本资料" name="info">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="80px"
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
                  <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
                  <el-button @click="resetProfileForm">重置</el-button>
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
                  <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
                  <el-button @click="resetPwdForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, User, Iphone, Message, Male, Timer } from '@element-plus/icons-vue'
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
  animation: fadeIn 0.3s ease;
  max-width: 960px;
  margin: 0 auto;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-container {
  .info-card {
    border-radius: 12px;
    margin-bottom: 16px;

    .avatar-section {
      text-align: center;
      padding-bottom: 20px;
      margin-bottom: 20px;
      border-bottom: 1px solid var(--patient-border);

      .avatar-uploader {
        display: inline-block;
        position: relative;
        cursor: pointer;
        margin-bottom: 12px;

        &:hover .avatar-overlay {
          opacity: 1;
        }
      }

      .user-avatar {
        border: 3px solid var(--el-fill-color-light);
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
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
        font-size: 1.15rem;
        color: var(--patient-text);
      }

      .user-role {
        font-size: 13px;
        color: var(--patient-text-secondary);
      }
    }

    .info-list {
      .info-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 0;
        border-bottom: 1px dashed var(--patient-border);
        font-size: 14px;

        &:last-child {
          border-bottom: none;
        }

        .el-icon {
          color: var(--el-color-primary);
          font-size: 16px;
          flex-shrink: 0;
        }

        label {
          flex: 0 0 56px;
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
    }
  }

  .edit-card {
    border-radius: 12px;

    .profile-form {
      max-width: 480px;
      padding-top: 8px;
    }
  }
}
</style>
