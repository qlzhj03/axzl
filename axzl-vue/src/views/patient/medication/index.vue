<template>
  <div class="patient-medication">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="28"><AlarmClock /></el-icon>
        </div>
        <div>
          <h2 class="page-title">用药提醒</h2>
          <p class="page-subtitle">管理您的用药计划，按时服药更健康</p>
        </div>
      </div>
      <el-button type="primary" round @click="openDialog()">
        <el-icon><Plus /></el-icon>添加药物
      </el-button>
    </div>

    <!-- 今日提醒区域 -->
    <section class="today-section" v-if="todayMeds.length > 0">
      <div class="section-header">
        <h3>
          <el-icon><Bell /></el-icon>
          今日用药
        </h3>
        <span class="today-date">{{ todayStr }}</span>
      </div>
      <div class="today-grid">
        <div
          v-for="med in todayMeds"
          :key="med.id"
          :class="['today-card', { done: isAllChecked(med) }]"
        >
          <div class="today-card-head">
            <div class="drug-badge">{{ med.drugName.charAt(0) }}</div>
            <div class="drug-info">
              <h4>{{ med.drugName }}</h4>
              <span class="dosage-text">{{ med.dosage || '按医嘱服用' }}</span>
            </div>
            <el-tag v-if="isAllChecked(med)" type="success" size="small" effect="plain">已完成</el-tag>
          </div>
          <div class="time-chips">
            <div
              v-for="time in parseTimes(med.reminderTimes)"
              :key="time"
              :class="['time-chip', { checked: isChecked(med.id, time) }]"
              @click="!isChecked(med.id, time) && doCheckIn(med.id, time)"
            >
              <el-icon v-if="isChecked(med.id, time)"><CircleCheck /></el-icon>
              <el-icon v-else><Clock /></el-icon>
              <span>{{ time }}</span>
            </div>
          </div>
          <div v-if="med.notes" class="today-notes">{{ med.notes }}</div>
        </div>
      </div>
    </section>

    <!-- 空状态 -->
    <section v-if="todayMeds.length === 0 && !loading" class="empty-today">
      <div class="empty-icon">💊</div>
      <p>暂无今日用药提醒</p>
      <span>点击上方"添加药物"开始管理您的用药计划</span>
    </section>

    <!-- 全部药物列表 -->
    <section class="list-section">
      <div class="section-header">
        <h3>
          <el-icon><List /></el-icon>
          全部药物
        </h3>
        <el-input
          v-model="searchKey"
          placeholder="搜索药物..."
          clearable
          :prefix-icon="Search"
          style="width: 200px"
          size="small"
        />
      </div>

      <div class="med-list" v-loading="loading">
        <div
          v-for="med in filteredMeds"
          :key="med.id"
          class="med-card"
        >
          <div class="med-card-left">
            <div class="med-avatar" :style="{ background: getColor(med.drugName) }">
              {{ med.drugName.charAt(0) }}
            </div>
            <div class="med-info">
              <h4>{{ med.drugName }}</h4>
              <div class="med-meta">
                <span v-if="med.dosage">{{ med.dosage }}</span>
                <span>{{ freqLabel(med.frequency) }}</span>
                <span v-if="med.notes">{{ med.notes }}</span>
              </div>
              <div class="med-times">
                <span v-for="t in parseTimes(med.reminderTimes)" :key="t" class="mini-chip">{{ t }}</span>
              </div>
            </div>
          </div>
          <div class="med-card-right">
            <el-tag :type="med.status === '0' ? 'success' : 'info'" size="small" effect="plain">
              {{ med.status === '0' ? '使用中' : '已停用' }}
            </el-tag>
            <div class="med-actions">
              <el-button link type="primary" @click="openDialog(med)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button link type="primary" @click="toggleStatus(med)">
                <el-icon><SwitchButton /></el-icon>
              </el-button>
              <el-button link type="danger" @click="handleDelete(med)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="filteredMeds.length === 0 && !loading" class="empty-list">
          <p>暂无药物记录</p>
        </div>
      </div>
    </section>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑药物' : '添加药物'"
      width="480px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="left">
        <el-form-item label="药品名称" prop="drugName">
          <el-input v-model="form.drugName" placeholder="如：阿莫西林" maxlength="50" />
        </el-form-item>
        <el-form-item label="剂量" prop="dosage">
          <el-input v-model="form.dosage" placeholder="如：每次1片" maxlength="30" />
        </el-form-item>
        <el-form-item label="服用频次" prop="frequency">
          <el-select v-model="form.frequency" style="width: 100%">
            <el-option label="每日一次" value="daily" />
            <el-option label="每日两次" value="twice" />
            <el-option label="每日三次" value="thrice" />
            <el-option label="每周一次" value="weekly" />
            <el-option label="按需服用" value="asNeeded" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒时间">
          <div class="time-picker-list">
            <div v-for="(t, i) in form.times" :key="i" class="time-picker-item">
              <el-time-picker v-model="form.times[i]" format="HH:mm" value-format="HH:mm" placeholder="选择时间" style="flex:1" />
              <el-button link type="danger" @click="form.times.splice(i, 1)" :disabled="form.times.length <= 1">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button v-if="form.times.length < 4" link type="primary" @click="form.times.push('')">
              <el-icon><Plus /></el-icon>添加时间
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="不填表示长期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="如：饭后半小时服用" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, SwitchButton, AlarmClock, Bell,
  List, Clock, CircleCheck, Search
} from '@element-plus/icons-vue'
import {
  getMyMedications, getTodayMedications,
  addMedication, updateMedication, delMedication,
  checkInMedication
} from '@/api/biz/medication'

defineOptions({ name: 'PatientMedication' })

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchKey = ref('')
const medications = ref<any[]>([])
const todayMeds = ref<any[]>([])
const todayLogs = ref<any[]>([])
const formRef = ref()

const todayStr = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const form = ref({
  id: null as number | null,
  drugName: '',
  dosage: '',
  frequency: 'daily',
  times: ['08:00'],
  startDate: '',
  endDate: '',
  notes: ''
})

const rules = {
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  frequency: [{ required: true, message: '请选择频次', trigger: 'change' }]
}

const filteredMeds = computed(() => {
  if (!searchKey.value) return medications.value
  const kw = searchKey.value.toLowerCase()
  return medications.value.filter(m =>
    m.drugName.toLowerCase().includes(kw) ||
    (m.notes || '').toLowerCase().includes(kw)
  )
})

function parseTimes(json: string): string[] {
  if (!json) return []
  try {
    const arr = JSON.parse(json)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

function isChecked(medId: number, time: string) {
  return todayLogs.value.some(
    (l: any) => l.medicationId === medId && l.scheduledTime === time && l.status === '0'
  )
}

function isAllChecked(med: any) {
  const times = parseTimes(med.reminderTimes)
  if (times.length === 0) return false
  return times.every(t => isChecked(med.id, t))
}

function freqLabel(f: string) {
  const map: Record<string, string> = {
    daily: '每日一次', twice: '每日两次', thrice: '每日三次',
    weekly: '每周一次', asNeeded: '按需服用'
  }
  return map[f] || f
}

const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#6366f1', '#10b981']
function getColor(name: string) {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

async function loadData() {
  loading.value = true
  try {
    const [medRes, todayRes] = await Promise.all([
      getMyMedications(),
      getTodayMedications()
    ])
    medications.value = medRes.data || []
    todayMeds.value = todayRes.data?.medications || []
    todayLogs.value = todayRes.data?.logs || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openDialog(med?: any) {
  if (med) {
    isEdit.value = true
    form.value = {
      id: med.id,
      drugName: med.drugName,
      dosage: med.dosage || '',
      frequency: med.frequency || 'daily',
      times: parseTimes(med.reminderTimes).length > 0 ? parseTimes(med.reminderTimes) : ['08:00'],
      startDate: med.startDate ? med.startDate.substring(0, 10) : '',
      endDate: med.endDate ? med.endDate.substring(0, 10) : '',
      notes: med.notes || ''
    }
  } else {
    isEdit.value = false
    form.value = {
      id: null, drugName: '', dosage: '', frequency: 'daily',
      times: ['08:00'], startDate: '', endDate: '', notes: ''
    }
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const data: any = {
      drugName: form.value.drugName,
      dosage: form.value.dosage,
      frequency: form.value.frequency,
      reminderTimes: JSON.stringify(form.value.times.filter(t => t)),
      startDate: form.value.startDate || null,
      endDate: form.value.endDate || null,
      notes: form.value.notes
    }
    if (isEdit.value && form.value.id) {
      data.id = form.value.id
      await updateMedication(data)
      ElMessage.success('修改成功')
    } else {
      await addMedication(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(med: any) {
  const newStatus = med.status === '0' ? '1' : '0'
  await updateMedication({ id: med.id, status: newStatus })
  ElMessage.success(newStatus === '0' ? '已启用' : '已停用')
  loadData()
}

async function handleDelete(med: any) {
  await ElMessageBox.confirm(`确定删除「${med.drugName}」吗？`, '提示', { type: 'warning' })
  await delMedication(med.id)
  ElMessage.success('已删除')
  loadData()
}

async function doCheckIn(medicationId: number, scheduledTime: string) {
  try {
    await checkInMedication({ medicationId, scheduledTime })
    ElMessage.success(`${scheduledTime} 已打卡`)
    // 刷新打卡记录
    const todayRes = await getTodayMedications()
    todayLogs.value = todayRes.data?.logs || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.patient-medication {
  max-width: 900px;
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
  background: linear-gradient(135deg, #2b7de9 0%, #66b1ff 100%);
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

/* ===== 今日提醒 ===== */
.today-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--patient-text);
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.today-date {
  font-size: 13px;
  color: var(--patient-text-secondary);
}

.today-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.today-card {
  background: var(--patient-card-bg);
  border: 1px solid var(--patient-border);
  border-radius: 14px;
  padding: 18px;
  transition: all 0.25s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }

  &.done {
    border-color: #67C23A;
    background: rgba(103, 194, 58, 0.03);
  }
}

.today-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.drug-badge {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2b7de9 0%, #66b1ff 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.drug-info {
  flex: 1;
  min-width: 0;

  h4 {
    font-size: 15px;
    font-weight: 600;
    color: var(--patient-text);
    margin: 0 0 2px;
  }
}

.dosage-text {
  font-size: 12px;
  color: var(--patient-text-secondary);
}

.time-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.time-chip {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--patient-bg);
  border: 1px solid var(--patient-border);
  color: var(--patient-text);

  &:hover:not(.checked) {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
  }

  &.checked {
    background: rgba(103, 194, 58, 0.1);
    border-color: #67C23A;
    color: #67C23A;
    cursor: default;
  }

  .el-icon {
    font-size: 14px;
  }
}

.today-notes {
  font-size: 12px;
  color: var(--patient-text-secondary);
  padding-top: 8px;
  border-top: 1px dashed var(--patient-border);
}

/* ===== 空状态 ===== */
.empty-today,
.empty-list {
  text-align: center;
  padding: 40px 20px;
  color: var(--patient-text-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-today p {
  font-size: 16px;
  font-weight: 500;
  color: var(--patient-text);
  margin: 0 0 4px;
}

.empty-today span {
  font-size: 13px;
}

/* ===== 全部药物 ===== */
.list-section {
  margin-bottom: 24px;
}

.med-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.med-card {
  background: var(--patient-card-bg);
  border: 1px solid var(--patient-border);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  }
}

.med-card-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.med-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.med-info {
  flex: 1;
  min-width: 0;

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--patient-text);
    margin: 0 0 4px;
  }
}

.med-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--patient-text-secondary);
  margin-bottom: 6px;
}

.med-times {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.mini-chip {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  background: var(--patient-bg);
  color: var(--patient-text-secondary);
  border: 1px solid var(--patient-border);
}

.med-card-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.med-actions {
  display: flex;
  gap: 4px;
}

/* ===== 时间选择器列表 ===== */
.time-picker-list {
  width: 100%;
}

.time-picker-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

/* ===== 弹窗表单 ===== */
:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 0;
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-dialog__footer) {
  padding: 0 24px 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .today-grid {
    grid-template-columns: 1fr;
  }

  .med-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .med-card-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
