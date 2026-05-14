<template>
  <div class="patient-health" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="28"><FirstAidKit /></el-icon>
        </div>
        <div>
          <h2 class="page-title">健康档案</h2>
          <p class="page-subtitle">管理您的个人健康信息</p>
        </div>
      </div>
      <el-button v-if="!editing" type="primary" round @click="startEdit">
        <el-icon><Edit /></el-icon>编辑档案
      </el-button>
      <div v-else class="edit-actions">
        <el-button @click="cancelEdit" round>取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile" round>
          <el-icon><Check /></el-icon>保存
        </el-button>
      </div>
    </div>

    <!-- 查看模式 -->
    <div v-if="!editing" class="profile-view">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>基本信息</span>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">性别</span>
            <span class="info-value">{{ genderMap[profile.gender] || '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">出生日期</span>
            <span class="info-value">{{ profile.birthday || '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">血型</span>
            <span class="info-value">{{ profile.bloodType ? profile.bloodType + '型' : '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">身高</span>
            <span class="info-value">{{ profile.height ? profile.height + ' cm' : '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">体重</span>
            <span class="info-value">{{ profile.weight ? profile.weight + ' kg' : '未填写' }}</span>
          </div>
          <div class="info-item highlight">
            <span class="info-label">BMI</span>
            <span class="info-value bmi-value">{{ bmi }}</span>
          </div>
        </div>
      </div>

      <!-- 病史信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>病史信息</span>
        </div>
        <div class="history-list">
          <div class="history-item">
            <div class="history-label">
              <span class="dot warning"></span>
              过敏史
            </div>
            <div class="history-value">{{ profile.allergyHistory || '无' }}</div>
          </div>
          <div class="history-item">
            <div class="history-label">
              <span class="dot info"></span>
              既往病史
            </div>
            <div class="history-value">{{ profile.medicalHistory || '无' }}</div>
          </div>
          <div class="history-item">
            <div class="history-label">
              <span class="dot error"></span>
              家族遗传史
            </div>
            <div class="history-value">{{ profile.familyHistory || '无' }}</div>
          </div>
          <div class="history-item">
            <div class="history-label">
              <span class="dot success"></span>
              生活习惯
            </div>
            <div class="history-value">{{ profile.lifestyle || '无' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑模式 -->
    <div v-else class="profile-edit">
      <div class="info-card">
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>基本信息</span>
        </div>
        <el-form :model="form" label-width="80px" label-position="left" class="edit-form">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                  <el-option label="男" value="0" />
                  <el-option label="女" value="1" />
                  <el-option label="未知" value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出生日期">
                <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="血型">
                <el-select v-model="form.bloodType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="t in ['A', 'B', 'AB', 'O']" :key="t" :label="t + '型'" :value="t" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身高(cm)">
                <el-input-number v-model="form.height" :min="50" :max="250" :precision="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="体重(kg)">
                <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <div class="info-card">
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>病史信息</span>
        </div>
        <el-form :model="form" label-width="80px" label-position="left" class="edit-form">
          <el-form-item label="过敏史">
            <el-input v-model="form.allergyHistory" type="textarea" :rows="2" placeholder="如：花粉过敏、青霉素过敏等" />
          </el-form-item>
          <el-form-item label="既往病史">
            <el-input v-model="form.medicalHistory" type="textarea" :rows="2" placeholder="如：高血压、糖尿病等" />
          </el-form-item>
          <el-form-item label="家族遗传史">
            <el-input v-model="form.familyHistory" type="textarea" :rows="2" placeholder="如：父亲高血压，母亲糖尿病" />
          </el-form-item>
          <el-form-item label="生活习惯">
            <el-input v-model="form.lifestyle" type="textarea" :rows="2" placeholder="如：吸烟、饮酒、运动频率等" />
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { FirstAidKit, Edit, Check, User, Document } from '@element-plus/icons-vue'
import { getMyProfile, updateMyProfile } from '@/api/biz/profile'

defineOptions({ name: 'PatientHealth' })

const genderMap: Record<string, string> = { '0': '男', '1': '女', '2': '未知' }

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const profile = ref<any>({})
const form = reactive<any>({
  gender: '',
  birthday: '',
  bloodType: '',
  height: null,
  weight: null,
  allergyHistory: '',
  medicalHistory: '',
  familyHistory: '',
  lifestyle: ''
})

const bmi = computed(() => {
  if (profile.value.height && profile.value.weight) {
    const h = Number(profile.value.height) / 100
    const b = Number(profile.value.weight) / (h * h)
    return b.toFixed(1)
  }
  return '未填写'
})

async function loadProfile() {
  loading.value = true
  try {
    const res = await getMyProfile()
    profile.value = res.data || {}
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function startEdit() {
  Object.assign(form, {
    gender: profile.value.gender || '',
    birthday: profile.value.birthday || '',
    bloodType: profile.value.bloodType || '',
    height: profile.value.height || null,
    weight: profile.value.weight || null,
    allergyHistory: profile.value.allergyHistory || '',
    medicalHistory: profile.value.medicalHistory || '',
    familyHistory: profile.value.familyHistory || '',
    lifestyle: profile.value.lifestyle || ''
  })
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function saveProfile() {
  saving.value = true
  try {
    await updateMyProfile({ ...profile.value, ...form })
    ElMessage.success('保存成功')
    editing.value = false
    await loadProfile()
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadProfile() })
</script>

<style scoped lang="scss">
.patient-health {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.edit-actions {
  display: flex;
  gap: 8px;
}

// 信息卡片
.info-card {
  background: var(--patient-card-bg);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid var(--patient-border);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--patient-text);
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--patient-border);

  .el-icon {
    color: var(--el-color-primary);
  }
}

// 信息网格
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &.highlight {
    background: var(--el-color-primary-light-9);
    padding: 16px;
    border-radius: 12px;
  }
}

.info-label {
  font-size: 13px;
  color: var(--patient-text-secondary);
}

.info-value {
  font-size: 15px;
  font-weight: 500;
  color: var(--patient-text);
}

.bmi-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-color-primary);
}

// 病史列表
.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  padding: 16px;
  background: var(--patient-bg);
  border-radius: 12px;
}

.history-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--patient-text);
  margin-bottom: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.warning { background: #f59e0b; }
  &.info { background: #3b82f6; }
  &.error { background: #ef4444; }
  &.success { background: #10b981; }
}

.history-value {
  font-size: 14px;
  color: var(--patient-text-secondary);
  line-height: 1.6;
}

// 编辑表单
.edit-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--patient-text-secondary);
  }

  :deep(.el-textarea__inner) {
    border-radius: 10px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select .el-input__wrapper) {
    border-radius: 10px;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
