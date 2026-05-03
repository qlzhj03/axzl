<template>
  <div class="patient-health">
    <div class="page-toolbar">
      <h2 class="page-title">健康档案</h2>
      <el-button v-if="!editing" type="primary" @click="startEdit">编辑档案</el-button>
      <div v-else>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </div>
    </div>

    <div v-loading="loading">
      <!-- 查看模式 -->
      <div v-if="!editing" class="profile-view">
        <el-card shadow="never">
          <template #header><span>基本信息</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="性别">{{ genderMap[profile.gender] || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ profile.birthday || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="血型">{{ profile.bloodType || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="身高">{{ profile.height ? profile.height + ' cm' : '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ profile.weight ? profile.weight + ' kg' : '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="BMI">{{ bmi }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" style="margin-top: 16px;">
          <template #header><span>病史信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="过敏史">{{ profile.allergyHistory || '无' }}</el-descriptions-item>
            <el-descriptions-item label="既往病史">{{ profile.medicalHistory || '无' }}</el-descriptions-item>
            <el-descriptions-item label="家族遗传史">{{ profile.familyHistory || '无' }}</el-descriptions-item>
            <el-descriptions-item label="生活习惯">{{ profile.lifestyle || '无' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="profile-edit">
        <el-card shadow="never">
          <template #header><span>基本信息</span></template>
          <el-form :model="form" label-width="90px" label-position="right">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="性别">
                  <el-select v-model="form.gender" placeholder="请选择">
                    <el-option label="男" value="0" />
                    <el-option label="女" value="1" />
                    <el-option label="未知" value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="出生日期">
                  <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="血型">
                  <el-select v-model="form.bloodType" placeholder="请选择">
                    <el-option v-for="t in ['A', 'B', 'AB', 'O']" :key="t" :label="t + '型'" :value="t" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身高(cm)">
                  <el-input-number v-model="form.height" :min="50" :max="250" :precision="1" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" style="width: 100%;" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card shadow="never" style="margin-top: 16px;">
          <template #header><span>病史信息</span></template>
          <el-form :model="form" label-width="90px" label-position="right">
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
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
  animation: fadeIn 0.3s ease;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--patient-text);
  margin: 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
