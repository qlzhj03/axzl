<template>
  <div class="app-container">
    <el-row :gutter="20" class="profile-container">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="6" :xs="24" class="mb20">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div v-loading="loading" class="user-info">
            <div class="avatar-container">
              <user-avatar />
            </div>
            
            <div class="info-list">
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ user.userName }}</span>
              </div>
              <div class="info-item">
                <label>昵称：</label>
                <span>{{ user.nickName }}</span>
              </div>
              <div class="info-item">
                <label>手机：</label>
                <span>{{ user.phonenumber || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>邮箱：</label>
                <span>{{ user.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>性别：</label>
                <span>{{ getSexText(user.sex) }}</span>
              </div>
              <div class="info-item">
                <label>角色：</label>
                <span>{{ roleGroup }}</span>
              </div>
              <div class="info-item">
                <label>创建时间：</label>
                <span>{{ parseTime(user.createTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧资料编辑区域 -->
      <el-col :span="18" :xs="24">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>基本资料</span>
            </div>
          </template>
          
          <el-tabs v-model="activeTab" class="profile-tabs">
            <el-tab-pane label="基本资料" name="userinfo">
              <el-form 
                ref="userInfoRef" 
                :model="form" 
                :rules="userInfoRules" 
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="用户昵称" prop="nickName">
                  <el-input 
                    v-model="form.nickName" 
                    placeholder="请输入用户昵称" 
                    maxlength="30"
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="手机号码" prop="phonenumber">
                  <el-input 
                    v-model="form.phonenumber" 
                    placeholder="请输入手机号码" 
                    maxlength="11"
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="邮箱" prop="email">
                  <el-input 
                    v-model="form.email" 
                    placeholder="请输入邮箱地址" 
                    maxlength="50"
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="性别">
                  <el-radio-group v-model="form.sex">
                    <el-radio value="0">男</el-radio>
                    <el-radio value="1">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="submitUserInfo" :loading="submitLoading">
                    保存修改
                  </el-button>
                  <el-button @click="resetUserInfoForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="修改密码" name="resetPwd">
              <el-form 
                ref="pwdRef" 
                :model="passwordForm" 
                :rules="passwordRules" 
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input 
                    v-model="passwordForm.oldPassword" 
                    placeholder="请输入旧密码" 
                    type="password" 
                    show-password 
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="新密码" prop="newPassword">
                  <el-input 
                    v-model="passwordForm.newPassword" 
                    placeholder="请输入新密码" 
                    type="password" 
                    show-password 
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input 
                    v-model="passwordForm.confirmPassword" 
                    placeholder="请确认新密码" 
                    type="password" 
                    show-password 
                    clearable
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="submitPassword" :loading="passwordLoading">
                    保存修改
                  </el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import UserAvatar from './userAvatar.vue'
import { getUserProfile, updateUserProfile, updateUserPwd } from '@/api/system/user'
import { parseTime } from '@/utils/axzl'

const activeTab = ref('userinfo')
const loading = ref(false)
const submitLoading = ref(false)
const passwordLoading = ref(false)

const user = ref<any>({})
const roleGroup = ref('')

// 表单引用
const userInfoRef = ref()
const pwdRef = ref()

// 基本资料表单
const form = reactive({
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '0'
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 基本资料验证规则
const userInfoRules = {
  nickName: [
    { required: true, message: "用户昵称不能为空", trigger: "blur" }
  ],
  email: [
    { required: true, message: "邮箱地址不能为空", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }
  ],
  phonenumber: [
    { required: true, message: "手机号码不能为空", trigger: "blur" },
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }
  ]
}

// 密码验证规则
const equalToPassword = (rule: any, value: string, callback: (error?: Error) => void): void => {
  if (passwordForm.newPassword !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: "旧密码不能为空", trigger: "blur" }
  ],
  newPassword: [
    { required: true, message: "新密码不能为空", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\ |", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "确认密码不能为空", trigger: "blur" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ]
}

// 获取性别文本
function getSexText(sex: string) {
  const sexMap: Record<string, string> = {
    '0': '男',
    '1': '女',
    '2': '未知'
  }
  return sexMap[sex] || '未知'
}

// 获取用户信息
function getUser() {
  loading.value = true
  getUserProfile().then(response => {
    user.value = response.data
    roleGroup.value = response.roleGroup || ''
    
    // 填充表单数据
    form.nickName = response.data.nickName || ''
    form.phonenumber = response.data.phonenumber || ''
    form.email = response.data.email || ''
    form.sex = response.data.sex || '0'
  }).catch(error => {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }).finally(() => {
    loading.value = false
  })
}

// 提交基本资料
function submitUserInfo() {
  userInfoRef.value.validate((valid: boolean) => {
    if (valid) {
      submitLoading.value = true
      const updateData = {
        ...user.value,
        nickName: form.nickName,
        phonenumber: form.phonenumber,
        email: form.email,
        sex: form.sex
      }
      
      updateUserProfile(updateData).then(() => {
        ElMessage.success("修改成功")
        getUser() // 重新获取最新数据
      }).catch(err => {
        ElMessage.error("修改失败")
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

// 重置基本资料表单
function resetUserInfoForm() {
  getUser() // 重新加载原始数据
}

// 提交密码修改
function submitPassword() {
  pwdRef.value.validate((valid: boolean) => {
    if (valid) {
      passwordLoading.value = true
      
      updateUserPwd(passwordForm.oldPassword, passwordForm.newPassword).then(() => {
        ElMessage.success("密码修改成功")
        resetPasswordForm()
      }).catch(err => {
        ElMessage.error("密码修改失败")
      }).finally(() => {
        passwordLoading.value = false
      })
    }
  })
}

// 重置密码表单
function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  
  // 清除验证状态
  if (pwdRef.value) {
    pwdRef.value.clearValidate()
  }
}

onMounted(() => {
  getUser()
})
</script>

<style scoped lang="scss">
.profile-container {
  .profile-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
    }
    
    .card-header {
      font-weight: bold;
      color: #303133;
      font-size: 16px;
    }
  }
  
  .user-info {
    .avatar-container {
      text-align: center;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #ebeef5;
    }
    
    .info-list {
      .info-item {
        display: flex;
        margin-bottom: 12px;
        padding: 8px 0;
        border-bottom: 1px dashed #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        label {
          flex: 0 0 80px;
          font-weight: 500;
          color: #606266;
        }
        
        span {
          flex: 1;
          color: #303133;
          word-break: break-all;
        }
      }
    }
  }
  
  .profile-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }
    
    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;
    }
  }
  
  .profile-form {
    max-width: 600px;
    margin-top: 20px;
    
    :deep(.el-form-item__label) {
      font-weight: 500;
    }
    
    :deep(.el-input__inner) {
      border-radius: 4px;
    }
    
    .el-button {
      min-width: 100px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-container {
    .user-info {
      .info-item {
        flex-direction: column;
        
        label {
          margin-bottom: 5px;
        }
      }
    }
    
    .profile-form {
      max-width: 100%;
    }
  }
}
</style>
